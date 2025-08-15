package com.ttb.crm.service.migrationdata.repository.secondary;

import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressUpdateStatusModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StgCaseInProgressUpdateStatusRepository extends JpaRepository<StgCaseInProgressUpdateStatusModel, Long> {
}
