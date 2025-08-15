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
@Table(name = "stg_case_document_reference")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StgCaseDocumentReferenceModel {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "Case__c")
    private String caseC;

    @Column(name = "Name")
    private String name;

    @Column(name = "ECM_App_ID__c")
    private String ecmAppIDC;

    @Column(name = "File_Name__c")
    private String fileNameC;

    @Column(name = "Object_ID__c")
    private String objectIDC;

    @Column(name = "Repository__c")
    private String repositoryC;

    @Column(name = "Document_Type__c")
    private String documentTypeC;

    @Column(name = "ECM_MS_Doctype_Key__c")
    private String ecmMsDoctypeKeyC;

    @Column(name = "Update_ECM_Message__c")
    private String updateEcmMessageC;

    @Column(name = "Update_ECM_Status__c")
    private String updateEcmStatusC;

    @Column(name = "ECM_Uploaded_By__c")
    private String ecmUploadedByC;

    @Column(name = "ECM_Uploaded_Date_Time__c")
    private String ecmUploadedDateTimeC;

    @Column(name = "CreatedDate")
    private String createdDate;

    @Column(name = "LastModifiedDate")
    private String lastModifiedDate;

    @Column(name = "migration_lot")
    private String migrationLot;

    @Column(name = "record_status")
    private String recordStatus;

    @Column(name = "record_remark")
    private String recordRemark;

    @Column(name = "record_lastdatetime")
    private ZonedDateTime recordLastDatetime;

}
