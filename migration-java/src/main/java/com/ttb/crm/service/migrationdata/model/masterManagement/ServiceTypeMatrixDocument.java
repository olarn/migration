package com.ttb.crm.service.migrationdata.model.masterManagement;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_type_matrix_document")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "serviceTypeMatrixDocumentId")
public class ServiceTypeMatrixDocument {
    @Id
    @GeneratedValue
    @Column(nullable = false, name = "service_type_matrix_document_id")
    private UUID serviceTypeMatrixDocumentId;

    @ManyToOne
    @JoinColumn(name = "service_type_matrix_id")
    @JsonBackReference
    private ServiceTypeMatrixModel serviceTypeMatrix;

    @Column(nullable = false, length = 50, name = "ecm_code")
    private String ecmCode;

    @Column(nullable = false, length = 300, name = "ecm_document_name")
    private String ecmDocumentName;

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