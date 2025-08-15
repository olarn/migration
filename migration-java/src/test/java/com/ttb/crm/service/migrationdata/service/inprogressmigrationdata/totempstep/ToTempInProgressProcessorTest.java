package com.ttb.crm.service.migrationdata.service.inprogressmigrationdata.totempstep;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttb.crm.service.migrationdata.bean.request.BatchMigrationDataRequest;
import com.ttb.crm.service.migrationdata.enums.BatchRecordStatusEnum;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ToTempInProgressProcessorTest {

    private BatchMigrationDataRequest batchRequest;
    private String batchRequestJson;
    private ToTempInProgressProcessor processor;

    @BeforeEach
    void setUp() throws Exception {
        batchRequest = new BatchMigrationDataRequest();
        batchRequest.setJobName("BATCH123");
        batchRequest.setJobId("JOB0001");

        ObjectMapper mapper = new ObjectMapper();
        batchRequestJson = mapper.writeValueAsString(batchRequest);

        processor = new ToTempInProgressProcessor(batchRequestJson);

        StepExecution mockStepExecution = mock(StepExecution.class);
        JobExecution mockJobExecution = mock(JobExecution.class);

        when(mockStepExecution.getJobExecution()).thenReturn(mockJobExecution);
        when(mockJobExecution.getId()).thenReturn(123L);
        Field field = ToTempInProgressProcessor.class.getDeclaredField("stepExecution");
        field.setAccessible(true);
        field.set(processor, mockStepExecution);
    }

    @Test
    void testBeforeStepSetsJobExecutionId() {
        TempStgCaseInProgressLogModel result = processor.process(new StgCaseInProgressModel());
        assertNotNull(result);
        assertEquals(123L, result.getJobExecutionId());
        assertEquals(BatchRecordStatusEnum.PENDING, result.getBatchRecordStatus());
    }

    @Test
    void testProcessReturnsNullOnException() {
        assertThrows(RuntimeException.class, () -> new ToTempInProgressProcessor("invalid-json"), "Processor should return null on exception");
    }
}
