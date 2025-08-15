package com.ttb.crm.service.migrationdata.model.secondary;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@Entity
@Table(name = "stg_sla_per_owner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class StgSlaPerOwnerModel {
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
    @Column(name = "End_Date_Time__c", length = 1000)
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

}
