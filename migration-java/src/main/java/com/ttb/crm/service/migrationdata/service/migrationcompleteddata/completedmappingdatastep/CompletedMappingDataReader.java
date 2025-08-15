package com.ttb.crm.service.migrationdata.service.migrationcompleteddata.completedmappingdatastep;

import com.ttb.crm.service.migrationdata.model.StgCaseInProgressRowMapper;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class CompletedMappingDataReader {

    @Bean(name = "completedMappingReader")
    @StepScope
    public ItemReader<StgCaseInProgressModel> mappingReader(
//            TempStgCaseInProgressRepository tempStgCaseInProgressRepository,
            @Qualifier("secondaryDataSource") DataSource dataSource) throws Exception {

//        Specification<TempStgCaseInProgressLogModel> spec = TempStgCaseInProgressSpecification.combineSpecifications(
//                TempStgCaseInProgressSpecification.filterStatusCode(List.of(CaseStatus.COMPLETED.toString())),
//                TempStgCaseInProgressSpecification.filterLoadStatus(Arrays.asList("", " ", null)),
//                TempStgCaseInProgressSpecification.filterTempStatus(List.of(PENDING.toString()))
//        );
//        return new RepositoryItemReaderBuilder<TempStgCaseInProgressLogModel>()
//                .name("completedMappingReader")
//                .repository(tempstgCaseInProgressRepository)
//                .methodName("findAll")
//                .arguments(List.of(spec))
//                .pageSize(500)
//                .sorts(Map.of("id", Sort.Direction.ASC))
//                .build();

        JdbcPagingItemReader<StgCaseInProgressModel> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setPageSize(500);

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT *");
        queryProvider.setFromClause("FROM stg_case_inprogress");
        queryProvider.setWhereClause(
                "WHERE record_status = 'Success' AND status_code = 'COMPLETED' " +
                        "AND (load_status IS NULL OR load_status = '' OR load_status = ' ') " +
                        "AND migration_lot IN ('202507_2')"
        );
//        queryProvider.setWhereClause("WHERE batch_record_status = 'PENDING' AND Status_Code = 'COMPLETED' AND load_status = 'TEST'");
        queryProvider.setSortKey("Id");

        reader.setQueryProvider(queryProvider.getObject());
        reader.setRowMapper(new StgCaseInProgressRowMapper());
        reader.setName ("jdbcPagingCompletedMappingItemReader");
        return reader;
    }
}
