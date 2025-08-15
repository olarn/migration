package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.service.migrationdata.bean.TempLogData;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseDocumentReferenceLogModel;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.model.batch.TempStgSlaPerOwnerLogModel;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgCaseDocumentReferenceLogRepository;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgCaseInProgressRepository;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgSlaPerOwnerLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.mockito.Mockito.*;

public class CleanTempLogServiceTest {

    @Mock
    private TempStgCaseInProgressRepository caseInProgressRepository;

    @Mock
    private TempStgSlaPerOwnerLogRepository slaPerOwnerLogRepository;

    @Mock
    private TempStgCaseDocumentReferenceLogRepository caseDocumentReferenceLogRepository;

    @InjectMocks
    private CleanTempLogService cleanTempLogService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testDeleteAll() throws Exception {
        TempStgCaseInProgressLogModel caseLog = new TempStgCaseInProgressLogModel();
        TempStgSlaPerOwnerLogModel slaLog = new TempStgSlaPerOwnerLogModel();
        TempStgCaseDocumentReferenceLogModel docLog = new TempStgCaseDocumentReferenceLogModel();

        TempLogData tempLogData = mock(TempLogData.class);
        when(tempLogData.getTempStgCaseInProgressLogModel()).thenReturn(caseLog);
        when(tempLogData.getTempStgSlaPerOwnerLogModel()).thenReturn(List.of(slaLog));
        when(tempLogData.getTempStgCaseDocumentReferenceLogModel()).thenReturn(List.of(docLog));

        List<TempLogData> items = List.of(tempLogData);

        cleanTempLogService.deleteAll(items);

        verify(caseInProgressRepository).deleteAll(List.of(caseLog));
        verify(slaPerOwnerLogRepository).deleteAll(List.of(slaLog));
        verify(caseDocumentReferenceLogRepository).deleteAll(List.of(docLog));
    }
}
