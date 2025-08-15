package com.ttb.crm.service.migrationdata.bean.response;

import com.ttb.crm.service.migrationdata.model.userManagement.EmployeeUserModel;
import com.ttb.crm.service.migrationdata.model.userManagement.TeamReadonlyModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamUserTotalDurationData {
    TeamReadonlyModel createTeam;
    TeamReadonlyModel resolveTeam;
    TeamReadonlyModel closedTeam;

    EmployeeUserModel createUser;
    EmployeeUserModel resolveUser;
    EmployeeUserModel closedUser;

    Float totalDuration;

    EmployeeUserModel naUser;
    EmployeeUserModel systemUser;
}
