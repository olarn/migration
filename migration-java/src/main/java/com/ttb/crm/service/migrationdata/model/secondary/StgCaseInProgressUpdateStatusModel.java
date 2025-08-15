package com.ttb.crm.service.migrationdata.model.secondary;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stg_case_inprogress")
public class StgCaseInProgressUpdateStatusModel {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "load_status", length = 50)
    private String loadStatus;

    @Column(name = "load_remark")
    private String loadRemark;

    @Column(name = "load_lastdatetime")
    private ZonedDateTime loadLastDatetime;
}
