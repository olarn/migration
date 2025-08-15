package com.ttb.crm.service.migrationdata.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttb.crm.lib.crmssp_common_utils_lib.bean.BaseResponse;
import com.ttb.crm.lib.crmssp_common_utils_lib.helper.BaseResponseUtil;
import com.ttb.crm.service.migrationdata.bean.request.BatchMigrationDataRequest;
import com.ttb.crm.service.migrationdata.bean.response.BatchMigrationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.ttb.crm.service.migrationdata.constants.CommonConstants.*;
import static com.ttb.crm.service.migrationdata.helper.Constant.PREFIX_ERROR;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/trigger/migration")
public class TriggerBatchJobController {

    private final JobLauncher jobLauncher;
    private final Job completedMigrationDataJob;
    private final Job inProgressDataMigrationJob;
    private final Job resolvedMigrationDataJob;
    private final Job migrateAllDataJob;

    @PostMapping("/completedData")
    public BaseResponse<BatchMigrationResponse> completedData(@Validated @RequestBody BatchMigrationDataRequest request) {
        try {
            String requestJson = new ObjectMapper().writeValueAsString(request);
            JobParameters params = new JobParametersBuilder()
                    .addString(LOWER_CASE_UUID, UUID.randomUUID().toString())
                    .addString(LOWER_CASE_TIMESTAMP, String.valueOf(System.currentTimeMillis()))
                    .addString(CAMEL_CASE_BATCH_REQUEST, requestJson)
                    .toJobParameters();

            JobExecution run = jobLauncher.run(completedMigrationDataJob, params);
            BatchMigrationResponse res = new BatchMigrationResponse(run.getJobId(), request.getJobId());
            return BaseResponseUtil.success(res);
        } catch (Exception e) {
            return BaseResponseUtil.error("Error: " + e.getMessage());
        }
    }

    @PostMapping("/resolvedData")
    public BaseResponse<BatchMigrationResponse> resolvedData(@Validated @RequestBody BatchMigrationDataRequest request) {
        try {
            String requestJson = new ObjectMapper().writeValueAsString(request);
            JobParameters params = new JobParametersBuilder()
                    .addString(LOWER_CASE_UUID, UUID.randomUUID().toString())
                    .addString(LOWER_CASE_TIMESTAMP, String.valueOf(System.currentTimeMillis()))
                    .addString(CAMEL_CASE_BATCH_REQUEST, requestJson)
                    .toJobParameters();

            JobExecution run = jobLauncher.run(resolvedMigrationDataJob, params);
            BatchMigrationResponse res = new BatchMigrationResponse(run.getJobId(), request.getJobId());
            return BaseResponseUtil.success(res);
        } catch (Exception e) {
            return BaseResponseUtil.error(PREFIX_ERROR + e.getMessage());
        }
    }

    @PostMapping("/inProgressData")
    public BaseResponse<BatchMigrationResponse> inProgressData(@Validated @RequestBody BatchMigrationDataRequest request) {
        try {
            String requestJson = new ObjectMapper().writeValueAsString(request);
            JobParameters params = new JobParametersBuilder()
                    .addString(LOWER_CASE_UUID, UUID.randomUUID().toString())
                    .addString(LOWER_CASE_TIMESTAMP, String.valueOf(System.currentTimeMillis()))
                    .addString(CAMEL_CASE_BATCH_REQUEST, requestJson)
                    .toJobParameters();

            JobExecution run = jobLauncher.run(inProgressDataMigrationJob, params);
            BatchMigrationResponse res = new BatchMigrationResponse(run.getJobId(), request.getJobId());
            return BaseResponseUtil.success(res);
        } catch (Exception e) {
            return BaseResponseUtil.error(PREFIX_ERROR + e.getMessage());
        }
    }

    @PostMapping("/migrateData")
    public BaseResponse<BatchMigrationResponse> migrateData(@Validated @RequestBody BatchMigrationDataRequest request) {
        log.debug("Received: {}", request);
        try {
            String requestJson = new ObjectMapper().writeValueAsString(request);
            JobParameters params = new JobParametersBuilder()
                    .addString(LOWER_CASE_UUID, UUID.randomUUID().toString())
                    .addString(LOWER_CASE_TIMESTAMP, String.valueOf(System.currentTimeMillis()))
                    .addString(CAMEL_CASE_BATCH_REQUEST, requestJson)
                    .toJobParameters();

            JobExecution run = jobLauncher.run(migrateAllDataJob, params);
            BatchMigrationResponse res = new BatchMigrationResponse(run.getJobId(), request.getJobId());
            return BaseResponseUtil.success(res);
        } catch (Exception e) {
            return BaseResponseUtil.error(PREFIX_ERROR + e.getMessage());
        }
    }

//    @PostMapping("/test2")
//    public BaseResponse<CountCaseOnGoingCanMigrateResponse> test2() {
//        return testService.countCaseOnGoingCanMigrate();
//    }
}
