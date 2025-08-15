package com.ttb.crm.service.migrationdata.repository.caseManagement;

import com.ttb.crm.service.migrationdata.bean.CaseIdAndModifiedOnOnDTO;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseTransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CaseTransactionRepository extends JpaRepository<CaseTransactionModel, UUID> {

    @Query("SELECT new com.ttb.crm.service.migrationdata.bean.CaseIdAndModifiedOnOnDTO(c.caseId, c.modifiedOn) FROM CaseTransactionModel c WHERE c.caseNumber = :caseNumber")
    Optional<CaseIdAndModifiedOnOnDTO> findByCaseNumber(String caseNumber);

    void deleteByCaseId (UUID caseId);
}
