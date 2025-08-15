package com.ttb.crm.service.migrationdata.service.preparecaesdataservice;

import com.ttb.crm.lib.crmssp_common_utils_lib.exception.NotFoundException;
import com.ttb.crm.service.migrationdata.bean.CreateCaseDTO;
import com.ttb.crm.service.migrationdata.bean.response.MetaData;
import com.ttb.crm.service.migrationdata.bean.response.TeamUserTotalDurationData;
import com.ttb.crm.service.migrationdata.enums.CaseSlaActivityAction;
import com.ttb.crm.service.migrationdata.enums.ServiceTypeMatrixTypeEnum;
import com.ttb.crm.service.migrationdata.helper.Constant;
import com.ttb.crm.service.migrationdata.helper.Utils;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaActivity;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaHopModel;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseTransactionModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import com.ttb.crm.service.migrationdata.model.userManagement.EmployeeUserModel;
import com.ttb.crm.service.migrationdata.model.userManagement.TeamReadonlyModel;
import com.ttb.crm.service.migrationdata.service.SlaService;
import com.ttb.crm.service.migrationdata.service.TeamService;
import com.ttb.crm.service.migrationdata.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ttb.crm.service.migrationdata.helper.DateTimeUtils.parseToZoneDateTime;

@Service
@RequiredArgsConstructor
public class PrepareHopAndActivityService {

    private final UserService userService;
    private final TeamService teamService;
    private final SlaService slaService;
    private final CheckCaseType checkCaseType;

    public void buildFirstHop(List<CaseSlaHopModel> hopModels, CreateCaseDTO dto, TeamUserTotalDurationData userData, ZonedDateTime endDate) {
        var hop = new CaseSlaHopModel();
        hop.setHopNumber(1);
        hop.setStartDatetime(dto.getCreatedOn());
        hop.setCreatedOn(dto.getCreatedOn());
        hop.setModifiedOn(endDate);
        hop.setEndDatetime(endDate);
        hop.setIteration(1);
        hop.setTotalDuration(0f);
        hop.setCloseByBu(Boolean.FALSE);
        Optional.ofNullable(userData.getCreateTeam()).ifPresent(hop::setTeamInfo);
        Optional.ofNullable(userData.getCreateUser()).ifPresent(user -> {
            hop.setOwnerCase(user.getUserId(), user.getFullNameTH());
            hop.setCreatedById(user.getUserId());
            hop.setModifiedById(user.getUserId());
        });
        hop.setStatusCode(Constant.ACTIVE_STATUS_CODE);

        hopModels.add(hop);
    }

    public void buildHops(
            List<CaseSlaHopModel> hopModels,
            List<StgSlaPerOwnerModel> sfActivities,
            TeamUserTotalDurationData userData,
            List<CaseSlaHopModel> previousHops,
            boolean autoCloseCaseAfterResolved,
            CreateCaseDTO dto
    ) {
        for (int i = 0; i < sfActivities.size(); i++) {
            int hopNumber = i + 2;
            StgSlaPerOwnerModel sfActivity = sfActivities.get(i);
            CaseSlaHopModel previousHop = previousHops.get(i);
            boolean previousHopHasOwner = previousHop.getOwnerId() != null;
            boolean hasOwner = StringUtils.isNoneBlank(sfActivity.getEmployeeIdC());
            boolean isAlreadyEndDate = StringUtils.isBlank(sfActivity.getEndDateTimeC());
            boolean isLastHop = hopNumber - 2 == sfActivities.size() - 1;
            boolean isCompleted = Constant.CASE_STATUS_COMPLETED.equals(dto.getCaseStatusCode());

            CaseSlaHopModel hop = new CaseSlaHopModel();
            hop.setHopNumber(hopNumber);
            hop.setIteration(1);
            hop.setCloseByBu(Boolean.FALSE);

            ZonedDateTime start = parseToZoneDateTime(sfActivity.getStartDateTimeC());
            hop.setStartDatetime(start);
            hop.setCreatedOn(start);
            hop.setModifiedOn(start);

            if (StringUtils.isNoneBlank(sfActivity.getOwnerTeamNew())) {
                hop.setTeamInfo(teamService.retrieveTeamByName(sfActivity.getOwnerTeamNew()));
            }

            UUID createdById = Optional.ofNullable(previousHop.getOwnerId())
                    .orElse(userData.getNaUser().getUserId());

            Optional.ofNullable(createdById).ifPresent(id -> {
                        hop.setCreatedById(id);
                        hop.setModifiedById(id);
                    }
            );

            if (StringUtils.isNoneBlank(sfActivity.getEmployeeIdC())) {
                Optional.ofNullable(sfActivity.getEmployeeIdC())
                        .filter(empId -> Constant.SF_EX_API_ID.equalsIgnoreCase(empId) || Constant.ADMIN_CRM.equalsIgnoreCase(empId))
                        .ifPresentOrElse(
                                id -> {
                                    EmployeeUserModel sys = userData.getSystemUser();
                                    hop.setOwnerCase(sys.getUserId(), sys.getFullNameTH());
                                    hop.setModifiedById(sys.getUserId());
                                },
                                () -> Optional.ofNullable(userService.retrieveUserByEmployeeIdIncludeInactive(sfActivity.getEmployeeIdC()))
                                        .map(u -> Utils.prepareUser(u, sfActivity.getName()))
                                        .ifPresent(u -> {
                                            hop.setOwnerCase(u.getUserId(), u.getFullNameTH());
                                            hop.setModifiedById(u.getUserId());
                                        })
                        );
            } else if ((previousHopHasOwner && !hasOwner && !isLastHop) || (isLastHop && !hasOwner && isCompleted)) {
                hop.setModifiedById(userData.getNaUser().getUserId());
            } else {
                java.util.Objects.requireNonNull(Boolean.TRUE);
            }

            if (!isAlreadyEndDate) {
                ZonedDateTime end = parseToZoneDateTime(sfActivity.getEndDateTimeC());
                hop.setEndDatetime(end);
                hop.setModifiedOn(end);
            }

            if ((isLastHop && isAlreadyEndDate) && !autoCloseCaseAfterResolved) {
                hop.setCloseByBu(Boolean.TRUE);
            }
            hop.setStatusCode(Constant.ACTIVE_STATUS_CODE);

            hopModels.add(hop);
            if (checkCaseType.isResolvedStatus(sfActivity) && autoCloseCaseAfterResolved && hasOwner) break;
        }
    }

    public void prepareActivitiesForCompleted(
            CreateCaseDTO dto,
            List<CaseSlaHopModel> hopModels,
            List<CaseSlaActivity> activities,
            List<StgSlaPerOwnerModel> sfActivities,
            TeamUserTotalDurationData teamUserTotalDurationData,
            ZonedDateTime slaStartDate
    ) {
        List<CaseSlaHopModel> sortedHops = sortHopsByNumber(hopModels);
        processActivitiesCompleted(sortedHops, dto, activities, sfActivities, teamUserTotalDurationData, slaStartDate);
    }

    public List<CaseSlaActivity> prepareActivityForInProgress(
            MetaData metaData,
            CreateCaseDTO dto,
            CaseTransactionModel caseModel
    ) {
        List<CaseSlaHopModel> hopModelList = metaData.slaHops();
        List<CaseSlaActivity> activityList = new ArrayList<>();

        boolean isFixType = caseModel.getServiceTypeMatrixType() == ServiceTypeMatrixTypeEnum.FIX;
        EmployeeUserModel naUser = metaData.naUser();
        for (CaseSlaHopModel hop : hopModelList) {
            int hopNumber = hop.getHopNumber();
            int iteration = hop.getIteration();
            boolean isFirstHop = Integer.valueOf(1).equals(hopNumber);
            boolean isLastHop = hopNumber == hopModelList.size();

            Float actualDuration = getActualDuration(hop, isFixType);
            TeamReadonlyModel currentTeam = getCurrentTeam(hop);

            if (isFirstHop) {
                EmployeeUserModel createdUser = metaData.createUser();
                activityList.add(createSlaActivity(
                        CaseSlaActivityAction.CREATE,
                        hopNumber,
                        iteration,
                        createdUser,
                        currentTeam,
                        dto,
                        actualDuration,
                        hop.getStartDatetime(),
                        hop.getEndDatetime(),
                        createdUser
                ));

                activityList.add(createSlaActivity(
                        CaseSlaActivityAction.NEXT_HOP,
                        hopNumber,
                        iteration,
                        createdUser,
                        currentTeam,
                        dto,
                        null,
                        hop.getStartDatetime(),
                        hop.getEndDatetime(),
                        createdUser
                ));
            } else {
                CaseSlaHopModel previousHop = hopModelList.get(hopNumber - 2);
                EmployeeUserModel previousUserOrNA = Optional.ofNullable(previousHop)
                        .map(CaseSlaHopModel::getOwnerId)
                        .flatMap(userService::fetchUserByUserId)
                        .orElse(naUser);
                if (hop.getTeamId() != null) {

                    CaseSlaActivity assignTeamActivity = createSlaActivity(
                            CaseSlaActivityAction.ASSIGN_TEAM,
                            hopNumber,
                            iteration,
                            null,
                            currentTeam,
                            dto,
                            actualDuration,
                            hop.getStartDatetime(),
                            hop.getEndDatetime(),
                            previousUserOrNA
                    );

                    Optional.ofNullable(assignTeamActivity.getEndDate())
                            .ifPresent(endDate -> Optional.ofNullable(hop.getOwnerId())
                                    .ifPresentOrElse(ownerIdHop -> assignTeamActivity.setModifiedById(ownerIdHop).setModifiedByName(hop.getOwnerName()),
                                            () -> assignTeamActivity.setModifiedById(naUser.getUserId()).setModifiedByName(naUser.getFullNameTH())));
                    activityList.add(assignTeamActivity);
                }

                EmployeeUserModel currentUserOrNaUser = Optional.of(hop)
                        .map(CaseSlaHopModel::getOwnerId)
                        .map(this::getCurrentUser)
                        .orElse(naUser);

                if (hop.getOwnerId() != null && hop.getTeamId() != null) {
                    activityList.add(createSlaActivity(
                            CaseSlaActivityAction.ASSIGN_OWNER,
                            hopNumber,
                            iteration,
                            currentUserOrNaUser,
                            currentTeam,
                            dto,
                            actualDuration,
                            hop.getStartDatetime(),
                            hop.getEndDatetime(),
                            currentUserOrNaUser
                    ));
                } else if (hop.getOwnerId() != null && hop.getTeamId() == null) {
                    activityList.add(createSlaActivity(
                            CaseSlaActivityAction.ASSIGN_OWNER,
                            hopNumber,
                            iteration,
                            currentUserOrNaUser,
                            currentTeam,
                            dto,
                            actualDuration,
                            hop.getStartDatetime(),
                            hop.getEndDatetime(),
                            previousUserOrNA
                    ));
                } else {
                    java.util.Objects.requireNonNull(Boolean.TRUE);
                }

                if (isLastHop || hop.getEndDatetime() == null) {
                    break;
                }

                if (hopNumber == hopModelList.size()) {
                    continue;
                }

                CaseSlaHopModel nextHop = hopModelList.get(hopNumber);

                boolean isCreateNextHop = shouldCreateNextHop(
                        hopNumber - 1,
                        nextHop,
                        hop,
                        isFixType,
                        hopModelList
                );

                if (isCreateNextHop) {
                    activityList.add(createSlaActivity(
                            CaseSlaActivityAction.NEXT_HOP,
                            hopNumber,
                            iteration,
                            currentUserOrNaUser,
                            currentTeam,
                            dto,
                            null,
                            hop.getStartDatetime(),
                            hop.getEndDatetime(),
                            currentUserOrNaUser
                    ));
                }
            }
        }
        return activityList;
    }

    private Float getActualDuration(CaseSlaHopModel hop, boolean isFixType) {
        if (isFixType) {
            return hop.getTotalDuration();
        }

        return getActualDurationDynamicType(hop);
    }

    private Float getActualDurationDynamicType(CaseSlaHopModel hop) {
        if (hop.getHopNumber().equals(1)) {
            return 0f;
        }

        if (hop.getEndDatetime() == null) {
            return null;
        }

        return calActualDuration(hop.getStartDatetime(), hop.getEndDatetime());
    }

    private Float calActualDuration(
            ZonedDateTime startDatetime,
            ZonedDateTime endDatetime
    ) {
        return slaService.calculateSpendingSla(startDatetime, endDatetime);
    }

    private TeamReadonlyModel getCurrentTeam(CaseSlaHopModel hop) {
        TeamReadonlyModel team = new TeamReadonlyModel();

        team.setTeamId(hop.getTeamId());
        team.setNameTh(hop.getTeamName());

        return team;
    }

    private EmployeeUserModel getCurrentUser(UUID ownerId) {
        return userService.fetchUserByUserId(ownerId)
                .orElseThrow(() -> new NotFoundException("User id" + ownerId + " already not exist"));
    }

    private boolean isSameTeam(TeamReadonlyModel team1, TeamReadonlyModel team2) {
        if (team1 == null || team2 == null) {
            return false;
        }
        return Objects.equals(team1.getTeamId(), team2.getTeamId()) ||
                Objects.equals(team1.getNameTh(), team2.getNameTh());
    }

    private boolean shouldCreateNextHop(
            int currentIndex,
            CaseSlaHopModel nextHop,
            CaseSlaHopModel currentHop,
            boolean isFixType,
            List<CaseSlaHopModel> allHops
    ) {
        // ไม่สร้าง NEXT_HOP จาก hop สุดท้าย
        if (currentIndex == allHops.size() - 1) {
            return false;
        }

        if (isFixType) {
            // FIX type: create NEXT_HOP if the previous hop is completed
            return currentHop.getEndDatetime() != null && nextHop.getStartDatetime() != null;
        } else {
            // DYNAMIC type: create NEXT_HOP on team change (except first transition)
            return !isSameTeam(
                    getCurrentTeam(nextHop),
                    getCurrentTeam(currentHop)
            );
        }
    }


    private List<CaseSlaHopModel> sortHopsByNumber(List<CaseSlaHopModel> hopModels) {
        return hopModels.stream()
                .sorted(Comparator.comparingInt(CaseSlaHopModel::getHopNumber))
                .toList();
    }

    private void processActivitiesCompleted(
            List<CaseSlaHopModel> sortedHops,
            CreateCaseDTO dto,
            List<CaseSlaActivity> activities,
            List<StgSlaPerOwnerModel> sfActivities,
            TeamUserTotalDurationData teamUserTotalDurationData,
            ZonedDateTime slaStartDate
    ) {
        for (int i = 0; i < sortedHops.size(); i++) {
            CaseSlaHopModel currentHop = sortedHops.get(i);
            CaseSlaHopModel previousHop = i > 0 ? sortedHops.get(i - 1) : null;
            StgSlaPerOwnerModel currentSfActivity = safeGet(sfActivities, i - 1);

            boolean isFirstHop = i == 0;
            boolean isLastHop = i == sortedHops.size() - 1;
            boolean hasOwner = currentHop.getOwnerId() != null;
            boolean hasTeam = currentHop.getTeamId() != null;
            boolean isResolvedActivity = currentSfActivity != null && Constant.CASE_STATUS_RESOLVED.equalsIgnoreCase(currentSfActivity.getCaseStatusC());
//            boolean isCompleted = Constant.CASE_STATUS_COMPLETED.equals(dto.getCaseStatusCode());
            boolean isAutoCloseCaseAfterResolved = isAutoclosedAfterResolved(dto);

            EmployeeUserModel currentUser = currentHop.getOwnerUser();
            EmployeeUserModel currentUserModified = currentHop.getUserIfNullGetNaUser(teamUserTotalDurationData.getNaUser());
            TeamReadonlyModel currentTeam = currentHop.getCurrentTeam();
            Float actualDuration = getActualDuration(currentHop);

            EmployeeUserModel previousUserOwner = getPreviousUserOwner(previousHop);
            EmployeeUserModel previousUserModified = getPreviousUserModified(previousHop, teamUserTotalDurationData);

            if (isFirstHop) {
                activities.add(buildSlaActivity(CaseSlaActivityAction.CREATE, currentHop, currentUser, currentTeam, dto, actualDuration, currentUser, currentHop.getStartDatetime(), currentHop.getStartDatetime()));
            } else {
                activities.add(buildSlaActivity(CaseSlaActivityAction.NEXT_HOP, previousHop, previousUserOwner, previousHop.getCurrentTeam(), dto, null, previousUserModified, currentHop.getStartDatetime(), currentHop.getStartDatetime()));

                if (hasTeam) {
                    activities.add(buildSlaActivityAssignTeam(CaseSlaActivityAction.ASSIGN_TEAM, currentHop, null, currentTeam, dto, actualDuration, previousUserModified, currentHop.getStartDatetime(), currentHop.getEndDatetime(), currentUserModified));
                }

                if (hasOwner) {
                    activities.add(buildSlaActivity(CaseSlaActivityAction.ASSIGN_OWNER, currentHop, currentUser, currentTeam, dto, actualDuration, firstNonNull(currentUser, previousUserModified), currentHop.getStartDatetime(), currentHop.getEndDatetime()));
                }

                if (isResolvedActivity) {
                    activities.add(buildSlaActivity(CaseSlaActivityAction.RESOLVED, currentHop, currentUser, currentTeam, dto, null, currentUserModified, currentHop.getEndDatetime(), currentHop.getEndDatetime()));
                    teamUserTotalDurationData.setResolveTeam(currentTeam);
                    teamUserTotalDurationData.setResolveUser(currentUser);
                    dto.setResolvedDate(currentHop.getEndDatetime());
                    if (isAutoCloseCaseAfterResolved && hasOwner) {
                        activities.add(buildSlaActivity(CaseSlaActivityAction.AUTO_COMPLETED, currentHop, currentUser, currentTeam, dto, null, currentUserModified, currentHop.getEndDatetime(), currentHop.getEndDatetime()));
                        teamUserTotalDurationData.setClosedTeam(currentTeam);
                        teamUserTotalDurationData.setClosedUser(currentUser);
                        dto.setClosedDate(currentHop.getEndDatetime());
                        getTotalDuration(teamUserTotalDurationData, slaStartDate, currentHop.getEndDatetime());
                        break;
                    } else if (isLastHop && hasOwner) {
                        activities.add(buildSlaActivity(CaseSlaActivityAction.ASSIGN_OWNER, currentHop, currentUser, currentTeam, dto, actualDuration, firstNonNull(currentUser, previousUserModified), currentHop.getEndDatetime(), currentHop.getEndDatetime()));
                    } else {
                        java.util.Objects.requireNonNull(Boolean.TRUE);
                    }
                }
            }

            if (isLastHop && (!isAutoCloseCaseAfterResolved || !isResolvedActivity)) {
                activities.add(buildSlaActivity(CaseSlaActivityAction.COMPLETED, currentHop, currentUser, currentTeam, dto, getTotalDuration(teamUserTotalDurationData, slaStartDate, currentHop.getEndDatetime()), currentUserModified, currentHop.getEndDatetime(), currentHop.getEndDatetime()));
                teamUserTotalDurationData.setClosedTeam(currentTeam);
                teamUserTotalDurationData.setClosedUser(currentUser);
                dto.setClosedDate(currentHop.getEndDatetime());
                sortedHops.getLast().setCloseByBu(Boolean.TRUE);
                break;
            }
        }
    }

    public void prepareActivitiesForResolved(
            CreateCaseDTO dto,
            List<CaseSlaHopModel> hopModels,
            List<CaseSlaActivity> activities,
            List<StgSlaPerOwnerModel> sfActivities,
            TeamUserTotalDurationData teamUserTotalDurationData,
            ZonedDateTime slaStartDate
    ) {
        List<CaseSlaHopModel> sortedHops = sortHopsByNumber(hopModels);
        processActivitiesResolved(sortedHops, dto, activities, sfActivities, teamUserTotalDurationData, slaStartDate);
    }

    private void processActivitiesResolved(
            List<CaseSlaHopModel> sortedHops,
            CreateCaseDTO dto,
            List<CaseSlaActivity> activities,
            List<StgSlaPerOwnerModel> sfActivities,
            TeamUserTotalDurationData teamUserTotalDurationData,
            ZonedDateTime slaStartDate
    ) {
        for (int i = 0; i < sortedHops.size(); i++) {
            CaseSlaHopModel currentHop = sortedHops.get(i);
            CaseSlaHopModel previousHop = i > 0 ? sortedHops.get(i - 1) : null;
            StgSlaPerOwnerModel currentSfActivity = safeGet(sfActivities, i - 1);

            boolean isFirstHop = i == 0;
            boolean isLastHop = i == sortedHops.size() - 1;
            boolean hasOwner = currentHop.getOwnerId() != null;
            boolean hasTeam = currentHop.getTeamId() != null;
            boolean isResolvedActivity = currentSfActivity != null && Constant.CASE_STATUS_RESOLVED.equalsIgnoreCase(currentSfActivity.getCaseStatusC());
            boolean isCompleted = Constant.CASE_STATUS_COMPLETED.equals(dto.getCaseStatusCode());
            boolean isAutoCloseCaseAfterResolved = isAutoclosedAfterResolved(dto);

            EmployeeUserModel currentUser = currentHop.getOwnerUser();
            EmployeeUserModel currentUserModified = currentHop.getUserIfNullGetNaUser(teamUserTotalDurationData.getNaUser());
            TeamReadonlyModel currentTeam = currentHop.getCurrentTeam();
            Float actualDuration = getActualDuration(currentHop);

            EmployeeUserModel previousUserOwner = getPreviousUserOwner(previousHop);
            EmployeeUserModel previousUserModified = getPreviousUserModified(previousHop, teamUserTotalDurationData);

            if (isFirstHop) {
                activities.add(buildSlaActivity(CaseSlaActivityAction.CREATE, currentHop, currentUser, currentTeam, dto, actualDuration, currentUser, currentHop.getStartDatetime(), currentHop.getStartDatetime()));
            } else {
                activities.add(buildSlaActivity(CaseSlaActivityAction.NEXT_HOP, previousHop, previousUserOwner, previousHop.getCurrentTeam(), dto, null, previousUserModified, currentHop.getStartDatetime(), currentHop.getStartDatetime()));

                if (hasTeam) {
                    activities.add(buildSlaActivityAssignTeam(CaseSlaActivityAction.ASSIGN_TEAM, currentHop, null, currentTeam, dto, actualDuration, previousUserModified, currentHop.getStartDatetime(), currentHop.getEndDatetime(), currentUserModified));
                }

                if (hasOwner) {
                    activities.add(buildSlaActivity(CaseSlaActivityAction.ASSIGN_OWNER, currentHop, currentUser, currentTeam, dto, actualDuration, firstNonNull(currentUser, previousUserModified), currentHop.getStartDatetime(), currentHop.getEndDatetime()));
                }

                if ((isResolvedActivity && !isLastHop) || (isResolvedActivity && isLastHop && isAutoCloseCaseAfterResolved)) {
                    activities.add(buildSlaActivity(CaseSlaActivityAction.RESOLVED, currentHop, currentUser, currentTeam, dto, null, currentUserModified, currentHop.getEndDatetime(), currentHop.getEndDatetime()));
                    teamUserTotalDurationData.setResolveTeam(currentTeam);
                    teamUserTotalDurationData.setResolveUser(currentUser);
                    dto.setResolvedDate(currentHop.getEndDatetime());
                    if (isAutoCloseCaseAfterResolved && hasOwner) {
                        activities.add(buildSlaActivity(CaseSlaActivityAction.AUTO_COMPLETED, currentHop, currentUser, currentTeam, dto, null, currentUserModified, currentHop.getEndDatetime(), currentHop.getEndDatetime()));
                        teamUserTotalDurationData.setClosedTeam(currentTeam);
                        teamUserTotalDurationData.setClosedUser(currentUser);
                        dto.setClosedDate(currentHop.getEndDatetime());
                        getTotalDuration(teamUserTotalDurationData, slaStartDate, currentHop.getEndDatetime());
                        break;
                    }
                }
            }

            if (isLastHop && !isAutoCloseCaseAfterResolved && isCompleted) {
                activities.add(buildSlaActivity(CaseSlaActivityAction.COMPLETED, currentHop, currentUser, currentTeam, dto, getTotalDuration(teamUserTotalDurationData, slaStartDate, currentHop.getEndDatetime()), currentUser, currentHop.getEndDatetime(), currentHop.getEndDatetime()));
                teamUserTotalDurationData.setClosedTeam(currentTeam);
                teamUserTotalDurationData.setClosedUser(currentUser);
                dto.setClosedDate(currentHop.getEndDatetime());
                sortedHops.getLast().setCloseByBu(Boolean.TRUE);
                break;
            }
        }
    }

    private StgSlaPerOwnerModel safeGet(List<StgSlaPerOwnerModel> list, int index) {
        return index < list.size() && index >= 0 ? list.get(index) : null;
    }

    private EmployeeUserModel getPreviousUserOwner(CaseSlaHopModel previousHop) {
        return previousHop != null ? previousHop.getOwnerUser() : null;
    }

    private EmployeeUserModel getPreviousUserModified(CaseSlaHopModel previousHop, TeamUserTotalDurationData teamUserTotalDurationData) {
        return previousHop != null ? previousHop.getUserIfNullGetNaUser(teamUserTotalDurationData.getNaUser()) : null;
    }

    private EmployeeUserModel firstNonNull(EmployeeUserModel... users) {
        for (EmployeeUserModel user : users) {
            if (user != null) return user;
        }
        return null;
    }

    private boolean isAutoclosedAfterResolved(CreateCaseDTO dto) {
        return Boolean.TRUE.equals(dto.getAutoCloseCaseAfterResolved());
    }

    private Float getActualDuration(CaseSlaHopModel currentHop) {
        if (currentHop.getStartDatetime() == null || currentHop.getEndDatetime() == null) return null;
        return slaService.calculateSpendingSla(
                slaService.calculateSlaStartDate(currentHop.getStartDatetime()),
                currentHop.getEndDatetime()
        );
    }

    private Float getTotalDuration(TeamUserTotalDurationData data, ZonedDateTime slaStartDate, ZonedDateTime endDate) {
        return Optional.ofNullable(slaStartDate)
                .flatMap(start -> Optional.ofNullable(endDate)
                        .map(end -> slaService.calculateSpendingSla(start, end)))
                .map(duration -> {
                    data.setTotalDuration(duration);
                    return duration;
                })
                .orElse(null);
    }

    public CaseSlaActivity buildSlaActivityAssignTeam(
            CaseSlaActivityAction action,
            CaseSlaHopModel slaHop,
            EmployeeUserModel ownerUser,
            TeamReadonlyModel ownerTeam,
            CreateCaseDTO dto,
            Float actualDuration,
            EmployeeUserModel actionUser,
            ZonedDateTime startDate,
            ZonedDateTime endDate,
            EmployeeUserModel modifyUser
    ) {
        return Optional.ofNullable(createSlaActivity(
                        action,
                        slaHop.getHopNumber(),
                        slaHop.getIteration(),
                        ownerUser,
                        ownerTeam,
                        dto,
                        actualDuration,
                        startDate,
                        endDate,
                        actionUser)
                ).map(setModifiedData(modifyUser, endDate))
                .orElseThrow(() -> new IllegalStateException("Unable to create CaseSlaActivity"));
    }

    public Function<CaseSlaActivity, CaseSlaActivity> setModifiedData(EmployeeUserModel modifyUser, ZonedDateTime endDate) {
        return activity -> {
            Optional.of(endDate != null)
                    .filter(Boolean::booleanValue)
                    .ifPresent(u -> {
                        activity.setModifiedById(modifyUser.getUserId());
                        activity.setModifiedByName(modifyUser.getFullNameTH());
                    });
            return activity;
        };
    }

    public CaseSlaActivity buildSlaActivity(
            CaseSlaActivityAction action,
            CaseSlaHopModel slaHop,
            EmployeeUserModel ownerUser,
            TeamReadonlyModel ownerTeam,
            CreateCaseDTO dto,
            Float actualDuration,
            EmployeeUserModel actionUser,
            ZonedDateTime startDate,
            ZonedDateTime endDate
    ) {
        return createSlaActivity(
                action,
                slaHop.getHopNumber(),
                slaHop.getIteration(),
                ownerUser,
                ownerTeam,
                dto,
                actualDuration,
                startDate,
                endDate,
                actionUser
        );
    }

    public CaseSlaActivity createSlaActivity(
            CaseSlaActivityAction action,
            int hopNumber,
            int iteration,
            EmployeeUserModel actionUser,
            TeamReadonlyModel team,
            CreateCaseDTO dto,
            Float actualDuration,
            ZonedDateTime startDate,
            ZonedDateTime endDate,
            EmployeeUserModel createdUser
    ) {
        // Validate inputs
        CaseSlaActivity activity = new CaseSlaActivity();
        activity.setAction(action)
                .setHopNumberRef(hopNumber)
                .setServiceTypeMatrixIteration(iteration)
                .setServiceTypeMatrixCode(dto.getServiceTypeMatrixCode())
                .setEndDate(endDate);
        Optional.ofNullable(startDate)
                .ifPresentOrElse(
                        startDatetime ->
                                activity.setStartDate(startDatetime).setCreatedOn(startDatetime),
                        () -> {
                            ZonedDateTime now = ZonedDateTime.now();
                            activity.setStartDate(now).setCreatedOn(now);
                        }
                );
        setModifiedDate(activity);
        Optional.ofNullable(actualDuration)
                .ifPresent(activity::setActualDuration);
        setSlaActivityCreatedOwner(activity, createdUser);
        setSlaActivityOwnerInfo(activity, actionUser);
        setSlaActivityTeamInfo(activity, team);
        setSlaActivityResolveInfo(dto, action, activity);
//        Optional.ofNullable(dto.getCaseId()).ifPresent(activity::setCaseId);
        activity.setStatusCode(Constant.ACTIVE_STATUS_CODE);
        return activity;
    }

    private void setModifiedDate(CaseSlaActivity activity) {
        Optional.ofNullable(activity.getEndDate())
                .ifPresentOrElse(
                        activity::setModifiedOn,
                        () -> activity.setModifiedOn(activity.getStartDate())
                );
    }

    public void setSlaActivityResolveInfo(CreateCaseDTO dto, CaseSlaActivityAction action, CaseSlaActivity activity) {
        Optional.ofNullable(action)
                .filter(act -> act == CaseSlaActivityAction.RESOLVED)
                .ifPresent(a -> {
                    activity.setResolutionListCode(dto.getResolutionListCode());
                    activity.setResolutionListValue(Utils.truncate(
                            dto.getResolutionListValue(),
                            Utils.getMaxLength(CaseSlaActivity.class, "resolutionListValue")
                    ));
                    activity.setResolutionListComment(Utils.truncate(
                            dto.getResolutionListComment(),
                            Utils.getMaxLength(CaseSlaActivity.class, "resolutionListComment")
                    ));
                    activity.setRootCauseListCode(dto.getRootCauseListCode());
                    activity.setRootCauseListValue(Utils.truncate(
                            dto.getRootCauseListValue(),
                            Utils.getMaxLength(CaseSlaActivity.class, "rootCauseListValue")
                    ));
                    activity.setRootCauseListComment(Utils.truncate(
                            dto.getRootCauseListComment(),
                            Utils.getMaxLength(CaseSlaActivity.class, "rootCauseListComment")
                    ));
                });
    }

    public void setSlaActivityOwnerInfo(CaseSlaActivity activity, EmployeeUserModel user) {
        Optional.ofNullable(user)
                .ifPresent(u -> activity.setOwnerId(u.getUserId())
                        .setOwnerName(u.getFullNameTH())
                        .setModifiedById(u.getUserId())
                        .setModifiedByName(u.getFullNameTH()));
    }

    public void setSlaActivityCreatedOwner(CaseSlaActivity activity, EmployeeUserModel user) {
        Optional.ofNullable(user)
                .ifPresent(u -> activity.setCreatedById(u.getUserId())
                        .setCreatedByName(u.getFullNameTH())
                        .setModifiedById(u.getUserId())
                        .setModifiedByName(u.getFullNameTH()));
    }

    public void setSlaActivityTeamInfo(CaseSlaActivity activity, TeamReadonlyModel team) {
        Optional.ofNullable(team)
                .ifPresent(t -> activity.setTeamId(t.getTeamId())
                        .setTeamNameTh(t.getNameTh())
                        .setTeamNameEn(t.getNameEn()));
    }

    public List<CaseSlaActivity> updateDateTimeActivity(List<CaseSlaActivity> activities) {
        AtomicInteger count = new AtomicInteger(0);

        return activities.stream()
                .map(activity -> {
                    Optional.ofNullable(activity.getStartDate())
                            .map(startDate -> startDate.plusNanos(count.get() * 1_000_000L))
                            .ifPresent(activity::setStartDate);
                    Optional.ofNullable(activity.getEndDate())
                            .map(endDate -> endDate.plusNanos(count.get() * 1_000_000L))
                            .ifPresent(activity::setEndDate);
                    activity.setCreatedOn(activity.getCreatedOn().plusNanos(count.get() * 1_000_000L));
                    activity.setModifiedOn(activity.getModifiedOn().plusNanos(count.getAndIncrement() * 1_000_000L));
                    return activity;
                })
                .collect(Collectors.toList());
    }
}
