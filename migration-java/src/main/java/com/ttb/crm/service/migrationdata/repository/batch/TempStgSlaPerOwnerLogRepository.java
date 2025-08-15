package com.ttb.crm.service.migrationdata.repository.batch;

import com.ttb.crm.service.migrationdata.model.batch.TempStgSlaPerOwnerLogModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TempStgSlaPerOwnerLogRepository extends JpaRepository<TempStgSlaPerOwnerLogModel, UUID> {
    List<TempStgSlaPerOwnerLogModel> findAllByCaseCOrderByStartDateTimeCAsc(String caseC);
}
