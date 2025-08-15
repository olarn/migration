package com.ttb.crm.service.migrationdata.service.migrationresolveddata.resolvedtotempstep;

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
public class ResolvedToTempReader {

    @Bean(name = "resolvedStagingReader")
    @StepScope
//    public RepositoryItemReader<StgCaseInProgressModel> reader(StgCaseInProgressRepository repository) {
    public ItemReader<StgCaseInProgressModel> reader(@Qualifier("secondaryDataSource") DataSource dataSource) throws Exception {
//        Specification<StgCaseInProgressModel> spec = StgCaseInProgressSpecification.combineSpecifications(
//                StgCaseInProgressSpecification.filterStatusCode(List.of(Constant.CASE_STATUS_RESOLVED)),
//                StgCaseInProgressSpecification.filterLoadStatus(Arrays.asList("", " ", null)),
//                StgCaseInProgressSpecification.filterContactPersonEmailC("nattawat"),
//                StgCaseInProgressSpecification.filterLoadStatus(Arrays.asList("test_resolved")),
//                StgCaseInProgressSpecification.filterRecordStatus(Constant.RECORD_STATUS_SUCCESS)
//        );

//        return new RepositoryItemReaderBuilder<StgCaseInProgressModel>()
//                .name("resolvedStagingReader")
//                .repository(repository)
//                .methodName("findAll")
//                .arguments(List.of(spec))
//                .pageSize(100)
//                .sorts(Map.of("id", Sort.Direction.ASC))
//                .build();

        JdbcPagingItemReader<StgCaseInProgressModel> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setPageSize(100);

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT *");
        queryProvider.setFromClause("FROM stg_case_inprogress");
        queryProvider.setWhereClause("WHERE Status_Code = 'RESOLVED' AND record_status = 'Success' AND (load_status IN ('', ' ') or load_status IS NULL)");
        queryProvider.setSortKey("Id");

        reader.setQueryProvider(queryProvider.getObject());
        reader.setRowMapper(new StgCaseInProgressRowMapper());
        reader.setName("resolvedStagingReader");
        return reader;
    }
}
