package com.ttb.crm.service.migrationdata.service.inprogressmigrationdata.mappingdatastep;

import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import com.ttb.crm.service.migrationdata.repository.secondary.StgCaseInProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InProgressMigrationDataWriterTest {
    private InProgressMigrationDataWriter writer;
    private StgCaseInProgressRepository stgCaseInProgressRepository;

    @BeforeEach
    void setUp() {
        stgCaseInProgressRepository = mock(StgCaseInProgressRepository.class);
        writer = new InProgressMigrationDataWriter(stgCaseInProgressRepository);
    }

    @Test
    void testWriteSuccess() throws Exception {
        StgCaseInProgressModel item1 = new StgCaseInProgressModel();
        StgCaseInProgressModel item2 = new StgCaseInProgressModel();
        List<StgCaseInProgressModel> items = List.of(item1, item2);
        Chunk<StgCaseInProgressModel> chunk = mock(Chunk.class);
        when(chunk.getItems()).thenReturn(items);

        writer.write(chunk);
        verify(stgCaseInProgressRepository, times(1)).saveAll(items);
    }

    @Test
    void testWriteFailure() {
        StgCaseInProgressModel item = new StgCaseInProgressModel();
        List<StgCaseInProgressModel> items = List.of(item);
        Chunk<StgCaseInProgressModel> chunk = mock(Chunk.class);
        when(chunk.getItems()).thenReturn(items);

        doThrow(new RuntimeException("DB error")).when(stgCaseInProgressRepository).saveAll(items);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> writer.write(chunk));
        assertTrue(exception.getMessage().contains("Failed to write items to temp table"));
    }
}
