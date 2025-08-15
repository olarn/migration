package com.ttb.crm.service.migrationdata.service.migratealldata.cleanTemp;

import com.ttb.crm.service.migrationdata.repository.batch.TempUpdateStgInProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CleanTempStep {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TempUpdateStgInProgressRepository tempUpdateStgInProgressRepository;

    @Bean(name = "cleanTemp")
    public Step cleanSuccessStep() {
        return new StepBuilder("cleanTemp", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    tempUpdateStgInProgressRepository.deleteAll();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}