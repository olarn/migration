package com.ttb.crm.service.migrationdata.repository.secondary;

import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StgCaseInProgressRepository extends JpaRepository<StgCaseInProgressModel, UUID>, JpaSpecificationExecutor<StgCaseInProgressModel> {

    //TODO : insert to temp_stg_case_inProgres
    //values
    //loadStatus
    //loadRemark
    //loadLastDatetime
    //id
}
