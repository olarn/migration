package com.ttb.crm.service.migrationdata.service.preparecaesdataservice;

import com.ttb.crm.lib.crmssp_common_utils_lib.exception.NotFoundException;
import com.ttb.crm.lib.crmssp_common_utils_lib.exception.ValidationException;
import com.ttb.crm.service.migrationdata.bean.CasePayload;
import com.ttb.crm.service.migrationdata.bean.CreateCaseDTO;
import com.ttb.crm.service.migrationdata.bean.PreparedCaseData;
import com.ttb.crm.service.migrationdata.bean.Secret;
import com.ttb.crm.service.migrationdata.bean.StgToCaseWriterDTO;
import com.ttb.crm.service.migrationdata.bean.request.client.RetrieveMasterDataByMasterGroupRequest;
import com.ttb.crm.service.migrationdata.bean.response.CaseCaptureEventDTO;
import com.ttb.crm.service.migrationdata.bean.response.CreateCaseData;
import com.ttb.crm.service.migrationdata.bean.response.MetaData;
import com.ttb.crm.service.migrationdata.bean.response.RetrieveCaseInfoResponse;
import com.ttb.crm.service.migrationdata.bean.response.client.RetrieveMasterDataResponse;
import com.ttb.crm.service.migrationdata.enums.CaseType;
import com.ttb.crm.service.migrationdata.helper.Constant;
import com.ttb.crm.service.migrationdata.helper.DateTimeUtils;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseDocumentReferenceModel;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaActivity;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaHopModel;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseTransactionModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseDocumentReferenceModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import com.ttb.crm.service.migrationdata.model.userManagement.EmployeeUserModel;
import com.ttb.crm.service.migrationdata.model.userManagement.RoleReadonlyModel;
import com.ttb.crm.service.migrationdata.model.userManagement.TeamReadonlyModel;
import com.ttb.crm.service.migrationdata.repository.secondary.StgCaseDocumentReferenceRepository;
import com.ttb.crm.service.migrationdata.service.CaseMovementDataUtils;
import com.ttb.crm.service.migrationdata.service.MasterDataService;
import com.ttb.crm.service.migrationdata.service.SlaService;
import com.ttb.crm.service.migrationdata.service.TeamService;
import com.ttb.crm.service.migrationdata.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateCaseService {
    private final UserService userService;
    private final MasterDataService masterDataService;
    private final SlaService slaService;
    private final PrepareMetaDataService prepareMetaDataService;
    private final EncryptFieldService encryptFieldService;
    private final TeamService teamService;
    private final EnhancedCaseValidationService enhancedCaseValidationService;
    private final CaseTransactionMapper caseTransactionMapper;
    private final CheckCaseType checkCaseType;
    private final PrepareHopAndActivityService prepareHopAndActivityService;
    private final CaseMovementDataUtils caseMovementDataUtils;
    private final StgCaseDocumentReferenceRepository stgCaseDocumentReferenceRepository;

    public StgToCaseWriterDTO createCase(StgCaseInProgressModel stgCase) {
        return Optional.ofNullable(stgCase)
                .map(caseTransactionMapper::map)
                .map(this::validateCaseByStatus)
                .map(this::prepareCaseData)
                .map(data -> prepareCaseModelAndCreateTransaction(data.getDto(), data.getCasePayload()))
                .orElseThrow(() -> new InternalException(Constant.UNEXPECTED_ERROR));
    }

    private CreateCaseDTO validateCaseByStatus(CreateCaseDTO dto) {
        String caseStatusCode = dto.getCaseStatusCode();

        switch (caseStatusCode) {
            case Constant.CASE_STATUS_NEW, Constant.CASE_STATUS_IN_PROGRESS:
                enhancedCaseValidationService.validateCaseOnGoingData(dto);
                break;
            case Constant.CASE_STATUS_RESOLVED:
                enhancedCaseValidationService.validateCaseResolvedData(dto);
                break;
            case Constant.CASE_STATUS_COMPLETED:
                enhancedCaseValidationService.validateCaseCompletedData(dto);
                break;
            default:
                throw new ValidationException("case status not supported");
        }

        return dto;
    }

    private PreparedCaseData prepareCaseData(CreateCaseDTO dto) {
        ServiceTypeMatrixModel stm = masterDataService.retrieveServiceTypeMatrix(dto);
        ZonedDateTime slaStartDate = slaService.calculateSlaStartDate(dto.getCreatedOn());
        MetaData metaData = prepareMetaDataService.prepareMetaData(dto, stm, slaStartDate);
        CasePayload casePayload = new CasePayload(stm, metaData, slaStartDate);
        return new PreparedCaseData(dto, casePayload);
    }

    private StgToCaseWriterDTO prepareCaseModelAndCreateTransaction(
            CreateCaseDTO dto,
            CasePayload payload
    ) {
        return Optional.ofNullable(payload)
                .map(comp -> prepareCaseModel(dto, comp.getServiceTypeMatrix(), comp.getMetaData(), comp.getSlaStartDate()))
//                .map(validationTemplateService::validateTemplate)
                .map(caseModel -> processCaseTransactionForOneAPP(caseModel, dto, payload.getMetaData()))
                .map(setCaseData(dto, payload.getMetaData()))
                .map(encryptField())
                .map(caseModel -> buildCaseTransaction(caseModel, payload.getMetaData(), dto))
                .orElseThrow(() -> new InternalException(Constant.UNEXPECTED_ERROR));
    }

    private StgToCaseWriterDTO buildCaseTransaction(
            CaseTransactionModel caseModel,
            MetaData metaData,
            CreateCaseDTO dto
    ) {
        List<CaseSlaActivity> activities = buildActivitiesBeforeSave(caseModel, metaData, dto);
        List<CaseDocumentReferenceModel> caseDocs = prepareCaseDocumentReference(dto, metaData.createUser());

        if (!activities.isEmpty()) {
            CaseSlaActivity lastActivity = activities.getLast();
            caseModel.setDataModifiedBy(lastActivity);
        }
        CaseCaptureEventDTO dataForCaseMovement = buildDataCaseToEventHup(caseModel, dto, activities);
        caseModel = prePairCaseWithHopAndDocRef(caseModel, metaData, caseDocs, activities);
        return StgToCaseWriterDTO
                .builder()
                .caseTransaction(caseModel)
                .dataForCaseMovement(dataForCaseMovement)
                .build();
    }

    public CaseTransactionModel prePairCaseWithHopAndDocRef(CaseTransactionModel caseTransactionModel, MetaData metaData, List<CaseDocumentReferenceModel> documentReferenceList, List<CaseSlaActivity> caseSlaActivities) {
        caseTransactionModel.setCaseDocumentReferences(insertCaseDoc(caseTransactionModel, documentReferenceList));
        caseTransactionModel.setSlaHop(insertSlaHop(metaData, caseTransactionModel));
        caseTransactionModel.setCaseSlaActivities(insertSlaActivities(caseTransactionModel, caseSlaActivities));
        return caseTransactionModel;
    }


    public List<CaseSlaActivity> insertSlaActivities(CaseTransactionModel caseModel, List<CaseSlaActivity> caseSlaActivities) {
        if (caseModel == null || caseSlaActivities == null || caseSlaActivities.isEmpty()) {
            return List.of();
        }
        caseSlaActivities.forEach(slaActivity -> slaActivity.setCases(caseModel));
        return caseSlaActivities;
    }

    public List<CaseSlaHopModel> insertSlaHop(MetaData data, CaseTransactionModel caseModel) {
        if (caseModel == null || data.slaHops() == null || data.slaHops().isEmpty()) {
            return List.of();
        }
        data.slaHops().forEach(slaHop -> slaHop.setCases(caseModel));
        return data.slaHops();
    }

    private List<CaseDocumentReferenceModel> insertCaseDoc(CaseTransactionModel caseModel, List<CaseDocumentReferenceModel> caseDocs) {
        if (caseModel == null || caseDocs == null || caseDocs.isEmpty()) {
            return List.of();
        }
        caseDocs.forEach(caseDocumentReferenceModel -> caseDocumentReferenceModel.setCases(caseModel));
        return caseDocs;
    }

    private CaseCaptureEventDTO buildDataCaseToEventHup(CaseTransactionModel caseModel, CreateCaseDTO body, List<CaseSlaActivity> activities) {
        CaseSlaActivity lastActivity = activities.getLast();
        String actionName = lastActivity.getAction().name();
        CaseCaptureEventDTO captureEvent = caseMovementDataUtils.captureAfterChange(
                caseModel,
                new CaseCaptureEventDTO(),
                body.getCreateByEmployeeID(),
                actionName
        );
        captureEvent.setBeforeChange(new RetrieveCaseInfoResponse());
        return captureEvent;
    }


    private List<CaseSlaActivity> buildActivitiesBeforeSave(CaseTransactionModel caseModel, MetaData metaData, CreateCaseDTO body) {
        List<CaseSlaActivity> activities = new ArrayList<>();

        if (metaData.activities() == null || metaData.activities().isEmpty()) {
            activities.addAll(
                    prepareHopAndActivityService.updateDateTimeActivity(
                            prepareHopAndActivityService.prepareActivityForInProgress(metaData, body, caseModel)
                    )
            );
        } else {
            activities.addAll(metaData.activities());
        }

        return activities;
    }

    private List<CaseDocumentReferenceModel> prepareCaseDocumentReference(CreateCaseDTO dto, EmployeeUserModel createdUser) {
        if (dto == null || dto.getExternalId() == null || dto.getExternalId().trim().isBlank()) {
            return Collections.emptyList();
        }
        List<StgCaseDocumentReferenceModel> tempCaseDocList = stgCaseDocumentReferenceRepository.findAllByCaseC(dto.getExternalId());
        if (tempCaseDocList.isEmpty()) {
            return Collections.emptyList();
        }

        return tempCaseDocList.stream()
                .map(tempCaseDoc -> {
                    CaseDocumentReferenceModel caseDoc = new CaseDocumentReferenceModel();
                    UUID createdById = createdUser.getCreatedById();
                    ZonedDateTime createdOn = tempCaseDoc.getEcmUploadedDateTimeC() != null ? DateTimeUtils.parseToZoneDateTime(tempCaseDoc.getEcmUploadedDateTimeC()) : dto.getCreatedOn();
                    caseDoc.setCreatedById(createdById)
                            .setModifiedById(createdById)
                            .setEmpAppId(tempCaseDoc.getEcmAppIDC())
                            .setEmpMsDocTypeKey(tempCaseDoc.getEcmMsDoctypeKeyC())
                            .setFileName(tempCaseDoc.getName())
                            .setObjectDocumentId(tempCaseDoc.getObjectIDC())
                            .setRepository(tempCaseDoc.getRepositoryC())
                            .setStatusCode(0)
                            .setCreatedOn(createdOn)
                            .setModifiedOn(createdOn);
                    return caseDoc;
                }).collect(Collectors.toList());
    }


    private CaseTransactionModel prepareCaseModel(
            CreateCaseDTO body,
            ServiceTypeMatrixModel stm,
            MetaData metaData,
            ZonedDateTime slaStartDate
    ) {
        return Optional.of(
                        new CreateCaseData(
                                body,
                                stm,
                                metaData.createTeam(),
                                metaData.resolveTeam(),
                                metaData.closedTeam(),
                                metaData.createUser(),
                                metaData.resolveUser(),
                                metaData.closedUser(),
                                metaData.totalDuration(),
                                Optional.ofNullable(metaData.slaHops()).orElse(List.of()),
                                Optional.ofNullable(metaData.activities()).orElse(List.of()),
                                slaStartDate
                        ))
                .map(this::prepareCreateCase)
                .orElseThrow(() -> new ValidationException("Failed prepare case model"));
    }

    public CaseTransactionModel prepareCreateCase(CreateCaseData caseData) {
        return Optional.of(new CaseTransactionModel(caseData))
                .map(prepareSlaOverAll(caseData.slaStartDate()))
                .orElseThrow(() -> new ValidationException("Failed prepare case model"));
    }

    public Function<CaseTransactionModel, CaseTransactionModel> prepareSlaOverAll(ZonedDateTime slaStartDate) {
        return caseModel -> {
            ZonedDateTime slaTargetDate = slaService.calculateSla(caseModel.getSla(), slaStartDate);
            caseModel.setSlaTargetDate(slaTargetDate)
                    .setSlaStartDate(slaStartDate);
            return caseModel;
        };
    }

    public Function<CaseTransactionModel, CaseTransactionModel> encryptField() {
        return caseTransaction -> {
            Secret secret = encryptFieldService.getKey(Constant.CRM);
            caseTransaction.setProductNumberFull1(encryptIfNotNull(
                    caseTransaction.getProductNumberFull1(), encryptFieldService.encryptField(secret)
            ));
            caseTransaction.setProductNumberFull2(encryptIfNotNull(
                    caseTransaction.getProductNumberFull2(), encryptFieldService.encryptField(secret)
            ));
            caseTransaction.setProductNumberFull3(encryptIfNotNull(
                    caseTransaction.getProductNumberFull3(), encryptFieldService.encryptField(secret)
            ));
            return caseTransaction;
        };
    }

    private String encryptIfNotNull(String value, Function<String, String> encryptFn) {
        return value != null ? encryptFn.apply(value) : null;
    }

    public Function<CaseTransactionModel, CaseTransactionModel> setCaseData(CreateCaseDTO body, MetaData metaData) {
        return caseTransaction -> {
            if (caseTransaction == null || body == null) return null;

            CaseType caseType = determineCaseType(caseTransaction, body);

            return caseTypeHandlers.getOrDefault(caseType, this::applyDefault)
                    .apply(caseTransaction, metaData);
        };
    }

    private CaseType determineCaseType(CaseTransactionModel model, CreateCaseDTO body) {
        if (Boolean.TRUE.equals(model.getFcr())) {
            return CaseType.FCR;
        }
        if (Constant.CASE_GROUP_STATUS_END.contains(body.getCaseStatusCode())) {
            return CaseType.CASE_END;
        }
        if (Constant.CASE_GROUP_STATUS_NEW_AND_IN_PROGRESS.contains(body.getCaseStatusCode())) {
            return CaseType.ON_PROCESS;
        }
        if (Constant.CASE_STATUS_RESOLVED.contains(body.getCaseStatusCode())) {
            return CaseType.CASE_RESOLVED;
        }
        return CaseType.DEFAULT;
    }

    private final Map<CaseType, BiFunction<CaseTransactionModel, MetaData, CaseTransactionModel>> caseTypeHandlers = Map.of(
            CaseType.FCR, this::applyFcrCase,
            CaseType.CASE_END, this::applyCaseEnd,
            CaseType.ON_PROCESS, this::applyInProgress,
            CaseType.CASE_RESOLVED, this::applyResolved,
            CaseType.DEFAULT, this::applyDefault
    );

    private CaseTransactionModel applyFcrCase(CaseTransactionModel model, MetaData metaData) {
        setClosedInfo(model, metaData.createTeam(), metaData.createUser(), metaData.activities().getLast().getEndDate());
        model.setActiveHopNumber(1);
        model.setModifiedById(metaData.createUser().getUserId());
        model.setStatusChange(metaData.createUser(), metaData.createTeam());
        model.setCaseStatusCode(Constant.CASE_STATUS_COMPLETED);
        model.setCaseStatusValue(Constant.CASE_STATUS_COMPLETED_VALUE);
        model.setResolvedById(null);
        model.setResolvedBy(null);
        model.setResolvedByTeam(null);
        model.setResolvedDate(null);
        model.setResolvedByTeamId(null);
        model.setOwnerId(null);
        model.setOwnerName(null);
        model.setTeamId(null);
        model.setTeamName(null);
        model.setResolvedBy(null);
        model.setStatusCode(0);
        return model;
    }

    private CaseTransactionModel applyCaseEnd(CaseTransactionModel model, MetaData metaData) {
        setResolvedInfo(model, metaData);
        setClosedInfo(model, metaData.closedTeam(), metaData.closedUser(), metaData.activities().getLast().getEndDate());

        model.setActiveHopNumber(metaData.slaHops().size());
        model.setStatusChange(metaData.resolveUser(), metaData.resolveTeam());

        // Clear ownership
        model.setOwnerId(null);
        model.setOwnerName(null);
        model.setTeamId(null);
        model.setTeamName(null);
        model.setStatusCode(0);
        return model;
    }

    private CaseTransactionModel applyResolved(CaseTransactionModel model, MetaData metaData) {
        setResolvedInfo(model, metaData);
        model.setTeamId(metaData.slaHops().getLast().getTeamId());
        model.setTeamName(metaData.slaHops().getLast().getTeamName());
        model.setActiveHopNumber(metaData.slaHops().size());
        model.setStatusChange(metaData.resolveUser(), metaData.resolveTeam());
        model.cleanClosedInfo();
        return model;
    }

    private CaseTransactionModel applyInProgress(CaseTransactionModel model, MetaData metaData) {
        if (!Constant.CASE_GROUP_STATUS_NEW_AND_IN_PROGRESS.contains(model.getCaseStatusCode())) {
            return model;
        }
        model.setCaseStatusCode(Constant.CASE_STATUS_IN_PROGRESS)
                .setCaseStatusValue(Constant.CASE_STATUS_IN_PROGRESS_VALUE)
                .setStatusCode(Constant.ACTIVE_STATUS_CODE);

        List<CaseSlaHopModel> hopModelList = metaData.slaHops();

        CaseSlaHopModel firstHop = hopModelList.getFirst();

        model.setStatusChangeById(firstHop.getOwnerId())
                .setStatusChangeByName(firstHop.getOwnerName())
                .setStatusChangeByTeamId(firstHop.getTeamId())
                .setStatusChangeByTeamName(firstHop.getTeamName());

        CaseSlaHopModel lastActiveHop = hopModelList.stream()
                .filter(hop -> hop.getStatusCode() == Constant.ACTIVE_STATUS_CODE)
                .filter(hop -> hop.getStartDatetime() != null)
                .filter(hop -> hop.getEndDatetime() == null)
                .findFirst()
                .orElse(null);

        if (lastActiveHop == null) {
            return model;
        }

        model.setOwnerId(lastActiveHop.getOwnerId())
                .setOwnerName(lastActiveHop.getOwnerName())
                .setTeamId(lastActiveHop.getTeamId())
                .setTeamName(lastActiveHop.getTeamName())
                .setActiveHopNumber(lastActiveHop.getHopNumber())
                .setStatusChangeDate(ZonedDateTime.now());

        model.cleanClosedInfo();
        model.cleanResolvedInfo();

        return model;
    }

    private CaseTransactionModel applyDefault(CaseTransactionModel model, MetaData metaData) {
        model.setStatusCode(0);
        return model;
    }

    private void setResolvedInfo(CaseTransactionModel model, MetaData metaData) {
        Optional.ofNullable(metaData.resolveTeam()).ifPresentOrElse(
                team -> {
                    model.setResolvedByTeam(team.getNameTh());
                    model.setResolvedByTeamId(team.getTeamId());
                },
                () -> {
                    model.setResolvedByTeam(null);
                    model.setResolvedByTeamId(null);
                }
        );

        Optional.ofNullable(metaData.resolveUser()).ifPresentOrElse(
                user -> {
                    model.setResolvedBy(user.getFullNameTH());
                    model.setResolvedById(user.getUserId());
                },
                () -> {
                    model.setResolvedBy(null);
                    model.setResolvedById(null);
                }
        );

        if (metaData.resolveUser() == null && metaData.resolveTeam() == null) {
            model.setResolvedDate(null);
        }
    }

    private void setClosedInfo(CaseTransactionModel model, TeamReadonlyModel closedTeam, EmployeeUserModel closedUser, ZonedDateTime closedDate) {
        Optional.ofNullable(closedTeam).ifPresentOrElse(
                team -> {
                    model.setClosedByTeam(team.getNameTh());
                    model.setClosedByTeamId(team.getTeamId());
                },
                () -> {
                    model.setClosedByTeam(null);
                    model.setClosedByTeamId(null);
                }
        );

        Optional.ofNullable(closedUser).ifPresentOrElse(
                user -> {
                    model.setClosedBy(closedUser.getFullNameTH());
                    model.setClosedById(closedUser.getUserId());
                },
                () -> {
                    model.setClosedBy(null);
                    model.setClosedById(null);
                }
        );

        model.setClosedDate(closedDate);
    }

    // maybe not use V
    public CaseTransactionModel processCaseTransactionForOneAPP(CaseTransactionModel caseTransaction, CreateCaseDTO body, MetaData metaData) {
        if (!(Constant.ONE_APP.equals(body.getIntegrationSystem()) && checkCaseType.isInProgressOrNew(body))) {
            return caseTransaction;
        }

        String stmCode = body.getServiceTypeMatrixCode();
        RetrieveMasterDataResponse retrieveMasterDataResponse = masterDataService.retrieveMasterDataByCodeAndGroupCode(Constant.ORIGINAL_PROBLEM_CHANNEL_GROUP_CODE, Constant.ORIGINAL_PROBLEM_CHANNEL_CODE);
        caseTransaction.setOriginalProblemChannelValue(retrieveMasterDataResponse.getNameTh());
        if (isCreateOneAppAndPayrollOrPWACase(body)) {
            validateOwnerForPayrollAndPWACase(caseTransaction, validateEmployeeIdForOneApp(body.getOwnerEmployeeId()), metaData);
        }
        if (isCreateAppAndFinCerCase(stmCode)) {
            validateFieldsForFinCer(caseTransaction, body);
        }

        return caseTransaction;
    }

    public boolean isCreateOneAppAndPayrollOrPWACase(CreateCaseDTO caseData) {
        return Constant.ONE_APP_STM_MY_ADVISOR_FROM_SALE_FORCE.contains(caseData.getServiceTypeMatrixCodeOld());
    }

    public void validateOwnerForPayrollAndPWACase(CaseTransactionModel caseTransaction, String employeeId, MetaData metaData) {
        Optional.ofNullable(validateOwnerIsSystemForPayrollAndPWACase(employeeId))
                .map(userService::retrieveUserByEmployeeIdIncludeInactive)
                .map(this::validateUserForPWAOrPayroll)
                .ifPresentOrElse(
                        user -> updateOwnerForCase(caseTransaction, user, metaData),
                        () -> validateTeamCaseForPWA(caseTransaction, employeeId, metaData)
                );
    }

    private String validateOwnerIsSystemForPayrollAndPWACase(String employeeId) {
        Optional.ofNullable(employeeId)
                .filter(empId -> empId.equals(Constant.SF_EX_API_ID))
                .ifPresent(empId -> {
                    throw new ValidationException("Case is one app my advisor but owner case is system");
                });
        return employeeId;
    }

    public EmployeeUserModel validateUserForPWAOrPayroll(EmployeeUserModel user) {
        return Optional.ofNullable(user)
                .filter(u -> u.getStatusCode() != Constant.ACTIVE_STATUS_CODE)
                .map(EmployeeUserModel::getManagerEmployeeId)
                .flatMap(userService::fetchUserByEmployeeId)
                .orElse(user);
    }

    private void updateOwnerForCase(CaseTransactionModel caseTransaction, EmployeeUserModel user, MetaData metaData) {
        caseTransaction.setOwnerId(user.getUserId());
        caseTransaction.setOwnerName(user.getFullNameTH());
        caseTransaction.setTeamId(null);
        caseTransaction.setTeamName(null);
        caseTransaction.setIsHopLocked(true);
        caseTransaction.setIsOwnerLocked(true);
        caseTransaction.setIsSTMLocked(true);

        metaData.slaHops().stream()
                .filter(hop -> hop.getHopNumber().equals(2))
                .findFirst()
                .ifPresent(hop -> hop.setTeamId(null)
                        .setTeamName(null)
                        .setOwnerId(user.getUserId())
                        .setOwnerName(user.getFullNameTH())
                        .setModifiedById(user.getUserId()));
    }

    public void validateTeamCaseForPWA(CaseTransactionModel caseTransaction, String employeeId, MetaData metaData) {
        EmployeeUserModel user = userService.retrieveUserByEmployeeIdIncludeInactive(employeeId);

        TeamReadonlyModel branchTeam = teamService.fetchByNameEn(Constant.BRANCH_TEAM_EN);
        TeamReadonlyModel wealthTeam = teamService.fetchByNameEn(Constant.WEALTH_TEAM_EN);

        RoleReadonlyModel role = user.getRole();

        boolean isAlreadyRole = role != null && role.getDepthLevel() != null;
        boolean isDepthLevel7 = isAlreadyRole && role.getDepthLevel() == 7;
        boolean isRoleTeamPrivateBanking = isDepthLevel7 && role.getName().equalsIgnoreCase(Constant.ROLE_TEAM_PRIVATE_BANKING);
        TeamReadonlyModel targetTeam = isRoleTeamPrivateBanking
                ? wealthTeam
                : branchTeam;

        caseTransaction.setTeamId(targetTeam.getTeamId());
        caseTransaction.setTeamName(targetTeam.getNameEn());
        caseTransaction.setIsHopLocked(true);
        caseTransaction.setIsSTMLocked(true);
        metaData.slaHops().stream().filter(hop -> hop.getHopNumber().equals(2)).findFirst().ifPresent(hop -> {
            hop.setTeamId(targetTeam.getTeamId());
            hop.setTeamName(targetTeam.getNameEn());
        });
    }

    public String validateEmployeeIdForOneApp(String employeeId) {
        if (employeeId == null || employeeId.isBlank()) {
            throw new ValidationException("Employee Id is mandatory");
        }
        return employeeId;
    }

    public boolean isCreateAppAndFinCerCase(String stmCode) {
        List<RetrieveMasterDataResponse> finCertData = masterDataService.retrieveByMasterGroupCode(
                new RetrieveMasterDataByMasterGroupRequest(Constant.STM_ONE_APP_FINCERT)
        );

        return finCertData.stream()
                .anyMatch(data -> stmCode.equals(data.getCode()));
    }

    public void validateFieldsForFinCer(CaseTransactionModel caseTransaction, CreateCaseDTO body) {
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("ObjectId", body.getObjectId());
        fieldMap.put("DocumentId", body.getDocumentId());
        fieldMap.put("RepositoryId", body.getRepositoryId());

        fieldMap.entrySet().stream()
                .filter(field -> field.getValue() == null || field.getValue().isEmpty() || field.getValue().isBlank())
                .findFirst()
                .ifPresent(field -> {
                    throw new ValidationException(field.getKey() + " is required");
                });

        caseTransaction.setReadyToPrint(true);
    }
}
