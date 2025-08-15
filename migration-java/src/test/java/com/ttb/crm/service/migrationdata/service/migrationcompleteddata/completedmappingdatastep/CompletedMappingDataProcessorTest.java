package com.ttb.crm.service.migrationdata.service.migrationcompleteddata.completedmappingdatastep;

import com.ttb.crm.service.migrationdata.enums.BatchRecordStatusEnum;
import com.ttb.crm.service.migrationdata.enums.LoadStatus;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import com.ttb.crm.service.migrationdata.service.StagingService;
import com.ttb.crm.service.migrationdata.service.preparecaesdataservice.CreateCaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Disabled
class CompletedMappingDataProcessorTest {

    private CreateCaseService caseService;
    //    private TempLogService tempLogService;
    private StagingService stagingService;
    private CompletedMappingDataProcessor processor;

    @BeforeEach
    void setUp() {
        caseService = mock(CreateCaseService.class);
//        tempLogService = mock(TempLogService.class);
        stagingService = mock(StagingService.class);
        processor = new CompletedMappingDataProcessor(caseService, stagingService);
    }

    @Test
    void testProcessSuccess() throws Exception {
        StgCaseInProgressModel input = new StgCaseInProgressModel();
        StgCaseInProgressModel expected = new StgCaseInProgressModel();

        when(caseService.createCase(input)).thenReturn(null);
        when(stagingService.setTempStgCaseInProgress(input, LoadStatus.SUCCESS, ""))
                .thenReturn(expected);

        StgCaseInProgressModel result = processor.process(input);

        assertEquals(expected, result);
        verify(caseService).createCase(input);
        verify(stagingService).setTempStgCaseInProgress(input, LoadStatus.SUCCESS, "");
    }

    @Test
    void testProcessFailure() throws Exception {
        StgCaseInProgressModel input = new StgCaseInProgressModel();
        StgCaseInProgressModel expected = new StgCaseInProgressModel();
        String errorMessage = "Simulated failure";

        doThrow(new RuntimeException(errorMessage)).when(caseService).createCase(input);
        when(stagingService.setTempStgCaseInProgress(input, LoadStatus.FAILED, errorMessage))
                .thenReturn(expected);

        StgCaseInProgressModel result = processor.process(input);

        assertEquals(expected, result);
        verify(caseService).createCase(input);
        verify(stagingService).setTempStgCaseInProgress(input, LoadStatus.FAILED, errorMessage);
    }
}
