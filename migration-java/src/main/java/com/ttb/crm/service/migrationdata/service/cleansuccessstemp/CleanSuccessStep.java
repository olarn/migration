package com.ttb.crm.service.migrationdata.service.cleansuccessstemp;

import com.ttb.crm.service.migrationdata.bean.TempLogData;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CleanSuccessStep {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean(name = "cleanSuccess")
    public Step cleanSuccessStep(@Qualifier("tempStgReader") RepositoryItemReader<TempStgCaseInProgressLogModel> reader,
                       @Qualifier("cleanSuccessWriter") ItemWriter<TempLogData> writer,
                       @Qualifier("cleanSuccessProcessor") CleanSuccessProcessor processor
                       ) {
        return new StepBuilder("cleanSuccessStep", jobRepository)
                .<TempStgCaseInProgressLogModel, TempLogData>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
