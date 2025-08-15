package com.ttb.crm.service.migrationdata.repository.caseManagement;

import com.ttb.crm.service.migrationdata.model.caseManagement.CaseDocumentReferenceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CaseDocumentReferenceRepository extends JpaRepository<CaseDocumentReferenceModel, UUID> {
    void deleteCaseDocumentReferenceModelByCases_CaseId(UUID CaseId);
}
