package com.ttb.crm.service.migrationdata.controller;

import com.ttb.crm.lib.crmssp_common_utils_lib.bean.BaseResponse;
import com.ttb.crm.service.migrationdata.bean.request.BatchMigrationDataRequest;
import com.ttb.crm.service.migrationdata.bean.response.BatchMigrationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TriggerBatchJobControllerTest {

    @InjectMocks
    private TriggerBatchJobController controller;

    @Mock private JobLauncher jobLauncher;
    @Mock private Job completedMigrationDataJob;
    @Mock private Job inProgressDataMigrationJob;
    @Mock private Job resolvedMigrationDataJob;
    @Mock private Job migrateAllDataJob;
    @Mock private JobExecution jobExecution;

    private BatchMigrationDataRequest req;

    @BeforeEach
    void setUp() {
        controller = new TriggerBatchJobController(
                jobLauncher,
                completedMigrationDataJob,
                inProgressDataMigrationJob,
                resolvedMigrationDataJob,
                migrateAllDataJob
        );
        req = new BatchMigrationDataRequest();
        req.setEmployeeId("emp123");
        req.setJobId("job123");
        req.setJobName("name123");
        when(jobExecution.getJobId()).thenReturn(999L);


    }
    @Test
    void completedData_success() throws Exception {
        when(jobLauncher.run(eq(completedMigrationDataJob), any(JobParameters.class)))
                .thenReturn(jobExecution);

        BaseResponse<BatchMigrationResponse> res = controller.completedData(req);

        assertNotNull(res);
        assertNotNull(res.getData());
        assertEquals("job123", res.getData().getJobId());
        assertEquals(999L, res.getData().getJobExecutionId());

        ArgumentCaptor<JobParameters> cap = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher).run(eq(completedMigrationDataJob), cap.capture());
        assertEquals(3, cap.getValue().getParameters().size());
    }

    @Test
    void migrateData_success() throws Exception {
        when(jobLauncher.run(eq(migrateAllDataJob), any(JobParameters.class)))
                .thenReturn(jobExecution);

        BaseResponse<BatchMigrationResponse> res = controller.migrateData(req);

        assertNotNull(res);
        assertNotNull(res.getData());
        assertEquals("job123", res.getData().getJobId());
        assertEquals(999L, res.getData().getJobExecutionId());
        verify(jobLauncher).run(eq(migrateAllDataJob), any(JobParameters.class));
    }

    @Test
    void resolvedData_success() throws Exception {
        when(jobLauncher.run(eq(resolvedMigrationDataJob), any(JobParameters.class)))
                .thenReturn(jobExecution);

        BaseResponse<BatchMigrationResponse> res = controller.resolvedData(req);

        assertNotNull(res.getData());
        assertEquals("job123", res.getData().getJobId());
        assertEquals(999L, res.getData().getJobExecutionId());
        verify(jobLauncher).run(eq(resolvedMigrationDataJob), any(JobParameters.class));
    }

    @Test
    void inProgressData_success() throws Exception {
        when(jobLauncher.run(eq(inProgressDataMigrationJob), any(JobParameters.class)))
                .thenReturn(jobExecution);

        BaseResponse<BatchMigrationResponse> res = controller.inProgressData(req);

        assertNotNull(res.getData());
        assertEquals("job123", res.getData().getJobId());
        assertEquals(999L, res.getData().getJobExecutionId());
        verify(jobLauncher).run(eq(inProgressDataMigrationJob), any(JobParameters.class));
    }

}