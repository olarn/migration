package com.ttb.crm.service.migrationdata.service.preparecaesdataservice;

import com.ttb.crm.lib.crmssp_common_utils_lib.exception.ValidationException;
import com.ttb.crm.service.migrationdata.bean.CreateCaseDTO;
import com.ttb.crm.service.migrationdata.bean.InProgressMigrationResult;
import com.ttb.crm.service.migrationdata.bean.response.TeamUserTotalDurationData;
import com.ttb.crm.service.migrationdata.enums.ServiceTypeMatrixTypeEnum;
import com.ttb.crm.service.migrationdata.helper.Constant;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaHopModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixSla;
import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import com.ttb.crm.service.migrationdata.model.userManagement.TeamReadonlyModel;
import com.ttb.crm.service.migrationdata.repository.masterManagement.ServiceTypeMatrixRepository;
import com.ttb.crm.service.migrationdata.service.CaseMigrationService;
import com.ttb.crm.service.migrationdata.service.SlaService;
import com.ttb.crm.service.migrationdata.service.TeamService;
import com.ttb.crm.service.migrationdata.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ttb.crm.service.migrationdata.helper.DateTimeUtils.parseToZoneDateTime;

@Service
@RequiredArgsConstructor
public class PrepareMetaDataInProgressService {

    // Repository
    private final ServiceTypeMatrixRepository serviceTypeMatrixRepository;
    // Service
    private final CaseMigrationService caseMigrationService;
    private final UserService userService;
    private final SlaService slaServices;
    private final TeamService teamService;

    public void processInProgressCaseStatus(
            CreateCaseDTO dto,
            List<CaseSlaHopModel> caseSlaHopModelList,
            List<StgSlaPerOwnerModel> sfActivities,
            ServiceTypeMatrixModel stm,
            TeamUserTotalDurationData teamUserData
    ) {
        InProgressMigrationResult migrationResult = caseMigrationService.shouldUseLegacySlaData(stm, sfActivities);
        boolean isCanMigrate = migrationResult.isCanMigrate();

        ServiceTypeMatrixTypeEnum stmType = stm.getServiceTypeMatrixType();
        boolean isSTMFix = stmType.equals(ServiceTypeMatrixTypeEnum.FIX);

        List<StgSlaPerOwnerModel> slaPerOwnerActivities = validateSfHistoriesOwnerTeam(dto, isSTMFix, migrationResult, sfActivities);

        if (!isSTMFix) {
            caseSlaHopModelList.addAll(buildCaseSlaHopForDynamic(stm, slaPerOwnerActivities, dto, teamUserData, isCanMigrate));
            return;
        }

        processFixTypeCase(
                stm,
                dto,
                migrationResult,
                caseSlaHopModelList,
                teamUserData
        );
    }

    private List<StgSlaPerOwnerModel> validateSfHistoriesOwnerTeam(
            CreateCaseDTO dto,
            boolean isSTMFix,
            InProgressMigrationResult migrationResult,
            List<StgSlaPerOwnerModel> sfActivities) {
        if (shouldSkipValidation(dto)) {
            return List.of();
        }

        List<StgSlaPerOwnerModel> activitiesToValidate = getActivitiesForValidation(isSTMFix, migrationResult, sfActivities);
        validateOwnerTeamNotNullOrBlank(activitiesToValidate);
        return activitiesToValidate;
    }

    private boolean shouldSkipValidation(CreateCaseDTO dto) {
        return Constant.ONE_APP_STM_MY_ADVISOR_FROM_SALE_FORCE.contains(dto.getServiceTypeMatrixCodeOld());
    }

    private List<StgSlaPerOwnerModel> getActivitiesForValidation(
            boolean isSTMFix,
            InProgressMigrationResult migrationResult,
            List<StgSlaPerOwnerModel> sfActivities
    ) {
        if (migrationResult.isCanMigrate()) {
            return migrationResult.matchedActivities();
        }

        if (!isSTMFix) {
            return sfActivities;
        }

        return List.of();
    }


    private void validateOwnerTeamNotNullOrBlank(List<StgSlaPerOwnerModel> activities) {
        for (StgSlaPerOwnerModel activity : activities) {
            if (activity.getOwnerTeamNew() == null || activity.getOwnerTeamNew().isBlank()) {
                throw new ValidationException("Owner team of Sale force history is required");
            }
        }
    }

    private void processFixTypeCase(
            ServiceTypeMatrixModel stm,
            CreateCaseDTO dto,
            InProgressMigrationResult migrationResult,
            List<CaseSlaHopModel> caseSlaHopModelList,
            TeamUserTotalDurationData teamUserData
    ) {
        boolean shouldUseAutoCreateLogic = shouldUseAutoCreate(dto, migrationResult.matchedActivities());

        if (!migrationResult.isCanMigrate() && !shouldUseAutoCreateLogic) {
            throw new ValidationException("Can not migrate case because of no matched service type matrix slas");
        }

        List<ServiceTypeMatrixSla> stmSlas = stm.getServiceTypeMatrixSlas();
        // Create hop models from STM SLAs
        List<CaseSlaHopModel> hopModels = stmSlas.stream()
                .map(sla -> buildCaseSlaHop(sla, stm, dto, teamUserData))
                .toList();

        setHopEndClosedByCreator(stm, hopModels);

        if (migrationResult.isCanMigrate() && !shouldUseAutoCreateLogic) {
            updateHopModelsWithSfData(hopModels, migrationResult.matchedActivities(), teamUserData);
            validationActiveHopModelIsValid(hopModels, stm.getServiceTypeMatrixType());
        }

        caseSlaHopModelList.addAll(hopModels);
    }

    public void setHopEndClosedByCreator(ServiceTypeMatrixModel stm, List<CaseSlaHopModel> hopModels) {
        if (isClosedByCreator(stm)) {
            CaseSlaHopModel firstHop = hopModels.getFirst();
            hopModels.getLast()
                    .setTeamId(firstHop.getTeamId())
                    .setTeamName(firstHop.getTeamName());
        }
    }

    private static final Set<String> SPECIAL_STM_CODES = Set.of(
            "18050", "29031", "29032", "29033", "29034",
            "27846", "27398", "25637", "XW002", "30697", "30699", "19700"
    );

    private static final Set<String> SPECIAL_TEAM_NAME = Set.of(
            "TMB Contact Center-Follow up"
    );

    public boolean shouldUseAutoCreate(CreateCaseDTO dto, List<StgSlaPerOwnerModel> sfActivities) {
        return isAutoCreateCase(dto, sfActivities) || isSpecialStmCode(dto) || isSpecialTeamName(dto);
    }

    public boolean isSpecialStmCode(CreateCaseDTO dto) {
        if (Objects.isNull(dto)) {
            return false;
        }

        String serviceTypeMatrixCodeOld = dto.getServiceTypeMatrixCodeOld();
        if (serviceTypeMatrixCodeOld != null && SPECIAL_STM_CODES.contains(serviceTypeMatrixCodeOld)) {

            String serviceTypeMatrixCodeNew = dto.getServiceTypeMatrixCode();
            if (serviceTypeMatrixCodeNew != null && !serviceTypeMatrixCodeNew.isBlank()) {
                try {
                    ServiceTypeMatrixModel foundStm = serviceTypeMatrixRepository.findByServiceTypeMatrixCode(serviceTypeMatrixCodeNew);
                    return foundStm != null;
                } catch (Exception e) {
                    return false;
                }
            }
        }

        return false;
    }

    public boolean isSpecialTeamName(CreateCaseDTO dto) {
        if (dto == null) {
            return false;
        }
        String teamName = dto.getResponsibleBuOld();
        return teamName != null && SPECIAL_TEAM_NAME.contains(teamName);
    }

    private void updateHopModelsWithSfData(
            List<CaseSlaHopModel> hopModels,
            List<StgSlaPerOwnerModel> sfActivities,
            TeamUserTotalDurationData teamUserData
    ) {
        for (StgSlaPerOwnerModel sfActivity : sfActivities) {
            hopModels.stream()
                    .filter(hop -> hop.getTeamName().equals(sfActivity.getOwnerTeamNew()))
                    .findFirst()
                    .ifPresent(matchedHop -> updateHopModelFromSfActivity(matchedHop, sfActivity, teamUserData));
        }
    }

    public void validationActiveHopModelIsValid(List<CaseSlaHopModel> hopModels, ServiceTypeMatrixTypeEnum stmType) {
        CaseSlaHopModel latestStartHop = getLatestStartHop(hopModels);

        if (latestStartHop != null) {
            return;
        }

        Optional<CaseSlaHopModel> latestEndHop = getLatestEndHop(hopModels);

        if (latestEndHop.isEmpty()) {
            return;
        }

        CaseSlaHopModel lastCaseEndHop = latestEndHop.get();

        if (lastCaseEndHop.getHopNumber() + 1 > hopModels.size()) {
//            if (stmType.equals(ServiceTypeMatrixTypeEnum.FIX)) {
//                                throw new ValidationException("Last hop of case is already end but case status is new or in_progress");
//            }

//            throw new ValidationException("Service type matrix type is dynamic but case cannot next hop because no histories left");
            lastCaseEndHop.setEndDatetime(null);
            return;
        }

        if (stmType.equals(ServiceTypeMatrixTypeEnum.FIX)) {
            autoStartNextHopWithSTMTypeIsFix(hopModels, lastCaseEndHop);
        }
    }

    public void autoStartNextHopWithSTMTypeIsFix(
            List<CaseSlaHopModel> hopModels,
            CaseSlaHopModel lastCaseEndHop
    ) {
        CaseSlaHopModel nextHop = hopModels.get(lastCaseEndHop.getHopNumber());
        ZonedDateTime slaTargetDate = slaServices.calculateSla(nextHop.getSlaTarget(), lastCaseEndHop.getEndDatetime());
        nextHop.setStartDatetime(lastCaseEndHop.getEndDatetime())
                .setSlaTargetDate(slaTargetDate)
                .setModifiedById(lastCaseEndHop.getModifiedById());
    }

    public CaseSlaHopModel getLatestStartHop(List<CaseSlaHopModel> hopModels) {
        return hopModels.stream()
                .filter(hop -> hop.getStartDatetime() != null)
                .filter(hop -> hop.getEndDatetime() == null)
                .findFirst()
                .orElse(null);
    }

    public Optional<CaseSlaHopModel> getLatestEndHop(List<CaseSlaHopModel> hopModels) {
        return hopModels.stream()
                .filter(hop -> hop.getHopNumber() > 1)
                .filter(hop -> hop.getStartDatetime() != null)
                .filter(hop -> hop.getEndDatetime() != null)
                .max(Comparator.comparingInt(CaseSlaHopModel::getHopNumber));
    }

    private Float calActualDuration(
            ZonedDateTime startDatetime,
            ZonedDateTime endDatetime
    ) {
        return slaServices.calculateSpendingSla(startDatetime, endDatetime);
    }

    private CaseSlaHopModel buildCaseSlaHop(
            ServiceTypeMatrixSla sla,
            ServiceTypeMatrixModel stm,
            CreateCaseDTO dto,
            TeamUserTotalDurationData teamUserData
    ) {
        CaseSlaHopModel hop = new CaseSlaHopModel();
        BeanUtils.copyProperties(sla, hop);
        hop.setIteration(1)
                .setTeamName(sla.getResponsibleBu())
                .setTeamId(sla.getResponsibleBuId())
                .setStatusCode(0);

        Optional.ofNullable(sla.getCloseByBu())
                .ifPresentOrElse(
                        hop::setCloseByBu,
                        () -> hop.setCloseByBu(false)
                );

        Optional.ofNullable(stm)
                .map(ServiceTypeMatrixModel::getServiceTypeMatrixType)
                .filter(type -> type.equals(ServiceTypeMatrixTypeEnum.FIX))
                .flatMap(type -> Optional.ofNullable(teamUserData.getCreateUser()))
                .ifPresent(createUser -> hop.setCreatedById(createUser.getUserId())
                        .setModifiedById(createUser.getUserId())
                        .setCreatedOn(dto.getCreatedOn())
                        .setModifiedOn(dto.getCreatedOn()));


//        hop.setSmsCodeInProgress(stm.getSmsCodeInProgress());

        if (sla.getHopNumber().equals(1)) {
            Optional.ofNullable(teamUserData.getCreateUser())
                    .ifPresent(createUser -> hop.setCreatedById(createUser.getUserId())
                            .setModifiedById(createUser.getUserId())
                            .setCreatedOn(dto.getCreatedOn())
                            .setModifiedOn(dto.getCreatedOn())
                            .setOwnerCase(createUser.getUserId(), createUser.getFullNameTH()));
            Optional.ofNullable(teamUserData.getCreateTeam())
                    .ifPresent(hop::setTeamInfo);

            hop.setTotalDuration(0F);

            hop.setStartDatetime(dto.getCreatedOn())
                    .setEndDatetime(dto.getCreatedOn());
            if (sla.getSlaTarget() != null) {
                ZonedDateTime slaTargetDate = slaServices.calculateSla(sla.getSlaTarget(), hop.getStartDatetime());
                hop.setSlaTargetDate(slaTargetDate);
            }
        } else if (sla.getHopNumber().equals(2)) {
            Optional.ofNullable(teamUserData.getCreateUser())
                    .ifPresent(createUser -> hop.setCreatedById(createUser.getUserId())
                            .setCreatedOn(dto.getCreatedOn())
                            .setModifiedById(createUser.getUserId())
                            .setModifiedOn(dto.getCreatedOn())
                    );
            hop.setStartDatetime(dto.getCreatedOn());
            if (sla.getSlaTarget() != null) {
                ZonedDateTime slaTargetDate = slaServices.calculateSla(sla.getSlaTarget(), hop.getStartDatetime());
                hop.setSlaTargetDate(slaTargetDate);
            }
        } else {
            java.util.Objects.requireNonNull(Boolean.TRUE);
        }

        return hop;
    }

    private void updateHopModelFromSfActivity(
            CaseSlaHopModel hop,
            StgSlaPerOwnerModel sfActivity,
            TeamUserTotalDurationData teamUserData
    ) {
        if (sfActivity.getEmployeeIdC() != null && !sfActivity.getEmployeeIdC().isBlank()) {
            userService.getTeamsByEmployeeId(sfActivity.getEmployeeIdC()).stream()
                    .filter(teamId -> teamId.equals(hop.getTeamId()))
                    .findFirst()
                    .flatMap(teamId -> userService.fetchUserByEmployeeId(sfActivity.getEmployeeIdC()))
                    .ifPresent(user -> hop.setOwnerCase(user.getUserId(), user.getFullNameTH()));

//            List<UUID> teamOfEmployeeList = userService.getTeamsByEmployeeId(sfActivity.getEmployeeIdC());
//            UUID matchTeamId = teamOfEmployeeList.stream().filter(teamId -> teamId.equals(hop.getTeamId())).findFirst().orElse(null);
//            if (matchTeamId != null) {
//                EmployeeUserModel employeeUserModel = userService.fetchUserByEmployeeId(sfActivity.getEmployeeIdC()).orElse(null);
//                if (employeeUserModel != null) {
//                    hop.setOwnerCase(employeeUserModel.getUserId(), employeeUserModel.getFullNameTH());
//                }
//            }
        }

        if (hop.getStartDatetime() == null) {
            hop.setStartDatetime(parseToZoneDateTime(sfActivity.getStartDateTimeC()));
        }

        if (hop.getCreatedOn() == null) {
            hop.setCreatedOn(hop.getStartDatetime());
        }

        if (hop.getSlaTarget() != null) {
            ZonedDateTime slaTargetDate = slaServices.calculateSla(hop.getSlaTarget(), hop.getStartDatetime());
            hop.setSlaTargetDate(slaTargetDate);
        }

        if (sfActivity.getEndDateTimeC() != null && !sfActivity.getEndDateTimeC().isBlank()) {
            hop.setEndDatetime(parseToZoneDateTime(sfActivity.getEndDateTimeC()));
            if (hop.getSlaTarget() != null) {
                hop.setTotalDuration(calActualDuration(hop.getStartDatetime(), hop.getEndDatetime()));
            }
        }

        Optional.ofNullable(hop.getEndDatetime())
                .ifPresentOrElse(
                        hop::setModifiedOn,
                        () -> hop.setModifiedOn(hop.getStartDatetime())
                );

        Optional.ofNullable(hop.getOwnerId())
                .ifPresentOrElse(
                        hop::setModifiedById,
                        () -> hop.setModifiedById(teamUserData.getNaUser().getUserId())
                );
    }

    private List<CaseSlaHopModel> buildCaseSlaHopForDynamic(
            ServiceTypeMatrixModel stm,
            List<StgSlaPerOwnerModel> sfActivities,
            CreateCaseDTO dto,
            TeamUserTotalDurationData teamUserData,
            boolean isCanMigrate
    ) {
        List<CaseSlaHopModel> hopModels = buildCaseSLAHopFirstAndSecondHop(stm, dto, teamUserData);
        boolean shouldUseAutoCreateLogic = shouldUseAutoCreate(dto, sfActivities);

        if (shouldUseAutoCreateLogic || sfActivities.isEmpty()) {
            return hopModels;
        }

        for (StgSlaPerOwnerModel sfActivity : sfActivities) {
            Optional<CaseSlaHopModel> matchedHop = hopModels.stream()
                    .filter(hop -> hop.getHopNumber() > 1)
                    .filter(hop -> hop.getTeamName().equals(sfActivity.getOwnerTeamNew()))
                    .findFirst();

            if (((java.util.Optional<?>) matchedHop).isPresent() && isCanMigrate) {
                updateHopModelFromSfActivity(matchedHop.get(), sfActivity, teamUserData);
                continue;
            } else {
                matchedHop.ifPresent(caseSlaHopModel -> caseSlaHopModel
                        .setStartDatetime(dto.getCreatedOn())
                        .setModifiedById(teamUserData.getNaUser().getUserId())
                        .setModifiedOn(dto.getCreatedOn()));
            }

            hopModels.add(
                    prepareSlaHopForDynamic(stm, dto, sfActivity, hopModels, teamUserData)
            );
        }
        validationActiveHopModelIsValid(hopModels, stm.getServiceTypeMatrixType());

        return hopModels;
    }

    private List<CaseSlaHopModel> buildCaseSLAHopFirstAndSecondHop(
            ServiceTypeMatrixModel stm,
            CreateCaseDTO dto,
            TeamUserTotalDurationData teamUserData
    ) {
        List<ServiceTypeMatrixSla> stmSlaFirstAndSecondHop = stm.getServiceTypeMatrixSlas()
                .stream()
                .filter(hop -> List.of(1, 2).contains(hop.getHopNumber()))
                .toList();

        return stmSlaFirstAndSecondHop.stream()
                .map(sla -> buildCaseSlaHop(sla, stm, dto, teamUserData))
                .collect(Collectors.toList());
    }

    private CaseSlaHopModel prepareSlaHopForDynamic(
            ServiceTypeMatrixModel stm,
            CreateCaseDTO dto,
            StgSlaPerOwnerModel sfActivity,
            List<CaseSlaHopModel> hopModels,
            TeamUserTotalDurationData teamUserData
    ) {
        ServiceTypeMatrixSla serviceTypeMatrixSla = buildServiceTypeMatrixSlaModelForDynamic(stm, sfActivity, hopModels);
        CaseSlaHopModel caseSlaHopModel = buildCaseSlaHop(serviceTypeMatrixSla, stm, dto, teamUserData);
        if (caseSlaHopModel.getTeamId() == null) {
            TeamReadonlyModel teamInfo = teamService.retrieveTeamByName(sfActivity.getOwnerTeamNew());
            caseSlaHopModel.setTeamInfo(teamInfo);
        }
        updateHopModelFromSfActivity(caseSlaHopModel, sfActivity, teamUserData);
        Optional.of(hopModels.getLast())
                .map(CaseSlaHopModel::getOwnerId)
                .ifPresentOrElse(caseSlaHopModel::setCreatedById,
                        () ->
                                caseSlaHopModel.setCreatedById(teamUserData.getNaUser().getUserId())
                );
        if (hopModels.getLast().getEndDatetime() == null) {
            hopModels.getLast().setEndDatetime(caseSlaHopModel.getStartDatetime());
        }

        return caseSlaHopModel;
    }

    public ServiceTypeMatrixSla buildServiceTypeMatrixSlaModelForDynamic(
            ServiceTypeMatrixModel stm,
            StgSlaPerOwnerModel sfActivity,
            List<CaseSlaHopModel> hopModels
    ) {
        ServiceTypeMatrixSla serviceTypeMatrixSla = new ServiceTypeMatrixSla();
        ServiceTypeMatrixSla slaEnd = stm.getServiceTypeMatrixSlas().getLast();
        boolean isHopEnd = slaEnd
                .getResponsibleBu()
                .equals(sfActivity.getOwnerTeamNew());
        if (isHopEnd) {
            serviceTypeMatrixSla.setSlaTarget(slaEnd.getSlaTarget())
                    .setServiceTypeMatrixSlaId(slaEnd.getServiceTypeMatrixSlaId())
                    .setResponsibleBu(slaEnd.getResponsibleBu())
                    .setResponsibleBuId(slaEnd.getResponsibleBuId())
                    .setCloseByBu(slaEnd.getCloseByBu());
        } else if (isClosedByCreator(stm) && sfActivity.getOwnerTeamNew().equals(hopModels.getFirst().getTeamName())) {
            serviceTypeMatrixSla.setSlaTarget(slaEnd.getSlaTarget())
                    .setServiceTypeMatrixSlaId(slaEnd.getServiceTypeMatrixSlaId())
                    .setCloseByBu(slaEnd.getCloseByBu())
                    .setResponsibleBu(hopModels.getFirst().getTeamName())
                    .setResponsibleBuId(hopModels.getFirst().getTeamId());
        } else {
            ServiceTypeMatrixSla slaBu = stm.getServiceTypeMatrixSlas().get(1);
            serviceTypeMatrixSla.setServiceTypeMatrixSlaId(slaBu.getServiceTypeMatrixSlaId())
                    .setCloseByBu(slaBu.getCloseByBu());
            if (sfActivity.getOwnerTeamNew().equals(slaBu.getResponsibleBu())) {
                serviceTypeMatrixSla.setResponsibleBu(slaBu.getResponsibleBu())
                        .setResponsibleBuId(slaBu.getResponsibleBuId());
            }
        }
        Integer hopNumber = hopModels.getLast().getHopNumber();
        serviceTypeMatrixSla.setHopNumber(hopNumber + 1);
        return serviceTypeMatrixSla;
    }

    public boolean isAutoCreateCase(CreateCaseDTO dto, List<StgSlaPerOwnerModel> slaLogs) {
        if (dto == null) {
            return false;
        }

        String createdByTeamName = dto.getCreateByTeamName();
        if (createdByTeamName == null || createdByTeamName.isBlank()) {
            return false;
        }

        String ownerTeamName = dto.getOwnerTeamName();
        if (ownerTeamName == null || ownerTeamName.isBlank()) {
            return false;
        }

        boolean isSameTeam = createdByTeamName.equals(ownerTeamName);

        if (slaLogs.isEmpty()) {
            return isSameTeam;
        }

        boolean hasOtherTeam = slaLogs.stream()
                .anyMatch(sla -> !Objects.equals(sla.getOwnerTeamNew(), createdByTeamName));

        return !hasOtherTeam;
    }

    public boolean isClosedByCreator(ServiceTypeMatrixModel stm) {
        if (stm == null) {
            return false;
        }

        if (stm.getServiceTypeMatrixSlas().isEmpty() || stm.getServiceTypeMatrixSlas().size() < 2) {
            return false;
        }

        String firstHopBu = stm.getServiceTypeMatrixSlas().getFirst().getResponsibleBu();
        String endHopBu = stm.getServiceTypeMatrixSlas().getLast().getResponsibleBu();
        return firstHopBu.equals(endHopBu);
    }

}


