package com.ttb.crm.service.migrationdata.service;

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

public class SaveStgToTempServiceTest {

    @Mock
    private TempStgCaseInProgressRepository caseInProgressRepository;

    @Mock
    private TempStgSlaPerOwnerLogRepository slaPerOwnerLogRepository;

    @Mock
    private TempStgCaseDocumentReferenceLogRepository caseDocumentReferenceLogRepository;

    @Mock
    private TempLogService tempLogService;

    @InjectMocks
    private SaveStgToTempService saveStgToTempService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSave() {
        TempStgCaseInProgressLogModel item = new TempStgCaseInProgressLogModel();
        item.setSfId("SF123");

        List<TempStgCaseInProgressLogModel> items = List.of(item);

        List<TempStgSlaPerOwnerLogModel> slaList = List.of(new TempStgSlaPerOwnerLogModel());
        List<TempStgCaseDocumentReferenceLogModel> docList = List.of(new TempStgCaseDocumentReferenceLogModel());

        when(tempLogService.getMatchSlaPerOwnerFromStg(anyList(), eq(items))).thenReturn(slaList);
        when(tempLogService.getMatchCaseDocumentFromStg(anyList(), eq(items))).thenReturn(docList);

        saveStgToTempService.save(items);

        verify(slaPerOwnerLogRepository).saveAll(slaList);
        verify(caseDocumentReferenceLogRepository).saveAll(docList);
        verify(caseInProgressRepository).saveAll(items);
    }
}

