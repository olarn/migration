package com.ttb.crm.service.migrationdata.model;

import com.ttb.crm.service.migrationdata.model.batch.TempUpdateStgInProgress;
import org.springframework.jdbc.core.RowMapper;

import java.util.UUID;

import static com.ttb.crm.service.migrationdata.helper.DateTimeUtils.parseToZoneDateTime;

public class TempUpdateStgInProgressRowMapper implements RowMapper<TempUpdateStgInProgress> {

    @Override
    public TempUpdateStgInProgress mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        TempUpdateStgInProgress tempUpdate = new TempUpdateStgInProgress();
        tempUpdate.setTempId(UUID.fromString(rs.getString("temp_id")));
        tempUpdate.setId(rs.getLong("id"));
        tempUpdate.setLoadStatus(rs.getString("load_status"));
        tempUpdate.setLoadRemark(rs.getString("load_remark"));
        tempUpdate.setLoadLastDatetime(parseToZoneDateTime(rs.getString("load_lastdatetime")));
        return tempUpdate;
    }
}
