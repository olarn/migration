/*
	05 - Transform and migrate case_transaction_comment (Java logic adapted)
	- Build comment text from resolution/root cause; truncate to 500
*/

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
	BEGIN TRAN;

	INSERT INTO [case-management].dbo.case_transaction_comment (
		comment_id,
		case_id,
		team_id,
		comment_desc,
		employee_id,
		team_name_th,
		team_name_en,
		status_code,
		created_on,
		created_by_id,
		modified_on,
		modified_by_id
	)
	SELECT 
		NEWID() AS comment_id,
		ct.case_id,
		NULL AS team_id,
		LEFT(
			CONCAT('Resolution: ', COALESCE(NULLIF(s.Resolution__c, ''), '-'), CHAR(13), CHAR(10),
			       'Root Cause: ', COALESCE(NULLIF(s.Root_Cause__c, ''), '-')
			), 500) AS comment_desc,
		LEFT(s.CreatedBy_Employee_ID__c, 255) AS employee_id,
		LEFT(COALESCE(s.Created_By_Team_New, s.Created_By_Team__c), 255) AS team_name_th,
		LEFT(COALESCE(s.Created_By_Team_New, s.Created_By_Team__c), 255) AS team_name_en,
		TRY_CONVERT(int, s.Status_Code) AS status_code,
		COALESCE(TRY_CONVERT(datetimeoffset, NULLIF(s.CreatedDate,'')), SYSDATETIMEOFFSET()) AS created_on,
		NULL AS created_by_id,
		COALESCE(TRY_CONVERT(datetimeoffset, NULLIF(s.LastModifiedDate,'')), SYSDATETIMEOFFSET()) AS modified_on,
		NULL AS modified_by_id
	FROM [case-migration].dbo.stg_case_inprogress s WITH (NOLOCK)
	JOIN [case-management].dbo.case_transaction ct WITH (NOLOCK)
		ON s.CaseNumber = ct.case_number
		AND ct.is_migration = 1;

	COMMIT;
END TRY
BEGIN CATCH
	IF @@TRANCOUNT > 0 ROLLBACK;
	THROW;
END CATCH;
GO
