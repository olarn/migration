package com.ttb.crm.service.migrationdata.service.migratealldata.updatestatustostg;

import com.ttb.crm.service.migrationdata.config.CustomStepExecutionListener;
import com.ttb.crm.service.migrationdata.model.batch.TempUpdateStgInProgress;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressUpdateStatusModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import static com.ttb.crm.service.migrationdata.helper.Constant.UPDATE_STG_STATUS_CHUNK_SIZE;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class UpdateStatusToStgStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TaskExecutor taskExecutor;

    @Bean
    public Step tempUpdateStatusToStgStep(
            @Qualifier("updateStatusBeanToStgReader") ItemReader<TempUpdateStgInProgress> reader,
            @Qualifier("updateStatusToStgProcessor") UpdateStatusToStgProcessor processor,
            @Qualifier("updateStatusToStgWriter") UpdateStatusToStgWriter writer
    ) {
        return new StepBuilder("tempUpdateStatusToStgStep", jobRepository)
                .<TempUpdateStgInProgress, StgCaseInProgressUpdateStatusModel>chunk(UPDATE_STG_STATUS_CHUNK_SIZE, transactionManager) // Adjust chunk size as needed
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor)
                .listener(new CustomStepExecutionListener())
                .build();
    }
}
