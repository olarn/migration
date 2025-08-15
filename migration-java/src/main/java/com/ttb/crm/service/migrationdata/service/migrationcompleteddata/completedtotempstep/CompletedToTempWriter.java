package com.ttb.crm.service.migrationdata.service.migrationcompleteddata.completedtotempstep;

import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.service.SaveStgToTempService;
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
public class CompletedToTempWriter implements ItemWriter<TempStgCaseInProgressLogModel> {
    private final SaveStgToTempService saveStgToTempService;

    @Override
    public void write(Chunk<? extends TempStgCaseInProgressLogModel> chunk) throws Exception {
        List<? extends TempStgCaseInProgressLogModel> items = chunk.getItems();

        log.info("Writing {} items to temp table", items.size());
        try {
            saveStgToTempService.save(items);
        } catch (Exception e) {
            log.error("Failed to write items to temp table", e);
            throw new IllegalStateException("Failed to write items to temp table", e);
        }
    }
}