package com.ttb.crm.service.migrationdata.repository.caseManagement;

import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaHopModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CaseSlaHopRepository extends JpaRepository<CaseSlaHopModel, UUID> {
    void deleteCaseSlaHopModelByCases_CaseId(UUID caseId);
}
