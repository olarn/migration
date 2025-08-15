package com.ttb.crm.service.migrationdata.service.migrationcompleteddata.completedtotempstep;

import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import com.ttb.crm.service.migrationdata.repository.secondary.StgCaseInProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

class CompletedToTempReaderTest {

    private StgCaseInProgressRepository repository;
    private CompletedToTempReader readerConfig;

//    @BeforeEach
//    void setUp() {
//        repository = mock(StgCaseInProgressRepository.class);
//        readerConfig = new CompletedToTempReader();
//    }
//
//    @Test
//    void testReaderBeanConfiguration() {
//        RepositoryItemReader<StgCaseInProgressModel> reader = readerConfig.reader(repository);
//
//        assertNotNull(reader, "Reader should not be null");
//        assertEquals("completedStagingReader", reader.getName(), "Reader name should match");
//    }

    @BeforeEach
    void setUp() {
        readerConfig = new CompletedToTempReader();
    }

    @Test
    void testCompletedToTempReaderBeanConfiguration() throws Exception {
        DataSource dataSource = new DriverManagerDataSource("jdbc:h2:mem:testdb", "sa", "");

        ItemReader<StgCaseInProgressModel> reader = readerConfig.reader(dataSource);

        assertNotNull(reader, "Reader should not be null");
    }
}
