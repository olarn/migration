package com.ttb.crm.service.migrationdata.service.migrationresolveddata.resolvedtotempstep;

import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import com.ttb.crm.service.migrationdata.repository.secondary.StgCaseInProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

//@SpringBootTest(classes = ResolvedToTempReader.class)
class ResolvedToTempReaderTest {

//    @Autowired
//    private ApplicationContext context;

//    @Test
//    void testResolvedStagingReaderBeanCreation() {
//        StgCaseInProgressRepository mockRepository = mock(StgCaseInProgressRepository.class);
//        ResolvedToTempReader readerConfig = new ResolvedToTempReader();
//        RepositoryItemReader<StgCaseInProgressModel> reader = readerConfig.reader(mockRepository);
//
//        assertThat(reader).isNotNull();
//    }

    private StgCaseInProgressRepository repository;
    private ResolvedToTempReader readerConfig;

    @BeforeEach
    void setUp() {
        readerConfig = new ResolvedToTempReader();
    }

    @Test
    void testResolvedToTempReaderBeanConfiguration() throws Exception {

        DataSource dataSource = new DriverManagerDataSource("jdbc:h2:mem:testdb", "sa", "");
        ItemReader<StgCaseInProgressModel> reader = readerConfig.reader(dataSource);

        assertNotNull(reader, "Reader should not be null");
    }
}
