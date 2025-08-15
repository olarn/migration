package com.ttb.crm.service.migrationdata.service.migrationresolveddata.resolvedmappingdatastep;

import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResolvedMappingDataStepTest {

    private JobRepository jobRepository;
    private PlatformTransactionManager transactionManager;
    private ResolvedMappingDataStep stepConfig;

    @BeforeEach
    void setUp() {
        jobRepository = mock(JobRepository.class);
        transactionManager = mock(PlatformTransactionManager.class);
        stepConfig = new ResolvedMappingDataStep(jobRepository, transactionManager);
    }

    @Test
    void testResolvedMappingDataStepConfiguration() {
        ItemReader<StgCaseInProgressModel> reader = mock(ItemReader.class);
        ItemWriter<StgCaseInProgressModel> writer = mock(ItemWriter.class);
        ResolvedMappingDataProcessor processor = mock(ResolvedMappingDataProcessor.class);

        Step step = stepConfig.CompletedMappingData(reader, writer, processor);

        assertNotNull(step, "Step should not be null");
        assertEquals("resolvedMappingData", step.getName(), "Step name should be 'resolvedMappingData'");
    }
}
