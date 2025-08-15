/*
	02 - Transform and migrate case_transaction_sla_activity (Java logic adapted)
	- Insert OWNER_CHANGE activities from stg_sla_per_owner
	- Parse dates; compute actual_duration in minutes
	- Truncate strings to destination column lengths
*/

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
	BEGIN TRAN;

	IF DB_ID('case-migration') IS NULL THROW 50000, 'Database [case-migration] not found', 1;
	IF DB_ID('case-management') IS NULL THROW 50001, 'Database [case-management] not found', 1;

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
		'CHANGE_OWNER' AS [action],
		CASE WHEN s.Start_Date_Time__c <> '' THEN TRY_CONVERT(datetimeoffset, s.Start_Date_Time__c) END AS start_date,
		CASE WHEN s.End_Date_Time__c <> '' THEN TRY_CONVERT(datetimeoffset, s.End_Date_Time__c) END AS end_date,
		CASE WHEN s.End_Date_Time__c <> '' AND s.Start_Date_Time__c <> ''
			THEN CAST(DATEDIFF(minute, TRY_CONVERT(datetimeoffset, s.Start_Date_Time__c), TRY_CONVERT(datetimeoffset, s.End_Date_Time__c)) AS real)
			ELSE NULL END AS actual_duration,
		LEFT(s.Name, 255) AS owner_name,
		LEFT(COALESCE(s.Owner_Team_NEW, s.Owner_Team__c), 255) AS team_name_en,
		LEFT(COALESCE(s.Owner_Team_NEW, s.Owner_Team__c), 255) AS team_name_th,
		TRY_CONVERT(int, s.Case_Status__c) AS status_code,
		COALESCE(TRY_CONVERT(datetimeoffset, NULLIF(s.Start_Date_Time__c,'')), SYSDATETIMEOFFSET()) AS created_on,
		COALESCE(TRY_CONVERT(datetimeoffset, NULLIF(s.End_Date_Time__c,'')), SYSDATETIMEOFFSET()) AS modified_on
	FROM [case-migration].dbo.stg_sla_per_owner s WITH (NOLOCK)
	JOIN [case-management].dbo.case_transaction ct WITH (NOLOCK)
		ON s.Case__c = ct.case_number
		AND ct.is_migration = 1
	WHERE (s.migration_lot IS NULL OR s.migration_lot IS NOT NULL)
		AND (s.record_status IS NULL OR s.record_status = 'ACTIVE');

	COMMIT;
END TRY
BEGIN CATCH
	IF @@TRANCOUNT > 0 ROLLBACK;
	THROW;
END CATCH;
GO
