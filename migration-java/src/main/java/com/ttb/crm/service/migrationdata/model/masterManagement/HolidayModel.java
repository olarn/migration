package com.ttb.crm.service.migrationdata.model.masterManagement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "holiday")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class HolidayModel {

    @Id
    @Column(nullable = false, updatable = false, name = "holiday_id")
    private UUID holidayId;

    @Column(name = "holiday_date")
    private LocalDate holidayDate;

    @Column(name = "holiday_name")
    private String holidayName;

    @Column(name = "holiday_description")
    private String holidayDescription;

    @Transient
    @Column(name = "status")
    private HolidayStatus status;

    @Column(name = "is_recalculate")
    private Boolean isRecalculate;
    @Column(name = "recalculate_date")
    private ZonedDateTime recalculateDate;

    @Column(name = "created_on")
    private ZonedDateTime createdOn;

    @Column(name = "created_by_id")
    private UUID createdById;

    @Column(name = "modified_on")
    private ZonedDateTime modifiedOn;

    @Column(name = "modified_by_id")
    private UUID modifiedById;

    @Column(name = "status_code")
    private Integer statusCode;
}
