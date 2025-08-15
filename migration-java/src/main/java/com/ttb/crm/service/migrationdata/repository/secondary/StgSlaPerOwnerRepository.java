package com.ttb.crm.service.migrationdata.repository.secondary;

import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StgSlaPerOwnerRepository extends JpaRepository<StgSlaPerOwnerModel, UUID> {
    List<StgSlaPerOwnerModel> findAllByCaseCIn(List<String> caseC);
    List<StgSlaPerOwnerModel> findAllByCaseCOrderByStartDateTimeCAsc(String caseC);
}
