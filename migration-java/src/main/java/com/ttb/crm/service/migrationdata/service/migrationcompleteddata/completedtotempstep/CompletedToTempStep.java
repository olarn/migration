package com.ttb.crm.service.migrationdata.service.migrationcompleteddata.completedtotempstep;

import com.ttb.crm.service.migrationdata.config.CustomStepExecutionListener;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
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
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CompletedToTempStep {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean(name = "completedToTemp")
    public Step completedToTemp(@Qualifier("completedStagingReader") ItemReader<StgCaseInProgressModel> reader,
                                @Qualifier("completedToTempWriter") ItemWriter<TempStgCaseInProgressLogModel> writer,
                                @Qualifier("completedToTempProcessor") CompletedToTempProcessor toTempProcessor
    ) {
        return new StepBuilder("completedToTemp", jobRepository)
                .<StgCaseInProgressModel, TempStgCaseInProgressLogModel>chunk(100, transactionManager)
                .reader(reader)
                .processor(toTempProcessor)
                .writer(writer)
                .taskExecutor(new SyncTaskExecutor())
                .listener(new CustomStepExecutionListener())
                .build();
    }
}
