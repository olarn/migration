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
public class ResolvedMigrationDataJobConfig {

    private final JobRepository jobRepository;
    private final JobTerminationListener jobTerminationListener;

    @Bean
    public Job resolvedMigrationDataJob(
            @Qualifier("resolvedToTemp") Step resolvedToTemp,
            @Qualifier("resolvedMappingData") Step resolvedMappingData,
            @Qualifier("cleanSuccess") Step cleanSuccessStep
    ) {

        return new JobBuilder("resolvedMigrationDataJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(resolvedToTemp)
                .next(resolvedMappingData)
                .next(cleanSuccessStep)
                .listener(jobTerminationListener)
                .build();
    }

}
