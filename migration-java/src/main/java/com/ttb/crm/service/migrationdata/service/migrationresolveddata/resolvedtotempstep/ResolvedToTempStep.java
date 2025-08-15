package com.ttb.crm.service.migrationdata.service.migrationresolveddata.resolvedtotempstep;

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
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ResolvedToTempStep {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean(name = "resolvedToTemp")
    public Step CompletedToTemp(@Qualifier("resolvedStagingReader") ItemReader<StgCaseInProgressModel> reader,
                       @Qualifier("resolvedToTempWriter") ItemWriter<TempStgCaseInProgressLogModel> writer,
                       @Qualifier("resolvedToTempProcessor") ResolvedToTempProcessor toTempProcessor
                       ) {
        return new StepBuilder("resolvedToTemp", jobRepository)
                .<StgCaseInProgressModel, TempStgCaseInProgressLogModel>chunk(1000, transactionManager)
                .reader(reader)
                .processor(toTempProcessor)
                .writer(writer)
                .listener(new CustomStepExecutionListener())
                .build();
    }
}
