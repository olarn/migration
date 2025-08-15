package com.ttb.crm.service.migrationdata.service.cleansuccessstemp;
import com.ttb.crm.service.migrationdata.enums.BatchRecordStatusEnum;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgCaseInProgressRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

@Configuration
public class CleanSuccessReader {

    @Bean(name = "tempStgReader")
    @StepScope
    public RepositoryItemReader<TempStgCaseInProgressLogModel> tempStgReader(
            TempStgCaseInProgressRepository tempStgCaseInProgressRepository) {
        return new RepositoryItemReaderBuilder<TempStgCaseInProgressLogModel>()
                .repository(tempStgCaseInProgressRepository)
                .methodName("findAllByBatchRecordStatusIn")
                .arguments(List.of(List.of(BatchRecordStatusEnum.SUCCESS)))
                .pageSize(50)
                .sorts(Map.of("Id", Sort.Direction.ASC))
                .name("tempStgReader")
                .build();
    }
}
