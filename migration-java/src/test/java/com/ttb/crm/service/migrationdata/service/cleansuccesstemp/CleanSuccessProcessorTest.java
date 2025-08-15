package com.ttb.crm.service.migrationdata.service.cleansuccesstemp;

import com.ttb.crm.service.migrationdata.bean.TempLogData;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseDocumentReferenceLogModel;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.model.batch.TempStgSlaPerOwnerLogModel;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgCaseDocumentReferenceLogRepository;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgSlaPerOwnerLogRepository;
import com.ttb.crm.service.migrationdata.service.cleansuccessstemp.CleanSuccessProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CleanSuccessProcessorTest {

    private TempStgSlaPerOwnerLogRepository slaLogRepository;
    private TempStgCaseDocumentReferenceLogRepository caseDocRepo;
    private CleanSuccessProcessor processor;

    @BeforeEach
    void setUp() {
        slaLogRepository = mock(TempStgSlaPerOwnerLogRepository.class);
        caseDocRepo = mock(TempStgCaseDocumentReferenceLogRepository.class);
        processor = new CleanSuccessProcessor(slaLogRepository, caseDocRepo);
    }

    @Test
    void testProcessReturnsCorrectTempLogData() {
        TempStgCaseInProgressLogModel inputModel = new TempStgCaseInProgressLogModel();
        inputModel.setSfId("001");

        List<TempStgSlaPerOwnerLogModel> mockSlaLogs = List.of(new TempStgSlaPerOwnerLogModel());
        when(slaLogRepository.findAllByCaseCOrderByStartDateTimeCAsc("001")).thenReturn(mockSlaLogs);
        List<TempStgCaseDocumentReferenceLogModel> mockDocLogs = List.of(new TempStgCaseDocumentReferenceLogModel());
        when(caseDocRepo.findAllByCaseC("001")).thenReturn(mockDocLogs);

        TempLogData result = processor.process(inputModel);

        assertThat(result).isNotNull();
        assertThat(result.getTempStgCaseInProgressLogModel()).isEqualTo(inputModel);
        assertThat(result.getTempStgSlaPerOwnerLogModel()).isEqualTo(mockSlaLogs);
        assertThat(result.getTempStgCaseDocumentReferenceLogModel()).isEqualTo(mockDocLogs);
    }
}
