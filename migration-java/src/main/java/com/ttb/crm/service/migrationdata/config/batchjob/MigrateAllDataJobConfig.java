package com.ttb.crm.service.migrationdata.config.batchjob;

import com.ttb.crm.service.migrationdata.config.JobTerminationListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MigrateAllDataJobConfig {

    private final JobRepository jobRepository;
    private final JobTerminationListener jobTerminationListener;

    @Bean
    Job migrateAllDataJob(
            @Qualifier("stgToCase") Step tempToCase,
            @Qualifier("tempUpdateStatusToStgStep") Step tempUpdateStatusToStgStep
    ) {
        return new JobBuilder("migrationAllDataJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(tempToCase)
                .next(tempUpdateStatusToStgStep)
                .listener(jobTerminationListener)
                .build();
    }
}
