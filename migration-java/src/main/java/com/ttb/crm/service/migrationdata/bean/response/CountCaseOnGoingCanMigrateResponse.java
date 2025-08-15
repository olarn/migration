package com.ttb.crm.service.migrationdata.bean.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CountCaseOnGoingCanMigrateResponse {
    private int totalCanMigrate;
    private int totalCases;
    private int totalFixedCases;
    private int totalDynamicCases;
    private int canMigrateFixedCases;
    private int canMatchDynamicCases;
    private int cannotMigrateFixedButCreate;
    private int nullStmCodeCount;
    private int notFoundStmCount;
    private int sameCreateAndOwnerTeamCase;
    private int fcrCaseFixCount;
    private int fcrCaseDynamicCount;


    public CountCaseOnGoingCanMigrateResponse(
            Integer totalCanMigrate,
            Integer totalCases,
            Integer totalFixedCases,
            Integer totalDynamicCases,
            Integer canMigrateFixedCases,
            Integer canMatchDynamicCases,
            Integer cannotMigrateFixedButCreate,
            Integer nullStmCodeCount,
            Integer notFoundStmCount,
            Integer sameCreateAndOwnerTeamCase,
            Integer fcrCaseFixCount,
            Integer fcrCaseDynamicCount
    ) {
        this.totalCanMigrate = totalCanMigrate;
        this.totalCases = totalCases;
        this.totalFixedCases = totalFixedCases;
        this.totalDynamicCases = totalDynamicCases;
        this.canMigrateFixedCases = canMigrateFixedCases;
        this.canMatchDynamicCases = canMatchDynamicCases;
        this.cannotMigrateFixedButCreate = cannotMigrateFixedButCreate;
        this.nullStmCodeCount = nullStmCodeCount;
        this.notFoundStmCount = notFoundStmCount;
        this.sameCreateAndOwnerTeamCase = sameCreateAndOwnerTeamCase;
        this.fcrCaseFixCount = fcrCaseFixCount;
        this.fcrCaseDynamicCount = fcrCaseDynamicCount;
    }
}