package com.ttb.crm.service.migrationdata.helper;

import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseDocumentReferenceLogModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MockTempDocumentReference {

    public List<TempStgCaseDocumentReferenceLogModel> mockTempDocumentReference(String caseC) {
        TempStgCaseDocumentReferenceLogModel docRef = new TempStgCaseDocumentReferenceLogModel();
        docRef.setDocumentTypeC("Document Type C")
                .setId(1L)
                .setCaseC(caseC)
                .setName("doc ref name")
                .setEcmAppIDC("ECM App ID C")
                .setFileNameC("File Name C")
                .setEcmUploadedByC("uploadUser")
                .setEcmUploadedDateTimeC("2021-01-01 00:00:00");

        TempStgCaseDocumentReferenceLogModel docRef2 = new TempStgCaseDocumentReferenceLogModel();
        docRef2.setDocumentTypeC("Document Type C")
                .setId(1L)
                .setCaseC(caseC)
                .setName("doc ref name")
                .setEcmAppIDC("ECM App ID C")
                .setFileNameC("File Name C")
                .setEcmUploadedByC("uploadUser")
                .setEcmUploadedDateTimeC(null);

        return List.of(docRef);
    }
}
