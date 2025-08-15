package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.service.migrationdata.bean.request.BatchMigrationDataRequest;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TempLogService {
    private final StgSlaPerOwnerRepository slaPerOwnerRepository;
    private final StgCaseDocumentReferenceRepository stgCaseDocumentReferenceRepository;

    public TempStgCaseInProgressLogModel setTempStgCaseInProgress(TempStgCaseInProgressLogModel tempStgCaseInProgressLogModel, BatchRecordStatusEnum batchRecordStatusEnum, LoadStatus loadStatus, String message) {
        tempStgCaseInProgressLogModel.setBatchRecordStatus(batchRecordStatusEnum);
        tempStgCaseInProgressLogModel.setBatchRecordErrorMessage(message);
        tempStgCaseInProgressLogModel.setLoadStatus(loadStatus.toString());
        tempStgCaseInProgressLogModel.setLoadRemark(message);
        tempStgCaseInProgressLogModel.setLoadLastDatetime(ZonedDateTime.now());
        return tempStgCaseInProgressLogModel;
    }

    public TempUpdateStgInProgress tempUpdateStgInProgress(Long id, LoadStatus loadStatus, String message) {
        var tempUpdateStgInProgress = new TempUpdateStgInProgress();
        tempUpdateStgInProgress.setId(id);
        tempUpdateStgInProgress.setLoadStatus(loadStatus.toString());
        tempUpdateStgInProgress.setLoadRemark(message);
        tempUpdateStgInProgress.setLoadLastDatetime(ZonedDateTime.now());
        return tempUpdateStgInProgress;
    }

    public List<TempStgSlaPerOwnerLogModel> getMatchSlaPerOwnerFromStg(
            List<String> sfIdList,
            List<? extends TempStgCaseInProgressLogModel> items
    ) {
        List<StgSlaPerOwnerModel> stgSlaPerOwnerLists = slaPerOwnerRepository.findAllByCaseCIn(sfIdList);
        return stgSlaPerOwnerLists.stream()
                .map(sfActivity -> {
                    TempStgCaseInProgressLogModel matchedItem = items.stream()
                            .filter(item -> item.getSfId().equals(sfActivity.getCaseC()))
                            .findFirst()
                            .orElse(null);

                    if (matchedItem == null) {
                        return null;
                    }

                    BatchMigrationDataRequest batchRequest = new BatchMigrationDataRequest();
                    batchRequest.setJobId(matchedItem.getJobId());

                    return new TempStgSlaPerOwnerLogModel(
                            sfActivity,
                            batchRequest,
                            matchedItem.getJobExecutionId(),
                            matchedItem.getBatchRecordStatus()
                    );

                })
                .filter(Objects::nonNull)
                .toList();
    }

    public List<TempStgCaseDocumentReferenceLogModel> getMatchCaseDocumentFromStg(
            List<String> sfIdList,
            List<? extends TempStgCaseInProgressLogModel> items
    ) {
        List<StgCaseDocumentReferenceModel> stgCaseDocumentList = stgCaseDocumentReferenceRepository.findAllByCaseCIn(sfIdList);
        return stgCaseDocumentList.stream()
                .map(document -> {
                    TempStgCaseInProgressLogModel matchedItem = items.stream()
                            .filter(item -> item.getSfId().equals(document.getCaseC()))
                            .findFirst()
                            .orElse(null);

                    if (matchedItem == null) {
                        return null;
                    }

                    BatchMigrationDataRequest batchRequest = new BatchMigrationDataRequest();
                    batchRequest.setJobId(matchedItem.getJobId());

                    return new TempStgCaseDocumentReferenceLogModel(
                            document,
                            batchRequest,
                            matchedItem.getJobExecutionId(),
                            matchedItem.getBatchRecordStatus()
                    );

                })
                .filter(Objects::nonNull)
                .toList();
    }

}
