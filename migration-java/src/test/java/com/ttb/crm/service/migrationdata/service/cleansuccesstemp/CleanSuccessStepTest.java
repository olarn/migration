package com.ttb.crm.service.migrationdata.service.cleansuccesstemp;

import com.ttb.crm.service.migrationdata.bean.TempLogData;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.service.cleansuccessstemp.CleanSuccessProcessor;
import com.ttb.crm.service.migrationdata.service.cleansuccessstemp.CleanSuccessStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CleanSuccessStepTest {

    private JobRepository jobRepository;
    private PlatformTransactionManager transactionManager;
    private CleanSuccessStep cleanSuccessStep;

    @BeforeEach
    void setUp() {
        jobRepository = mock(JobRepository.class);
        transactionManager = mock(PlatformTransactionManager.class);
        cleanSuccessStep = new CleanSuccessStep(jobRepository, transactionManager);
    }

    @Test
    void testCleanSuccessStepCreation() {
        RepositoryItemReader<TempStgCaseInProgressLogModel> reader = mock(RepositoryItemReader.class);
        ItemWriter<TempLogData> writer = mock(ItemWriter.class);
        CleanSuccessProcessor processor = mock(CleanSuccessProcessor.class);

        Step step = cleanSuccessStep.cleanSuccessStep(reader, writer, processor);

        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("cleanSuccessStep");
    }
}
