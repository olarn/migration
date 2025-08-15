package com.ttb.crm.service.migrationdata.service.migrationcompleteddata.completedtotempstep;

import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgCaseDocumentReferenceLogRepository;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgCaseInProgressRepository;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgSlaPerOwnerLogRepository;
import com.ttb.crm.service.migrationdata.service.SaveStgToTempService;
import com.ttb.crm.service.migrationdata.service.TempLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled
class CompletedToTempWriterTest {

    private TempStgCaseInProgressRepository caseRepo;
    private TempStgSlaPerOwnerLogRepository slaLogRepo;
    private TempStgCaseDocumentReferenceLogRepository caseDocRepo;
    private TempLogService tempLogService;
    private SaveStgToTempService saveStgToTempService;

    private CompletedToTempWriter writer;

    @BeforeEach
    void setUp() {
        caseRepo = mock(TempStgCaseInProgressRepository.class);
        slaLogRepo = mock(TempStgSlaPerOwnerLogRepository.class);
        caseDocRepo = mock(TempStgCaseDocumentReferenceLogRepository.class);
        tempLogService = mock(TempLogService.class);
        saveStgToTempService = mock(SaveStgToTempService.class);
        writer = new CompletedToTempWriter(saveStgToTempService);
    }

    @Test
    void testWriteSuccess() throws Exception {
        TempStgCaseInProgressLogModel item = new TempStgCaseInProgressLogModel();
        item.setSfId("SF123");
        item.setJobId("JOB001");
        item.setJobExecutionId(1L);
        item.setClosedStartDatetime("2025-07-25");

        Chunk<TempStgCaseInProgressLogModel> chunk = mock(Chunk.class);
        when(chunk.getItems()).thenReturn(List.of(item));

        StgSlaPerOwnerModel slaModel = new StgSlaPerOwnerModel();
        slaModel.setCaseC("SF123");

        writer.write(chunk);
        verify(saveStgToTempService, times(1)).save(List.of(item));
    }

    @Test
    void testWriteFailure() {
        TempStgCaseInProgressLogModel item = new TempStgCaseInProgressLogModel();
        item.setSfId("SF123");
        item.setClosedStartDatetime("2025-07-25");

        Chunk<TempStgCaseInProgressLogModel> chunk = mock(Chunk.class);
        when(chunk.getItems()).thenReturn(List.of(item));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> writer.write(chunk));
        assertTrue(exception.getMessage().contains("Failed to write items to temp table"));
    }
}
