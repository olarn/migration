package com.ttb.crm.service.migrationdata.service.preparecaesdataservice;

import com.ttb.crm.service.migrationdata.bean.CreateCaseDTO;
import com.ttb.crm.service.migrationdata.helper.Constant;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CheckCaseType {
    public boolean isResolvedStatus(StgSlaPerOwnerModel sf) {
        return Constant.CASE_STATUS_RESOLVED.equalsIgnoreCase(sf.getCaseStatusC());
    }

    public boolean isResolvedStatus(CreateCaseDTO dto) {
        return Constant.CASE_STATUS_RESOLVED.equalsIgnoreCase(dto.getCaseStatusCode());
    }

    public boolean isCompletedStatus(CreateCaseDTO dto) {
        return Constant.CASE_STATUS_COMPLETED.equalsIgnoreCase(dto.getCaseStatusCode());
    }

    public boolean isInProgressOrNew(CreateCaseDTO dto) {
        return Set.of(Constant.CASE_STATUS_IN_PROGRESS, Constant.CASE_STATUS_NEW).contains(dto.getCaseStatusCode());
    }

    public boolean isCreateOneAppAndPayrollOrPWACase(String stmCodeOld) {
        return Constant.ONE_APP_STM_MY_ADVISOR_FROM_SALE_FORCE.contains(stmCodeOld);
    }

    public boolean isAutoclosedAfterResolved(ServiceTypeMatrixModel stm) {
        return Boolean.TRUE.equals(stm.getAutoCloseCaseAfterResolved());
    }

    public boolean isAutoclosedAfterResolved(CreateCaseDTO dto) {
        return Boolean.TRUE.equals(dto.getAutoCloseCaseAfterResolved());
    }

    public boolean isOneApp(String integrationSystem) {
        return Constant.ONE_APP.equals(integrationSystem);
    }

    public boolean isTtbWeb(String integrationSystem) {
        return Constant.TTB_WEB.equals(integrationSystem);
    }

    public boolean isFcr(Boolean isFcr) {
        return Boolean.TRUE.equals(isFcr);
    }

    public boolean isOneAppOrTtbWeb(String integrationSystem) {
        return isOneApp(integrationSystem) || isTtbWeb(integrationSystem);
    }

    public boolean isTtbWebOrCreateOneAppAndPayrollOrPWACase(String integrationSystem, String stmCodeOld) {
        return isOneApp(integrationSystem) || isCreateOneAppAndPayrollOrPWACase(stmCodeOld) || isTtbWeb(integrationSystem);
    }

}
