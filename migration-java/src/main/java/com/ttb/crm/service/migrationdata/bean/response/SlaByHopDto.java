package com.ttb.crm.service.migrationdata.bean.response;

import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaHopModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class SlaByHopDto {
    private Integer hopNumber;
    private Float slaTarget;
    private String employeeId;
    private UUID ownerId;
    private String ownerName;
    private UUID teamId;
    private String teamName;
    private String smsCodeInProgress;
    private Boolean closeByBu;
    private Float totalDuration;

    public SlaByHopDto(CaseSlaHopModel caseSlaHop) {
        BeanUtils.copyProperties(caseSlaHop, this);
    }
}