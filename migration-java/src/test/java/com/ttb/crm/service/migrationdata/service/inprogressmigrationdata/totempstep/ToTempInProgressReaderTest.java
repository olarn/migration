package com.ttb.crm.service.migrationdata.service.inprogressmigrationdata.totempstep;

import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

class ToTempInProgressReaderTest {

    private ToTempInProgressReader readerConfig;

    @BeforeEach
    void setUp() {
        readerConfig = new ToTempInProgressReader();
    }

    @Test
    void testToTempInProgressReaderBeanConfiguration() throws Exception {
        DataSource dataSource = new DriverManagerDataSource("jdbc:h2:mem:testdb", "sa", "");

        ItemReader<StgCaseInProgressModel> reader = readerConfig.reader(dataSource);

        assertNotNull(reader, "Reader should not be null");
    }
}
