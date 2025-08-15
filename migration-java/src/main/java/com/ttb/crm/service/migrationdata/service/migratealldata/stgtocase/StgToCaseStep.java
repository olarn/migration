package com.ttb.crm.service.migrationdata.service.migratealldata.stgtocase;

import com.ttb.crm.service.migrationdata.bean.StgToCaseWriterDTO;
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
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import static com.ttb.crm.service.migrationdata.helper.Constant.STG_TO_CASE_CHUNK_SIZE;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class StgToCaseStep {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TaskExecutor taskExecutor;

    @Bean(name = "stgToCase")
    public Step tempToCaseData(
            @Qualifier("stagingDataToCaseReader") ItemReader<StgCaseInProgressModel> reader,
            @Qualifier("stgToCaseProcessor") StgToCaseProcessor processor,
            @Qualifier("stgToCaseWriter") ItemWriter<StgToCaseWriterDTO> writer
    ) {
        return new StepBuilder("tempToCaseData", jobRepository)
                .<StgCaseInProgressModel, StgToCaseWriterDTO>chunk(STG_TO_CASE_CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor)
                .listener(new CustomStepExecutionListener())
                .build();
    }
}
