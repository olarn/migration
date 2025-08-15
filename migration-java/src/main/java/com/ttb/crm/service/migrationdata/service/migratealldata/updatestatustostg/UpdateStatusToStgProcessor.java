package com.ttb.crm.service.migrationdata.service.migratealldata.updatestatustostg;

import com.ttb.crm.service.migrationdata.model.batch.TempUpdateStgInProgress;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressUpdateStatusModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@StepScope
@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateStatusToStgProcessor implements ItemProcessor<TempUpdateStgInProgress, StgCaseInProgressUpdateStatusModel> {

    @Override
    public StgCaseInProgressUpdateStatusModel process(TempUpdateStgInProgress tempStgCaseInProgress) throws Exception {

        var stgCaseInProgressUpdateStatus = new StgCaseInProgressUpdateStatusModel();
        stgCaseInProgressUpdateStatus.setId(tempStgCaseInProgress.getId());
        stgCaseInProgressUpdateStatus.setLoadStatus(tempStgCaseInProgress.getLoadStatus());
        stgCaseInProgressUpdateStatus.setLoadRemark(tempStgCaseInProgress.getLoadRemark());
        stgCaseInProgressUpdateStatus.setLoadLastDatetime(tempStgCaseInProgress.getLoadLastDatetime());

        return stgCaseInProgressUpdateStatus;
    }
}
