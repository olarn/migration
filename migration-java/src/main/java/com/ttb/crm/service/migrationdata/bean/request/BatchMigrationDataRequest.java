package com.ttb.crm.service.migrationdata.bean.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchMigrationDataRequest {
    @NotEmpty(message = "employeeId is required")
    private String employeeId;

    @NotEmpty(message = "jobId is required")
    private String jobId;

    @NotEmpty(message = "jobName is required")
    private String jobName;

    @Getter
    private Map<String, Object> extraData = new HashMap<>();

}
