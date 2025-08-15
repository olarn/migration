-- =====================================================
-- Case Transaction Comment Migration Script
-- Source: [case-migration].dbo.stg_case_inprogress
-- Destination: [case-management].dbo.case_transaction_comment
-- =====================================================

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
		CONCAT(
			'Resolution: ', COALESCE(NULLIF(s.Resolution__c, ''), '-'), CHAR(13), CHAR(10),
			'Root Cause: ', COALESCE(NULLIF(s.Root_Cause__c, ''), '-')
		) AS comment_desc,
		s.CreatedBy_Employee_ID__c AS employee_id,
		s.Created_By_Team_New AS team_name_th,
		s.Created_By_Team_New AS team_name_en,
		TRY_CAST(s.Status_Code AS int) AS status_code,
		CASE 
			WHEN s.CreatedDate IS NOT NULL AND s.CreatedDate != '' 
			THEN TRY_CAST(s.CreatedDate AS datetimeoffset)
			ELSE SYSDATETIMEOFFSET() 
		END AS created_on,
		NULL AS created_by_id,
		CASE 
			WHEN s.LastModifiedDate IS NOT NULL AND s.LastModifiedDate != '' 
			THEN TRY_CAST(s.LastModifiedDate AS datetimeoffset)
			ELSE SYSDATETIMEOFFSET() 
		END AS modified_on,
		NULL AS modified_by_id
	FROM [case-migration].dbo.stg_case_inprogress s WITH (NOLOCK)
	INNER JOIN [case-management].dbo.case_transaction ct WITH (NOLOCK)
		ON s.CaseNumber = ct.case_number
		AND ct.is_migration = 1
	WHERE s.migration_lot IS NOT NULL;

	COMMIT;
END TRY
BEGIN CATCH
	IF @@TRANCOUNT > 0 ROLLBACK;
	THROW;
END CATCH;

-- Display migration summary
SELECT 
	'Case Transaction Comment Migration Complete' as Status,
	COUNT(*) as Records_Migrated,
	SYSDATETIMEOFFSET() as Migration_Completed_At
FROM [case-management].dbo.case_transaction_comment c
INNER JOIN [case-management].dbo.case_transaction ct ON c.case_id = ct.case_id
WHERE ct.is_migration = 1;
