package com.ttb.crm.service.migrationdata.service.preparecaesdataservice;

import com.ttb.crm.service.migrationdata.bean.CreateCaseDTO;
import com.ttb.crm.service.migrationdata.bean.response.TeamUserTotalDurationData;
import com.ttb.crm.service.migrationdata.helper.Constant;
import com.ttb.crm.service.migrationdata.helper.Utils;
import com.ttb.crm.service.migrationdata.model.userManagement.EmployeeUserModel;
import com.ttb.crm.service.migrationdata.model.userManagement.TeamReadonlyModel;
import com.ttb.crm.service.migrationdata.service.TeamService;
import com.ttb.crm.service.migrationdata.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class PrepareTeamUserData {
    private final UserService userService;
    private final TeamService teamService;

    public TeamUserTotalDurationData prepareTeamUserData(CreateCaseDTO dto) {
        return Optional.of(new TeamUserTotalDurationData())
                .map(data -> enrichWithCreateData(dto).apply(data))
                .map(this::enrichWithNAData)
                .map(this::enrichWithSystemData)
                .orElseThrow();
    }

    private Function<TeamUserTotalDurationData, TeamUserTotalDurationData> enrichWithCreateData(CreateCaseDTO dto) {
        return buildFunctionIfSourceFromSfEx(dto)
                .or(() -> buildFunctionIfValid(dto))
                .orElse(Function.identity());
    }

    private Optional<Function<TeamUserTotalDurationData, TeamUserTotalDurationData>> buildFunctionIfSourceFromSfEx(CreateCaseDTO dto) {
        return Optional.ofNullable(dto)
                .filter(this::isSourceFromSfEx)
                .map(d -> buildWithSystemDefaults(dto));
    }

    private Optional<Function<TeamUserTotalDurationData, TeamUserTotalDurationData>> buildFunctionIfValid(CreateCaseDTO dto) {
        return Optional.ofNullable(dto)
                .filter(this::isValidCreateInfo)
                .map(this::buildWithProvidedInfo);
    }

    private boolean isValidCreateInfo(CreateCaseDTO dto) {
        return StringUtils.isNotBlank(dto.getCreateByTeamName()) &&
                StringUtils.isNotBlank(dto.getCreateByEmployeeID());
    }

    private boolean isSourceFromSfEx(CreateCaseDTO dto) {
        return Constant.SF_EX_API_ID.equalsIgnoreCase(dto.getCreateByEmployeeID()) || Constant.ADMIN_CRM.equalsIgnoreCase(dto.getCreateByEmployeeID());
    }

    private Function<TeamUserTotalDurationData, TeamUserTotalDurationData> buildWithProvidedInfo(CreateCaseDTO dto) {
        return data -> {
            TeamReadonlyModel team = teamService.retrieveTeamByName(dto.getCreateByTeamName());
            EmployeeUserModel user = userService.retrieveUserByEmployeeIdIncludeInactive(dto.getCreateByEmployeeID());
            data.setCreateTeam(team);
            data.setCreateUser(Utils.prepareUser(user, dto.getCreateByName()));
            return data;
        };
    }

    private Function<TeamUserTotalDurationData, TeamUserTotalDurationData> buildWithSystemDefaults(CreateCaseDTO dto) {
        return data -> {
            TeamReadonlyModel team = teamService.retrieveTeamByName(Constant.SYSTEM_TEAM);
            EmployeeUserModel user = userService.retrieveUserByEmployeeIdIncludeInactive(Constant.SYSTEM_EMPLOYEE_ID);
            data.setCreateTeam(team);
            data.setCreateUser(Utils.prepareUser(user, user.getFullNameTH()));
            dto.setCreateByEmployeeID(user.getEmployeeId());
            dto.setCreateByName(user.getFullNameTH());
            return data;
        };
    }

    private TeamUserTotalDurationData enrichWithNAData(TeamUserTotalDurationData data) {
        data.setNaUser(userService.retrieveUserByEmployeeIdIncludeInactive(Constant.NA_EMPLOYEE_ID));
        return data;
    }

    private TeamUserTotalDurationData enrichWithSystemData(TeamUserTotalDurationData data) {
        data.setSystemUser(userService.retrieveUserByEmployeeIdIncludeInactive(Constant.SYSTEM_EMPLOYEE_ID));
        return data;
    }
}
