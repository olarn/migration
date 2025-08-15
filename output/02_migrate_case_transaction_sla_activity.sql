-- =====================================================
-- Case Transaction SLA Activity Migration Script
-- Source: [case-migration].dbo.stg_sla_per_owner
-- Destination: [case-management].dbo.case_transaction_sla_activity
-- =====================================================

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
	BEGIN TRAN;

	-- Bulk insert SLA activity as OWNER_CHANGE events derived from owner hops
	INSERT INTO [case-management].dbo.case_transaction_sla_activity (
		case_sla_activity_id,
		case_id,
		[action],
		start_date,
		end_date,
		actual_duration,
		owner_name,
		team_name_en,
		team_name_th,
		status_code,
		created_on,
		modified_on
	)
	SELECT 
		NEWID() AS case_sla_activity_id,
		ct.case_id,
		'OWNER_CHANGE' AS [action],
		CASE 
			WHEN s.Start_Date_Time__c IS NOT NULL AND s.Start_Date_Time__c != '' 
			THEN TRY_CAST(s.Start_Date_Time__c AS datetimeoffset)
			ELSE NULL 
		END AS start_date,
		CASE 
			WHEN s.End_Date_Time__c IS NOT NULL AND s.End_Date_Time__c != '' 
			THEN TRY_CAST(s.End_Date_Time__c AS datetimeoffset)
			ELSE NULL 
		END AS end_date,
		CASE 
			WHEN s.End_Date_Time__c IS NOT NULL AND s.Start_Date_Time__c IS NOT NULL 
			AND s.End_Date_Time__c != '' AND s.Start_Date_Time__c != ''
			THEN CAST(DATEDIFF(MINUTE, TRY_CAST(s.Start_Date_Time__c AS datetimeoffset), TRY_CAST(s.End_Date_Time__c AS datetimeoffset)) AS real)
			ELSE NULL 
		END AS actual_duration,
		s.Name AS owner_name,
		COALESCE(s.Owner_Team_NEW, s.Owner_Team__c) AS team_name_en,
		COALESCE(s.Owner_Team_NEW, s.Owner_Team__c) AS team_name_th,
		TRY_CAST(s.Case_Status__c AS int) AS status_code,
		CASE 
			WHEN s.Start_Date_Time__c IS NOT NULL AND s.Start_Date_Time__c != '' 
			THEN TRY_CAST(s.Start_Date_Time__c AS datetimeoffset)
			ELSE SYSDATETIMEOFFSET() 
		END AS created_on,
		CASE 
			WHEN s.End_Date_Time__c IS NOT NULL AND s.End_Date_Time__c != '' 
			THEN TRY_CAST(s.End_Date_Time__c AS datetimeoffset)
			ELSE SYSDATETIMEOFFSET() 
		END AS modified_on
	FROM [case-migration].dbo.stg_sla_per_owner s WITH (NOLOCK)
	INNER JOIN [case-management].dbo.case_transaction ct WITH (NOLOCK)
		ON s.Case__c = ct.case_number
		AND ct.is_migration = 1
	WHERE s.migration_lot IS NOT NULL
		AND s.record_status = 'ACTIVE';

	COMMIT;
END TRY
BEGIN CATCH
	IF @@TRANCOUNT > 0 ROLLBACK;
	THROW;
END CATCH;

-- Display migration summary
SELECT 
	'Case Transaction SLA Activity Migration Complete' as Status,
	COUNT(*) as Records_Migrated,
	SYSDATETIMEOFFSET() as Migration_Completed_At
FROM [case-management].dbo.case_transaction_sla_activity s
INNER JOIN [case-management].dbo.case_transaction ct ON s.case_id = ct.case_id
WHERE ct.is_migration = 1;
