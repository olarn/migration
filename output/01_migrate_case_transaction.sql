-- =====================================================
-- Case Transaction Migration Script
-- Source: [case-migration].dbo.stg_case_inprogress
-- Destination: [case-management].dbo.case_transaction
-- =====================================================

-- Disable constraints and indexes for faster bulk load
ALTER TABLE [case-management].dbo.case_transaction NOCHECK CONSTRAINT ALL;
ALTER INDEX ALL ON [case-management].dbo.case_transaction DISABLE;

-- Set minimal logging for bulk operations
ALTER DATABASE [case-management] SET RECOVERY BULK_LOGGED;

-- Bulk insert with parallel processing
INSERT INTO [case-management].dbo.case_transaction (
    case_id,
    case_number,
    case_status_code,
    case_status_value,
    parent_case_number,
    issue_th,
    suffix_1,
    suffix_2,
    suffix_3,
    fund_code_1,
    fund_code_2,
    fund_code_3,
    integration_system,
    participant_id,
    conversation_id,
    staff_id,
    sla_status,
    modified_by_id,
    previous_reason_code,
    previous_reason_value,
    customer_name,
    rm_id,
    staff_name,
    external_id,
    sla_target_date,
    is_migration,
    migration_lot,
    assign_owner_date,
    start_team_date,
    modified_on,
    fcr,
    service_type_matrix_code,
    service_type_matrix_code_old,
    to_ext_2,
    transaction_date,
    biller_provider_name,
    ref_1,
    ref_2,
    recipient_bank_code,
    recipient_bank_value,
    inter_bank_recipient_account_no,
    wrong_transfer_account,
    prompt_pay,
    wrong_prompt_pay,
    receiver_name,
    transaction_type_code,
    transaction_type_value,
    amount_withdrawal_deposit,
    amount_received_deposit_to_account,
    fund_transfer_bill_payment_amount,
    transfer_amount,
    amount_deposit_withdrawal,
    approved_amount_1,
    branch_atm_shop_location_transaction,
    atm_bank_owner_code,
    atm_bank_owner_value,
    description,
    original_problem_channel_code,
    original_problem_channel_value,
    data_source_code,
    data_source_value,
    priority_code,
    priority_value,
    branch_code,
    branch_name,
    call_number,
    sla,
    visible_on_touch,
    service_category_code,
    service_category_value,
    service_template_code,
    service_template_value,
    pta_segment_code,
    pta_segment_value,
    address,
    document_id,
    document_type,
    ems_tracking,
    object_id,
    repository_id,
    resolution_list_code,
    resolution_list_value,
    issue_name_ttb_touch_th,
    issue_name_ttb_touch_en,
    severity_code,
    severity_value,
    correct_recipient_bank_code,
    correct_recipient_bank_value,
    correct_bank_recipient_account_no,
    correct_target_account,
    sms_code_in_progress,
    sms_code_new,
    sms_code_resolution_1,
    sms_code_resolution_2,
    sms_code_resolved,
    product_type_code_1,
    product_type_value_1,
    product_number_marking_1,
    product_number_full_1,
    product_type_code_2,
    product_type_value_2,
    product_number_marking_2,
    product_number_full_2,
    product_type_code_3,
    product_type_value_3,
    product_number_marking_3,
    product_number_full_3,
    product_service_code,
    product_service_value_th,
    contact_person_name,
    contact_person_phone,
    contact_person_phone_2,
    contact_person_email,
    contact_person_channel_code,
    contact_person_channel_value,
    team_name,
    created_by_team_name,
    created_by_id,
    created_by_name,
    created_close_date_team,
    created_on,
    responsible_bu_old,
    responsible_bu,
    responsible_owner_name,
    responsible_bu_datetime,
    responsible_owner_id,
    responsible_owner_datetime,
    responsible_close_date,
    owner_team_name,
    owner_id,
    owner_name,
    resolved_by,
    resolved_by_employee_id,
    resolved_by_team,
    resolved_date,
    resolution_list_comment,
    root_cause_list_code,
    root_cause_list_value,
    root_cause_list_comment,
    closed_by,
    closed_by_employee_id,
    closed_by_team,
    closed_start_date,
    closed_date,
    created_by_team_id,
    team_id
)
SELECT 
    NEWID() as case_id,
    s.CaseNumber as case_number,
    s.Status_Code as case_status_code,
    s.Status_Value as case_status_value,
    s.ParentCaseNumber as parent_case_number,
    s.Case_Issue__c as issue_th,
    s.Suffix_1__c as suffix_1,
    s.Suffix_2__c as suffix_2,
    s.Suffix_3__c as suffix_3,
    s.FundCode_1__c as fund_code_1,
    s.FundCode_2__c as fund_code_2,
    s.FundCode_3__c as fund_code_3,
    'MIGRATION' as integration_system,
    s.Participant_ID__c as participant_id,
    s.Call_Log_ID__c as conversation_id,
    s.Staff_Employee_ID__c as staff_id,
    s.sla_status as sla_status,
    NULL as modified_by_id, -- Will be mapped later
    s.Return_Reason_Code as previous_reason_code,
    s.Return_Reason_Value as previous_reason_value,
    s.Account_Name as customer_name,
    s.TMB_Customer_ID_PE__c as rm_id,
    s.Staff_ID__c as staff_name,
    s.SF_ID as external_id,
    CASE 
        WHEN s.Commit_Date__c IS NOT NULL AND s.Commit_Date__c != '' 
        THEN TRY_CAST(s.Commit_Date__c AS datetimeoffset)
        ELSE NULL 
    END as sla_target_date,
    1 as is_migration,
    s.migration_lot as migration_lot,
    CASE 
        WHEN s.Owner_Start_Datetime__c IS NOT NULL AND s.Owner_Start_Datetime__c != '' 
        THEN TRY_CAST(s.Owner_Start_Datetime__c AS datetimeoffset)
        ELSE NULL 
    END as assign_owner_date,
    CASE 
        WHEN s.Owner_Team_Datetime__c IS NOT NULL AND s.Owner_Team_Datetime__c != '' 
        THEN TRY_CAST(s.Owner_Team_Datetime__c AS datetimeoffset)
        ELSE NULL 
    END as start_team_date,
    CASE 
        WHEN s.LastModifiedDate IS NOT NULL AND s.LastModifiedDate != '' 
        THEN TRY_CAST(s.LastModifiedDate AS datetimeoffset)
        ELSE NULL 
    END as modified_on,
    CASE WHEN s.FCR__c = 'true' THEN 1 ELSE 0 END as fcr,
    s.Service_Type_Matrix_Code_New as service_type_matrix_code,
    s.Service_Type_Matrix_Code__c as service_type_matrix_code_old,
    s.To_Ext_2__c as to_ext_2,
    CASE 
        WHEN s.Transaction_Date__c IS NOT NULL AND s.Transaction_Date__c != '' 
        THEN TRY_CAST(s.Transaction_Date__c AS datetimeoffset)
        ELSE NULL 
    END as transaction_date,
    s.Biller_Provider_Name__c as biller_provider_name,
    s.Ref_1__c as ref_1,
    s.Ref_2__c as ref_2,
    s.Recipient_Bank__Code as recipient_bank_code,
    s.Recipient_Bank__Value as recipient_bank_value,
    s.Inter_Bank_Recipient_Account_No__c as inter_bank_recipient_account_no,
    s.Wrong_Transfer_Account__c as wrong_transfer_account,
    s.Prompt_Pay__c as prompt_pay,
    s.Wrong_Prompt_Pay as wrong_prompt_pay,
    s.Receiver_Name__c as receiver_name,
    s.Transaction_Type__Code as transaction_type_code,
    s.Transaction_Type__Value as transaction_type_value,
    CASE WHEN ISNUMERIC(s.Amount_Withdrawal_Deposit__c) = 1 THEN CAST(s.Amount_Withdrawal_Deposit__c AS real) ELSE NULL END as amount_withdrawal_deposit,
    CASE WHEN ISNUMERIC(s.Amount_Received_Deposit_to_Account__c) = 1 THEN CAST(s.Amount_Received_Deposit_to_Account__c AS real) ELSE NULL END as amount_received_deposit_to_account,
    CASE WHEN ISNUMERIC(s.Fund_Transfer_Bill_Payment_Amount__c) = 1 THEN CAST(s.Fund_Transfer_Bill_Payment_Amount__c AS real) ELSE NULL END as fund_transfer_bill_payment_amount,
    CASE WHEN ISNUMERIC(s.Transfer_Amount__c) = 1 THEN CAST(s.Transfer_Amount__c AS real) ELSE NULL END as transfer_amount,
    CASE WHEN ISNUMERIC(s.Amount_Deposit_Withdrawal__c) = 1 THEN CAST(s.Amount_Deposit_Withdrawal__c AS real) ELSE NULL END as amount_deposit_withdrawal,
    CASE WHEN ISNUMERIC(s.Approved_Amount__c) = 1 THEN CAST(s.Approved_Amount__c AS real) ELSE NULL END as approved_amount_1,
    s.Branch_ATM_Shop_Location_Transaction__c as branch_atm_shop_location_transaction,
    s.ATM_Bank_Owner__Code as atm_bank_owner_code,
    s.ATM_Bank_Owner__Value as atm_bank_owner_value,
    s.Description__c as description,
    s.original_problem_channel_code as original_problem_channel_code,
    s.original_problem_channel_value as original_problem_channel_value,
    s.data_source_code as data_source_code,
    s.data_source_value as data_source_value,
    s.Priority_Code as priority_code,
    s.Priority_Value as priority_value,
    s.Branch_Code__c as branch_code,
    s.Branch_Name__c as branch_name,
    s.Call_Number__c as call_number,
    CASE WHEN ISNUMERIC(s.SLA_Day__c) = 1 THEN CAST(s.SLA_Day__c AS real) ELSE 0 END as sla,
    CASE WHEN s.Display_on_OneApp__c = 'true' THEN 1 ELSE 0 END as visible_on_touch,
    s.Category__Code as service_category_code,
    s.Category__Value as service_category_value,
    s.Current_Service_Template_Code as service_template_code,
    s.Current_Service_Template_Value as service_template_value,
    s.PTA_Segment_Code as pta_segment_code,
    s.PTA_Segment_Value as pta_segment_value,
    s.Address__c as address,
    s.Document_ID__c as document_id,
    s.Document_Type__c as document_type,
    s.EMS_Tracking_No__c as ems_tracking,
    s.Object_ID__c as object_id,
    s.Repository_ID__c as repository_id,
    s.Resolution_List__c as resolution_list_code,
    s.Resolution_List_Value as resolution_list_value,
    s.Issue_New_for_OneApp__c as issue_name_ttb_touch_th,
    s.Issue_New_for_OneApp_EN__c as issue_name_ttb_touch_en,
    s.Case_Severity_Code as severity_code,
    s.Case_Severity_Value as severity_value,
    s.Correct_Recipient_Bank__Code as correct_recipient_bank_code,
    s.Correct_Recipient_Bank__Value as correct_recipient_bank_value,
    s.Correct_Bank_Recipient_Account_No__c as correct_bank_recipient_account_no,
    s.Correct_Target_Account__c as correct_target_account,
    s.SMS_Code_In_Progress__c as sms_code_in_progress,
    s.SMS_Code_New__c as sms_code_new,
    s.SMS_Code_Resolution_1__c as sms_code_resolution_1,
    s.SMS_Code_Resolution_2__c as sms_code_resolution_2,
    s.SMS_Code_Resolved__c as sms_code_resolved,
    s.Product_Type_1__Code as product_type_code_1,
    s.Product_Type_1__Value as product_type_value_1,
    s.Product_Number_1__c as product_number_marking_1,
    s.Product_Number_Full_1__c as product_number_full_1,
    s.Product_Type_2__Code as product_type_code_2,
    s.Product_Type_2__Value as product_type_value_2,
    s.Product_Number_2__c as product_number_marking_2,
    s.Product_Number_Full_2__c as product_number_full_2,
    s.Product_Type_3__Code as product_type_code_3,
    s.Product_Type_3__Value as product_type_value_3,
    s.Product_Number_3__c as product_number_marking_3,
    s.Product_Number_Full_3__c as product_number_full_3,
    s.Product_Category__Code as product_service_code,
    s.Product_Category__Value as product_service_value_th,
    s.Contact_Person_Name__c as contact_person_name,
    s.Contact_Person_Phone__c as contact_person_phone,
    s.Contact_Person_Phone_2__c as contact_person_phone_2,
    s.Contact_Person_Email__c as contact_person_email,
    s.Contact_Person_Channel__Code as contact_person_channel_code,
    s.Contact_Person_Channel__Value as contact_person_channel_value,
    s.Owner_Team_New as team_name,
    s.Created_By_Team_New as created_by_team_name,
    NULL as created_by_id, -- Will be mapped later
    s.Created_Name__c as created_by_name,
    CASE 
        WHEN s.Created_Close_Date_Team IS NOT NULL AND s.Created_Close_Date_Team != '' 
        THEN TRY_CAST(s.Created_Close_Date_Team AS datetimeoffset)
        ELSE NULL 
    END as created_close_date_team,
    CASE 
        WHEN s.CreatedDate IS NOT NULL AND s.CreatedDate != '' 
        THEN TRY_CAST(s.CreatedDate AS datetimeoffset)
        ELSE NULL 
    END as created_on,
    s.Responsible_BU__c as responsible_bu_old,
    s.Responsible_BU_New as responsible_bu,
    s.Responsible_Owner_Name as responsible_owner_name,
    CASE 
        WHEN s.Responsible_BU_Datetime__c IS NOT NULL AND s.Responsible_BU_Datetime__c != '' 
        THEN TRY_CAST(s.Responsible_BU_Datetime__c AS datetimeoffset)
        ELSE NULL 
    END as responsible_bu_datetime,
    s.Responsible_Owner__c as responsible_owner_id,
    CASE 
        WHEN s.Responsible_Owner_Datetime__c IS NOT NULL AND s.Responsible_Owner_Datetime__c != '' 
        THEN TRY_CAST(s.Responsible_Owner_Datetime__c AS datetimeoffset)
        ELSE NULL 
    END as responsible_owner_datetime,
    CASE 
        WHEN s.Responsible_Close_Date IS NOT NULL AND s.Responsible_Close_Date != '' 
        THEN TRY_CAST(s.Responsible_Close_Date AS datetimeoffset)
        ELSE NULL 
    END as responsible_close_date,
    s.Owner_Team_New as owner_team_name,
    NULL as owner_id, -- Will be mapped later
    s.Owner_Name__c as owner_name,
    s.Resolved_Name as resolved_by,
    s.Resolved_Employee_ID as resolved_by_employee_id,
    s.Resolved_Team_New as resolved_by_team,
    CASE 
        WHEN s.Resolved_DateTime__c IS NOT NULL AND s.Resolved_DateTime__c != '' 
        THEN TRY_CAST(s.Resolved_DateTime__c AS datetimeoffset)
        ELSE NULL 
    END as resolved_date,
    CASE 
        WHEN s.Resolution_List__c = 'Other' OR s.Resolution_List_Value = 'Other'
        THEN ISNULL(s.Resolution__c, '-')
        ELSE NULL 
    END as resolution_list_comment,
    s.Root_Cause_List_Code as root_cause_list_code,
    s.Root_Cause_List_Value as root_cause_list_value,
    CASE 
        WHEN s.Root_Cause_List_Code = 'Other' OR s.Root_Cause_List_Value = 'Other'
        THEN ISNULL(s.Root_Cause__c, '-')
        ELSE NULL 
    END as root_cause_list_comment,
    s.Closed_Name__c as closed_by,
    s.Closed_Employee_ID as closed_by_employee_id,
    s.Closed_By_BU_New as closed_by_team,
    CASE 
        WHEN s.Closed_Start_Datetime IS NOT NULL AND s.Closed_Start_Datetime != '' 
        THEN TRY_CAST(s.Closed_Start_Datetime AS datetimeoffset)
        ELSE NULL 
    END as closed_start_date,
    CASE 
        WHEN s.Closed_Date IS NOT NULL AND s.Closed_Date != '' 
        THEN TRY_CAST(s.Closed_Date AS datetimeoffset)
        ELSE NULL 
    END as closed_date,
    NULL as created_by_team_id, -- Will be mapped later
    NULL as team_id -- Will be mapped later
FROM [case-migration].dbo.stg_case_inprogress s WITH (NOLOCK)
WHERE s.migration_lot IS NOT NULL
OPTION (MAXDOP 4, OPTIMIZE FOR UNKNOWN);

-- Re-enable constraints and indexes
ALTER INDEX ALL ON [case-management].dbo.case_transaction REBUILD;
ALTER TABLE [case-management].dbo.case_transaction WITH CHECK CHECK CONSTRAINT ALL;

-- Reset database recovery model
ALTER DATABASE [case-management] SET RECOVERY FULL;

-- Display migration summary
SELECT 
    'Case Transaction Migration Complete' as Status,
    COUNT(*) as Records_Migrated,
    GETDATE() as Migration_Completed_At
FROM [case-management].dbo.case_transaction 
WHERE is_migration = 1;
