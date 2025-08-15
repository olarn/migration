package com.ttb.crm.service.migrationdata.bean;

import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaActivity;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseTransactionModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCaseTransactionResponse {
    private CaseTransactionModel caseTransaction;
    private List<CaseSlaActivity> caseSlaActivities;
}
