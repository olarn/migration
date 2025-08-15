package com.ttb.crm.service.migrationdata.service.migrationcompleteddata.completedmappingdatastep;

import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
class CompletedMappingDataReaderTest {

    private CompletedMappingDataReader readerConfig;

    @BeforeEach
    void setUp() {
        readerConfig = new CompletedMappingDataReader();
    }

    @Test
    void testCompletedMappingReaderConfiguration() throws Exception {
        DataSource dataSource = new DriverManagerDataSource("jdbc:h2:mem:testdb", "sa", "");

        JdbcPagingItemReader<StgCaseInProgressModel> reader =
                (JdbcPagingItemReader<StgCaseInProgressModel>) readerConfig.mappingReader(dataSource);

        assertNotNull(reader, "Reader should not be null");
        assertEquals("jdbcPagingCompletedMappingItemReader", reader.getName(), "Reader name should match");
        assertEquals(50, reader.getPageSize(), "Page size should be 50");
    }
}
