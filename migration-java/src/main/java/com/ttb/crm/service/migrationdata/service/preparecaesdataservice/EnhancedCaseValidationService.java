package com.ttb.crm.service.migrationdata.service.preparecaesdataservice;

import com.ttb.crm.lib.crmssp_common_utils_lib.exception.NotFoundException;
import com.ttb.crm.service.migrationdata.bean.CreateCaseDTO;
import com.ttb.crm.service.migrationdata.enums.CaseStatus;
import com.ttb.crm.service.migrationdata.helper.Constant;
import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import com.ttb.crm.service.migrationdata.model.userManagement.TeamReadonlyModel;
import com.ttb.crm.service.migrationdata.repository.caseManagement.CaseCommentRepository;
import com.ttb.crm.service.migrationdata.repository.caseManagement.CaseDocumentReferenceRepository;
import com.ttb.crm.service.migrationdata.repository.caseManagement.CaseSlaActivityRepository;
import com.ttb.crm.service.migrationdata.repository.caseManagement.CaseSlaHopRepository;
import com.ttb.crm.service.migrationdata.repository.caseManagement.CaseTransactionRepository;
import com.ttb.crm.service.migrationdata.service.TeamService;
import com.ttb.crm.service.migrationdata.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;


@Service
@RequiredArgsConstructor
public class EnhancedCaseValidationService {

    private final CaseTransactionRepository caseRepository;
    private final CaseSlaHopRepository caseSlaHopRepository;
    private final CaseSlaActivityRepository caseSlaActivityRepository;
    private final CaseDocumentReferenceRepository caseDocumentReferenceRepository;
    private final CaseCommentRepository caseCommentRepository;
    private final UserService userService;
    private final TeamService teamService;
    private final CheckCaseType checkCaseType;
    private static final String AT_SLA_PER_OWNER = " at SLA per owner record: ";


    @PersistenceContext
    private EntityManager entityManager;

    public void validateCaseCompletedData(CreateCaseDTO caseData) {
//        validateModifiedDateCaseData(caseData);
        validateMandatoryData(caseData);
        validateResolveData(caseData);
        validateByIntegrationSystem(caseData);
        validateClosedDateData(caseData);
        validateCreatedCaseData(caseData);
    }

    public void validateCaseResolvedData(CreateCaseDTO caseData) {
//        validateModifiedDateCaseData(caseData);
        validateMandatoryData(caseData);
        validateResolveData(caseData);
        validateResolveDate(caseData);
        validateByIntegrationSystem(caseData);
        validateCreatedCaseData(caseData);
    }

    public void validateCaseOnGoingData(CreateCaseDTO caseData) {
        validateIsOnGoingStatus(caseData);
//        validateModifiedDateCaseData(caseData);
        validateMandatoryData(caseData);
        validateByIntegrationSystem(caseData);
        validateCreatedCaseData(caseData);
    }

    void validateIsOnGoingStatus(CreateCaseDTO caseData) {
        boolean isCaseOnGoingStatus = List.of(CaseStatus.NEW.toString(), CaseStatus.IN_PROGRESS.toString()).contains(caseData.getCaseStatusCode());
        if (!isCaseOnGoingStatus) {
            throw new IllegalArgumentException("Case status is " + caseData.getCaseStatusCode() + ". Please check if case status is NEW or IN_PROGRESS");
        }
    }

    void validateRequired(Object value, String message) {
        if (value == null || (value instanceof String valueStr && valueStr.isEmpty())) {
            throw new IllegalArgumentException(message);
        }
    }

    void validateMandatoryData(CreateCaseDTO caseData) {
        validateRequired(caseData.getCaseNumber(), "Case number is required");
        if (!checkCaseType.isCompletedStatus(caseData)) {
            validateRequired(caseData.getServiceTypeMatrixCode(), "Service type matrix code new is required");
        }
        validateRequired(caseData.getServiceTypeMatrixCodeOld(), "Service type matrix code is required");
        validateRequired(caseData.getCaseStatusCode(), "Case status code is required");

        boolean hasOriginalProblemChannelCode = StringUtils.isNotBlank(caseData.getOriginalProblemChannelCode());
        boolean hasDataSourceCode = StringUtils.isNotBlank(caseData.getDataSourceCode());
        if (!(hasOriginalProblemChannelCode || hasDataSourceCode)) {
            throw new IllegalArgumentException("Original problem channel code and DataSource are required");
        } else if (hasOriginalProblemChannelCode) {
            validateRequired(caseData.getOriginalProblemChannelCode(), "Original problem channel code is required");
            validateRequired(caseData.getOriginalProblemChannelValue(), "Original problem channel value is required");
        } else {
            validateRequired(caseData.getDataSourceCode(), "DataSource code is required");
            validateRequired(caseData.getDataSourceValue(), "DataSource value is required");
        }
    }

    void validateByIntegrationSystem(CreateCaseDTO caseData) {
        validateRequired(caseData.getIntegrationSystem(), "Integration system is required");
        if (!Constant.ALL_INTEGRATION_SYSTEM.contains(caseData.getIntegrationSystem())) {
            throw new IllegalArgumentException("Unsupported integration system: " + caseData.getIntegrationSystem());
        }
    }

    void validateCreatedCaseData(CreateCaseDTO caseData) {
        if (isCompletedOrResolvedIsTtbWebOrOneAppPayrollAndPwaCase(caseData)) {
            validateOwnerRequired(caseData);
        } else if (isOneAppPayrollAndPwaCase(caseData)) {
            validateOwnerOrTeamRequired(caseData);
        } else if (!isInProgressFromOneAppOrTtbWebAndSystemCreate(caseData)) {
            validateRequired(caseData.getCreateByTeamName(), "Create by team name is required");
        } else {
            java.util.Objects.requireNonNull(Boolean.TRUE);
        }
        validateCommonFields(caseData);
    }

//    boolean isCompletedOrResolvedOneAppOrTtbWebCase(CreateCaseDTO caseData) {
//        boolean isCompletedOrResolveStatus = (checkCaseType.isCompletedStatus(caseData) || checkCaseType.isResolvedStatus(caseData));
//        boolean isOneAppOrTtbWeb = checkCaseType.isOneAppOrTtbWeb(caseData.getIntegrationSystem());
//        boolean isCreateBySystem = Constant.SF_EX_API_ID.equals(caseData.getCreateByEmployeeID());
//        boolean isOneAppPayrollAndPwaCase = isOneAppPayrollAndPwaCase(caseData);
//        boolean isCreateOneAppAndPayrollOrPWACaseOrIsCreateBySystem = isOneAppPayrollAndPwaCase || isCreateBySystem;
//        return isOneAppOrTtbWeb && isCompletedOrResolveStatus && isCreateOneAppAndPayrollOrPWACaseOrIsCreateBySystem;
//    }

    boolean isCompletedOrResolvedIsTtbWebOrOneAppPayrollAndPwaCase(CreateCaseDTO caseData) {
        boolean isCompletedOrResolveStatus = (checkCaseType.isCompletedStatus(caseData) || checkCaseType.isResolvedStatus(caseData));
        boolean isOneAppPayrollAndPwaCase = isOneAppPayrollAndPwaCase(caseData);
        return isCompletedOrResolveStatus && isOneAppPayrollAndPwaCase;
    }

    boolean isInProgressFromOneAppOrTtbWebAndSystemCreate(CreateCaseDTO dto) {
        boolean isInProgressStatus = Constant.CASE_GROUP_STATUS_NEW_AND_IN_PROGRESS.contains(dto.getCaseStatusCode());
        boolean isOneAppOrTtbWeb = checkCaseType.isOneAppOrTtbWeb(dto.getIntegrationSystem());
        boolean isCreateBySystem = Constant.SF_EX_API_ID.equalsIgnoreCase(dto.getCreateByEmployeeID()) || Constant.ADMIN_CRM.equalsIgnoreCase(dto.getCreateByEmployeeID());
        return isOneAppOrTtbWeb && isCreateBySystem;
    }

    boolean isOneAppPayrollAndPwaCase(CreateCaseDTO caseData) {
        return checkCaseType.isOneApp(caseData.getIntegrationSystem()) &&
                checkCaseType.isCreateOneAppAndPayrollOrPWACase(caseData.getServiceTypeMatrixCodeOld());
    }

    void validateOwnerRequired(CreateCaseDTO caseData) {
        validateRequired(caseData.getOwnerEmployeeId(), "Owner employee ID is required");
        validateRequired(caseData.getOwnerName(), "Owner name is required");
    }

    void validateOwnerOrTeamRequired(CreateCaseDTO caseData) {
        if (StringUtils.isNotBlank(caseData.getOwnerEmployeeId())) {
            validateOwnerRequired(caseData);
        } else {
            validateRequired(caseData.getOwnerTeamName(),
                    "Owner team name is required when the case has no assigned owner (e.g., OneApp advisor case).");
        }
    }

    void validateCommonFields(CreateCaseDTO caseData) {
        validateRequired(caseData.getCreateByEmployeeID(), "Create by employee ID is required");
        validateRequired(caseData.getCreateByName(), "Create by name is required");
        validateRequired(caseData.getCreatedOn(), "Create on is required");
        validateRequired(caseData.getModifiedOn(), "Last modified on is required");
    }

//    void validateCreatedCaseCompletedData(CreateCaseDTO caseData) {
//        Optional.of(checkCaseType.isCreateOneAppAndPayrollOrPWACase(caseData.getServiceTypeMatrixCodeOld()))
//                .filter(b -> b)
//                .ifPresent(b -> validateRequired(caseData.getClosedDate(), "Closed date is required"));
//    }

    void validateClosedDateData(CreateCaseDTO caseData) {
        validateRequired(caseData.getClosedDate(), "Closed date is required");
    }

    void validateResolveDate(CreateCaseDTO caseData) {
        Optional.of(checkCaseType.isCreateOneAppAndPayrollOrPWACase(caseData.getServiceTypeMatrixCodeOld()))
                .filter(b -> b)
                .ifPresent(b -> validateRequired(caseData.getResolvedDate(), "Resolved date is required"));
    }

    void validateResolveData(CreateCaseDTO caseData) {
        if (!checkCaseType.isFcr(caseData.getFcr())) {
            validateRequired(caseData.getResolutionListCode(), "Resolution code is required");
            validateRequired(caseData.getResolutionListValue(), "Resolution value is required");
            validateRequired(caseData.getRootCauseListCode(), "Root cause code is required");
            validateRequired(caseData.getRootCauseListValue(), "Root cause value is required");
        }
    }

//    void validateModifiedDateCaseData(CreateCaseDTO caseData) {
//        caseRepository.findByCaseNumber(caseData.getCaseNumber())
//                .ifPresent(existingCase -> {
//                    ZonedDateTime existingModifiedOn = existingCase.getModifiedOn();
//                    ZonedDateTime newModifiedOn = caseData.getModifiedOn();
//
//                    if (newModifiedOn == null) {
//                        return;
//                    }
//
//                    if (existingModifiedOn.isAfter(newModifiedOn) || existingModifiedOn.equals(newModifiedOn)) {
//                        throw new IllegalArgumentException("Case has already been modified");
//                    }
//
//                    if (existingModifiedOn.isBefore(newModifiedOn)) {
//                        UUID caseId = existingCase.getCaseId();
//                        caseCommentRepository.deleteCaseCommentModelByCases_CaseId(caseId);
//                        entityManager.flush();
//                        caseDocumentReferenceRepository.deleteCaseDocumentReferenceModelByCases_CaseId(caseId);
//                        entityManager.flush();
//                        caseSlaHopRepository.deleteCaseSlaHopModelByCases_CaseId(caseId);
//                        entityManager.flush();
//                        caseSlaActivityRepository.deleteByCases_caseId(caseId);
//                        entityManager.flush();
//                        caseRepository.deleteByCaseId(caseId);
//                    }
//                });
//    }

    public void validateSfActivitiesCompleted(List<StgSlaPerOwnerModel> sfActivities, CreateCaseDTO dto) {
        boolean isTtbWebOrCreateOneAppAndPayrollOrPWACase = checkCaseType.isTtbWebOrCreateOneAppAndPayrollOrPWACase(dto.getIntegrationSystem(), dto.getServiceTypeMatrixCodeOld());
//        ensureSfActivitiesNotEmpty(sfActivities, isTtbWebOrCreateOneAppAndPayrollOrPWACase);
        validateStartDateAndEndDateSfActivities(sfActivities, isTtbWebOrCreateOneAppAndPayrollOrPWACase, checkCaseType.isCompletedStatus(dto), checkCaseType.isResolvedStatus(dto));
    }

    public void validateSfActivitiesResolved(List<StgSlaPerOwnerModel> sfActivities, CreateCaseDTO dto) {
        boolean isTtbWebOrCreateOneAppAndPayrollOrPWACase = checkCaseType.isTtbWebOrCreateOneAppAndPayrollOrPWACase(dto.getIntegrationSystem(), dto.getServiceTypeMatrixCodeOld());
//        ensureSfActivitiesNotEmpty(sfActivities, isTtbWebOrCreateOneAppAndPayrollOrPWACase);
        ensureAtLeastOneResolved(sfActivities);
        prepareSlaPerOwnerActivities(sfActivities);
        validateStartDateAndEndDateSfActivities(sfActivities, isTtbWebOrCreateOneAppAndPayrollOrPWACase, checkCaseType.isCompletedStatus(dto), checkCaseType.isResolvedStatus(dto));
    }

//    public void ensureSfActivitiesNotEmpty(List<StgSlaPerOwnerModel> sfActivities, boolean isTtbWebOrCreateOneAppAndPayrollOrPWACase) {
//        Optional.of(isTtbWebOrCreateOneAppAndPayrollOrPWACase)
//                .filter(isAllowedEmpty -> !isAllowedEmpty)
//                .map(ignored -> Optional.ofNullable(sfActivities)
//                        .filter(list -> !list.isEmpty())
//                        .orElseThrow(() -> new NotFoundException("SLA per owner is empty"))
//                );
//    }

    void ensureAtLeastOneResolved(List<StgSlaPerOwnerModel> sfActivities) {
        sfActivities.stream()
                .filter(checkCaseType::isResolvedStatus)
                .findAny()
                .orElseThrow(() -> new NotFoundException("SLA per owner not resolved yet"));
    }

    void prepareSlaPerOwnerActivities(List<StgSlaPerOwnerModel> sfActivities) {
        if (sfActivities == null) return;

        sfActivities.stream()
                .filter(activity -> !hasMatchingTeam(activity) && activity.getOwnerTeamNew() != null)
                .forEach(activity -> {
                    activity.setEmployeeIdC(null);
                    activity.setName(null);
                });
    }

    boolean hasMatchingTeam(StgSlaPerOwnerModel sf) {
        return Optional.ofNullable(sf.getEmployeeIdC())
                .map(userService::getTeamsByEmployeeId)
                .map(teamService::findAllTeamsByTeamId)
                .orElseGet(List::of).stream()
                .map(TeamReadonlyModel::getNameTh)
                .anyMatch(name -> Objects.equals(name, sf.getOwnerTeamNew()));
    }

    public void validateStartDateAndEndDateSfActivities(
            List<StgSlaPerOwnerModel> sfActivities,
            boolean isTtbWebOrCreateOneAppAndPayrollOrPWACase,
            boolean isCompleted,
            boolean isResolved
    ) {
        BiConsumer<Integer, StgSlaPerOwnerModel> validator = (index, model) -> {
            StgSlaPerOwnerModel next = (index + 1 < sfActivities.size())
                    ? sfActivities.get(index + 1)
                    : sfActivities.get(sfActivities.size() - 1);

            if (!isTtbWebOrCreateOneAppAndPayrollOrPWACase) {
                isBlankTeamName(index, model);
            }
            isInvalidStart(index, model);
            isInvalidEnd(index, sfActivities.size(), model, next, isCompleted, isResolved);
        };

        IntStream.range(0, sfActivities.size())
                .forEach(i -> validator.accept(i, sfActivities.get(i)));
    }

    void isInvalidStart(Integer index, StgSlaPerOwnerModel model) {
        Optional.ofNullable(model.getStartDateTimeC())
                .filter(StringUtils::isBlank)
                .ifPresent(startDate -> {
                    throw new NotFoundException("Invalid start date: " + model.getStartDateTimeC()
                            + AT_SLA_PER_OWNER + index);
                });
    }

    void isInvalidEnd(
            int index,
            int size,
            StgSlaPerOwnerModel model,
            StgSlaPerOwnerModel sfActivity,
            boolean isCompleted,
            boolean isResolved
    ) {
        boolean isLastHop = index == size - 1;

        Optional.of(isLastHop)
                .filter(isLast -> (isLast && isCompleted && StringUtils.isBlank(model.getEndDateTimeC())))
                .ifPresentOrElse(
                        isLast -> {
                            model.setEndDateTimeC(model.getStartDateTimeC());
                        },
                        () -> {
                            if (!isLastHop && isResolved && StringUtils.isBlank(model.getEndDateTimeC())) {
                                model.setEndDateTimeC(sfActivity.getStartDateTimeC());
                            }
                        }
                );
    }

    void isBlankTeamName(int index, StgSlaPerOwnerModel model) {
        Optional.ofNullable(model.getOwnerTeamNew())
                .filter(StringUtils::isBlank)
                .ifPresent(teamName -> {
                    throw new NotFoundException("Invalid team name: " + teamName + AT_SLA_PER_OWNER + index);
                });
    }
}