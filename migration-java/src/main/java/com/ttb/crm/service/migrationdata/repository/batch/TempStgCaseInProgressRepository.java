package com.ttb.crm.service.migrationdata.repository.batch;

import com.ttb.crm.service.migrationdata.enums.BatchRecordStatusEnum;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface TempStgCaseInProgressRepository extends JpaRepository<TempStgCaseInProgressLogModel, UUID>, JpaSpecificationExecutor<TempStgCaseInProgressLogModel> {
    Page<TempStgCaseInProgressLogModel> findAllByBatchRecordStatusIn(Collection<BatchRecordStatusEnum> batchStatuses, Pageable pageable);
}
