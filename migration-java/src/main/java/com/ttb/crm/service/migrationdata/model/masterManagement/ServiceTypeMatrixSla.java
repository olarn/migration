package com.ttb.crm.service.migrationdata.model.masterManagement;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_type_matrix_sla")
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "serviceTypeMatrixSlaId")
public class ServiceTypeMatrixSla {

    @Id
    @GeneratedValue
    @Column(nullable = false, name = "service_type_matrix_sla_id")
    private UUID serviceTypeMatrixSlaId;

    @ManyToOne
    @JoinColumn(name = "service_type_matrix_id")
    @JsonBackReference
    private ServiceTypeMatrixModel serviceTypeMatrix;

    @Column(name = "hop_number", nullable = false)
    private Integer hopNumber;

    @Column(name = "responsible_bu_id", nullable = false)
    private UUID responsibleBuId;

    @Column(name = "responsible_bu", length = 100)
    private String responsibleBu;

    @Column(name = "sla_target", nullable = true)
    private Float slaTarget;

    @Column(name = "close_by_bu")
    private Boolean closeByBu;

    @Column(name = "sms_code_in_progress")
    private String smsCodeInProgress;

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

    public ServiceTypeMatrixSla(ServiceTypeMatrixSla sla) {
        BeanUtils.copyProperties(sla, this);
    }
}
