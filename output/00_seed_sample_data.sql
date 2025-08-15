/*
	Seed sample data for migration simulation
	- Creates databases if missing
	- Verifies required tables exist
	- Inserts coherent sample rows into staging tables
	- Optional cleanup in destination for repeated runs
*/

SET NOCOUNT ON;
SET XACT_ABORT ON;

-- Ensure databases exist
IF DB_ID('case-migration') IS NULL
BEGIN
	EXEC('CREATE DATABASE [case-migration]');
END;
IF DB_ID('case-management') IS NULL
BEGIN
	EXEC('CREATE DATABASE [case-management]');
END;
GO

-- Guard: verify staging tables exist
IF OBJECT_ID('[case-migration].[dbo].[stg_case_inprogress]', 'U') IS NULL
	RAISERROR('Missing table: [case-migration].dbo.stg_case_inprogress. Please create it using the provided source schema DDL.', 16, 1);
IF OBJECT_ID('[case-migration].[dbo].[stg_sla_per_owner]', 'U') IS NULL
	RAISERROR('Missing table: [case-migration].dbo.stg_sla_per_owner. Please create it using the provided source schema DDL.', 16, 1);
IF OBJECT_ID('[case-migration].[dbo].[stg_case_document_reference]', 'U') IS NULL
	RAISERROR('Missing table: [case-migration].dbo.stg_case_document_reference. Please create it using the provided source schema DDL.', 16, 1);
GO

BEGIN TRY
	BEGIN TRAN;

	-- Reset previous sample data in staging
	DELETE FROM [case-migration].[dbo].[stg_case_document_reference]
		WHERE Case__c IN (N'CASE-001', N'CASE-002');
	DELETE FROM [case-migration].[dbo].[stg_sla_per_owner]
		WHERE Case__c IN (N'CASE-001', N'CASE-002');
	DELETE FROM [case-migration].[dbo].[stg_case_inprogress]
		WHERE CaseNumber IN (N'CASE-001', N'CASE-002');

	-- Insert sample cases into stg_case_inprogress
	INSERT INTO [case-migration].[dbo].[stg_case_inprogress] (
		CaseNumber, SF_ID, ParentCaseNumber,
		Case_Issue__c,
		Suffix_1__c, Suffix_2__c, Suffix_3__c,
		FundCode_1__c, FundCode_2__c, FundCode_3__c,
		Transaction_Date__c,
		Fund_Transfer_Bill_Payment_Amount__c,
		Amount_Deposit_Withdrawal__c,
		Amount_Withdrawal_Deposit__c,
		Amount_Received_Deposit_to_Account__c,
		Deposit_Amount__c,
		Recipient_Bank__Code, Recipient_Bank__Value,
		Inter_Bank_Recipient_Account_No__c,
		Wrong_Transfer_Account__c,
		Prompt_Pay, Prompt_Pay__c, Wrong_Prompt_Pay, Receiver_Name__c,
		Transaction_Type__Code, Transaction_Type__Value,
		Address__c, Document_Id__c, Document_Type__c, EMS_Tracking_No__c, Object_Id__c, Repository_Id__c,
		Staff_ID__c, Staff_Employee_ID__c, Owner_Name__c,
		Branch_Code__c, Branch_Name__c,
		Call_Number__c,
		SLA_Day__c, Display_on_OneApp__c, FCR__c,
		original_problem_channel_code, original_problem_channel_value,
		data_source_code, data_source_value,
		Priority_Code, Priority_Value,
		Integration_System,
		Created_Channel__c, Created_Channel_New,
		Issue_New_for_OneApp__c, Issue_New_for_OneApp_EN__c,
		Category__Code, Category__Value,
		Product_Category__c, Product_Category__Value,
		Current_Service_Template__Code, Current_Service_Template__Value,
		Service_Type_Matrix_Code__c,
		Product_Type_1__Code, Product_Type_1__Value, Product_Number_1__c, Product_Number_Full_1__c,
		Product_Type_2__Code, Product_Type_2__Value, Product_Number_2__c, Product_Number_Full_2__c,
		Product_Type_3__Code, Product_Type_3__Value, Product_Number_3__c, Product_Number_Full_3__c,
		CreatedDate, LastModifiedDate, Resolved_Date_Time__c, Closed_date,
		Participant_ID__c, Contact_Person_Name__c, Contact_Person_Phone__c, Contact_Person_Phone_2__c, Contact_Person_Email__c,
		Contact_Person_Channel__Code, Contact_Person_Channel__Value,
		Resolution_List__c, Resolution_List__Value, Resolution__c,
		Root_Cause_List__Code, Root_Cause_List__Value, Root_Cause__c,
		migration_lot, is_migration
	)
	VALUES
	-- CASE-001
	(N'CASE-001', N'SF-001', NULL,
	 N'PromptPay transfer issue',
	 N'01', N'02', N'03',
	 N'F001', N'F002', N'F003',
	 CONVERT(nvarchar(30), DATEADD(day, -5, SYSDATETIME()), 126),
	 N'1500.75',
	 N'200.00',
	 N'50.00',
	 N'300.00',
	 N'100.00',
	 N'RB001', N'RecBank A',
	 N'123-456-789',
	 N'WT-ACC-1',
	 N'0812345678', N'PP-ACC-001', N'0', N'John Receiver',
	 N'TX01', N'Transfer',
	 N'123 Main St', N'DOC-001', N'ECM', N'EMS-001', N'OBJ-001', N'REPO-001',
	 N'STAFF-001', N'EMP-001', N'Owner A',
	 N'001', N'Bangkok HQ',
	 N'CALL-001',
	 N'2', N'true', N'false',
	 N'OPC01', N'One App',
	 N'DS01', N'CRM',
	 N'P1', N'High',
	 N'SF',
	 N'TOUCH', N'TOUCH_NEW',
	 N'Issue TH', N'Issue EN',
	 N'CAT01', N'Category 1',
	 N'PS01', N'Product Service 1',
	 N'ST01', N'Service Template 1',
	 N'STM01',
	 N'PTC1', N'Product Type 1', N'PN1', N'PNF1',
	 N'PTC2', N'Product Type 2', N'PN2', N'PNF2',
	 N'PTC3', N'Product Type 3', N'PN3', N'PNF3',
	 CONVERT(nvarchar(30), DATEADD(day, -10, SYSDATETIME()), 126),
	 CONVERT(nvarchar(30), DATEADD(day, -1, SYSDATETIME()), 126),
	 CONVERT(nvarchar(30), DATEADD(day, -2, SYSDATETIME()), 126),
	 CONVERT(nvarchar(30), DATEADD(day, -1, SYSDATETIME()), 126),
	 N'PART-001', N'Jane Doe', N'0890000001', N'0890000002', N'jane@example.com',
	 N'CH01', N'Phone',
	 N'Resolution A', N'Other', N'Please call back',
	 N'RC01', N'Other', N'Card Damaged',
	 N'LOT-TEST', 1),
	-- CASE-002
	(N'CASE-002', N'SF-002', N'CASE-001',
	 N'ATM cash deposit missing',
	 N'11', N'12', N'13',
	 N'G001', N'G002', N'G003',
	 CONVERT(nvarchar(30), DATEADD(day, -3, SYSDATETIME()), 126),
	 N'2500.00',
	 N'400.00',
	 N'75.50',
	 N'500.00',
	 N'200.00',
	 N'RB002', N'RecBank B',
	 N'987-654-321',
	 N'WT-ACC-2',
	 N'0823456789', N'PP-ACC-002', N'1', N'Mary Receiver',
	 N'TX02', N'Bill Payment',
	 N'456 Second St', N'DOC-002', N'ECM', N'EMS-002', N'OBJ-002', N'REPO-002',
	 N'STAFF-002', N'EMP-002', N'Owner B',
	 N'002', N'Chiang Mai',
	 N'CALL-002',
	 N'3', N'false', N'true',
	 N'OPC02', N'Branch',
	 N'DS02', N'Integration',
	 N'P2', N'Medium',
	 N'SF',
	 N'BRANCH', N'BRANCH_NEW',
	 N'Issue2 TH', N'Issue2 EN',
	 N'CAT02', N'Category 2',
	 N'PS02', N'Product Service 2',
	 N'ST02', N'Service Template 2',
	 N'STM02',
	 N'QTC1', N'Q Type 1', N'QPN1', N'QPNF1',
	 N'QTC2', N'Q Type 2', N'QPN2', N'QPNF2',
	 N'QTC3', N'Q Type 3', N'QPN3', N'QPNF3',
	 CONVERT(nvarchar(30), DATEADD(day, -8, SYSDATETIME()), 126),
	 CONVERT(nvarchar(30), DATEADD(day, -2, SYSDATETIME()), 126),
	 CONVERT(nvarchar(30), DATEADD(day, -1, SYSDATETIME()), 126),
	 CONVERT(nvarchar(30), SYSDATETIME(), 126),
	 N'PART-002', N'John Smith', N'0870000001', N'0870000002', N'john@example.com',
	 N'CH02', N'App',
	 N'Resolution B', N'R2', N'',
	 N'RC02', N'Root cause desc', N'',
	 N'LOT-TEST', 1);

	-- Insert sample document references
	INSERT INTO [case-migration].[dbo].[stg_case_document_reference] (
		Case__c, Name, ECM_App_ID__c, File_Name__c, Object_ID__c, Repository__c, Document_Type__c,
		ECM_MS_Doctype_Key__c, Update_ECM_Status__c, CreatedDate, LastModifiedDate, migration_lot, record_status
	) VALUES
	(N'CASE-001', N'ID Card', N'ECM-APP-1', N'idcard.pdf', N'OB-1', N'REPO-1', N'ID', N'DOC-TYPE-1', N'1',
	 CONVERT(nvarchar(30), DATEADD(day, -10, SYSDATETIME()), 126), CONVERT(nvarchar(30), DATEADD(day, -1, SYSDATETIME()), 126), N'LOT-TEST', N'ACTIVE'),
	(N'CASE-002', N'Slip', N'ECM-APP-2', N'slip.jpg', N'OB-2', N'REPO-2', N'SLIP', N'DOC-TYPE-2', N'1',
	 CONVERT(nvarchar(30), DATEADD(day, -8, SYSDATETIME()), 126), CONVERT(nvarchar(30), SYSDATETIME(), 126), N'LOT-TEST', N'ACTIVE');

	-- Insert sample SLA per owner rows
	INSERT INTO [case-migration].[dbo].[stg_sla_per_owner] (
		Case__c, Owner_Team__c, Owner_Team_NEW, Name, Employee_ID__c,
		Start_Date_Time__c, End_Date_Time__c, Case_Status__c, migration_lot, record_status
	) VALUES
	(N'CASE-001', N'TEAM-A', N'TEAM-A-NEW', N'Owner A1', N'EMP-001',
	 CONVERT(nvarchar(30), DATEADD(day, -5, SYSDATETIME()), 126), CONVERT(nvarchar(30), DATEADD(day, -4, SYSDATETIME()), 126), N'100', N'LOT-TEST', N'ACTIVE'),
	(N'CASE-001', N'TEAM-B', N'TEAM-B-NEW', N'Owner A2', N'EMP-002',
	 CONVERT(nvarchar(30), DATEADD(day, -4, SYSDATETIME()), 126), CONVERT(nvarchar(30), DATEADD(day, -2, SYSDATETIME()), 126), N'200', N'LOT-TEST', N'ACTIVE'),
	(N'CASE-002', N'TEAM-C', N'TEAM-C-NEW', N'Owner B1', N'EMP-010',
	 CONVERT(nvarchar(30), DATEADD(day, -3, SYSDATETIME()), 126), CONVERT(nvarchar(30), DATEADD(day, -1, SYSDATETIME()), 126), N'300', N'LOT-TEST', N'ACTIVE');

	COMMIT;
END TRY
BEGIN CATCH
	IF @@TRANCOUNT > 0 ROLLBACK;
	THROW;
END CATCH;
GO

-- Optional cleanup in destination for repeated runs (ignores if tables are missing)
IF OBJECT_ID('[case-management].[dbo].[case_transaction]', 'U') IS NOT NULL
BEGIN
	DELETE ct
	FROM [case-management].[dbo].[case_transaction] ct
	WHERE ct.case_number IN (N'CASE-001', N'CASE-002');
END;
IF OBJECT_ID('[case-management].[dbo].[case_transaction_document_reference]', 'U') IS NOT NULL
BEGIN
	DELETE dr
	FROM [case-management].[dbo].[case_transaction_document_reference] dr
	JOIN [case-management].[dbo].[case_transaction] ct ON ct.case_id = dr.case_id
	WHERE ct.case_number IN (N'CASE-001', N'CASE-002');
END;
IF OBJECT_ID('[case-management].[dbo].[case_transaction_sla_activity]', 'U') IS NOT NULL
BEGIN
	DELETE sa
	FROM [case-management].[dbo].[case_transaction_sla_activity] sa
	JOIN [case-management].[dbo].[case_transaction] ct ON ct.case_id = sa.case_id
	WHERE ct.case_number IN (N'CASE-001', N'CASE-002');
END;
IF OBJECT_ID('[case-management].[dbo].[case_transaction_sla_hop]', 'U') IS NOT NULL
BEGIN
	DELETE sh
	FROM [case-management].[dbo].[case_transaction_sla_hop] sh
	JOIN [case-management].[dbo].[case_transaction] ct ON ct.case_id = sh.case_id
	WHERE ct.case_number IN (N'CASE-001', N'CASE-002');
END;
IF OBJECT_ID('[case-management].[dbo].[case_transaction_comment]', 'U') IS NOT NULL
BEGIN
	DELETE cm
	FROM [case-management].[dbo].[case_transaction_comment] cm
	JOIN [case-management].[dbo].[case_transaction] ct ON ct.case_id = cm.case_id
	WHERE ct.case_number IN (N'CASE-001', N'CASE-002');
END;
GO

PRINT 'Sample data seeded. Now run the migration scripts in order: 01 -> 02 -> 03 -> 04 -> 05 -> 06.';
