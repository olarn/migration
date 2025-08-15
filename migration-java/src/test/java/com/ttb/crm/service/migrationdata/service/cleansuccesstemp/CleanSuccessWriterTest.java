package com.ttb.crm.service.migrationdata.service.cleansuccesstemp;

import com.ttb.crm.service.migrationdata.bean.TempLogData;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseDocumentReferenceLogModel;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.model.batch.TempStgSlaPerOwnerLogModel;
import com.ttb.crm.service.migrationdata.service.CleanTempLogService;
import com.ttb.crm.service.migrationdata.service.cleansuccessstemp.CleanSuccessWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CleanSuccessWriterTest {
    private CleanSuccessWriter writer;
    private CleanTempLogService cleanTempLogService;

    @BeforeEach
    void setUp() {
        cleanTempLogService = mock(CleanTempLogService.class);
        writer = new CleanSuccessWriter(cleanTempLogService);
    }

    @Test
    void testWriteSuccess() throws Exception {
        TempStgCaseInProgressLogModel caseLog = new TempStgCaseInProgressLogModel();
        TempStgSlaPerOwnerLogModel slaLog = new TempStgSlaPerOwnerLogModel();
        TempStgCaseDocumentReferenceLogModel docLog = new TempStgCaseDocumentReferenceLogModel();

        TempLogData logData = new TempLogData();
        logData.setTempStgCaseInProgressLogModel(caseLog);
        logData.setTempStgSlaPerOwnerLogModel(List.of(slaLog));
        logData.setTempStgCaseDocumentReferenceLogModel(List.of(docLog));

        Chunk<TempLogData> chunk = new Chunk<>(List.of(logData));

        writer.write(chunk);

        verify(cleanTempLogService, times(1)).deleteAll(any());
    }

    @Test
    void testWriteThrowsException() throws Exception {
        TempStgCaseInProgressLogModel caseLog = new TempStgCaseInProgressLogModel();
        TempStgSlaPerOwnerLogModel slaLog = new TempStgSlaPerOwnerLogModel();
        TempStgCaseDocumentReferenceLogModel docLog = new TempStgCaseDocumentReferenceLogModel();

        TempLogData logData = new TempLogData();
        logData.setTempStgCaseInProgressLogModel(caseLog);
        logData.setTempStgSlaPerOwnerLogModel(List.of(slaLog));
        logData.setTempStgCaseDocumentReferenceLogModel(List.of(docLog));

        Chunk<TempLogData> chunk = new Chunk<>(List.of(logData));

        doThrow(new RuntimeException("DB error")).when(cleanTempLogService).deleteAll(anyList());

        assertThatThrownBy(() -> writer.write(chunk))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to write items to temp table");
    }
}
