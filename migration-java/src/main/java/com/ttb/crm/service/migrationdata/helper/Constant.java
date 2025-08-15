package com.ttb.crm.service.migrationdata.helper;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Constant {

    public static final int ACTIVE_STATUS_CODE = 0;
    public static final int INACTIVE_STATUS_CODE = 1;

    public static final String WEALTH_TEAM_EN = "Wealth Support";
    public static final String BRANCH_TEAM_EN = "Branch Sale Support";
    public static final String ROLE_TEAM_PRIVATE_BANKING = "private banking";
    public static final Set<String> ONE_APP_STM_MY_ADVISOR_FROM_SALE_FORCE = Set.of("O0011", "O0012", "O0013");

    // Case status
    public static final String CASE_STATUS_NEW = "NEW";
    public static final String CASE_STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String CASE_STATUS_RESOLVED = "RESOLVED";
    public static final String CASE_STATUS_COMPLETED = "COMPLETED";
    public static final String CASE_STATUS_CANCEL = "CANCEL";
    public static final String CASE_STATUS_NEW_VALUE = "New";
    public static final String CASE_STATUS_IN_PROGRESS_VALUE = "In Progress";
    public static final String CASE_STATUS_RESOLVED_VALUE = "Resolved";
    public static final String CASE_STATUS_COMPLETED_VALUE = "Completed";
    public static final String CASE_STATUS_CANCEL_VALUE = "Cancel";

    public static final String EMPTY_STRING = "";

    public static final List<String> CASE_GROUP_STATUS_ON_PROCESS = List.of(CASE_STATUS_NEW, CASE_STATUS_IN_PROGRESS, CASE_STATUS_RESOLVED);
    public static final List<String> CASE_GROUP_STATUS_END = List.of(CASE_STATUS_COMPLETED, CASE_STATUS_CANCEL);
    public static final List<String> CASE_GROUP_STATUS_NEW_AND_IN_PROGRESS = List.of(CASE_STATUS_NEW, CASE_STATUS_IN_PROGRESS);

    public static final String RECORD_STATUS_SUCCESS = "Success";
    public static final String CASE_STATUS_CODE_COMPLETED = "COMPLETED";
    public static final String LOAD_STATUS_SUCCESS = "success";

    public static final int CUT_OFF_DAY = 30;

    public static final String SERVICE_CATEGORY_CODE_COMPLAINT = "COMPLAINT";

    public static final String SYSTEM_EMPLOYEE_ID = "SYSTEM";
    public static final String SYSTEM_TEAM = "SYSTEM";
    public static final String NA_EMPLOYEE_ID = "N/A";
    public static final String NA_EMPLOYEE_FULL_NAME = "N/A N/A";
    public static final String SF_EX_API_ID = "99998";
    public static final String ADMIN_CRM = "CRMADM001";
    public static final String SF_EX_API_FULL_NAME = "sfexapi";


    public static final String INTEGRATION_SYSTEM = "Integration-System";

    public static final String ORIGINAL_PROBLEM_CHANNEL_GROUP_CODE = "originalProblemChannel";
    public static final String ORIGINAL_PROBLEM_CHANNEL_CODE = "OneApp";

    public static final String ONE_APP = "OneApp";
    public static final String TTB_WEB = "ttbWeb";
    public static final String TEP = "TEP";
    public static final String CRM = "CRM";

    public static final String CREATOR_TEAM = "Creator";

    public static final List<String> ALL_INTEGRATION_SYSTEM = List.of(ONE_APP, TTB_WEB, TEP);


    //One APP
    public static final String STM_ONE_APP_PAYROLL = "stmOneAppPayroll";
    public static final String STM_ONE_APP_DEPOSIT = "stmOneAppDeposit";
    public static final String STM_ONE_APP_WEALTH = "stmOneAppWealth";
    public static final String STM_ONE_APP_FINCERT = "stmOneAppFincert";
    public static final String SERVICE_TYPE_MATRIX_CODE_F = "STM000024";
    public static final String SERVICE_TYPE_MATRIX_CODE_DEPOSIT = "STM000025";
    public static final Set<String> SERVICE_TYPE_MATRIX_CODE_W = Set.of("STM000021", "STM000022", "STM000023");
    public static final String BANGKOK_ZONE_ID = "Asia/Bangkok";
    public static final String DEFAULT_DATETIME_FORMAT = "yyyyddMM'T'HH:mm:ss.SSS";
    public static final String DATE_TIME_STAGING_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // Reason code
    public static final String OTHER_REASON_CODE = "crs00002";

    // Group reason
    public static final String GROUP_CANCEL_REASON = "cancelReason";

    // root cause
    public static final String ROOT_CAUSE_GROUP_CODE = "issueCause";
    public static final String OTHER_ROOT_CAUSE_CODE = "issc00034";
    public static final String OTHER_ROOT_CAUSE_VALUE = "Other";

    // resolution
    public static final String RESOLUTION_GROUP_CODE = "resolution";
    public static final String OTHER_RESOLUTION_CODE = "reso00016";
    public static final String OTHER_RESOLUTION_VALUE = "Other";

    public static final String UNEXPECTED_ERROR = "Unexpected error.";

    public static final String VALUE_CHAIN_ERROR = "User not allowed to access this case";

    public static final String USER_NOT_ALLOW_ACCESS = "User not allowed to access";

    public static final String COMMENT_REGEX = "^[\\p{IsThai}a-zA-Z0-9 _.,“'()\\[\\]{}\\/\\\\@#$%&*]*$";

    // validate template
    public static final String ATM_BANK_OWNER_CODE = "011";
    public static final String TRANSFER_TYPE_BANKING_CODE = "tnft00001";
    public static final String TRANSFER_TYPE_PROMPTPAY_CODE = "tnft00002";
    public static final String CONTACT_EMAIL_CHANNEL_CODE_CODE = "ctpc00002";
    public static final String ATM_SERVICE_POINT_CODE = "gsrvp00001";
    public static final String MUTUAL_FUND_CODE = "pser00007";
    public static final String GENERAL_TEMPLATE = "stem00001";
    public static final String CARD_PRODUCT_TEMPLATE = "stem00002";
    public static final String BILL_PAY_TOP_UP_TEMPLATE = "stem00003";
    public static final String CHECK_INVALID_TRANSFER_TEMPLATE = "stem00004";
    public static final String DEPOSIT_WITHDRAW_TEMPLATE = "stem00005";
    public static final String MONEY_TRANSFER_TEMPLATE = "stem00006";
    public static final String BANKING_AGENT_TEMPLATE = "stem00007";
    public static final String TEMPLATE_EIGHT = "stem00008";
    public static final String GROUP_PASSED = "passed";
    public static final String GROUP_FAILED = "failed";

    // validate template
    public static final String CASE_TRANSACTION_TRANSACTION_DATE = "transactionDate";
    public static final String CASE_TRANSACTION_TRANSACTION_TYPE_CODE = "transactionTypeCode";
    public static final String CASE_TRANSACTION_AMOUNT_WITHDRAWAL_DEPOSIT = "amountWithdrawalDeposit";
    public static final String CASE_TRANSACTION_AMOUNT_RECEIVED_DEPOSIT_TO_ACCOUNT = "amountReceivedDepositToAccount";
    public static final String CASE_TRANSACTION_GROUPING_SERVICE_POINT_CODE = "groupingServicePointCode";
    public static final String CASE_TRANSACTION_ATM_BANK_OWNER_CODE = "atmBankOwnerCode";
    public static final String CASE_TRANSACTION_TRANSFER_TYPE_CODE = "transferTypeCode";
    public static final String CASE_TRANSACTION_INTER_BANK_RECIPIENT_ACCOUNT_NO = "interBankRecipientAccountNo";
    public static final String CASE_TRANSACTION_WRONG_TRANSFER_ACCOUNT = "wrongTransferAccount";
    public static final String CASE_TRANSACTION_FUND_TRANSFER_BILL_PAYMENT_AMOUNT = "fundTransferBillPaymentAmount";
    public static final String CASE_TRANSACTION_AMOUNT_DEPOSIT_WITHDRAWAL = "amountDepositWithdrawal";
    public static final String CASE_TRANSACTION_BILLER_PROVIDER_NAME = "billerProviderName";
    public static final String CASE_TRANSACTION_TRANSFER_AMOUNT = "transferAmount";
    public static final String CASE_TRANSACTION_CORRECT_RECIPIENT_BANK_CODE = "correctRecipientBankCode";
    public static final String CASE_TRANSACTION_CORRECT_BANK_RECIPIENT_ACCOUNT_NO = "correctBankRecipientAccountNo";
    public static final String CASE_TRANSACTION_CORRECT_TARGET_ACCOUNT = "correctTargetAccount";
    public static final String CASE_TRANSACTION_CONTACT_PERSON_NAME = "contactPersonName";
    public static final String CASE_TRANSACTION_CONTACT_PERSON_PHONE = "contactPersonPhone";
    public static final String CASE_TRANSACTION_CONTACT_PERSON_EMAIL = "contactPersonEmail";
    public static final String CASE_TRANSACTION_PRODUCT_TYPE_CODE_1 = "productTypeCode1";
    public static final String CASE_TRANSACTION_PRODUCT_TYPE_VALUE_1 = "productTypeValue1";
    public static final String CASE_TRANSACTION_SUB_PRODUCT_TYPE_CODE_1 = "subProductTypeCode1";
    public static final String CASE_TRANSACTION_SUB_PRODUCT_TYPE_VALUE_1 = "subProductTypeValue1";
    public static final String CASE_TRANSACTION_PRODUCT_NUMBER_MARKING_1 = "productNumberMarking1";
    public static final String CASE_TRANSACTION_PRODUCT_NUMBER_FULL_1 = "productNumberFull1";
    public static final String CASE_TRANSACTION_FUND_CODE_1 = "fundCode1";
    public static final String CASE_TRANSACTION_SUFFIX_1 = "suffix1";
    public static final String CASE_TRANSACTION_PRODUCT_TYPE_CODE_2 = "productTypeCode2";
    public static final String CASE_TRANSACTION_PRODUCT_TYPE_VALUE_2 = "productTypeValue2";
    public static final String CASE_TRANSACTION_SUB_PRODUCT_TYPE_CODE_2 = "subProductTypeCode2";
    public static final String CASE_TRANSACTION_SUB_PRODUCT_TYPE_VALUE_2 = "subProductTypeValue2";
    public static final String CASE_TRANSACTION_PRODUCT_NUMBER_MARKING_2 = "productNumberMarking2";
    public static final String CASE_TRANSACTION_PRODUCT_NUMBER_FULL_2 = "productNumberFull2";
    public static final String CASE_TRANSACTION_FUND_CODE_2 = "fundCode2";
    public static final String CASE_TRANSACTION_SUFFIX_2 = "suffix2";
    public static final String CASE_TRANSACTION_PRODUCT_TYPE_CODE_3 = "productTypeCode3";
    public static final String CASE_TRANSACTION_PRODUCT_TYPE_VALUE_3 = "productTypeValue3";
    public static final String CASE_TRANSACTION_SUB_PRODUCT_TYPE_CODE_3 = "subProductTypeCode3";
    public static final String CASE_TRANSACTION_SUB_PRODUCT_TYPE_VALUE_3 = "subProductTypeValue3";
    public static final String CASE_TRANSACTION_PRODUCT_NUMBER_MARKING_3 = "productNumberMarking3";
    public static final String CASE_TRANSACTION_PRODUCT_NUMBER_FULL_3 = "productNumberFull3";
    public static final String CASE_TRANSACTION_FUND_CODE_3 = "fundCode3";
    public static final String CASE_TRANSACTION_SUFFIX_3 = "suffix3";
    public static final String CASE_TRANSACTION_BRANCH_ATM_SHOP_LOCATION_TRANSACTION = "branchAtmShopLocationTransaction";
    public static final String CASE_TRANSACTION_BRANCH_ATM_SHOP_LOCATION_TRANSACTION_CODE = "branchAtmShopLocationTransactionCode";
    public static final String CASE_TRANSACTION_BRANCH_ATM_SHOP_ADDRESS_THAI = "branchAtmShopAddressThai";
    public static final String CASE_TRANSACTION_BRANCH_ATM_SHOP_NUMBER = "branchAtmShopNumber";
    public static final String CASE_TRANSACTION_RECIPIENT_BANK_CODE = "recipientBankCode";
    public static final String CASE_TRANSACTION_DEPOSIT_AMOUNT = "depositAmount";
    public static final String CASE_TRANSACTION_PROMPT_PAY = "promptPay";
    public static final String CASE_TRANSACTION_PROMPT_PAY_TRANSFER_ACCOUNT = "promptPayTransferAccount";
    public static final int STG_TO_CASE_CHUNK_SIZE = 1000;
    public static final int UPDATE_STG_STATUS_CHUNK_SIZE = 2000;
    public static final String PREFIX_ERROR = "Error: ";
    public static final UUID EMPTY_UUID = new UUID(0L, 0L);
}
