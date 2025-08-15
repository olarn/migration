package com.ttb.crm.service.migrationdata.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalException {

    @ExceptionHandler(JobExecutionAlreadyRunningException.class)
    public ResponseEntity<Void> handleJobRunning(JobExecutionAlreadyRunningException ex) {

        log.error("Got error : {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                null,
                HttpStatus.CONFLICT
        );
    }
}
