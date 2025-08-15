package com.ttb.crm.service.migrationdata.service.inprogressmigrationdata.totempstep;

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
public class ToTempInProgressReader {

    @Bean(name = "stagingInProgressReader")
    @StepScope
    public ItemReader<StgCaseInProgressModel> reader(@Qualifier("secondaryDataSource") DataSource dataSource) throws Exception {
        JdbcPagingItemReader<StgCaseInProgressModel> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setPageSize(100);

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT *");
        queryProvider.setFromClause("FROM stg_case_inprogress");
        queryProvider.setWhereClause("WHERE migration_lot = '202507_3' AND Status_Code IN ('NEW', 'IN_PROGRESS') AND record_status = 'Success' AND (load_status IN ('', ' ') or load_status IS NULL)");
        queryProvider.setSortKey("Id");

        reader.setQueryProvider(queryProvider.getObject());
        reader.setRowMapper(new StgCaseInProgressRowMapper());
        reader.setName("stagingInProgressReader");
        return reader;
    }
}
