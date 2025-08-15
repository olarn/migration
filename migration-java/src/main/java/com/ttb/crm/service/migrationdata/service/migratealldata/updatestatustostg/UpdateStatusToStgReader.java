package com.ttb.crm.service.migrationdata.service.migratealldata.updatestatustostg;

import com.ttb.crm.service.migrationdata.model.TempUpdateStgInProgressRowMapper;
import com.ttb.crm.service.migrationdata.model.batch.TempUpdateStgInProgress;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static com.ttb.crm.service.migrationdata.helper.Constant.UPDATE_STG_STATUS_CHUNK_SIZE;

@Configuration
public class UpdateStatusToStgReader {
    @Bean(name = "updateStatusBeanToStgReader")
    public ItemReader<TempUpdateStgInProgress> reader(
            @Qualifier("batchDataSource") DataSource dataSource
    ) throws Exception {
        JdbcPagingItemReader<TempUpdateStgInProgress> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setPageSize(UPDATE_STG_STATUS_CHUNK_SIZE);

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT *");
        queryProvider.setFromClause("FROM temp_update_stg_in_progress");
        queryProvider.setSortKey("temp_id");
        reader.setRowMapper(new TempUpdateStgInProgressRowMapper());
        reader.setQueryProvider(queryProvider.getObject());
        reader.setName("tempUpdateStgInProgress");
        return reader;
    }
}
