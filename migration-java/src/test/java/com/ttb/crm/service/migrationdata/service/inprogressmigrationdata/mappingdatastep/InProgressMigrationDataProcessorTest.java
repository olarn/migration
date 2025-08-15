package com.ttb.crm.service.migrationdata.service.inprogressmigrationdata.mappingdatastep;

import com.ttb.crm.service.migrationdata.enums.LoadStatus;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import com.ttb.crm.service.migrationdata.service.StagingService;
import com.ttb.crm.service.migrationdata.service.preparecaesdataservice.CreateCaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class InProgressMigrationDataProcessorTest {

    private CreateCaseService createCaseService;
    //    private TempLogService tempLogService;
    private StagingService stagingService;
    private InProgressMigrationDataProcessor processor;

    @BeforeEach
    void setUp() {
        createCaseService = mock(CreateCaseService.class);
//        tempLogService = mock(TempLogService.class);
        stagingService = mock(StagingService.class);
        processor = new InProgressMigrationDataProcessor(createCaseService, stagingService);
    }

    @Test
    void testProcess_Success() throws Exception {
        StgCaseInProgressModel input = new StgCaseInProgressModel();
        StgCaseInProgressModel expected = new StgCaseInProgressModel();

        when(createCaseService.createCase(any())).thenReturn(null);
        when(stagingService.setTempStgCaseInProgress(input, LoadStatus.SUCCESS, ""))
                .thenReturn(expected);

        StgCaseInProgressModel result = processor.process(input);

        assertEquals(expected, result);
        verify(createCaseService).createCase(input);
        verify(stagingService).setTempStgCaseInProgress(input, LoadStatus.SUCCESS, "");
    }

    @Test
    @Disabled
    void testProcess_Failure() throws Exception {
        StgCaseInProgressModel input = new StgCaseInProgressModel();
        StgCaseInProgressModel expected = new StgCaseInProgressModel();
        String errorMessage = "Creation failed";

        doThrow(new RuntimeException(errorMessage)).when(createCaseService).createCase(input);
        when(stagingService.setTempStgCaseInProgress(input, LoadStatus.FAILED, errorMessage))
                .thenReturn(expected);

        StgCaseInProgressModel result = processor.process(input);

        assertEquals(expected, result);
        verify(createCaseService).createCase(input);
        verify(stagingService).setTempStgCaseInProgress(input, LoadStatus.FAILED, errorMessage);
    }
}
