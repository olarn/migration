package com.ttb.crm.service.migrationdata.model.batch;

import com.ttb.crm.service.migrationdata.bean.request.BatchMigrationDataRequest;
import com.ttb.crm.service.migrationdata.enums.BatchRecordStatusEnum;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "temp_stg_sla_per_owner_log")
@Accessors(chain = true)
public class TempStgSlaPerOwnerLogModel {
    @Id
    private Long id;
    @Column(name = "Case__C")
    private String caseC;
    @Column(name = "Owner_Team__c")
    private String ownerTeamC;
    @Column(name = "Owner_Team_New")
    private String ownerTeamNew;
    @Column(name = "Name")
    private String name;
    @Column(name = "Employee_ID__c")
    private String employeeIdC;
    @Column(name = "Start_Date_Time__c")
    private String startDateTimeC;
    @Column(name = "End_Date_Time__c")
    private String endDateTimeC;
    @Column(name = "Case_Status__c")
    private String caseStatusC;
    @Column(name = "migration_lot")
    private String migrationLot;
    @Column(name = "mid")
    private Long mid;
    @Column(name = "record_status")
    private String recordStatus;
    @Column(name = "record_remark")
    private String recordRemark;
    @Column(name = "record_lastdatetime")
    private ZonedDateTime recordLastDatetime;

    // Batch
    @Column(name = "job_id")
    private String jobId; // new

    @Column(name = "job_execution_id")
    private Long jobExecutionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "batch_record_status")
    private BatchRecordStatusEnum batchRecordStatus;

    @Column(name = "batch_record_error_message")
    private String batchRecordErrorMessage;

    public TempStgSlaPerOwnerLogModel(
            StgSlaPerOwnerModel stgSlaPerOwnerModel,
            BatchMigrationDataRequest batchMigrationDataRequest,
            Long jobExecutionId,
            BatchRecordStatusEnum batchRecordStatus
    ) {
        BeanUtils.copyProperties(stgSlaPerOwnerModel, this);
        BeanUtils.copyProperties(batchMigrationDataRequest, this);
        this.jobExecutionId = jobExecutionId;
        this.batchRecordStatus = batchRecordStatus;
    }

    public TempStgSlaPerOwnerLogModel(
            StgCaseInProgressModel stgSlaPerOwnerModel,
            BatchMigrationDataRequest batchMigrationDataRequest,
            Long jobExecutionId,
            BatchRecordStatusEnum batchRecordStatus
    ) {
        BeanUtils.copyProperties(stgSlaPerOwnerModel, this);
        BeanUtils.copyProperties(batchMigrationDataRequest, this);
        this.jobExecutionId = jobExecutionId;
        this.batchRecordStatus = batchRecordStatus;
    }
}
