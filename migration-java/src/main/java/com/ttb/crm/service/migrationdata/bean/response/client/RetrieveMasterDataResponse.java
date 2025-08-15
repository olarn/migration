package com.ttb.crm.service.migrationdata.bean.response.client;

import com.ttb.crm.service.migrationdata.model.masterManagement.MasterDataModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RetrieveMasterDataResponse {
    private UUID id;
    private String nameTh;
    private String nameEn;
    private String code;
    private String groupCode;

    public RetrieveMasterDataResponse(MasterDataModel masterDataModel) {
        BeanUtils.copyProperties(masterDataModel, this);
        this.groupCode = masterDataModel.getMasterGroup().getCode();
    }
}
