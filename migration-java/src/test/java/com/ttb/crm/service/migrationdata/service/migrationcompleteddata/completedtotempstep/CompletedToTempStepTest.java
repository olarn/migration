package com.ttb.crm.service.migrationdata.service.migrationcompleteddata.completedtotempstep;

import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class CompletedToTempStepTest {

    private JobRepository jobRepository;
    private PlatformTransactionManager transactionManager;
    private CompletedToTempStep stepConfig;

    @BeforeEach
    void setUp() {
        jobRepository = mock(JobRepository.class);
        transactionManager = mock(PlatformTransactionManager.class);
        stepConfig = new CompletedToTempStep(jobRepository, transactionManager);
    }

    @Test
    void testCompletedToTempStepConfiguration() {
        RepositoryItemReader<StgCaseInProgressModel> reader = mock(RepositoryItemReader.class);
        ItemWriter<TempStgCaseInProgressLogModel> writer = mock(ItemWriter.class);
        CompletedToTempProcessor processor = mock(CompletedToTempProcessor.class);

        Step step = stepConfig.completedToTemp(reader, writer, processor);

        assertNotNull(step, "Step should not be null");
        assertEquals("completedToTemp", step.getName(), "Step name should be 'completedToTemp'");
    }
}
