package com.ttb.crm.service.migrationdata.repository.batch;

import com.ttb.crm.service.migrationdata.model.batch.TempUpdateStgInProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository

public interface TempUpdateStgInProgressRepository extends JpaRepository<TempUpdateStgInProgress, UUID> {
}
