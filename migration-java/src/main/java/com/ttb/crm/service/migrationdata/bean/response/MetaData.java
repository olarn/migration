package com.ttb.crm.service.migrationdata.bean.response;

import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaActivity;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaHopModel;
import com.ttb.crm.service.migrationdata.model.userManagement.EmployeeUserModel;
import com.ttb.crm.service.migrationdata.model.userManagement.TeamReadonlyModel;

import java.util.List;

public record MetaData(
        TeamReadonlyModel createTeam,
        TeamReadonlyModel resolveTeam,
        TeamReadonlyModel closedTeam,
        EmployeeUserModel createUser,
        EmployeeUserModel resolveUser,
        EmployeeUserModel closedUser,
        EmployeeUserModel naUser,

        Float totalDuration,

        List<CaseSlaHopModel> slaHops,
        List<CaseSlaActivity> activities
) { }