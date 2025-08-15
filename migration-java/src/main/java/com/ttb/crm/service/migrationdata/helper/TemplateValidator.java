package com.ttb.crm.service.migrationdata.helper;

import com.ttb.crm.service.migrationdata.model.caseManagement.CaseTransactionModel;
import lombok.Getter;

import java.util.function.Function;

@Getter
public class TemplateValidator {
    private final String fieldName;
    private final Function<CaseTransactionModel, Object> getter;

    public TemplateValidator(String fieldName, Function<CaseTransactionModel, Object> getter) {
        this.fieldName = fieldName;
        this.getter = getter;
    }
}
