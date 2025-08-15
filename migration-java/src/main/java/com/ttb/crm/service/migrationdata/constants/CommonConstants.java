package com.ttb.crm.service.migrationdata.constants;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.ZoneId;

public class CommonConstants {
    public static final String APPLICATION_NAME = "boilerplate-crmssp-batch";
    public static final String SCHEMA_NAME = "dbo";

    public static final String PREFIX_JOB = APPLICATION_NAME + "-job";
    public static final String PREFIX_BATCH_METADATA = SCHEMA_NAME + ".BATCH_";
    public static final String PREFIX_JOB_EXECUTOR = APPLICATION_NAME + "-job-asynchronous-executor-";
    public static final ZoneId asiaBangkokZoneId = ZoneId.of("Asia/Bangkok");
    public static final ZoneId utcZoneId = ZoneId.of("UTC");
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    public static final String LOWER_CASE_UUID = "uuid";
    public static final String LOWER_CASE_TIMESTAMP = "timestamp";
    public static final String CAMEL_CASE_BATCH_REQUEST = "batchRequest";
    public static final String MIGRATION_LOT = "migrationLot";
    public static final String CASE_STATUS_CODE = "caseStatusCode";

    private CommonConstants() {
        throw new IllegalArgumentException();
    }

    public enum JobStatus {
        SUCCESS, FAILED, PARTIAL_FAILED
    }
}
