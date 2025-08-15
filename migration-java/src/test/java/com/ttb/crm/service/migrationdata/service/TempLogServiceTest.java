package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.service.migrationdata.enums.BatchRecordStatusEnum;
import com.ttb.crm.service.migrationdata.enums.LoadStatus;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseDocumentReferenceLogModel;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.model.batch.TempStgSlaPerOwnerLogModel;
import com.ttb.crm.service.migrationdata.model.batch.TempUpdateStgInProgress;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseDocumentReferenceModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import com.ttb.crm.service.migrationdata.repository.secondary.StgCaseDocumentReferenceRepository;
import com.ttb.crm.service.migrationdata.repository.secondary.StgSlaPerOwnerRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TempLogServiceTest {

    @Mock
    private StgSlaPerOwnerRepository slaPerOwnerRepository;

    @Mock
    private StgCaseDocumentReferenceRepository stgCaseDocumentReferenceRepository;

    @InjectMocks
    private TempLogService tempLogService;

    @Test
    public void testSetTempStgCaseInProgress_setsFieldsCorrectly() {
        TempStgCaseInProgressLogModel model = new TempStgCaseInProgressLogModel();
        String message = "Test message";

        TempStgCaseInProgressLogModel result = tempLogService.setTempStgCaseInProgress(
                model,
                BatchRecordStatusEnum.SUCCESS,
                LoadStatus.SUCCESS,
                message
        );

        assertEquals(BatchRecordStatusEnum.SUCCESS, result.getBatchRecordStatus());
        assertEquals(message, result.getBatchRecordErrorMessage());
        assertEquals(LoadStatus.SUCCESS.toString(), result.getLoadStatus());
        assertEquals(message, result.getLoadRemark());
        assertNotNull(result.getLoadLastDatetime());
    }

    @Test
    void testGetMatchSlaPerOwnerFromStg_returnsMatchedModels() {
        String sfId = "SF123";
        String jobId = "JOB001";
        Long jobExecutionId = 110L;

        TempStgCaseInProgressLogModel item = new TempStgCaseInProgressLogModel();
        item.setSfId(sfId);
        item.setJobId(jobId);
        item.setJobExecutionId(jobExecutionId);
        item.setBatchRecordStatus(BatchRecordStatusEnum.SUCCESS);

        StgSlaPerOwnerModel stgModel = new StgSlaPerOwnerModel();
        stgModel.setCaseC(sfId);

        when(slaPerOwnerRepository.findAllByCaseCIn(List.of(sfId))).thenReturn(List.of(stgModel));

        List<TempStgSlaPerOwnerLogModel> result = tempLogService.getMatchSlaPerOwnerFromStg(
                List.of(sfId),
                List.of(item)
        );

        assertEquals(1, result.size());
        TempStgSlaPerOwnerLogModel logModel = result.get(0);
        assertEquals(jobId, logModel.getJobId());
        assertEquals(jobExecutionId, logModel.getJobExecutionId());
        assertEquals(BatchRecordStatusEnum.SUCCESS, logModel.getBatchRecordStatus());
    }

    @Test
    void testGetMatchCaseDocumentFromStg_returnsMatchedModels() {
        String sfId = "SF456";
        String jobId = "JOB002";
        Long jobExecutionId = 101L;

        TempStgCaseInProgressLogModel item = new TempStgCaseInProgressLogModel();
        item.setSfId(sfId);
        item.setJobId(jobId);
        item.setJobExecutionId(jobExecutionId);
        item.setBatchRecordStatus(BatchRecordStatusEnum.FAILED);

        StgCaseDocumentReferenceModel docModel = new StgCaseDocumentReferenceModel();
        docModel.setCaseC(sfId);

        when(stgCaseDocumentReferenceRepository.findAllByCaseCIn(List.of(sfId))).thenReturn(List.of(docModel));

        List<TempStgCaseDocumentReferenceLogModel> result = tempLogService.getMatchCaseDocumentFromStg(
                List.of(sfId),
                List.of(item)
        );

        assertEquals(1, result.size());
        TempStgCaseDocumentReferenceLogModel logModel = result.get(0);
        assertEquals(jobId, logModel.getJobId());
        assertEquals(jobExecutionId, logModel.getJobExecutionId());
        assertEquals(BatchRecordStatusEnum.FAILED, logModel.getBatchRecordStatus());
    }

    @Test
    void tempUpdateStgInProgress_ShouldReturnCorrectObject() {
        Long id = 123L;
        LoadStatus loadStatus = LoadStatus.SUCCESS;
        String message = "Test message";

        TempUpdateStgInProgress result = tempLogService.tempUpdateStgInProgress(id, loadStatus, message);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(loadStatus.toString(), result.getLoadStatus());
        assertEquals(message, result.getLoadRemark());

        ZonedDateTime now = ZonedDateTime.now();
        assertTrue(!result.getLoadLastDatetime().isBefore(now.minusSeconds(1)) &&
                        !result.getLoadLastDatetime().isAfter(now.plusSeconds(1)),
                "LoadLastDatetime should be close to current time");
    }

}
