package com.ttb.crm.service.migrationdata;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

import static com.ttb.crm.service.migrationdata.constants.CommonConstants.APPLICATION_NAME;

@SpringBootApplication(scanBasePackages = {"com.ttb.crm.*"})
@EnableFeignClients(basePackages = {
        "com.ttb.crm.service",
        "com.ttb.crm.lib"
})
@EnableBatchProcessing
@EnableCaching
@EnableAsync
public class BatchApplication {

    private static final Logger log = LoggerFactory.getLogger(BatchApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

    @PostConstruct
    public void logStart() {
        log.info("Starting :{}", APPLICATION_NAME);
    }

}
