package com.ttb.crm.service.migrationdata.bean;

import com.ttb.crm.service.migrationdata.bean.response.CaseCaptureEventDTO;
import com.ttb.crm.service.migrationdata.model.batch.TempUpdateStgInProgress;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseTransactionModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StgToCaseWriterDTO {
    CaseTransactionModel caseTransaction;
    CaseCaptureEventDTO dataForCaseMovement;
    TempUpdateStgInProgress tempUpdateStgInProgress;
}
