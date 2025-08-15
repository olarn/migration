package com.ttb.crm.service.migrationdata.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CaseIdAndModifiedOnOnDTO {
    private UUID caseId;
    private ZonedDateTime modifiedOn;
}
