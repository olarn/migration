package com.ttb.crm.service.migrationdata.service.migrationcompleteddata.completedmappingdatastep;

import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import com.ttb.crm.service.migrationdata.repository.secondary.StgCaseInProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CompletedMappingDataWriter implements ItemWriter<StgCaseInProgressModel> {

//    private final SaveTempToCaseTransactionService saveTempToCaseTransactionService;
    private final StgCaseInProgressRepository stgCaseInProgressRepository;

    @Override
    public void write(Chunk<? extends StgCaseInProgressModel> chunk) throws Exception {
        List<? extends StgCaseInProgressModel> items = chunk.getItems();

        log.info("Writing {} items to temp table", items.size());

        try {
//            saveTempToCaseTransactionService.save(items);
            stgCaseInProgressRepository.saveAll(items);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException("Failed to write items to temp table", e);
        }
    }
}