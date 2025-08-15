package com.ttb.crm.service.migrationdata.model.caseManagement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
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
@Table(name = "case_transaction_document_reference")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class CaseDocumentReferenceModel {
    @Id
    @GeneratedValue
    @Column(nullable = false, name = "document_reference_id")
    private UUID documentReferenceId;

    @Column(nullable = false, name = "emp_ms_doc_type_key")
    private String empMsDocTypeKey;

    @Column(nullable = false, name = "file_name")
    private String fileName;

    @Column(nullable = false, name = "emp_app_id")
    private String empAppId;

    @Column(nullable = false, name = "object_document_id")
    private String objectDocumentId;

    @Column(nullable = false)
    private String repository;

    @ManyToOne
    @JoinColumn(name = "case_id")
    private CaseTransactionModel cases;

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

    public CaseDocumentReferenceModel(CaseTransactionModel caseModel) {
        this.objectDocumentId = caseModel.getObjectId();
        this.repository = caseModel.getRepositoryId();
        this.setCases(caseModel);
    }

}
