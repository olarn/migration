package com.ttb.crm.service.migrationdata.service.cleansuccessstemp;

import com.ttb.crm.service.migrationdata.bean.TempLogData;
import com.ttb.crm.service.migrationdata.service.CleanTempLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CleanSuccessWriter implements ItemWriter<TempLogData> {
    private final CleanTempLogService cleanTempLogService;

    @Override
    public void write(Chunk<? extends TempLogData> chunk) throws Exception {
        List<? extends TempLogData> items = chunk.getItems();

        log.info("Writing {} items to temp table", items.size());

        try {
            cleanTempLogService.deleteAll(items);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException("Failed to write items to temp table", e);
        }
    }
}