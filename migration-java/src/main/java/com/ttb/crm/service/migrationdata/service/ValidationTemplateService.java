package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.lib.crmssp_common_utils_lib.exception.BadRequestException;
import com.ttb.crm.lib.crmssp_common_utils_lib.exception.EarlyReturnResponseException;
import com.ttb.crm.service.migrationdata.helper.Constant;
import com.ttb.crm.service.migrationdata.helper.TemplateValidator;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseTransactionModel;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.ttb.crm.service.migrationdata.helper.Constant.*;

@Service
@RequiredArgsConstructor
public class ValidationTemplateService {

    public CaseTransactionModel validateTemplate(CaseTransactionModel caseTransaction) {
        String serviceTemplateCode = caseTransaction.getServiceTemplateCode();
        switch (serviceTemplateCode) {
            case Constant.GENERAL_TEMPLATE -> validateGeneralTemplate(caseTransaction);
            case Constant.CARD_PRODUCT_TEMPLATE -> validateCardProductTemplate(caseTransaction);
            case Constant.DEPOSIT_WITHDRAW_TEMPLATE -> validateDepositWithdrawTemplate(caseTransaction);
            case Constant.CHECK_INVALID_TRANSFER_TEMPLATE -> validateCheckInvalidTransfer(caseTransaction);
            case Constant.MONEY_TRANSFER_TEMPLATE -> validateMoneyTransferTemplate(caseTransaction);
            case Constant.BILL_PAY_TOP_UP_TEMPLATE -> validateBillPayTopUpTemplate(caseTransaction);
            case Constant.BANKING_AGENT_TEMPLATE -> validateBankingAgentTemplate(caseTransaction);
            case Constant.TEMPLATE_EIGHT -> validateTemplateEight(caseTransaction);
            default -> throw new BadRequestException("Unknown service template code: " + serviceTemplateCode);
        }
        return caseTransaction;
    }

    private void validateGeneralTemplate(CaseTransactionModel caseTransaction) {
        List<TemplateValidator> templateSpecificFields = Stream.of(
                getContactRequiredFields()
        ).flatMap(List::stream).toList();
        validateFields(caseTransaction, templateSpecificFields);
    }

    private void validateCardProductTemplate(CaseTransactionModel caseTransaction) {
        List<TemplateValidator> templateSpecificFields = Stream.of(
                getProductRequiredFields(caseTransaction),
                getProductRelevantRequiredFields(caseTransaction),
                getContactRequiredFields()
        ).flatMap(List::stream).toList();
        validateFields(caseTransaction, templateSpecificFields);
    }

    private void validateDepositWithdrawTemplate(CaseTransactionModel caseTransaction) {
        List<TemplateValidator> templateSpecificFields = Stream.of(
                List.of(
                        new TemplateValidator(CASE_TRANSACTION_TRANSACTION_DATE, CaseTransactionModel::getTransactionDate),
                        new TemplateValidator(CASE_TRANSACTION_TRANSACTION_TYPE_CODE, CaseTransactionModel::getTransactionTypeCode),
                        new TemplateValidator(CASE_TRANSACTION_AMOUNT_WITHDRAWAL_DEPOSIT, CaseTransactionModel::getAmountWithdrawalDeposit),
                        new TemplateValidator(CASE_TRANSACTION_AMOUNT_RECEIVED_DEPOSIT_TO_ACCOUNT, CaseTransactionModel::getAmountReceivedDepositToAccount),
                        new TemplateValidator(CASE_TRANSACTION_GROUPING_SERVICE_POINT_CODE, CaseTransactionModel::getGroupingServicePointCode)
                ),
                getProductRequiredFields(caseTransaction),
                getAtmBankOwnerCodeRequiredFields(caseTransaction),
//                getBranchATMRequiredFields(caseTransaction),
                getContactRequiredFields()
        ).flatMap(List::stream).toList();
        validateFields(caseTransaction, templateSpecificFields);
    }

    private void validateMoneyTransferTemplate(CaseTransactionModel caseTransaction) {
        List<TemplateValidator> templateSpecificFields = Stream.of(
                List.of(
                        new TemplateValidator(CASE_TRANSACTION_TRANSACTION_DATE, CaseTransactionModel::getTransactionDate),
                        new TemplateValidator(CASE_TRANSACTION_GROUPING_SERVICE_POINT_CODE, CaseTransactionModel::getGroupingServicePointCode),
                        new TemplateValidator(CASE_TRANSACTION_TRANSFER_TYPE_CODE, CaseTransactionModel::getTransferTypeCode)
                ),
                getProductRequiredFields(caseTransaction),
                getProductRelevantRequiredFields(caseTransaction),
                getAtmBankOwnerCodeRequiredFields(caseTransaction),
//                getBranchATMRequiredFields(caseTransaction),
                getMoneyTransferInformationRequiredFields(caseTransaction),
                getContactRequiredFields()
        ).flatMap(List::stream).toList();
        validateFields(caseTransaction, templateSpecificFields);
    }

    private void validateBankingAgentTemplate(CaseTransactionModel caseTransaction) {
        List<TemplateValidator> templateSpecificFields = Stream.of(
                List.of(
                        new TemplateValidator(CASE_TRANSACTION_TRANSACTION_DATE, CaseTransactionModel::getTransactionDate),
                        new TemplateValidator(CASE_TRANSACTION_TRANSACTION_TYPE_CODE, CaseTransactionModel::getTransactionTypeCode),
                        new TemplateValidator(CASE_TRANSACTION_AMOUNT_WITHDRAWAL_DEPOSIT, CaseTransactionModel::getAmountWithdrawalDeposit),
                        new TemplateValidator(CASE_TRANSACTION_AMOUNT_RECEIVED_DEPOSIT_TO_ACCOUNT, CaseTransactionModel::getAmountReceivedDepositToAccount),
//                        new TemplateValidator(CASE_TRANSACTION_INTER_BANK_RECIPIENT_ACCOUNT_NO, CaseTransactionModel::getInterBankRecipientAccountNo),
//                        new TemplateValidator(CASE_TRANSACTION_WRONG_TRANSFER_ACCOUNT, CaseTransactionModel::getWrongTransferAccount),
                        new TemplateValidator(CASE_TRANSACTION_GROUPING_SERVICE_POINT_CODE, CaseTransactionModel::getGroupingServicePointCode)
                ),
                getProductRequiredFields(caseTransaction),
                getAtmBankOwnerCodeRequiredFields(caseTransaction),
//                getBranchATMRequiredFields(caseTransaction),
                getContactRequiredFields()
        ).flatMap(List::stream).toList();
        validateFields(caseTransaction, templateSpecificFields);
    }

    private void validateBillPayTopUpTemplate(CaseTransactionModel caseTransaction) {
        List<TemplateValidator> templateSpecificFields = Stream.of(List.of(
                        new TemplateValidator(CASE_TRANSACTION_TRANSACTION_DATE, CaseTransactionModel::getTransactionDate),
                        new TemplateValidator(CASE_TRANSACTION_FUND_TRANSFER_BILL_PAYMENT_AMOUNT, CaseTransactionModel::getFundTransferBillPaymentAmount),
                        new TemplateValidator(CASE_TRANSACTION_AMOUNT_DEPOSIT_WITHDRAWAL, CaseTransactionModel::getAmountDepositWithdrawal),
                        new TemplateValidator(CASE_TRANSACTION_BILLER_PROVIDER_NAME, CaseTransactionModel::getBillerProviderName),
                        new TemplateValidator(CASE_TRANSACTION_GROUPING_SERVICE_POINT_CODE, CaseTransactionModel::getGroupingServicePointCode)
                ),
                getProductRequiredFields(caseTransaction),
                getAtmBankOwnerCodeRequiredFields(caseTransaction),
//                getBranchATMRequiredFields(caseTransaction),
                getContactRequiredFields()
        ).flatMap(List::stream).toList();
        validateFields(caseTransaction, templateSpecificFields);
    }

    private void validateCheckInvalidTransfer(CaseTransactionModel caseTransaction) {
        List<TemplateValidator> templateSpecificFields = Stream.of(List.of(
                        new TemplateValidator(CASE_TRANSACTION_TRANSACTION_DATE, CaseTransactionModel::getTransactionDate),
                        new TemplateValidator(CASE_TRANSACTION_TRANSFER_AMOUNT, CaseTransactionModel::getTransferAmount),
                        new TemplateValidator(CASE_TRANSACTION_GROUPING_SERVICE_POINT_CODE, CaseTransactionModel::getGroupingServicePointCode),
//                        new TemplateValidator(CASE_TRANSACTION_INTER_BANK_RECIPIENT_ACCOUNT_NO, CaseTransactionModel::getInterBankRecipientAccountNo),
//                        new TemplateValidator(CASE_TRANSACTION_WRONG_TRANSFER_ACCOUNT, CaseTransactionModel::getWrongTransferAccount),
                        new TemplateValidator(CASE_TRANSACTION_TRANSFER_TYPE_CODE, CaseTransactionModel::getTransferTypeCode)

                ),
                getProductRequiredFields(caseTransaction),
                getAtmBankOwnerCodeRequiredFields(caseTransaction),
//                getBranchATMRequiredFields(caseTransaction),
                getCheckInvalidTransferInformationRequiredFields(caseTransaction),
                getContactRequiredFields()
        ).flatMap(List::stream).toList();
        validateFields(caseTransaction, templateSpecificFields);
    }

    private void validateTemplateEight(CaseTransactionModel caseTransaction) {
        List<TemplateValidator> templateSpecificFields = List.of();
        validateFields(caseTransaction, templateSpecificFields);
    }

    private List<TemplateValidator> getContactRequiredFields() {
        return List.of(
                new TemplateValidator(CASE_TRANSACTION_CONTACT_PERSON_NAME, CaseTransactionModel::getContactPersonName),
                new TemplateValidator(CASE_TRANSACTION_CONTACT_PERSON_PHONE, CaseTransactionModel::getContactPersonPhone)
        );
    }

    private List<TemplateValidator> getProductRequiredFields(CaseTransactionModel caseTransaction) {
        List<TemplateValidator> product1Fields = new ArrayList<>();

        product1Fields.add(new TemplateValidator(CASE_TRANSACTION_PRODUCT_TYPE_CODE_1, CaseTransactionModel::getProductTypeCode1));
        product1Fields.add(new TemplateValidator(CASE_TRANSACTION_PRODUCT_TYPE_VALUE_1, CaseTransactionModel::getProductTypeValue1));
//        product1Fields.add(new TemplateValidator(CASE_TRANSACTION_SUB_PRODUCT_TYPE_CODE_1, CaseTransactionModel::getSubProductTypeCode1));
        product1Fields.add(new TemplateValidator(CASE_TRANSACTION_PRODUCT_NUMBER_MARKING_1, CaseTransactionModel::getProductNumberMarking1));
        product1Fields.add(new TemplateValidator(CASE_TRANSACTION_PRODUCT_NUMBER_FULL_1, CaseTransactionModel::getProductNumberFull1));
//        product1Fields.add(new TemplateValidator(CASE_TRANSACTION_SUFFIX_1, CaseTransactionModel::getSuffix1));

        Optional.ofNullable(caseTransaction.getProductTypeCode1())
                .filter(MUTUAL_FUND_CODE::equals)
                .ifPresent(code ->product1Fields.add(new TemplateValidator(CASE_TRANSACTION_FUND_CODE_1, CaseTransactionModel::getFundCode1)));
        return product1Fields;
    }

    private List<TemplateValidator> getProductRelevantRequiredFields(CaseTransactionModel caseTransaction) {
        List<TemplateValidator> product2Fields = new ArrayList<>(List.of(
                new TemplateValidator(CASE_TRANSACTION_PRODUCT_TYPE_CODE_2, CaseTransactionModel::getProductTypeCode2),
                new TemplateValidator(CASE_TRANSACTION_PRODUCT_TYPE_VALUE_2, CaseTransactionModel::getProductTypeValue2),
//                new TemplateValidator(CASE_TRANSACTION_SUB_PRODUCT_TYPE_CODE_2, CaseTransactionModel::getSubProductTypeCode2),
                new TemplateValidator(CASE_TRANSACTION_PRODUCT_NUMBER_FULL_2, CaseTransactionModel::getProductNumberFull2),
                new TemplateValidator(CASE_TRANSACTION_PRODUCT_NUMBER_MARKING_2, CaseTransactionModel::getProductNumberMarking2)
//                new TemplateValidator(CASE_TRANSACTION_SUFFIX_2, CaseTransactionModel::getSuffix2)
        ));

        List<TemplateValidator> product3Fields = new ArrayList<>(List.of(
                new TemplateValidator(CASE_TRANSACTION_PRODUCT_TYPE_CODE_3, CaseTransactionModel::getProductTypeCode3),
                new TemplateValidator(CASE_TRANSACTION_PRODUCT_TYPE_VALUE_3, CaseTransactionModel::getProductTypeValue3),
//                new TemplateValidator(CASE_TRANSACTION_SUB_PRODUCT_TYPE_CODE_3, CaseTransactionModel::getSubProductTypeCode3),
                new TemplateValidator(CASE_TRANSACTION_PRODUCT_NUMBER_FULL_3, CaseTransactionModel::getProductNumberFull3),
                new TemplateValidator(CASE_TRANSACTION_PRODUCT_NUMBER_MARKING_3, CaseTransactionModel::getProductNumberMarking3)
//                new TemplateValidator(CASE_TRANSACTION_SUFFIX_3, CaseTransactionModel::getSuffix3)
        ));

        Optional.ofNullable(caseTransaction.getProductTypeCode2())
                .filter(MUTUAL_FUND_CODE::equals)
                .ifPresent(code -> product2Fields.add(new TemplateValidator(CASE_TRANSACTION_FUND_CODE_2, CaseTransactionModel::getFundCode2)));

        Optional.ofNullable(caseTransaction.getProductTypeCode3())
                .filter(MUTUAL_FUND_CODE::equals)
                .ifPresent(code -> product3Fields.add(new TemplateValidator(CASE_TRANSACTION_FUND_CODE_3, CaseTransactionModel::getFundCode3)));

        boolean hasProduct3 = hasNonEmptyValue(product3Fields, caseTransaction);
        boolean hasProduct2 = hasNonEmptyValue(product2Fields, caseTransaction);

        if (hasProduct3) {
            return Stream.concat(product2Fields.stream(), product3Fields.stream()).toList();
        }
        if (hasProduct2) {
            return product2Fields;
        }
        return List.of();
    }

    private List<TemplateValidator> getAtmBankOwnerCodeRequiredFields(CaseTransactionModel caseTransaction) {
        return Optional.ofNullable(caseTransaction.getGroupingServicePointCode())
                .filter(Constant.ATM_SERVICE_POINT_CODE::equals)
                .map(code -> List.of(new TemplateValidator(CASE_TRANSACTION_ATM_BANK_OWNER_CODE, CaseTransactionModel::getAtmBankOwnerCode)))
                .orElse(List.of());
    }

//    private List<TemplateValidator> getBranchATMRequiredFields(CaseTransactionModel caseTransaction) {
//        return Optional.ofNullable(caseTransaction.getAtmBankOwnerCode())
//                .filter(Constant.ATM_BANK_OWNER_CODE::equals)
//                .map(code -> List.of(
//                        new TemplateValidator(CASE_TRANSACTION_BRANCH_ATM_SHOP_LOCATION_TRANSACTION, CaseTransactionModel::getBranchAtmShopLocationTransaction),
//                        new TemplateValidator(CASE_TRANSACTION_BRANCH_ATM_SHOP_LOCATION_TRANSACTION_CODE, CaseTransactionModel::getBranchAtmShopLocationTransactionCode),
//                        new TemplateValidator(CASE_TRANSACTION_BRANCH_ATM_SHOP_ADDRESS_THAI, CaseTransactionModel::getBranchAtmShopAddressThai),
//                        new TemplateValidator(CASE_TRANSACTION_BRANCH_ATM_SHOP_NUMBER, CaseTransactionModel::getBranchAtmShopNumber)
//                ))
//                .orElse(List.of());
//    }

    private List<TemplateValidator> getMoneyTransferInformationRequiredFields(CaseTransactionModel caseTransaction) {
        String transferTypeCode = caseTransaction.getTransferTypeCode();

        return switch (String.valueOf(transferTypeCode)) {
            case Constant.TRANSFER_TYPE_BANKING_CODE -> List.of(
                    new TemplateValidator(CASE_TRANSACTION_RECIPIENT_BANK_CODE, CaseTransactionModel::getRecipientBankCode),
//                    new TemplateValidator(CASE_TRANSACTION_INTER_BANK_RECIPIENT_ACCOUNT_NO, CaseTransactionModel::getInterBankRecipientAccountNo),
                    new TemplateValidator(CASE_TRANSACTION_TRANSFER_AMOUNT, CaseTransactionModel::getTransferAmount),
                    new TemplateValidator(CASE_TRANSACTION_DEPOSIT_AMOUNT, CaseTransactionModel::getDepositAmount)
            );
            case Constant.TRANSFER_TYPE_PROMPTPAY_CODE -> List.of(
                    new TemplateValidator(CASE_TRANSACTION_PROMPT_PAY, CaseTransactionModel::getPromptPay),
                    new TemplateValidator(CASE_TRANSACTION_TRANSFER_AMOUNT, CaseTransactionModel::getTransferAmount),
                    new TemplateValidator(CASE_TRANSACTION_DEPOSIT_AMOUNT, CaseTransactionModel::getDepositAmount)
            );
            default -> List.of();
        };
    }

    private List<TemplateValidator> getCheckInvalidTransferInformationRequiredFields(CaseTransactionModel caseTransaction) {
        String transferTypeCode = caseTransaction.getTransferTypeCode();

        return switch (String.valueOf(transferTypeCode)) {
            case Constant.TRANSFER_TYPE_BANKING_CODE -> List.of(
                    new TemplateValidator(CASE_TRANSACTION_CORRECT_RECIPIENT_BANK_CODE, CaseTransactionModel::getCorrectRecipientBankCode),
                    new TemplateValidator(CASE_TRANSACTION_CORRECT_BANK_RECIPIENT_ACCOUNT_NO, CaseTransactionModel::getCorrectBankRecipientAccountNo),
                    new TemplateValidator(CASE_TRANSACTION_CORRECT_TARGET_ACCOUNT, CaseTransactionModel::getCorrectTargetAccount)
            );
            case Constant.TRANSFER_TYPE_PROMPTPAY_CODE -> List.of(
                    new TemplateValidator(CASE_TRANSACTION_PROMPT_PAY, CaseTransactionModel::getPromptPay),
                    new TemplateValidator(CASE_TRANSACTION_PROMPT_PAY_TRANSFER_ACCOUNT, CaseTransactionModel::getPromptPayTransferAccount)
            );
            default -> List.of();
        };
    }

    private void validateFields(CaseTransactionModel caseTransaction, List<TemplateValidator> templateFields) {
        Map<String, List<String>> fieldErrors = new HashMap<>();
        for (TemplateValidator fieldValidator : templateFields) {
            Object value = fieldValidator.getGetter().apply(caseTransaction);
            boolean isEmptyValue = value == null || (value instanceof String s && s.trim().isEmpty());
            if (isEmptyValue) {
                fieldErrors
                        .computeIfAbsent(fieldValidator.getFieldName(), k -> new ArrayList<>())
                        .add(fieldValidator.getFieldName() + " is required");
            }
        }

        if (!fieldErrors.isEmpty()) {
            throw new EarlyReturnResponseException(HttpStatus.BAD_REQUEST, fieldErrors, "Service template validation failed");
        }
    }

    private boolean hasNonEmptyValue(List<TemplateValidator> fields, CaseTransactionModel caseTransaction) {
        return fields.stream().anyMatch(f -> {
            Object value = f.getGetter().apply(caseTransaction);
            return value != null && (!(value instanceof String) || StringUtils.isNotBlank(((String) value).trim()));
        });
    }
}
