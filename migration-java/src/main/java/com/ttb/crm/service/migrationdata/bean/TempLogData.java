package com.ttb.crm.service.migrationdata.bean;

import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseDocumentReferenceLogModel;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.model.batch.TempStgSlaPerOwnerLogModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TempLogData {
    private TempStgCaseInProgressLogModel tempStgCaseInProgressLogModel;
    private List<TempStgSlaPerOwnerLogModel> tempStgSlaPerOwnerLogModel;
    private List<TempStgCaseDocumentReferenceLogModel> tempStgCaseDocumentReferenceLogModel;
}
