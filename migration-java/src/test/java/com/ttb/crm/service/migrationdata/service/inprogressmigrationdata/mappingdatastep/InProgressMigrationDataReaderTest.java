package com.ttb.crm.service.migrationdata.service.inprogressmigrationdata.mappingdatastep;

import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled
class InProgressMigrationDataReaderTest {

    private InProgressMigrationDataReader readerConfig;

    @BeforeEach
    void setUp() {
        readerConfig = new InProgressMigrationDataReader();
    }

    @Test
    void testInProgressMigrationReaderBeanConfiguration() throws Exception {
        DataSource dataSource = new DriverManagerDataSource("jdbc:h2:mem:testdb", "sa", "");

        JdbcPagingItemReader<StgCaseInProgressModel> reader =
                (JdbcPagingItemReader<StgCaseInProgressModel>) readerConfig.inProgressMigrationReader(dataSource);

        assertNotNull(reader, "Reader should not be null");
        assertEquals("jdbcPagingItemReader", reader.getName(), "Reader name should match");
        assertEquals(100, reader.getPageSize(), "Page size should be 100");
    }
}
