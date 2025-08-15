package com.ttb.crm.service.migrationdata.model.caseManagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ttb.crm.service.migrationdata.enums.CaseSlaActivityAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "case_transaction_sla_activity")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class CaseSlaActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false, name = "case_sla_activity_id")
    private UUID caseSlaActivityId;

    @ManyToOne
    @JoinColumn(name = "case_id", nullable = false)
    private CaseTransactionModel cases;

    @Column(name = "hop_number_ref")
    private Integer hopNumberRef;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private CaseSlaActivityAction action;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "UTC")
    @Column(name = "actual_duration")
    private Float actualDuration;

    @Column(name = "change_owner_reason")
    private String changeOwnerReason;

    @Column(name = "service_type_matrix_code")
    private String serviceTypeMatrixCode;

    @Column(name = "created_by_name")
    private String createdByName;

    @Column(name = "modified_by_name")
    private String modifiedByName;

    @Column(name = "service_type_matrix_iteration")
    private Integer serviceTypeMatrixIteration;

    @Column(name = "team_name_th")
    private String teamNameTh;

    @Column(name = "team_name_en")
    private String teamNameEn;

    @Column(name = "start_date")
    private ZonedDateTime startDate;

    @Column(name = "end_date")
    private ZonedDateTime endDate;

    @Column(name = "previous_reason_code")
    private String previousReasonCode;

    @Column(name = "previous_reason_comment")
    private String previousReasonComment;

    @Column(name = "previous_reason_value")
    private String previousReasonValue;

    @Column(name = "cancel_reason_code")
    private String cancelReasonCode;

    @Column(name = "cancel_reason_comment")
    private String cancelReasonComment;

    @Column(name = "cancel_reason_value")
    private String cancelReasonValue;

    @Column(name = "resolution_list_code")
    private String resolutionListCode;

    @Column(name = "resolution_list_comment", length = 500)
    private String resolutionListComment;

    @Column(name = "resolution_list_value")
    private String resolutionListValue;

    @Column(name = "root_cause_list_code")
    private String rootCauseListCode;

    @Column(name = "root_cause_list_comment", length = 500)
    private String rootCauseListComment;

    @Column(name = "root_cause_list_value")
    private String rootCauseListValue;

    @Column(name = "change_service_type_matrix_reason_code")
    private String changeServiceTypeMatrixReasonCode;

    @Column(name = "change_service_type_matrix_reason_value")
    private String changeServiceTypeMatrixReasonValue;

    @Column(name = "change_service_type_matrix_reason_comment")
    private String changeServiceTypeMatrixReasonComment;

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
