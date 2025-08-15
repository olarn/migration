package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgCaseInProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SaveTempToCaseTransactionService {
    private final TempStgCaseInProgressRepository tempStgCaseInProgressRepository;
    private final StagingService stagingService;

    public void save(List<? extends TempStgCaseInProgressLogModel> items) {
        items.forEach(stagingService::updateLoadStatusStgCaseInProgress);
        tempStgCaseInProgressRepository.saveAll(items);
    }
}
