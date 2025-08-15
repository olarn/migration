package com.ttb.crm.service.migrationdata.repository.secondary;

import com.ttb.crm.service.migrationdata.model.secondary.StgCaseDocumentReferenceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StgCaseDocumentReferenceRepository extends JpaRepository<StgCaseDocumentReferenceModel, UUID> {
    List<StgCaseDocumentReferenceModel> findAllByCaseCIn(List<String> caseC);
    List<StgCaseDocumentReferenceModel> findAllByCaseC(String caseC);
}
