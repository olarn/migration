package com.ttb.crm.service.migrationdata.bean.request;

import com.ttb.crm.service.migrationdata.constants.CommonConstants.JobStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@Getter
@Setter
@Accessors(chain = true)
public class AfterJobRequest {
    private Long jobExecutionId;
    private JobStatus jobStatus;
    private int totalRecord;
    private int totalSuccess;
    private int totalFailed;
    private String message;
    private String jobId;
    private String jobName;
    private String employeeId;
    private ZonedDateTime finishDate;
}
