/*
	01 - Transform and migrate case_transaction (Java logic adapted)
	- Deterministic case_id using case number / SF_ID
	- Booleans normalized (Display_on_OneApp__c, FCR__c)
	- Dates parsed with TRY_CONVERT
	- Strings truncated to destination column lengths using LEFT
*/

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
	BEGIN TRAN;

	IF DB_ID('case-migration') IS NULL THROW 50000, 'Database [case-migration] not found', 1;
	IF DB_ID('case-management') IS NULL THROW 50001, 'Database [case-management] not found', 1;

	USE [case-migration];

	IF OBJECT_ID('[dbo].[migration_case_keymap]', 'U') IS NULL
	BEGIN
		CREATE TABLE [dbo].[migration_case_keymap](
			source_case_number	nvarchar(255) NULL,
			source_sf_id		nvarchar(255) NULL,
			case_id			uniqueidentifier NOT NULL PRIMARY KEY,
			created_on		datetimeoffset NOT NULL DEFAULT SYSDATETIMEOFFSET()
		);
	END;

	;WITH base_keys AS (
		SELECT DISTINCT
			LTRIM(RTRIM(NULLIF(CaseNumber, ''))) AS case_number,
			LTRIM(RTRIM(NULLIF(SF_ID, ''))) AS sf_id
		FROM [dbo].[stg_case_inprogress]
	),
	ids AS (
		SELECT
			bk.case_number,
			bk.sf_id,
			CONVERT(varchar(32), HASHBYTES('MD5', UPPER(CONVERT(nvarchar(255), COALESCE(bk.case_number, bk.sf_id)))), 2) AS md5hex
		FROM base_keys bk
		WHERE COALESCE(bk.case_number, bk.sf_id) IS NOT NULL
	),
	ids_fmt AS (
		SELECT
			case_number,
			sf_id,
			LOWER(STUFF(STUFF(STUFF(STUFF(md5hex, 9, 0, '-'), 14, 0, '-'), 19, 0, '-'), 24, 0, '-')) AS guid_str
		FROM ids
	)
	MERGE [dbo].[migration_case_keymap] AS tgt
	USING (
		SELECT case_number, sf_id, CONVERT(uniqueidentifier, guid_str) AS gen_case_id FROM ids_fmt
	) AS src
	ON (
		(tgt.source_case_number IS NOT NULL AND tgt.source_case_number = src.case_number)
		OR (tgt.source_sf_id IS NOT NULL AND tgt.source_sf_id = src.sf_id)
	)
	WHEN NOT MATCHED THEN
		INSERT (source_case_number, source_sf_id, case_id)
		VALUES (src.case_number, src.sf_id, src.gen_case_id)
	WHEN MATCHED AND tgt.case_id <> src.gen_case_id THEN
		UPDATE SET
			tgt.source_case_number = src.case_number,
			tgt.source_sf_id = src.sf_id,
			tgt.case_id = src.gen_case_id;

	USE [case-management];

	WITH src AS (
		SELECT
			ci.*, mk.case_id,
			-- Dates
			TRY_CONVERT(datetimeoffset, ci.CreatedDate) AS created_on_dt,
			TRY_CONVERT(datetimeoffset, ci.LastModifiedDate) AS modified_on_dt,
			TRY_CONVERT(datetimeoffset, ci.Resolved_Date_Time__c) AS resolved_dt,
			TRY_CONVERT(datetimeoffset, ci.Closed_date) AS closed_dt,
			TRY_CONVERT(datetimeoffset, ci.Transaction_Date__c) AS txn_dt,
			-- Numerics
			TRY_CONVERT(real, ci.Fund_Transfer_Bill_Payment_Amount__c) AS fund_transfer_bill_payment_amount_num,
			TRY_CONVERT(real, ci.Amount_Deposit_Withdrawal__c) AS amount_deposit_withdrawal_num,
			TRY_CONVERT(real, ci.Amount_Withdrawal_Deposit__c) AS amount_withdrawal_deposit_num,
			TRY_CONVERT(real, ci.Amount_Received_Deposit_to_Account__c) AS amount_received_deposit_to_account_num,
			TRY_CONVERT(real, ci.Deposit_Amount__c) AS deposit_amount_num,
			TRY_CONVERT(int, ci.Status_Code) AS status_code_int,
			TRY_CONVERT(real, ci.SLA_Day__c) AS sla_num,
			-- Booleans
			CASE WHEN UPPER(ci.Display_on_OneApp__c) IN ('1','Y','YES','TRUE') THEN 1 WHEN UPPER(ci.Display_on_OneApp__c) IN ('0','N','NO','FALSE') THEN 0 ELSE NULL END AS visible_on_touch_bit,
			CASE WHEN UPPER(ci.FCR__c) IN ('1','Y','YES','TRUE') THEN 1 WHEN UPPER(ci.FCR__c) IN ('0','N','NO','FALSE') THEN 0 ELSE NULL END AS fcr_bit
		FROM [case-migration].[dbo].[stg_case_inprogress] ci
		JOIN [case-migration].[dbo].[migration_case_keymap] mk
			ON (ci.CaseNumber IS NOT NULL AND mk.source_case_number = ci.CaseNumber)
			OR (ci.CaseNumber IS NULL AND ci.SF_ID IS NOT NULL AND mk.source_sf_id = ci.SF_ID)
	)
	MERGE [dbo].[case_transaction] AS tgt
	USING (
		SELECT
			case_id,
			created_on_dt, modified_on_dt, resolved_dt, closed_dt, txn_dt,
			fund_transfer_bill_payment_amount_num, amount_deposit_withdrawal_num,
			amount_withdrawal_deposit_num, amount_received_deposit_to_account_num,
			deposit_amount_num, status_code_int, sla_num,
			-- Truncate to destination lengths
			LEFT(ci.CaseNumber, 255) AS case_number,
			LEFT(ci.Call_Number__c, 10) AS call_number,
			LEFT(ci.Contact_Person_Name__c, 100) AS contact_person_name,
			LEFT(ci.Contact_Person_Phone__c, 10) AS contact_person_phone,
			LEFT(ci.Contact_Person_Phone_2__c, 10) AS contact_person_phone_2,
			LEFT(ci.Contact_Person_Email__c, 250) AS contact_person_email,
			LEFT(ci.Participant_ID__c, 50) AS participant_id,
			LEFT(ci.Prompt_Pay, 20) AS prompt_pay,
			LEFT(ci.Prompt_Pay__c, 255) AS prompt_pay_transfer_account,
			LEFT(ci.Wrong_Prompt_Pay, 255) AS wrong_prompt_pay,
			LEFT(ci.Receiver_Name__c, 255) AS receiver_name,
			LEFT(ci.Transaction_Type__Code, 50) AS transaction_type_code,
			LEFT(ci.Transaction_Type__Value, 300) AS transaction_type_value,
			LEFT(ci.Address__c, 255) AS address,
			LEFT(ci.Document_Id__c, 255) AS document_id,
			LEFT(ci.Document_Type__c, 255) AS document_type,
			LEFT(ci.EMS_Tracking_No__c, 255) AS ems_tracking,
			LEFT(ci.Object_Id__c, 255) AS object_id,
			LEFT(ci.Repository_Id__c, 255) AS repository_id,
			LEFT(ci.Staff_ID__c, 255) AS staff_id,
			LEFT(ci.Owner_Name__c, 255) AS owner_name,
			LEFT(ci.Branch_Code__c, 255) AS branch_code,
			LEFT(ci.Branch_Name__c, 255) AS branch_name,
			LEFT(ci.SMS_Code_In_progress__c, 50) AS sms_code_in_progress,
			LEFT(ci.SMS_Code_New__c, 50) AS sms_code_new,
			LEFT(ci.SMS_Code_Resolution_1__c, 50) AS sms_code_resolution_1,
			LEFT(ci.SMS_Code_Resolution_2__c, 50) AS sms_code_resolution_2,
			LEFT(ci.SMS_Code_Resolved__c, 50) AS sms_code_resolved,
			LEFT(ci.PTA_Segment__Code, 50) AS pta_segment_code,
			LEFT(ci.PTA_Segment__Value, 300) AS pta_segment_value,
			LEFT(ci.original_problem_channel_code, 50) AS original_problem_channel_code,
			LEFT(ci.original_problem_channel_value, 300) AS original_problem_channel_value,
			LEFT(ci.data_source_code, 50) AS data_source_code,
			LEFT(ci.data_source_value, 300) AS data_source_value,
			LEFT(ci.Priority_Code, 50) AS priority_code,
			LEFT(ci.Priority_Value, 300) AS priority_value,
			LEFT(ci.Integration_System, 255) AS integration_system,
			LEFT(ci.Created_Channel__c, 50) AS created_channel,
			LEFT(ci.Created_Channel_New, 50) AS created_channel_new,
			LEFT(ci.Issue_New_for_OneApp_EN__c, 250) AS issue_name_ttb_touch_en,
			LEFT(ci.Issue_New_for_OneApp__c, 250) AS issue_name_ttb_touch_th,
			LEFT(ci.Case_Issue__c, 250) AS issue_th,
			LEFT(ci.Category__Code, 50) AS service_category_code,
			LEFT(ci.Category__Value, 300) AS service_category_value,
			LEFT(ci.Product_Category__c, 255) AS service_tab_code,
			LEFT(ci.Product_Category__Value, 300) AS service_tab_value,
			LEFT(ci.Current_Service_Template__Code, 50) AS service_template_code,
			LEFT(ci.Current_Service_Template__Value, 300) AS service_template_value,
			LEFT(ci.Service_Type_Matrix_Code__c, 50) AS service_type_matrix_code,
			LEFT(ci.Product_Type_1__Code, 255) AS product_type_code_1,
			LEFT(ci.Product_Type_2__Code, 255) AS product_type_code_2,
			LEFT(ci.Product_Type_3__Code, 255) AS product_type_code_3,
			LEFT(ci.Product_Type_1__Value, 300) AS product_type_value_1,
			LEFT(ci.Product_Type_2__Value, 300) AS product_type_value_2,
			LEFT(ci.Product_Type_3__Value, 300) AS product_type_value_3,
			LEFT(ci.Product_Number_Full_1__c, 255) AS product_number_full_1,
			LEFT(ci.Product_Number_Full_2__c, 255) AS product_number_full_2,
			LEFT(ci.Product_Number_Full_3__c, 255) AS product_number_full_3,
			LEFT(ci.Product_Number_1__c, 255) AS product_number_marking_1,
			LEFT(ci.Product_Number_2__c, 255) AS product_number_marking_2,
			LEFT(ci.Product_Number_3__c, 255) AS product_number_marking_3,
			LEFT(ci.Suffix_1__c, 5) AS suffix_1,
			LEFT(ci.Suffix_2__c, 5) AS suffix_2,
			LEFT(ci.Suffix_3__c, 5) AS suffix_3,
			LEFT(ci.FundCode_1__c, 25) AS fund_code_1,
			LEFT(ci.FundCode_2__c, 25) AS fund_code_2,
			LEFT(ci.FundCode_3__c, 25) AS fund_code_3,
			LEFT(ci.Recipient_Bank__Code, 50) AS recipient_bank_code,
			LEFT(ci.Recipient_Bank__Value, 300) AS recipient_bank_value,
			LEFT(ci.Inter_Bank_Recipient_Account_No__c, 175) AS inter_bank_recipient_account_no,
			LEFT(ci.Wrong_Transfer_Account__c, 255) AS wrong_transfer_account,
			LEFT(ci.Correct_Recipient_Bank__Code, 50) AS correct_recipient_bank_code,
			LEFT(ci.Correct_Recipient_Bank__Value, 300) AS correct_recipient_bank_value,
			LEFT(ci.Correct_Bank_Recipient_Account_No__c, 175) AS correct_bank_recipient_account_no,
			LEFT(ci.Correct_Target_Account__c, 255) AS correct_target_account,
			ci.migration_lot,
			ci.is_migration,
			visible_on_touch_bit,
			fcr_bit
		FROM src ci
	) AS s
	ON (tgt.case_id = s.case_id)
	WHEN MATCHED THEN UPDATE SET
		-- Numerics
		tgt.fund_transfer_bill_payment_amount = s.fund_transfer_bill_payment_amount_num,
		tgt.amount_deposit_withdrawal = s.amount_deposit_withdrawal_num,
		tgt.amount_withdrawal_deposit = s.amount_withdrawal_deposit_num,
		tgt.amount_received_deposit_to_account = s.amount_received_deposit_to_account_num,
		tgt.deposit_amount = s.deposit_amount_num,
		tgt.sla = s.sla_num,
		-- Dates
		tgt.created_on = s.created_on_dt,
		tgt.modified_on = s.modified_on_dt,
		tgt.resolved_date = s.resolved_dt,
		tgt.closed_date = s.closed_dt,
		tgt.transaction_date = s.txn_dt,
		-- Status and flags
		tgt.status_code = s.status_code_int,
		tgt.visible_on_touch = s.visible_on_touch_bit,
		tgt.fcr = s.fcr_bit,
		-- Texts (already truncated)
		tgt.case_number = s.case_number,
		tgt.call_number = s.call_number,
		tgt.contact_person_name = s.contact_person_name,
		tgt.contact_person_phone = s.contact_person_phone,
		tgt.contact_person_phone_2 = s.contact_person_phone_2,
		tgt.contact_person_email = s.contact_person_email,
		tgt.participant_id = s.participant_id,
		tgt.prompt_pay = s.prompt_pay,
		tgt.prompt_pay_transfer_account = s.prompt_pay_transfer_account,
		tgt.wrong_prompt_pay = s.wrong_prompt_pay,
		tgt.receiver_name = s.receiver_name,
		tgt.transaction_type_code = s.transaction_type_code,
		tgt.transaction_type_value = s.transaction_type_value,
		tgt.address = s.address,
		tgt.document_id = s.document_id,
		tgt.document_type = s.document_type,
		tgt.ems_tracking = s.ems_tracking,
		tgt.object_id = s.object_id,
		tgt.repository_id = s.repository_id,
		tgt.staff_id = s.staff_id,
		tgt.owner_name = s.owner_name,
		tgt.branch_code = s.branch_code,
		tgt.branch_name = s.branch_name,
		tgt.sms_code_in_progress = s.sms_code_in_progress,
		tgt.sms_code_new = s.sms_code_new,
		tgt.sms_code_resolution_1 = s.sms_code_resolution_1,
		tgt.sms_code_resolution_2 = s.sms_code_resolution_2,
		tgt.sms_code_resolved = s.sms_code_resolved,
		tgt.pta_segment_code = s.pta_segment_code,
		tgt.pta_segment_value = s.pta_segment_value,
		tgt.original_problem_channel_code = s.original_problem_channel_code,
		tgt.original_problem_channel_value = s.original_problem_channel_value,
		tgt.data_source_code = s.data_source_code,
		tgt.data_source_value = s.data_source_value,
		tgt.priority_code = s.priority_code,
		tgt.priority_value = s.priority_value,
		tgt.integration_system = s.integration_system,
		tgt.sub_integration_system = COALESCE(s.created_channel_new, s.created_channel),
		tgt.issue_name_ttb_touch_en = s.issue_name_ttb_touch_en,
		tgt.issue_name_ttb_touch_th = s.issue_name_ttb_touch_th,
		tgt.issue_th = s.issue_th,
		tgt.service_category_code = s.service_category_code,
		tgt.service_category_value = s.service_category_value,
		tgt.service_tab_code = s.service_tab_code,
		tgt.service_tab_value = s.service_tab_value,
		tgt.service_template_code = s.service_template_code,
		tgt.service_template_value = s.service_template_value,
		tgt.service_template_type_code = s.service_type_matrix_code,
		tgt.product_type_code_1 = s.product_type_code_1,
		tgt.product_type_code_2 = s.product_type_code_2,
		tgt.product_type_code_3 = s.product_type_code_3,
		tgt.product_type_value_1 = s.product_type_value_1,
		tgt.product_type_value_2 = s.product_type_value_2,
		tgt.product_type_value_3 = s.product_type_value_3,
		tgt.product_number_full_1 = s.product_number_full_1,
		tgt.product_number_full_2 = s.product_number_full_2,
		tgt.product_number_full_3 = s.product_number_full_3,
		tgt.product_number_marking_1 = s.product_number_marking_1,
		tgt.product_number_marking_2 = s.product_number_marking_2,
		tgt.product_number_marking_3 = s.product_number_marking_3,
		tgt.suffix_1 = s.suffix_1,
		tgt.suffix_2 = s.suffix_2,
		tgt.suffix_3 = s.suffix_3,
		tgt.fund_code_1 = s.fund_code_1,
		tgt.fund_code_2 = s.fund_code_2,
		tgt.fund_code_3 = s.fund_code_3,
		tgt.recipient_bank_code = s.recipient_bank_code,
		tgt.recipient_bank_value = s.recipient_bank_value,
		tgt.inter_bank_recipient_account_no = s.inter_bank_recipient_account_no,
		tgt.wrong_transfer_account = s.wrong_transfer_account,
		tgt.correct_recipient_bank_code = s.correct_recipient_bank_code,
		tgt.correct_recipient_bank_value = s.correct_recipient_bank_value,
		tgt.correct_bank_recipient_account_no = s.correct_bank_recipient_account_no,
		tgt.correct_target_account = s.correct_target_account,
		tgt.is_migration = s.is_migration,
		tgt.migration_lot = s.migration_lot
	WHEN NOT MATCHED THEN
		INSERT (
			case_id, created_on, modified_on, resolved_date, closed_date, transaction_date,
			fund_transfer_bill_payment_amount, amount_deposit_withdrawal, amount_withdrawal_deposit, amount_received_deposit_to_account, deposit_amount,
			status_code, visible_on_touch, fcr,
			case_number, call_number, contact_person_name, contact_person_phone, contact_person_phone_2, contact_person_email,
			participant_id, prompt_pay, prompt_pay_transfer_account, wrong_prompt_pay, receiver_name,
			transaction_type_code, transaction_type_value, address, document_id, document_type, ems_tracking, object_id, repository_id,
			staff_id, owner_name, branch_code, branch_name,
			sms_code_in_progress, sms_code_new, sms_code_resolution_1, sms_code_resolution_2, sms_code_resolved,
			pta_segment_code, pta_segment_value, original_problem_channel_code, original_problem_channel_value,
			data_source_code, data_source_value, priority_code, priority_value, integration_system, sub_integration_system,
			issue_name_ttb_touch_en, issue_name_ttb_touch_th, issue_th,
			service_category_code, service_category_value, service_tab_code, service_tab_value,
			service_template_code, service_template_value, service_template_type_code,
			product_type_code_1, product_type_code_2, product_type_code_3,
			product_type_value_1, product_type_value_2, product_type_value_3,
			product_number_full_1, product_number_full_2, product_number_full_3,
			product_number_marking_1, product_number_marking_2, product_number_marking_3,
			suffix_1, suffix_2, suffix_3,
			fund_code_1, fund_code_2, fund_code_3,
			recipient_bank_code, recipient_bank_value, inter_bank_recipient_account_no, wrong_transfer_account,
			correct_recipient_bank_code, correct_recipient_bank_value, correct_bank_recipient_account_no, correct_target_account,
			is_migration, migration_lot
		) VALUES (
			s.case_id, s.created_on_dt, s.modified_on_dt, s.resolved_dt, s.closed_dt, s.txn_dt,
			s.fund_transfer_bill_payment_amount_num, s.amount_deposit_withdrawal_num, s.amount_withdrawal_deposit_num, s.amount_received_deposit_to_account_num, s.deposit_amount_num,
			s.status_code_int, s.visible_on_touch_bit, s.fcr_bit,
			s.case_number, s.call_number, s.contact_person_name, s.contact_person_phone, s.contact_person_phone_2, s.contact_person_email,
			s.participant_id, s.prompt_pay, s.prompt_pay_transfer_account, s.wrong_prompt_pay, s.receiver_name,
			s.transaction_type_code, s.transaction_type_value, s.address, s.document_id, s.document_type, s.ems_tracking, s.object_id, s.repository_id,
			s.staff_id, s.owner_name, s.branch_code, s.branch_name,
			s.sms_code_in_progress, s.sms_code_new, s.sms_code_resolution_1, s.sms_code_resolution_2, s.sms_code_resolved,
			s.pta_segment_code, s.pta_segment_value, s.original_problem_channel_code, s.original_problem_channel_value,
			s.data_source_code, s.data_source_value, s.priority_code, s.priority_value, s.integration_system, COALESCE(s.created_channel_new, s.created_channel),
			s.issue_name_ttb_touch_en, s.issue_name_ttb_touch_th, s.issue_th,
			s.service_category_code, s.service_category_value, s.service_tab_code, s.service_tab_value,
			s.service_template_code, s.service_template_value, s.service_type_matrix_code,
			s.product_type_code_1, s.product_type_code_2, s.product_type_code_3,
			s.product_type_value_1, s.product_type_value_2, s.product_type_value_3,
			s.product_number_full_1, s.product_number_full_2, s.product_number_full_3,
			s.product_number_marking_1, s.product_number_marking_2, s.product_number_marking_3,
			s.suffix_1, s.suffix_2, s.suffix_3,
			s.fund_code_1, s.fund_code_2, s.fund_code_3,
			s.recipient_bank_code, s.recipient_bank_value, s.inter_bank_recipient_account_no, s.wrong_transfer_account,
			s.correct_recipient_bank_code, s.correct_recipient_bank_value, s.correct_bank_recipient_account_no, s.correct_target_account,
			s.is_migration, s.migration_lot
		);

	COMMIT;
END TRY
BEGIN CATCH
	IF @@TRANCOUNT > 0 ROLLBACK;
	THROW;
END CATCH;
GO
