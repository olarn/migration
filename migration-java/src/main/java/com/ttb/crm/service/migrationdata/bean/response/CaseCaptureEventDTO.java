package com.ttb.crm.service.migrationdata.bean.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaseCaptureEventDTO {
    RetrieveCaseInfoResponse beforeChange;
    RetrieveCaseInfoResponse afterChange;
    String employeeId;
    String eventAction;
}
