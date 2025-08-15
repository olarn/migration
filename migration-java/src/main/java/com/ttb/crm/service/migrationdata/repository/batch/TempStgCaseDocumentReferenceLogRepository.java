package com.ttb.crm.service.migrationdata.repository.batch;

import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseDocumentReferenceLogModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TempStgCaseDocumentReferenceLogRepository extends JpaRepository<TempStgCaseDocumentReferenceLogModel, UUID> {
    List<TempStgCaseDocumentReferenceLogModel> findAllByCaseC(String caseC);
}
