package com.ttb.crm.service.migrationdata.model.batch;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "temp_update_stg_in_progress")
public class TempUpdateStgInProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "temp_id")
    private UUID tempId;
    private Long id;
    @Column(name = "load_status")
    private String loadStatus;
    @Column(name = "load_remark")
    private String loadRemark;
    @Column(name = "load_lastdatetime")
    private ZonedDateTime loadLastDatetime;
}
