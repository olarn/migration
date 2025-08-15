package com.ttb.crm.service.migrationdata.service.preparecaesdataservice;

import com.ttb.crm.service.migrationdata.bean.CreateCaseDTO;
import com.ttb.crm.service.migrationdata.bean.response.TeamUserTotalDurationData;
import com.ttb.crm.service.migrationdata.helper.Constant;
import com.ttb.crm.service.migrationdata.helper.DateTimeUtils;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaActivity;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaHopModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class PrepareMetaDataCompletedData {

    private final PrepareHopAndActivityService prepareHopAndActivityService;
    private final EnhancedCaseValidationService enhancedCaseValidationService;
    private final CheckCaseType checkCaseType;

    public void processCompletedCaseStatus(
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
        enhancedCaseValidationService.validateSfActivitiesCompleted(sfActivities, dto);
        prepareHopAndActivityService.buildFirstHop(hopModels, dto, teamUserData, dto.getCreatedOn());
        prepareHopAndActivityService.buildHops(hopModels, sfActivities, teamUserData, hopModels, checkCaseType.isAutoclosedAfterResolved(stm), dto);
        prepareHopAndActivityService.prepareActivitiesForCompleted(dto, hopModels, activities, sfActivities, teamUserData, slaStartDate);
    }

    private void handleSfActivitiesEmpty(
            CreateCaseDTO dto,
            TeamUserTotalDurationData teamUserData,
            List<CaseSlaHopModel> hopModels,
            List<CaseSlaActivity> activities,
            List<StgSlaPerOwnerModel> sfActivities,
            ZonedDateTime slaStartDate
    ) {
        if (sfActivities == null) {
            sfActivities = new ArrayList<>();
        }
        sfActivities.add(createClosedActivity(dto));
        prepareHopAndActivityService.buildFirstHop(hopModels, dto, teamUserData, dto.getClosedDate());
        prepareHopAndActivityService.prepareActivitiesForCompleted(dto, hopModels, activities, sfActivities, teamUserData, slaStartDate);
    }

    private StgSlaPerOwnerModel createClosedActivity(CreateCaseDTO dto) {
        return new StgSlaPerOwnerModel()
                .setCaseC(dto.getExternalId())
                .setOwnerTeamC(dto.getCreateByTeamName())
                .setOwnerTeamNew(dto.getCreateByTeamName())
                .setStartDateTimeC(DateTimeUtils.convertToString(dto.getCreatedOn()))
                .setEndDateTimeC(DateTimeUtils.convertToString(dto.getClosedDate()))
                .setCaseStatusC(Constant.CASE_STATUS_COMPLETED);
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

        Optional.of(dto.getOwnerEmployeeId())
                .map(id -> updateCaseMyAdvisorData().apply(dto))
                .map(createActivityWithEmployeeId())
                .ifPresentOrElse(sfActivity -> {
                    sfActivities.clear();
                    sfActivities.add(sfActivity);
                }, () -> {
                    throw new IllegalArgumentException("OwnerEmployeeId is required (e.g., OneApp advisor case).");
                });
    }

    private Function<CreateCaseDTO, StgSlaPerOwnerModel> createActivityWithEmployeeId() {
        return dto -> new StgSlaPerOwnerModel()
                .setCaseC(dto.getExternalId())
                .setOwnerTeamC(null)
                .setOwnerTeamNew(null)
                .setEmployeeIdC(dto.getOwnerEmployeeId())
                .setName(dto.getOwnerName())
                .setStartDateTimeC(DateTimeUtils.convertToString(dto.getCreatedOn()))
                .setEndDateTimeC(DateTimeUtils.convertToString(dto.getClosedDate()))
                .setCaseStatusC(Constant.CASE_STATUS_RESOLVED_VALUE);
    }

    private Function<CreateCaseDTO, CreateCaseDTO> updateCaseMyAdvisorData() {
        return dto -> {
            dto.setIsHopLocked(Boolean.TRUE);
            dto.setIsSTMLocked(Boolean.TRUE);
            dto.setIsHopLocked(Boolean.TRUE);
            return dto;
        };
    }
}
