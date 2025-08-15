package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.service.migrationdata.enums.LoadStatus;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import com.ttb.crm.service.migrationdata.repository.secondary.StgCaseInProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class StagingService {
    private final StgCaseInProgressRepository stgCaseInprogressRepository;

    public void updateLoadStatusStgCaseInProgress(TempStgCaseInProgressLogModel tempStgCaseInProgressLogModel) {
        StgCaseInProgressModel stg = new StgCaseInProgressModel(tempStgCaseInProgressLogModel);
        stgCaseInprogressRepository.save(stg);
    }

    public StgCaseInProgressModel setTempStgCaseInProgress(StgCaseInProgressModel stgCaseInProgressModel, LoadStatus loadStatus, String message) {
        stgCaseInProgressModel.setLoadStatus(loadStatus.toString());
        stgCaseInProgressModel.setLoadRemark(message);
        stgCaseInProgressModel.setLoadLastDatetime(ZonedDateTime.now());
        return stgCaseInProgressModel;
    }
}
