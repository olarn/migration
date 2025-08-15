package com.ttb.crm.service.migrationdata.bean.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BatchMigrationResponse {
    private String jobId;
    private Long jobExecutionId;

    public BatchMigrationResponse(Long jobExecutionId, String jobId) {
        this.jobId = jobId;
        this.jobExecutionId = jobExecutionId;
    }
}
