package com.ttb.crm.service.migrationdata.service.cleansuccesstemp;

import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgCaseInProgressRepository;
import com.ttb.crm.service.migrationdata.service.cleansuccessstemp.CleanSuccessReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.data.RepositoryItemReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class CleanSuccessReaderTest {

    @Test
    void testTempStgReaderConfiguration() {
        TempStgCaseInProgressRepository mockRepository = mock(TempStgCaseInProgressRepository.class);
        CleanSuccessReader readerConfig = new CleanSuccessReader();

        RepositoryItemReader<TempStgCaseInProgressLogModel> reader = readerConfig.tempStgReader(mockRepository);

        assertNotNull(reader);
        assertEquals("tempStgReader", reader.getName());
    }
}