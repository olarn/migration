package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.lib.crmssp_common_utils_lib.exception.BadRequestException;
import com.ttb.crm.lib.crmssp_common_utils_lib.exception.EarlyReturnResponseException;
import com.ttb.crm.service.migrationdata.helper.Constant;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseTransactionModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ValidationTemplateServiceTest {

    @InjectMocks
    private ValidationTemplateService validationTemplateService;

    @Test
    public void validateTemplate_ShouldThrowBadRequestException() {
        CaseTransactionModel caseTransactionModel = new CaseTransactionModel();
        caseTransactionModel.setServiceTemplateCode("Test");
        Exception exception = assertThrows(BadRequestException.class, () -> validationTemplateService.validateTemplate(caseTransactionModel));

        assertTrue(exception.getMessage().contains("Unknown service template code: Test"));
    }


    @Test
    void testValidateTemplate_generalTemplate_callsValidateGeneralTemplate() {
        CaseTransactionModel caseTransactionModel = new CaseTransactionModel();
        caseTransactionModel.setServiceTemplateCode(Constant.GENERAL_TEMPLATE);

        assertThrows(EarlyReturnResponseException.class, () -> validationTemplateService.validateTemplate(caseTransactionModel));
    }

    @Test
    void testValidateTemplate_cardProductTemplate_callsValidateCardProductTemplate() {
        CaseTransactionModel caseTransactionModel = new CaseTransactionModel();
        caseTransactionModel.setServiceTemplateCode(Constant.CARD_PRODUCT_TEMPLATE);

        assertThrows(EarlyReturnResponseException.class, () -> validationTemplateService.validateTemplate(caseTransactionModel));
    }

    @Test
    void testValidateTemplate_cardProductTemplate_callsValidateDepositWithdrawTemplate() {
        CaseTransactionModel caseTransactionModel = new CaseTransactionModel();
        caseTransactionModel.setServiceTemplateCode(Constant.DEPOSIT_WITHDRAW_TEMPLATE);

        assertThrows(EarlyReturnResponseException.class, () -> validationTemplateService.validateTemplate(caseTransactionModel));
    }

    @Test
    void testValidateTemplate_cardProductTemplate_callsValidateCheckInvalidTransferTemplate() {
        CaseTransactionModel caseTransactionModel = new CaseTransactionModel();
        caseTransactionModel.setServiceTemplateCode(Constant.CHECK_INVALID_TRANSFER_TEMPLATE);

        assertThrows(EarlyReturnResponseException.class, () -> validationTemplateService.validateTemplate(caseTransactionModel));
    }

    @Test
    void testValidateTemplate_cardProductTemplate_callsValidateMoneyTransferTemplate() {
        CaseTransactionModel caseTransactionModel = new CaseTransactionModel();
        caseTransactionModel.setServiceTemplateCode(Constant.MONEY_TRANSFER_TEMPLATE);

        assertThrows(EarlyReturnResponseException.class, () -> validationTemplateService.validateTemplate(caseTransactionModel));
    }

    @Test
    void testValidateTemplate_cardProductTemplate_callsValidateBillPayTopUpTemplate() {
        CaseTransactionModel caseTransactionModel = new CaseTransactionModel();
        caseTransactionModel.setServiceTemplateCode(Constant.BILL_PAY_TOP_UP_TEMPLATE);

        assertThrows(EarlyReturnResponseException.class, () -> validationTemplateService.validateTemplate(caseTransactionModel));
    }

    @Test
    void testValidateTemplate_cardProductTemplate_callsValidateBankingAgentTemplate() {
        CaseTransactionModel caseTransactionModel = new CaseTransactionModel();
        caseTransactionModel.setServiceTemplateCode(Constant.BANKING_AGENT_TEMPLATE);

        assertThrows(EarlyReturnResponseException.class, () -> validationTemplateService.validateTemplate(caseTransactionModel));
    }

    @Test
    void testValidateTemplate_cardProductTemplate_callsValidateTemplateEight() {
        CaseTransactionModel caseTransactionModel = new CaseTransactionModel();
        caseTransactionModel.setServiceTemplateCode(Constant.TEMPLATE_EIGHT);

        assertDoesNotThrow(() -> validationTemplateService.validateTemplate(caseTransactionModel));
    }

}
