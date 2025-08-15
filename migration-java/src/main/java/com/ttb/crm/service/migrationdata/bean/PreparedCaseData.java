package com.ttb.crm.service.migrationdata.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PreparedCaseData {
    private CreateCaseDTO dto;
    private CasePayload casePayload;
}