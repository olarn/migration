package com.ttb.crm.service.migrationdata.service.migrationresolveddata.resolvedtotempstep;

import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.service.SaveStgToTempService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResolvedToTempWriterTest {

    private SaveStgToTempService saveStgToTempService;
    private ResolvedToTempWriter writer;

    @BeforeEach
    void setUp() {
        saveStgToTempService = mock(SaveStgToTempService.class);
        writer = new ResolvedToTempWriter(saveStgToTempService);
    }

    @Test
    void testWriteSuccess() throws Exception {
        TempStgCaseInProgressLogModel item1 = new TempStgCaseInProgressLogModel();
        TempStgCaseInProgressLogModel item2 = new TempStgCaseInProgressLogModel();
        List<TempStgCaseInProgressLogModel> items = Arrays.asList(item1, item2);
        Chunk<TempStgCaseInProgressLogModel> chunk = new Chunk<>(items);

        writer.write(chunk);

        verify(saveStgToTempService, times(1)).save(items);
    }

    @Test
    void testWriteThrowsException() {
        TempStgCaseInProgressLogModel item = new TempStgCaseInProgressLogModel();
        List<TempStgCaseInProgressLogModel> items = List.of(item);
        Chunk<TempStgCaseInProgressLogModel> chunk = new Chunk<>(items);

        doThrow(new RuntimeException("DB error")).when(saveStgToTempService).save(items);

        Exception exception = assertThrows(IllegalStateException.class, () -> writer.write(chunk));
        assertTrue(exception.getMessage().contains("Failed to write items to temp table"));
        verify(saveStgToTempService, times(1)).save(items);
    }
}
