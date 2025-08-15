package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgCaseInProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.mockito.Mockito.*;

public class SaveTempToCaseTransactionServiceTest {

    @Mock
    private TempStgCaseInProgressRepository caseInProgressRepository;

    @Mock
    private StagingService stagingService;

    @InjectMocks
    private SaveTempToCaseTransactionService saveTempToCaseTransactionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSave() {
        TempStgCaseInProgressLogModel item1 = new TempStgCaseInProgressLogModel();
        TempStgCaseInProgressLogModel item2 = new TempStgCaseInProgressLogModel();
        List<TempStgCaseInProgressLogModel> items = List.of(item1, item2);

        saveTempToCaseTransactionService.save(items);

        verify(stagingService).updateLoadStatusStgCaseInProgress(item1);
        verify(stagingService).updateLoadStatusStgCaseInProgress(item2);
        verify(caseInProgressRepository).saveAll(items);
    }
}
