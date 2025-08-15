package com.ttb.crm.service.migrationdata.service.inprogressmigrationdata.totempstep;

import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ToTempInProgressStepTest {

    private JobRepository jobRepository;
    private PlatformTransactionManager transactionManager;
    private ToTempInProgressStep stepConfig;

    @BeforeEach
    void setUp() {
        jobRepository = mock(JobRepository.class);
        transactionManager = mock(PlatformTransactionManager.class);
        stepConfig = new ToTempInProgressStep(jobRepository, transactionManager);
    }

    @Test
    void testToTempStepConfiguration() {
        RepositoryItemReader<StgCaseInProgressModel> reader = mock(RepositoryItemReader.class);
        ItemWriter<TempStgCaseInProgressLogModel> writer = mock(ItemWriter.class);
        ToTempInProgressProcessor processor = mock(ToTempInProgressProcessor.class);

        Step step = stepConfig.ToTemp(reader, writer, processor);

        assertNotNull(step, "Step should not be null");
        assertEquals("toTemp", step.getName(), "Step name should be 'toTemp'");
    }
}
