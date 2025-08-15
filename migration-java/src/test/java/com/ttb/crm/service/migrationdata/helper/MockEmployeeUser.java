package com.ttb.crm.service.migrationdata.helper;

import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import com.ttb.crm.service.migrationdata.model.userManagement.EmployeeUserModel;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockEmployeeUser {

    public EmployeeUserModel mockEmployeeUser(String employeeId) {
        EmployeeUserModel userModel = new EmployeeUserModel();
        userModel.setEmployeeId(employeeId);
        userModel.setStatusCode(0);
        userModel.setFirstNameTh(employeeId);
        userModel.setLastNameTh("Last Name Th");
        userModel.setUserId(UUID.randomUUID());
        return userModel;
    }

    public EmployeeUserModel mockEmployeeUser(StgSlaPerOwnerModel tempSlaModel) {
        EmployeeUserModel userModel = new EmployeeUserModel();
        userModel.setEmployeeId(tempSlaModel.getEmployeeIdC());
        userModel.setStatusCode(0);
        userModel.setUserId(UUID.randomUUID());
        Utils.prepareUser(userModel, tempSlaModel.getName());
        return userModel;
    }

    public EmployeeUserModel mockEmployeeUser(String employeeId, String fullNameTh) {
        EmployeeUserModel userModel = new EmployeeUserModel();
        userModel.setEmployeeId(employeeId);
        userModel.setStatusCode(0);
        userModel.setUserId(UUID.randomUUID());
        Utils.prepareUser(userModel, fullNameTh);
        return userModel;
    }
}
