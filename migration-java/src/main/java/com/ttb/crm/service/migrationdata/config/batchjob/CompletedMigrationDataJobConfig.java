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
public class CompletedMigrationDataJobConfig {

    private final JobRepository jobRepository;
    private final JobTerminationListener jobTerminationListener;

    @Bean
    public Job completedMigrationDataJob(
            @Qualifier("completedToTemp") Step completedToTemp,
            @Qualifier("completedMappingData") Step completedMappingData,
            @Qualifier("cleanSuccess") Step cleanSuccessStep
    ) {
        return new JobBuilder("completedMigrationDataJob", jobRepository)
                .incrementer(new RunIdIncrementer())
//                .start(completedToTemp)
                .start(completedMappingData)
//                .next(cleanSuccessStep)
                .listener(jobTerminationListener)
                .build();
    }

}
