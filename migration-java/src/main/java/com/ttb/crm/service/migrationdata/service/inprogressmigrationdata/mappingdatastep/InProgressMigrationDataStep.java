package com.ttb.crm.service.migrationdata.service.inprogressmigrationdata.mappingdatastep;

import com.ttb.crm.service.migrationdata.config.CustomStepExecutionListener;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class InProgressMigrationDataStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean(name = "inProgressMigration")
    public Step inProgressMigrationDataStep(
            @Qualifier("inProgressMigrationReader") ItemReader<StgCaseInProgressModel> reader,
            @Qualifier("inProgressMigrationDataProcessor") InProgressMigrationDataProcessor processor,
            @Qualifier("inProgressMigrationDataWriter") ItemWriter<StgCaseInProgressModel> writer
    ) throws Exception {
        return new StepBuilder("inProgressMigration", jobRepository)
                .<StgCaseInProgressModel, StgCaseInProgressModel>chunk(1000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(new CustomStepExecutionListener())
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

}
