package com.ttb.crm.service.migrationdata.service.migratealldata.stgtocase;

import com.ttb.crm.service.migrationdata.bean.StgToCaseWriterDTO;
import com.ttb.crm.service.migrationdata.bean.response.CaseCaptureEventDTO;
import com.ttb.crm.service.migrationdata.model.batch.TempUpdateStgInProgress;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseTransactionModel;
import com.ttb.crm.service.migrationdata.repository.batch.TempUpdateStgInProgressRepository;
import com.ttb.crm.service.migrationdata.repository.caseManagement.CaseTransactionRepository;
import com.ttb.crm.service.migrationdata.service.CaseMovementDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@StepScope
public class StgToCaseWriter implements ItemWriter<StgToCaseWriterDTO> {
    private final CaseTransactionRepository caseRepository;
    private final TempUpdateStgInProgressRepository tempUpdateStgInProgressRepository;
    private final CaseMovementDataService caseMovementDataService;

    @Override
    public void write(Chunk<? extends StgToCaseWriterDTO> chunk) throws Exception {
        List<? extends StgToCaseWriterDTO> items = chunk.getItems();
        try {
            Map<String, CaseCaptureEventDTO> caseCaptureEventDTOMap = new HashMap<>();
            List<CaseTransactionModel> caseChunk = new ArrayList<>();
            List<TempUpdateStgInProgress> tempStgStausChunk = new ArrayList<>();
            for (StgToCaseWriterDTO item : items) {
                tempStgStausChunk.add(item.getTempUpdateStgInProgress());
                if (item.getCaseTransaction() != null) {
                    caseCaptureEventDTOMap.put(item.getCaseTransaction().getCaseNumber(), item.getDataForCaseMovement());
                    caseChunk.add(item.getCaseTransaction());
                }
            }
            tempUpdateStgInProgressRepository.saveAll(tempStgStausChunk);
            List<CaseTransactionModel> caseTranSave = caseRepository.saveAll(caseChunk);
            caseMovementDataService.pushAllCaseToEventHub(caseTranSave, caseCaptureEventDTOMap);
        } catch (Exception e) {
            log.error("Failed to write items to temp table", e);
            throw new IllegalStateException("Failed to write items to temp table", e);
        }
    }


}
