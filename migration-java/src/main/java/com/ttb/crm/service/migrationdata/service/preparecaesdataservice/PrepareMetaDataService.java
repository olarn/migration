package com.ttb.crm.service.migrationdata.service.preparecaesdataservice;

import com.ttb.crm.service.migrationdata.bean.CreateCaseDTO;
import com.ttb.crm.service.migrationdata.bean.response.MetaData;
import com.ttb.crm.service.migrationdata.bean.response.TeamUserTotalDurationData;
import com.ttb.crm.service.migrationdata.enums.CaseSlaActivityAction;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaActivity;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaHopModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import com.ttb.crm.service.migrationdata.repository.secondary.StgSlaPerOwnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrepareMetaDataService {

    private final PrepareTeamUserData prepareTeamUserActualDurationData;
    private final PrepareHopAndActivityService prepareHopAndActivityData;
    private final PrepareMetaDataCompletedData prepareMetaDataCompletedData;
    private final PrepareMetaDataInProgressService prepareMetaDataInProgressService;
    private final PrepareMetaDataResolvedData prepareMetaDataResolvedData;
    private final StgSlaPerOwnerRepository stgSlaPerOwnerRepository;
    private final CheckCaseType checkCaseType;

    public MetaData prepareMetaData(CreateCaseDTO dto, ServiceTypeMatrixModel stm, ZonedDateTime slaStartDate) {
        TeamUserTotalDurationData teamUserData = prepareTeamUserActualDurationData.prepareTeamUserData(dto);

        boolean isFcrFromStm = Boolean.TRUE.equals(stm.getFcr());

        List<CaseSlaActivity> activities = new ArrayList<>();

        if (isFcr(dto, isFcrFromStm)) {
            return handleFcr(dto, teamUserData, activities);
        }

        List<StgSlaPerOwnerModel> sfActivities = stgSlaPerOwnerRepository
                .findAllByCaseCOrderByStartDateTimeCAsc(dto.getExternalId());

        List<CaseSlaHopModel> slaHops = new ArrayList<>();

        ServiceTypeMatrixModel detachedCopy = new ServiceTypeMatrixModel(stm);

        return handleNonFcr(dto, detachedCopy, teamUserData, sfActivities, slaHops, activities, slaStartDate);
    }

    private MetaData handleFcr(CreateCaseDTO dto, TeamUserTotalDurationData teamUserData, List<CaseSlaActivity> activities) {
        activities.addAll(List.of(
                buildActivityFcr(CaseSlaActivityAction.CREATE, dto, teamUserData, 0f),
                buildActivityFcr(CaseSlaActivityAction.AUTO_COMPLETED, dto, teamUserData, null)
        ));

        return buildMetaData(teamUserData, new ArrayList<>(), prepareHopAndActivityData.updateDateTimeActivity(activities));
    }

    private MetaData handleNonFcr(
            CreateCaseDTO dto,
            ServiceTypeMatrixModel stm,
            TeamUserTotalDurationData teamUserData,
            List<com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel> sfActivities,
            List<CaseSlaHopModel> slaHops,
            List<CaseSlaActivity> activities,
            ZonedDateTime slaStartDate
    ) {
        if (checkCaseType.isCompletedStatus(dto)) {
            prepareMetaDataCompletedData.processCompletedCaseStatus(dto, teamUserData, slaHops, activities, sfActivities, stm, slaStartDate);
        } else if (checkCaseType.isResolvedStatus(dto)) {
            prepareMetaDataResolvedData.processResolvedCaseStatus(dto, teamUserData, slaHops, activities, sfActivities, stm, slaStartDate);
        } else if (checkCaseType.isInProgressOrNew(dto)) {
            prepareMetaDataInProgressService.processInProgressCaseStatus(dto, slaHops, sfActivities, stm, teamUserData);
        }
        else {
            java.util.Objects.requireNonNull(Boolean.TRUE);
        }

        return buildMetaData(teamUserData, slaHops, prepareHopAndActivityData.updateDateTimeActivity(activities));
    }

    private MetaData buildMetaData(TeamUserTotalDurationData data, List<CaseSlaHopModel> hops, List<CaseSlaActivity> activities) {
        return new MetaData(
                data.getCreateTeam(),
                data.getResolveTeam(),
                data.getClosedTeam(),
                data.getCreateUser(),
                data.getResolveUser(),
                data.getClosedUser(),
                data.getNaUser(),
                data.getTotalDuration(),
                hops,
                activities
        );
    }

    private boolean isFcr(CreateCaseDTO dto, boolean isFcrFromStm) {
        return isFcrFromStm && (
                checkCaseType.isCompletedStatus(dto)
                        || checkCaseType.isInProgressOrNew(dto)
                        || checkCaseType.isResolvedStatus(dto));
    }

    private CaseSlaActivity buildActivityFcr(
            CaseSlaActivityAction action,
            CreateCaseDTO dto,
            TeamUserTotalDurationData data,
            Float duration
    ) {
        return prepareHopAndActivityData.createSlaActivity(
                action,
                1,
                1,
                data.getCreateUser(),
                data.getCreateTeam(),
                dto,
                duration,
                dto.getCreatedOn(),
                dto.getCreatedOn(),
                data.getCreateUser()
        );
    }
}
