package com.ttb.crm.service.migrationdata.repository.masterManagement;

import com.ttb.crm.service.migrationdata.model.masterManagement.MasterDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MasterDataRepository extends JpaRepository<MasterDataModel, UUID> {
    Optional<MasterDataModel> findByCodeAndStatusCodeAndMasterGroup_CodeAndMasterGroup_StatusCode(String code, Integer statusCode, String masterGroupCode, Integer masterGroupStatusCode);
}
