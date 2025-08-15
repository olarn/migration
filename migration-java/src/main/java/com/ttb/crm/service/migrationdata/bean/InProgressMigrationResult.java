package com.ttb.crm.service.migrationdata.bean;

import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixSla;
import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;

import java.util.List;


public record InProgressMigrationResult(
        boolean isCanMigrate,
        List<StgSlaPerOwnerModel> matchedActivities,
        List<ServiceTypeMatrixSla> matchedSlas
) {
}
