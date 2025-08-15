package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseDocumentReferenceLogModel;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.model.batch.TempStgSlaPerOwnerLogModel;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgCaseDocumentReferenceLogRepository;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgCaseInProgressRepository;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgSlaPerOwnerLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SaveStgToTempService {

    private final TempStgCaseInProgressRepository tempStgCaseInProgressRepository;
    private final TempStgSlaPerOwnerLogRepository tempStgSlaPerOwnerLogRepository;
    private final TempStgCaseDocumentReferenceLogRepository tempStgCaseDocumentReferenceLogRepository;
    private final TempLogService tempLogService;

    public void save(List<? extends TempStgCaseInProgressLogModel> items) {
        List<String> sfIdList = items.stream()
                .map(TempStgCaseInProgressLogModel::getSfId)
                .toList();

        List<TempStgSlaPerOwnerLogModel> slaPerOwnerFromStgList = tempLogService.getMatchSlaPerOwnerFromStg(sfIdList, items);
        List<TempStgCaseDocumentReferenceLogModel> caseDocumentFromStgList = tempLogService.getMatchCaseDocumentFromStg(sfIdList, items);

        tempStgSlaPerOwnerLogRepository.saveAll(slaPerOwnerFromStgList);
        tempStgCaseDocumentReferenceLogRepository.saveAll(caseDocumentFromStgList);
        tempStgCaseInProgressRepository.saveAll(items);
    }
}
