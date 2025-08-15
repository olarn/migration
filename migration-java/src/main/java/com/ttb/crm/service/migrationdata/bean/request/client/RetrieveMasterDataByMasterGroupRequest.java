package com.ttb.crm.service.migrationdata.bean.request.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class RetrieveMasterDataByMasterGroupRequest {
    private String masterGroupCode;
}
