package com.ttb.crm.service.migrationdata.config;

import com.ttb.crm.service.migrationdata.bean.request.AfterJobRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "BatchManagementClient", url = "${batch-management.client.url}")
public interface BatchManagementClient {

    @PostMapping("/internal/job/afterJob")
    void callAfterJob(AfterJobRequest req);
}