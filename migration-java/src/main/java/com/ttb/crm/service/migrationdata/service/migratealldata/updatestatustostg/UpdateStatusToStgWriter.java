package com.ttb.crm.service.migrationdata.service.migratealldata.updatestatustostg;

import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressUpdateStatusModel;
import com.ttb.crm.service.migrationdata.repository.secondary.StgCaseInProgressUpdateStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@StepScope
public class UpdateStatusToStgWriter implements ItemWriter<StgCaseInProgressUpdateStatusModel> {
    private final StgCaseInProgressUpdateStatusRepository stgCaseInProgressUpdateStatusRepository;

    @Override
    public void write(Chunk<? extends StgCaseInProgressUpdateStatusModel> chunk) throws Exception {
        List<? extends StgCaseInProgressUpdateStatusModel> items = chunk.getItems();

        try {
            stgCaseInProgressUpdateStatusRepository.saveAll(items);
        } catch (Exception e) {
            log.error("Failed to write items to temp table", e);
            throw new IllegalStateException("Failed to write items to temp table", e);
        }
    }
}
