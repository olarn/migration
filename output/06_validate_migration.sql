/*
	Validation checks comparing source staging to destination
*/

SET NOCOUNT ON;

-- 1) Total cases migrated
SELECT
	(SELECT COUNT(*) FROM [case-management].[dbo].[case_transaction]) AS dest_case_count,
	(SELECT COUNT(DISTINCT COALESCE(NULLIF(ci.CaseNumber,''), NULLIF(ci.SF_ID,''))) FROM [case-migration].[dbo].[stg_case_inprogress] ci) AS src_case_count;

-- 2) Cases missing from destination
SELECT TOP 50
	COALESCE(NULLIF(ci.CaseNumber,''), NULLIF(ci.SF_ID,'')) AS source_case_key
FROM [case-migration].[dbo].[stg_case_inprogress] ci
LEFT JOIN [case-migration].[dbo].[migration_case_keymap] km
	ON (km.source_case_number = ci.CaseNumber OR km.source_sf_id = ci.SF_ID)
LEFT JOIN [case-management].[dbo].[case_transaction] ct
	ON ct.case_id = km.case_id
WHERE ct.case_id IS NULL
ORDER BY source_case_key;

-- 3) Document reference counts by case
SELECT TOP 50
	ct.case_number,
	COUNT(dr.document_reference_id) AS doc_count
FROM [case-management].[dbo].[case_transaction] ct
LEFT JOIN [case-management].[dbo].[case_transaction_document_reference] dr
	ON dr.case_id = ct.case_id
GROUP BY ct.case_number
ORDER BY doc_count DESC;

-- 4) SLA hop presence
SELECT
	COUNT(*) AS sla_hop_rows
FROM [case-management].[dbo].[case_transaction_sla_hop];
GO
