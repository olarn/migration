package com.ttb.crm.service.migrationdata.service.preparecaesdataservice;

import com.ttb.crm.lib.crmssp_common_utils_lib.exception.NotFoundException;
import com.ttb.crm.service.migrationdata.bean.CreateCaseDTO;
import com.ttb.crm.service.migrationdata.bean.response.TeamUserTotalDurationData;
import com.ttb.crm.service.migrationdata.helper.Constant;
import com.ttb.crm.service.migrationdata.helper.DateTimeUtils;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaActivity;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaHopModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixSla;
import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import com.ttb.crm.service.migrationdata.service.MasterDataService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class PrepareMetaDataResolvedData {

    private final PrepareHopAndActivityService prepareHopAndActivityService;
    private final EnhancedCaseValidationService enhancedCaseValidationService;
    private final CheckCaseType checkCaseType;
    private final MasterDataService masterDataService;

    public void processResolvedCaseStatus(
            CreateCaseDTO dto,
            TeamUserTotalDurationData teamUserData,
            List<CaseSlaHopModel> hopModels,
            List<CaseSlaActivity> activities,
            List<StgSlaPerOwnerModel> sfActivities,
            ServiceTypeMatrixModel stm,
            ZonedDateTime slaStartDate
    ) {
        updateCaseSurroundingWeb(dto, stm, sfActivities);

        Optional.ofNullable(sfActivities)
                .filter(list -> !list.isEmpty())
                .ifPresentOrElse(
                        list -> handleSfActivitiesPresent(dto, teamUserData, hopModels, activities, sfActivities, stm, slaStartDate),
                        () -> handleSfActivitiesEmpty(dto, teamUserData, hopModels, activities, sfActivities, slaStartDate)
                );
    }

    private void handleSfActivitiesPresent(
            CreateCaseDTO dto,
            TeamUserTotalDurationData teamUserData,
            List<CaseSlaHopModel> hopModels,
            List<CaseSlaActivity> activities,
            List<StgSlaPerOwnerModel> sfActivities,
            ServiceTypeMatrixModel stm,
            ZonedDateTime slaStartDate
    ) {
        updateTeamClosedByBuInStm(teamUserData, stm);
        validateSfActivities(sfActivities, dto);
        updateCaseStatusAndPrepareSfActivities(dto, sfActivities, stm);
        buildCaseHopsAndActivities(dto, teamUserData, hopModels, activities, sfActivities, slaStartDate);
    }

    private void handleSfActivitiesEmpty(
            CreateCaseDTO dto,
            TeamUserTotalDurationData teamUserData,
            List<CaseSlaHopModel> hopModels,
            List<CaseSlaActivity> activities,
            List<StgSlaPerOwnerModel> sfActivities,
            ZonedDateTime slaStartDate
    ) {
        completeCase(dto);
        if (sfActivities == null) {
            sfActivities = new ArrayList<>();
        }
        sfActivities.add(createClosedActivity(dto));
        prepareHopAndActivityService.buildFirstHop(hopModels, dto, teamUserData, dto.getResolvedDate());
        prepareHopAndActivityService.prepareActivitiesForResolved(dto, hopModels, activities, sfActivities, teamUserData, slaStartDate);
    }

    private void completeCase(CreateCaseDTO dto) {
        dto.setCaseStatusCode(Constant.CASE_STATUS_COMPLETED);
        dto.setCaseStatusValue(Constant.CASE_STATUS_COMPLETED_VALUE);
    }

    private StgSlaPerOwnerModel createClosedActivity(CreateCaseDTO dto) {
        return new StgSlaPerOwnerModel()
                .setCaseC(dto.getExternalId())
                .setOwnerTeamC(dto.getCreateByTeamName())
                .setOwnerTeamNew(dto.getCreateByTeamName())
                .setStartDateTimeC(DateTimeUtils.convertToString(dto.getCreatedOn()))
                .setEndDateTimeC(DateTimeUtils.convertToString(dto.getResolvedDate()))
                .setCaseStatusC(Constant.CASE_STATUS_COMPLETED);
    }

    private void validateSfActivities(List<StgSlaPerOwnerModel> sfActivities, CreateCaseDTO dto) {
        enhancedCaseValidationService.validateSfActivitiesResolved(sfActivities, dto);
    }

    private void updateTeamClosedByBuInStm(TeamUserTotalDurationData teamUserData, ServiceTypeMatrixModel stm) {
        Optional.ofNullable(findLastSla(stm))
                .filter(this::isLastHopCreatedByCreatorTeam)
                .ifPresent(lastSla -> masterDataService.updateHopWithCreatorTeam(teamUserData.getCreateTeam(), lastSla, stm.getServiceTypeMatrixCode()));
    }

    private boolean isLastHopCreatedByCreatorTeam(ServiceTypeMatrixSla hop) {
        return hop != null && Constant.CREATOR_TEAM.equalsIgnoreCase(hop.getResponsibleBu());
    }

//    @CacheEvict(value = "serviceTypeMatrixByCode", key = "#serviceTypeMatrixCode", beforeInvocation = true)
//    public void updateHopWithCreatorTeam(TeamReadonlyModel createTeam, ServiceTypeMatrixSla hop, String serviceTypeMatrixCode) {
//        hop.setResponsibleBuId(createTeam.getTeamId());
//        hop.setResponsibleBu(createTeam.getNameTh());
//    }

    private void updateCaseStatusAndPrepareSfActivities(CreateCaseDTO dto, List<StgSlaPerOwnerModel> sfActivities, ServiceTypeMatrixModel stm) {
        Optional.of(isAutoclosedAfterResolved(stm, dto.getServiceTypeMatrixCodeOld()))
            .filter(Boolean::booleanValue)
            .ifPresentOrElse(
                v -> completeCaseAndAutoClosedAfterResolve(dto),
                () -> prepareSfActivities(sfActivities, stm)
            );
    }

    private void buildCaseHopsAndActivities(
            CreateCaseDTO dto,
            TeamUserTotalDurationData teamUserData,
            List<CaseSlaHopModel> hopModels,
            List<CaseSlaActivity> activities,
            List<StgSlaPerOwnerModel> sfActivities,
            ZonedDateTime slaStartDate
    ) {
        prepareHopAndActivityService.buildFirstHop(hopModels, dto, teamUserData, dto.getCreatedOn());
        prepareHopAndActivityService.buildHops(hopModels, sfActivities, teamUserData, hopModels, checkCaseType.isAutoclosedAfterResolved(dto), dto);
        prepareHopAndActivityService.prepareActivitiesForResolved(dto, hopModels, activities, sfActivities, teamUserData, slaStartDate);
    }

    private void prepareSfActivities(
            List<StgSlaPerOwnerModel> sfActivities,
            ServiceTypeMatrixModel stm
    ) {
        findLastValidResolvedActivity(sfActivities)
            .ifPresent(entry -> handleResolvedActivity(sfActivities, stm, entry));
    }

    private Optional<Map.Entry<Integer, StgSlaPerOwnerModel>> findLastValidResolvedActivity(
            List<StgSlaPerOwnerModel> sfActivities) {

        return IntStream.iterate(sfActivities.size() - 1, i -> i >= 0, i -> i - 1)
                .mapToObj(i -> Map.entry(i, sfActivities.get(i)))
                .filter(entry -> isResolvedActivityWithUser(entry.getValue()))
                .findFirst();
    }

    private void handleResolvedActivity(
            List<StgSlaPerOwnerModel> sfActivities,
            ServiceTypeMatrixModel stm,
            Map.Entry<Integer, StgSlaPerOwnerModel> entry
    ) {
        int index = entry.getKey();
        StgSlaPerOwnerModel current = entry.getValue();

        Optional.of(current)
                .filter(act -> isClosedByBu(stm, act.getOwnerTeamNew()))
                .ifPresentOrElse(
                        closed -> clearSfActivityAfterIndex(sfActivities, index),
                        () -> handleNextOrInsertClosedByBu(sfActivities, stm, index, current)
                );
    }

    private void handleNextOrInsertClosedByBu(
            List<StgSlaPerOwnerModel> sfActivities,
            ServiceTypeMatrixModel stm,
            int index,
            StgSlaPerOwnerModel current
    ) {
        getNextActivity(sfActivities, index)
                .filter(next -> isClosedByBu(stm, next.getOwnerTeamNew()))
                .ifPresentOrElse(
                        next -> clearSfActivityAfterIndex(sfActivities, index + 1),
                        () -> insertClosedByBuAfter(sfActivities, index, current, stm)
                );
    }

    private void insertClosedByBuAfter(
            List<StgSlaPerOwnerModel> sfActivities,
            int index,
            StgSlaPerOwnerModel currentActivity,
            ServiceTypeMatrixModel stm
    ) {
        clearSfActivityAfterIndex(sfActivities, index);
        String now = DateTimeUtils.getStaringLocalDateTime();
        ensureEndDateSet(currentActivity, now);
        sfActivities.add(createClosedByBuActivity(stm, now));
    }

    private boolean isResolvedActivityWithUser(StgSlaPerOwnerModel activity) {
        return Constant.CASE_STATUS_RESOLVED.equalsIgnoreCase(activity.getCaseStatusC()) &&
                !StringUtils.isBlank(activity.getEmployeeIdC());
    }

    private void completeCaseAndAutoClosedAfterResolve(CreateCaseDTO dto) {
        dto.setCaseStatusCode(Constant.CASE_STATUS_COMPLETED);
        dto.setCaseStatusValue(Constant.CASE_STATUS_COMPLETED_VALUE);
        dto.setAutoCloseCaseAfterResolved(true);
    }

    private void  clearSfActivityAfterIndex(List<StgSlaPerOwnerModel> list, int index) {
        Optional.of(index + 1)
                .filter(start -> start < list.size())
                .ifPresent(start -> list.subList(start, list.size()).clear());
    }

    private Optional<StgSlaPerOwnerModel> getNextActivity(List<StgSlaPerOwnerModel> list, int index) {
        return Optional.of(index + 1)
                .filter(i -> i < list.size())
                .map(list::get);
    }

    private void ensureEndDateSet(StgSlaPerOwnerModel activity, String endDate) {
        Optional.ofNullable(activity.getEndDateTimeC())
                .filter(StringUtils::isBlank)
                .ifPresent(end -> {
                    activity.setEndDateTimeC(endDate);
                });
    }

    private StgSlaPerOwnerModel createClosedByBuActivity(ServiceTypeMatrixModel stm, String now) {
        StgSlaPerOwnerModel closedByBuActivity = new StgSlaPerOwnerModel();
        closedByBuActivity.setOwnerTeamNew(getClosedByBu(stm));
        closedByBuActivity.setStartDateTimeC(now);
        return closedByBuActivity;
    }

    private void updateCaseSurroundingWeb(CreateCaseDTO dto, ServiceTypeMatrixModel stm,
                                          List<StgSlaPerOwnerModel> sfActivities) {
        Optional.of(checkCaseType.isOneAppOrTtbWeb(dto.getIntegrationSystem()))
                .filter(Boolean::booleanValue)
                .ifPresent(result -> {
                    updateAutoCloseCaseAfterResolvedData(dto, stm);
                    prepareSfActivityFormStg(dto, sfActivities);
                });
    }

    private void updateAutoCloseCaseAfterResolvedData(CreateCaseDTO dto, ServiceTypeMatrixModel stm) {
        Optional.of(checkCaseType.isAutoclosedAfterResolved(stm))
                .filter(Boolean::booleanValue)
                .ifPresent(t -> dto.setAutoCloseCaseAfterResolved(Boolean.TRUE));
    }

    private void prepareSfActivityFormStg(CreateCaseDTO dto, List<StgSlaPerOwnerModel> sfActivities) {
        if (!checkCaseType.isCreateOneAppAndPayrollOrPWACase(dto.getServiceTypeMatrixCodeOld())) return;

        String ownerId = dto.getOwnerEmployeeId();

        Optional.ofNullable(ownerId)
                .map(id -> createActivityWithEmployeeId(dto, id))
                .ifPresentOrElse(sfActivity -> {
                    sfActivities.clear();
                    sfActivities.add(sfActivity);
                }, () -> {
                    throw new IllegalArgumentException("OwnerEmployeeId is required (e.g., OneApp advisor case).");
                });
    }

    private StgSlaPerOwnerModel createBaseActivity(CreateCaseDTO dto) {
        return new StgSlaPerOwnerModel()
                .setCaseC(dto.getExternalId())
                .setOwnerTeamC(null)
                .setOwnerTeamNew(null)
                .setStartDateTimeC(DateTimeUtils.convertToString(dto.getCreatedOn()))
                .setEndDateTimeC(DateTimeUtils.convertToString(dto.getResolvedDate()))
                .setCaseStatusC(Constant.CASE_STATUS_RESOLVED_VALUE);
    }

    private StgSlaPerOwnerModel createActivityWithEmployeeId(CreateCaseDTO dto, String employeeId) {
        return createBaseActivity(dto)
                .setEmployeeIdC(employeeId)
                .setName(dto.getOwnerName());
    }

    private boolean isAutoclosedAfterResolved(ServiceTypeMatrixModel stm, String stmCodeOld) {
        return Boolean.TRUE.equals(stm.getAutoCloseCaseAfterResolved()) || checkCaseType.isCreateOneAppAndPayrollOrPWACase(stmCodeOld) ;
    }

    private Boolean isClosedByBu(ServiceTypeMatrixModel stm, String teamName) {
        return Optional.ofNullable(getClosedByBu(stm)).map(closedByBu -> closedByBu.equals(teamName)).orElse(false);
    }

    private String getClosedByBu(ServiceTypeMatrixModel stm) {
        return Optional.ofNullable(findLastSla(stm)).map(ServiceTypeMatrixSla::getResponsibleBu).orElse(null);
    }

    private ServiceTypeMatrixSla findLastSla(ServiceTypeMatrixModel stm) {
        return Optional.ofNullable(stm.getServiceTypeMatrixSlas()).map(List::getLast).orElse(null);
    }
}
