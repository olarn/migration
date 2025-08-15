/*
	03 - Transform and migrate case_transaction_document_reference (Java logic adapted)
	- Deterministic document_reference_id (MD5 of case+object+file)
	- Truncate strings to destination lengths
*/

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
	BEGIN TRAN;

	IF DB_ID('case-migration') IS NULL THROW 50000, 'Database [case-migration] not found', 1;
	IF DB_ID('case-management') IS NULL THROW 50001, 'Database [case-management] not found', 1;

	WITH base AS (
		SELECT
			mk.case_id,
			dr.Case__c,
			dr.Object_ID__c,
			dr.File_Name__c,
			LEFT(dr.ECM_App_ID__c, 255) AS emp_app_id,
			LEFT(dr.ECM_MS_Doctype_Key__c, 255) AS emp_ms_doc_type_key,
			LEFT(dr.Repository__c, 255) AS repository,
			TRY_CONVERT(int, dr.Update_ECM_Status__c) AS status_code,
			TRY_CONVERT(datetimeoffset, dr.CreatedDate) AS created_on,
			TRY_CONVERT(datetimeoffset, dr.LastModifiedDate) AS modified_on
		FROM [case-migration].[dbo].[stg_case_document_reference] dr
		JOIN [case-migration].[dbo].[migration_case_keymap] mk
			ON mk.source_case_number = dr.Case__c
	), ids AS (
		SELECT *, CONVERT(varchar(32), HASHBYTES('MD5', UPPER(CONCAT(ISNULL(Case__c,''),'|',ISNULL(Object_ID__c,''),'|',ISNULL(File_Name__c,'')))), 2) AS md5hex
		FROM base
	), ids_fmt AS (
		SELECT *, LOWER(STUFF(STUFF(STUFF(STUFF(md5hex, 9, 0, '-'), 14, 0, '-'), 19, 0, '-'), 24, 0, '-')) AS guid_str
		FROM ids
	)
	MERGE [case-management].dbo.case_transaction_document_reference AS tgt
	USING (
		SELECT
			CONVERT(uniqueidentifier, guid_str) AS document_reference_id,
			case_id,
			LEFT(File_Name__c, 255) AS file_name,
			LEFT(Object_ID__c, 255) AS object_document_id,
			emp_app_id,
			emp_ms_doc_type_key,
			repository,
			status_code,
			created_on,
			modified_on
		FROM ids_fmt
	) AS s
	ON (tgt.document_reference_id = s.document_reference_id)
	WHEN MATCHED THEN UPDATE SET
		tgt.case_id = s.case_id,
		tgt.emp_app_id = s.emp_app_id,
		tgt.emp_ms_doc_type_key = s.emp_ms_doc_type_key,
		tgt.file_name = s.file_name,
		tgt.object_document_id = s.object_document_id,
		tgt.repository = s.repository,
		tgt.status_code = s.status_code,
		tgt.created_on = s.created_on,
		tgt.modified_on = s.modified_on
	WHEN NOT MATCHED THEN INSERT (
		document_reference_id, case_id, emp_app_id, emp_ms_doc_type_key, file_name, object_document_id, repository, status_code, created_on, modified_on
	) VALUES (
		s.document_reference_id, s.case_id, s.emp_app_id, s.emp_ms_doc_type_key, s.file_name, s.object_document_id, s.repository, s.status_code, s.created_on, s.modified_on
	);

	COMMIT;
END TRY
BEGIN CATCH
	IF @@TRANCOUNT > 0 ROLLBACK;
	THROW;
END CATCH;
GO
