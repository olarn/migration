package com.ttb.crm.service.migrationdata.bean.response;

import com.ttb.crm.service.migrationdata.bean.CreateCaseDTO;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaActivity;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaHopModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixModel;
import com.ttb.crm.service.migrationdata.model.userManagement.EmployeeUserModel;
import com.ttb.crm.service.migrationdata.model.userManagement.TeamReadonlyModel;

import java.time.ZonedDateTime;
import java.util.List;

public record CreateCaseData(
        CreateCaseDTO request,
        ServiceTypeMatrixModel stm,
        TeamReadonlyModel createTeam,
        TeamReadonlyModel resolveTeam,
        TeamReadonlyModel closedTeam,
        EmployeeUserModel createUser,
        EmployeeUserModel resolveUser,
        EmployeeUserModel closedUser,
        Float toTolDuration,
        List<CaseSlaHopModel> slaHops,
        List<CaseSlaActivity> activities,
        ZonedDateTime slaStartDate
) { }