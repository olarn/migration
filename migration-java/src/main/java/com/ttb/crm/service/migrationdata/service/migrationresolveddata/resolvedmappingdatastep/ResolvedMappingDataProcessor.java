package com.ttb.crm.service.migrationdata.service.migrationresolveddata.resolvedmappingdatastep;

import com.ttb.crm.service.migrationdata.enums.LoadStatus;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import com.ttb.crm.service.migrationdata.service.StagingService;
import com.ttb.crm.service.migrationdata.service.preparecaesdataservice.CreateCaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@StepScope
@Component
@RequiredArgsConstructor
@Slf4j
public class ResolvedMappingDataProcessor implements ItemProcessor<StgCaseInProgressModel, StgCaseInProgressModel> {
    private final CreateCaseService caseServices;
//    private final TempLogService tempLogService;
    private final StagingService stagingService;

    @Override
    public StgCaseInProgressModel process(StgCaseInProgressModel stgCaseInProgress) throws Exception {
        try {
            caseServices.createCase(stgCaseInProgress);
            return stagingService.setTempStgCaseInProgress(stgCaseInProgress, LoadStatus.SUCCESS, "");
//            return tempLogService.setTempStgCaseInProgress(stgCaseInProgress, BatchRecordStatusEnum.SUCCESS,  LoadStatus.SUCCESS, "");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return stagingService.setTempStgCaseInProgress(stgCaseInProgress, LoadStatus.FAILED, e.getMessage());
//            return tempLogService.setTempStgCaseInProgress(stgCaseInProgress, BatchRecordStatusEnum.FAILED,  LoadStatus.FAILED, e.getMessage());
        }
    }
}
