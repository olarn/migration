package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.service.migrationdata.enums.BatchRecordStatusEnum;
import com.ttb.crm.service.migrationdata.enums.LoadStatus;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import com.ttb.crm.service.migrationdata.repository.secondary.StgCaseInProgressRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StagingServiceTest {
    @InjectMocks
    private StagingService stagingService;

    @Mock
    private StgCaseInProgressRepository stgCaseInprogressRepository;

    @InjectMocks
    private TempLogService tempLogService;


    @Test
    public void updateLoadStatusStgCaseInProgress_success() {
        TempStgCaseInProgressLogModel tempStgCaseInProgressLogModel = new TempStgCaseInProgressLogModel();

        when(stgCaseInprogressRepository.save(any(StgCaseInProgressModel.class))).thenReturn(null);
        assertDoesNotThrow(() -> stagingService.updateLoadStatusStgCaseInProgress(tempStgCaseInProgressLogModel));
    }

    @Test
    public void updateLoadStatusStgCaseInProgress_shouldThrowNullPointerException() {
        TempStgCaseInProgressLogModel tempStgCaseInProgressLogModel = new TempStgCaseInProgressLogModel();

        when(stgCaseInprogressRepository.save(any(StgCaseInProgressModel.class))).thenThrow(new NullPointerException());
        assertThrows(NullPointerException.class,() -> stagingService.updateLoadStatusStgCaseInProgress(tempStgCaseInProgressLogModel));
    }

    @Test
    void setTempStgCaseInProgress_ShouldUpdateAllFields() {
        // Arrange
        TempStgCaseInProgressLogModel model = new TempStgCaseInProgressLogModel();
        BatchRecordStatusEnum batchStatus = BatchRecordStatusEnum.FAILED;
        LoadStatus loadStatus = LoadStatus.FAILED;
        String message = "This is an error message";

        TempStgCaseInProgressLogModel result = tempLogService
                .setTempStgCaseInProgress(model, batchStatus, loadStatus, message);

        assertSame(model, result);

        assertEquals(batchStatus, result.getBatchRecordStatus());
        assertEquals(message, result.getBatchRecordErrorMessage());
        assertEquals(loadStatus.toString(), result.getLoadStatus());
        assertEquals(message, result.getLoadRemark());

        ZonedDateTime now = ZonedDateTime.now();
        assertTrue(!result.getLoadLastDatetime().isBefore(now.minusSeconds(1)) &&
                        !result.getLoadLastDatetime().isAfter(now.plusSeconds(1)),
                "LoadLastDatetime should be close to current time");
    }

    @Test
    void testSetTempStgCaseInProgress_ShouldSetAllFieldsCorrectly() {
        // Arrange
        StgCaseInProgressModel model = new StgCaseInProgressModel();
        LoadStatus status = LoadStatus.SUCCESS;
        String message = "Load completed successfully";

        // Act
        StgCaseInProgressModel result = stagingService.setTempStgCaseInProgress(model, status, message);

        assertSame(model, result, "Should return the same object reference");
        assertEquals(status.toString(), result.getLoadStatus(), "LoadStatus should match enum name");
        assertEquals(message, result.getLoadRemark(), "LoadRemark should match provided message");
        assertNotNull(result.getLoadLastDatetime(), "LoadLastDatetime should not be null");

        ZonedDateTime now = ZonedDateTime.now();
        assertTrue(
                !result.getLoadLastDatetime().isBefore(now.minusSeconds(2)) &&
                        !result.getLoadLastDatetime().isAfter(now.plusSeconds(2)),
                "LoadLastDatetime should be within ±2 seconds of now"
        );
    }
}
