package com.ttb.crm.service.migrationdata.service.inprogressmigrationdata.totempstep;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttb.crm.service.migrationdata.bean.request.BatchMigrationDataRequest;
import com.ttb.crm.service.migrationdata.enums.BatchRecordStatusEnum;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@StepScope
@Component
@Slf4j
public class ToTempInProgressProcessor implements ItemProcessor<StgCaseInProgressModel, TempStgCaseInProgressLogModel> {

    private final BatchMigrationDataRequest batchRequest;
    @Value("#{stepExecution}")
    private StepExecution stepExecution;

    public ToTempInProgressProcessor(@Value("#{jobParameters['batchRequest']}") String batchRequestJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.batchRequest = mapper.readValue(batchRequestJson, BatchMigrationDataRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid job parameter", e);
        }
    }

    @Override
    public TempStgCaseInProgressLogModel process(StgCaseInProgressModel item) {
        try {
            Long jobExecutionId = stepExecution.getJobExecution().getId();
            return new TempStgCaseInProgressLogModel(
                    item,
                    batchRequest,
                    jobExecutionId,
                    BatchRecordStatusEnum.PENDING
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
