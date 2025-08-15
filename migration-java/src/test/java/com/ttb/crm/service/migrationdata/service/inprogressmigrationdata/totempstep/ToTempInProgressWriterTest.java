package com.ttb.crm.service.migrationdata.service.inprogressmigrationdata.totempstep;

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
import static org.mockito.Mockito.*;

@Disabled
class ToTempInProgressWriterTest {

    private TempStgCaseInProgressRepository caseRepo;
    private TempStgSlaPerOwnerLogRepository slaLogRepo;
    private TempStgCaseDocumentReferenceLogRepository caseDocRepo;
    private TempLogService tempLogService;
    private ToTempInProgressWriter writer;
    private SaveStgToTempService saveStgToTempService;

    @BeforeEach
    void setUp() {
        caseRepo = mock(TempStgCaseInProgressRepository.class);
        slaLogRepo = mock(TempStgSlaPerOwnerLogRepository.class);
        caseDocRepo = mock(TempStgCaseDocumentReferenceLogRepository.class);
        tempLogService = mock(TempLogService.class);
        saveStgToTempService = mock(SaveStgToTempService.class);
        writer = new ToTempInProgressWriter(saveStgToTempService);
    }

    @Test
    void testWriteSuccess() throws Exception {
        TempStgCaseInProgressLogModel item = new TempStgCaseInProgressLogModel();
        item.setSfId("SF123");
        item.setJobId("JOB1");
        item.setJobExecutionId(1L);
        item.setBatchRecordStatus(com.ttb.crm.service.migrationdata.enums.BatchRecordStatusEnum.SUCCESS);
        item.setClosedStartDatetime("2025-07-25");

        Chunk<TempStgCaseInProgressLogModel> chunk = mock(Chunk.class);
        when(chunk.getItems()).thenReturn(List.of(item));

        StgSlaPerOwnerModel slaModel = new StgSlaPerOwnerModel();
        slaModel.setCaseC("SF123");

        writer.write(chunk);

        verify(slaLogRepo).saveAll(anyList());
        verify(caseDocRepo).saveAll(anyList());
        verify(caseRepo).saveAll(List.of(item));
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

        verify(slaLogRepo, never()).saveAll(anyList());
        verify(caseDocRepo).saveAll(anyList());
        verify(caseRepo, never()).saveAll(anyList());
    }
}
