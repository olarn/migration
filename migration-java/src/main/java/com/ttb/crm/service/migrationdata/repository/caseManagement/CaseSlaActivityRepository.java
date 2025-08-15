package com.ttb.crm.service.migrationdata.repository.caseManagement;

import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CaseSlaActivityRepository extends JpaRepository<CaseSlaActivity, UUID> {

    void deleteByCases_caseId(UUID caseId);

}