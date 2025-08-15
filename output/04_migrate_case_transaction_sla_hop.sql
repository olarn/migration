/*
	Migrate case_transaction_sla_hop from stg_sla_per_owner
	- Uses deterministic GUID for case_sla_hop_id
*/

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
	BEGIN TRAN;

	IF DB_ID('case-migration') IS NULL THROW 50000, 'Database [case-migration] not found', 1;
	IF DB_ID('case-management') IS NULL THROW 50001, 'Database [case-management] not found', 1;

	USE [case-management];

	WITH base AS (
		SELECT
			mk.case_id,
			so.Case__c,
			so.Owner_Team__c,
			so.Start_Date_Time__c,
			so.End_Date_Time__c,
			COALESCE(so.Owner_Team_NEW, so.Owner_Team__c) AS team_name,
			so.Name AS owner_name,
			TRY_CONVERT(int, so.Case_Status__c) AS status_code,
			TRY_CONVERT(datetimeoffset, so.Start_Date_Time__c) AS start_datetime,
			TRY_CONVERT(datetimeoffset, so.End_Date_Time__c) AS end_datetime,
			CASE WHEN TRY_CONVERT(datetimeoffset, so.Start_Date_Time__c) IS NOT NULL AND TRY_CONVERT(datetimeoffset, so.End_Date_Time__c) IS NOT NULL
				THEN TRY_CONVERT(real, DATEDIFF(minute, TRY_CONVERT(datetimeoffset, so.Start_Date_Time__c), TRY_CONVERT(datetimeoffset, so.End_Date_Time__c)))
				ELSE NULL END AS total_duration
		FROM [case-migration].[dbo].[stg_sla_per_owner] so
		LEFT JOIN [case-migration].[dbo].[migration_case_keymap] mk
			ON mk.source_case_number = so.Case__c
		WHERE mk.case_id IS NOT NULL
	),
	ids AS (
		SELECT *,
			CONVERT(varchar(32), HASHBYTES('MD5', UPPER(CONCAT(ISNULL(Case__c,''),'|',ISNULL(Owner_Team__c,''),'|',ISNULL(Start_Date_Time__c,''),'|',ISNULL(End_Date_Time__c,'')))), 2) AS md5hex
		FROM base
	),
	ids_fmt AS (
		SELECT *,
			LOWER(STUFF(STUFF(STUFF(STUFF(md5hex, 9, 0, '-'), 14, 0, '-'), 19, 0, '-'), 24, 0, '-')) AS guid_str
		FROM ids
	)
	MERGE [dbo].[case_transaction_sla_hop] AS tgt
	USING (
		SELECT
			CONVERT(uniqueidentifier, guid_str) AS case_sla_hop_id,
			case_id,
			start_datetime,
			end_datetime,
			team_name,
			owner_name,
			status_code,
			total_duration
		FROM ids_fmt
	) AS s
	ON (tgt.case_sla_hop_id = s.case_sla_hop_id)
	WHEN MATCHED THEN UPDATE SET
		tgt.case_id = s.case_id,
		tgt.start_datetime = s.start_datetime,
		tgt.end_datetime = s.end_datetime,
		tgt.team_name = s.team_name,
		tgt.owner_name = s.owner_name,
		tgt.status_code = s.status_code,
		tgt.total_duration = s.total_duration
	WHEN NOT MATCHED THEN INSERT (
		case_sla_hop_id, case_id, start_datetime, end_datetime, team_name, owner_name, status_code, total_duration
	) VALUES (
		s.case_sla_hop_id, s.case_id, s.start_datetime, s.end_datetime, s.team_name, s.owner_name, s.status_code, s.total_duration
	);

	COMMIT;
END TRY
BEGIN CATCH
	IF @@TRANCOUNT > 0 ROLLBACK;
	THROW;
END CATCH;
GO
