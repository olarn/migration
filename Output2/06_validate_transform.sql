/*
	06 - Validation for transformed migration
*/

SET NOCOUNT ON;

SELECT COUNT(*) AS dest_cases FROM [case-management].dbo.case_transaction;
SELECT COUNT(*) AS dest_doc_refs FROM [case-management].dbo.case_transaction_document_reference;
SELECT COUNT(*) AS dest_sla_activities FROM [case-management].dbo.case_transaction_sla_activity;
SELECT COUNT(*) AS dest_sla_hops FROM [case-management].dbo.case_transaction_sla_hop;
SELECT COUNT(*) AS dest_comments FROM [case-management].dbo.case_transaction_comment;

-- Check for missing cases
SELECT TOP 50 COALESCE(NULLIF(ci.CaseNumber,''), NULLIF(ci.SF_ID,'')) AS source_case_key
FROM [case-migration].[dbo].[stg_case_inprogress] ci
LEFT JOIN [case-migration].[dbo].[migration_case_keymap] km
	ON (km.source_case_number = ci.CaseNumber OR km.source_sf_id = ci.SF_ID)
LEFT JOIN [case-management].[dbo].[case_transaction] ct
	ON ct.case_id = km.case_id
WHERE ct.case_id IS NULL;
GO
