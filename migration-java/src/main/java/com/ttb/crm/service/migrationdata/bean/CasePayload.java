package com.ttb.crm.service.migrationdata.bean;

import com.ttb.crm.service.migrationdata.bean.response.MetaData;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CasePayload {
    private ServiceTypeMatrixModel serviceTypeMatrix;
    private MetaData metaData;
    private ZonedDateTime slaStartDate;
}
