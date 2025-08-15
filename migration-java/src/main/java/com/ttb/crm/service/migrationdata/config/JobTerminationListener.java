package com.ttb.crm.service.migrationdata.config;

import com.ttb.crm.service.migrationdata.bean.request.AfterJobRequest;
import com.ttb.crm.service.migrationdata.constants.CommonConstants.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobTerminationListener implements JobExecutionListener {

    private final BatchManagementClient batchManagementClient;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // Insert code, If you have a business logic running before job started
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long totalReadCount = 0;
        long totalReadTemp = 0L;
        long jobExecutionId = 0L;
        int noMappingSuccess = 0;
        int noMappingFail = 0;
        JobParameters params = jobExecution.getJobParameters();
        String jobId = params.getString("jobId");
        String employeeId = params.getString("employeeId");
        String jobName = params.getString("jobName");

        // Gathering records from each step
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            jobExecutionId = stepExecution.getJobExecutionId();
            if (stepExecution.getStepName().equals("toTemp") || stepExecution.getStepName().equals("completedToTemp") || stepExecution.getStepName().equals("resolvedToTemp")) {
                totalReadTemp = stepExecution.getReadCount();
            }
            if (stepExecution.getStepName().equals("inProgressMigration") || stepExecution.getStepName().equals("completedMappingData") || stepExecution.getStepName().equals("resolvedMappingData")) {
                noMappingSuccess = Math.toIntExact(stepExecution.getWriteCount());
                noMappingFail = Math.toIntExact(stepExecution.getSkipCount());
            }
            totalReadCount += stepExecution.getReadCount();
            log.info("Step: {} - Read: {}, Processed: {}, Written: {}",
                    stepExecution.getStepName(),
                    stepExecution.getReadCount(),
                    stepExecution.getFilterCount(),
                    stepExecution.getWriteCount());
        }

        log.info("Job completed successfully. Total records read: {}", totalReadCount);
        log.info("Job status: {}", jobExecution.getStatus());
        log.info("Job start time: {}", jobExecution.getStartTime());
        log.info("Job end time: {}", jobExecution.getEndTime());

        JobStatus jobStatus;
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            if (noMappingFail > 0) {
                jobStatus = JobStatus.PARTIAL_FAILED;
            } else {
                jobStatus = JobStatus.SUCCESS;
            }
        } else {
            jobStatus = JobStatus.FAILED;
        }
        AfterJobRequest req = new AfterJobRequest()
                .setJobExecutionId(jobExecutionId)
                .setJobStatus(jobStatus)
                .setTotalRecord((int) totalReadTemp)
                .setEmployeeId(employeeId)
                .setTotalSuccess(noMappingSuccess)
                .setTotalFailed(noMappingFail)
                .setJobId(jobId)
                .setEmployeeId(employeeId)
                .setJobName(jobName)
                .setMessage("")
                .setFinishDate(jobExecution.getEndTime().atZone(ZoneId.systemDefault()));

        try {
            log.info("Sending... after job");
            batchManagementClient.callAfterJob(req);
            log.info("Sending... after job successfully");
        } catch (Exception ex) {
            log.error("Cannot sent after job data {}", ex.getMessage());
        }
        log.info("Running successfully");
    }
}
