package com.ttb.crm.service.migrationdata.service.migratealldata.stgtocase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttb.crm.service.migrationdata.bean.request.BatchMigrationDataRequest;
import com.ttb.crm.service.migrationdata.constants.CommonConstants;
import com.ttb.crm.service.migrationdata.model.StgCaseInProgressRowMapper;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ttb.crm.service.migrationdata.helper.Constant.STG_TO_CASE_CHUNK_SIZE;

@Slf4j
@Configuration
public class StgToCaseReader {

    @Bean(name = "stagingDataToCaseReader")
    @StepScope
    public ItemReader<StgCaseInProgressModel> reader(
            @Qualifier("secondaryDataSource") DataSource dataSource,
            @Value("#{jobParameters['batchRequest']}") String batchRequestJson
    ) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        BatchMigrationDataRequest batchRequest =
                objectMapper.readValue(batchRequestJson, BatchMigrationDataRequest.class);
        Map<String, Object> extraData = batchRequest.getExtraData();

        Object migrationLotObj = extraData.get(CommonConstants.MIGRATION_LOT);
        List<String> migrationLots = new ArrayList<>();

        if (migrationLotObj instanceof List<?> migrationList) {
            migrationLots = migrationList.stream()
                    .map(Object::toString)
                    .toList();
        }

        Object caseStatusObj = extraData.get(CommonConstants.CASE_STATUS_CODE);
        List<String> caseStatus = new ArrayList<>();

        if (caseStatusObj instanceof List<?> caseStatusList) {
            caseStatus = caseStatusList.stream()
                    .map(Object::toString)
                    .toList();
        }

        String whereClause = "WHERE record_status IN ('Success') " +
                "AND (load_status IN ('', ' ') OR load_status IS NULL) AND Status_Code = 'COMPLETED'";

//        String whereClause = "WHERE record_status IN ('Success') " +
//                "AND load_status IN 'test";


//        if (!caseStatus.isEmpty()) {
//            String inClause = caseStatus.stream()
//                    .map(lot -> "'" + lot.replace("'", "''") + "'")
//                    .collect(Collectors.joining(","));
//            whereClause += " AND status_code IN (" + inClause + ")";
//        }
//
//        if (!migrationLots.isEmpty()) {
//            String inClause = migrationLots.stream()
//                    .map(lot -> "'" + lot.replace("'", "''") + "'")
//                    .collect(Collectors.joining(","));
//            whereClause += " AND migration_lot IN (" + inClause + ")";
//        }

        JdbcPagingItemReader<StgCaseInProgressModel> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setPageSize(STG_TO_CASE_CHUNK_SIZE);

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT *");
        queryProvider.setFromClause("FROM stg_case_inprogress");
        queryProvider.setWhereClause(whereClause);
        queryProvider.setSortKey("Id");

        reader.setQueryProvider(queryProvider.getObject());
        reader.setRowMapper(new StgCaseInProgressRowMapper());
        reader.setName("stagingDataToCaseReader");
        return reader;
    }

    private String buildWhereClause(String customWhereClause, Map<String, Object> extraData, List<String> migrationLots) {
        StringBuilder whereClause = new StringBuilder();

        if (customWhereClause != null && !customWhereClause.trim().isEmpty()) {
            whereClause.append("WHERE ").append(customWhereClause);
        }

        if (!migrationLots.isEmpty()) {
            String inClause = migrationLots.stream()
                    .map(lot -> "'" + lot.replace("'", "''") + "'")
                    .collect(Collectors.joining(","));
            whereClause.append(" AND migration_lot IN (").append(inClause).append(")");
        }

        addExtraConditions(whereClause, extraData);

        log.debug("Generated WHERE clause: {}", whereClause.toString());
        return whereClause.toString();
    }

    private void addExtraConditions(StringBuilder whereClause, Map<String, Object> extraData) {
        if (extraData.containsKey("status")) {
            Object status = extraData.get("status");
            if (status instanceof String statusStr && !statusStr.trim().isEmpty()) {
                whereClause.append(" AND record_status = '").append(statusStr.replace("'", "''")).append("'");
            }
        }

        if (extraData.containsKey("startDate") && extraData.containsKey("endDate")) {
            Object startDate = extraData.get("startDate");
            Object endDate = extraData.get("endDate");
            if (startDate != null && endDate != null) {
                whereClause.append(" AND created_date BETWEEN '")
                        .append(startDate.toString().replace("'", "''"))
                        .append("' AND '")
                        .append(endDate.toString().replace("'", "''"))
                        .append("'");
            }
        }
    }
}
