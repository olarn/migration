package com.ttb.crm.service.migrationdata.service.migrationcompleteddata.completedtotempstep;

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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompletedToTempProcessorTest {

    private BatchMigrationDataRequest batchRequest;
    private String batchRequestJson;
    private CompletedToTempProcessor processor;

    @BeforeEach
    void setUp() throws Exception {
        batchRequest = new BatchMigrationDataRequest();
        batchRequest.setJobName("BATCH001");
        batchRequest.setJobId("JOB0001");

        ObjectMapper mapper = new ObjectMapper();
        batchRequestJson = mapper.writeValueAsString(batchRequest);

        processor = new CompletedToTempProcessor(batchRequestJson);

        StepExecution mockStepExecution = mock(StepExecution.class);
        JobExecution mockJobExecution = mock(JobExecution.class);

        when(mockStepExecution.getJobExecution()).thenReturn(mockJobExecution);
        when(mockJobExecution.getId()).thenReturn(123L);
        Field field = CompletedToTempProcessor.class.getDeclaredField("stepExecution");
        field.setAccessible(true);
        field.set(processor, mockStepExecution);
    }

    @Test
    void testBeforeStepSetsJobExecutionId() {

        StgCaseInProgressModel input = new StgCaseInProgressModel();
        TempStgCaseInProgressLogModel result = processor.process(input);

        assertNotNull(result);
        assertEquals(123L, result.getJobExecutionId());
        assertEquals(BatchRecordStatusEnum.PENDING, result.getBatchRecordStatus());
    }

    @Test
    void testConstructorThrowsExceptionOnInvalidJson() {
        String invalidJson = "not-a-json";
        Exception exception = assertThrows(RuntimeException.class, () -> new CompletedToTempProcessor(invalidJson));
        assertTrue(exception.getMessage().contains("Invalid job parameter"));
    }

    @Test
    void testProcessReturnsNullOnException() {
        processor = new CompletedToTempProcessor(batchRequestJson) {
            @Override
            public TempStgCaseInProgressLogModel process(StgCaseInProgressModel item) {
                throw new RuntimeException("Simulated error");
            }
        };

        assertThrows(RuntimeException.class, () -> new CompletedToTempProcessor("invalid-json"), "Processor should return null on exception");
    }
}
