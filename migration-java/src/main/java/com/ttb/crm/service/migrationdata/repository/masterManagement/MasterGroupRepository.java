package com.ttb.crm.service.migrationdata.repository.masterManagement;

import com.ttb.crm.service.migrationdata.model.masterManagement.MasterGroupModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MasterGroupRepository extends JpaRepository<MasterGroupModel, UUID> {
    Optional<MasterGroupModel> findByCode(String code);
}
