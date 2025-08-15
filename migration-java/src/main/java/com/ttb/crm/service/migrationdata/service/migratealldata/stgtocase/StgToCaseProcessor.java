package com.ttb.crm.service.migrationdata.service.migratealldata.stgtocase;

import com.ttb.crm.service.migrationdata.bean.StgToCaseWriterDTO;
import com.ttb.crm.service.migrationdata.enums.LoadStatus;
import com.ttb.crm.service.migrationdata.model.batch.TempUpdateStgInProgress;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import com.ttb.crm.service.migrationdata.service.TempLogService;
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
public class StgToCaseProcessor implements ItemProcessor<StgCaseInProgressModel, StgToCaseWriterDTO> {
    private final CreateCaseService createCaseService;
    private final TempLogService tempLogService;

    @Override
    public StgToCaseWriterDTO process(StgCaseInProgressModel tempStgCaseInProgress) throws Exception {
        try {
            StgToCaseWriterDTO createCaseData = createCaseService.createCase(tempStgCaseInProgress);
            TempUpdateStgInProgress stgData = tempLogService.tempUpdateStgInProgress(tempStgCaseInProgress.getId(), LoadStatus.SUCCESS, "");
            createCaseData.setTempUpdateStgInProgress(stgData);
            return createCaseData;
        } catch (Exception e) {
            TempUpdateStgInProgress stgFailed = tempLogService.tempUpdateStgInProgress(tempStgCaseInProgress.getId(), LoadStatus.FAILED, e.getMessage());
            return StgToCaseWriterDTO.builder()
                    .caseTransaction(null)
                    .tempUpdateStgInProgress(stgFailed)
                    .build();
        }
    }
}
