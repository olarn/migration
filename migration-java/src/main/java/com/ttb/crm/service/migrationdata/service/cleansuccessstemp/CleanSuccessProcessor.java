package com.ttb.crm.service.migrationdata.service.cleansuccessstemp;

import com.ttb.crm.service.migrationdata.bean.TempLogData;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgCaseDocumentReferenceLogRepository;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgSlaPerOwnerLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@StepScope
@Component
@Slf4j
@RequiredArgsConstructor
public class CleanSuccessProcessor implements ItemProcessor<TempStgCaseInProgressLogModel, TempLogData> {

    private final TempStgSlaPerOwnerLogRepository tempStgSlaPerOwnerLogRepository;
    private final TempStgCaseDocumentReferenceLogRepository tempStgCaseDocumentReferenceLogRepository;

    @Override
    public TempLogData process(TempStgCaseInProgressLogModel item) {
        TempLogData tempLogData = new TempLogData();
        tempLogData.setTempStgSlaPerOwnerLogModel(tempStgSlaPerOwnerLogRepository.findAllByCaseCOrderByStartDateTimeCAsc(item.getSfId()))
                .setTempStgCaseDocumentReferenceLogModel(
                        tempStgCaseDocumentReferenceLogRepository.findAllByCaseC(item.getSfId())
                );
        tempLogData.setTempStgCaseInProgressLogModel(item);
        return tempLogData;
    }
}

