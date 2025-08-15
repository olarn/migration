package com.ttb.crm.service.migrationdata.service.inprogressmigrationdata.mappingdatastep;

import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;


class InProgressMigrationDataStepTest {

    private JobRepository jobRepository;
    private PlatformTransactionManager transactionManager;
    private DataSource dataSource;
    private InProgressMigrationDataReader reader;
    private InProgressMigrationDataProcessor processor;
    private InProgressMigrationDataWriter writer;
    private InProgressMigrationDataStep stepConfig;

    @BeforeEach
    void setUp() {
        jobRepository = mock(JobRepository.class);
        transactionManager = mock(PlatformTransactionManager.class);

        stepConfig = new InProgressMigrationDataStep(jobRepository, transactionManager);
    }

    @Test
    void testInProgressMigrationStepConfiguration() throws Exception {
        ItemReader<StgCaseInProgressModel> reader = mock(ItemReader.class);
        ItemWriter<StgCaseInProgressModel> writer = mock(ItemWriter.class);
        InProgressMigrationDataProcessor processor = mock(InProgressMigrationDataProcessor.class);

        Step step = stepConfig.inProgressMigrationDataStep(reader, processor, writer);

        assertNotNull(step, "Step should not be null");
        assertEquals("inProgressMigration", step.getName(), "Step name should be 'completedMappingData'");
    }
}
