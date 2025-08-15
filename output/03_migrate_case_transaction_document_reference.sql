-- =====================================================
-- Case Transaction Document Reference Migration Script
-- Source: [case-migration].dbo.stg_case_document_reference
-- Destination: [case-management].dbo.case_transaction_document_reference
-- =====================================================

-- Disable constraints and indexes for faster bulk load
ALTER TABLE [case-management].dbo.case_transaction_document_reference NOCHECK CONSTRAINT ALL;
ALTER INDEX ALL ON [case-management].dbo.case_transaction_document_reference DISABLE;

-- Set minimal logging for bulk operations
ALTER DATABASE [case-management] SET RECOVERY BULK_LOGGED;

-- Bulk insert with parallel processing
INSERT INTO [case-management].dbo.case_transaction_document_reference (
    case_transaction_document_reference_id,
    case_id,
    document_name,
    document_type,
    document_reference,
    file_name,
    object_id,
    repository_id,
    created_on,
    modified_on
)
SELECT 
    NEWID() as case_transaction_document_reference_id,
    ct.case_id,
    s.Name as document_name,
    s.Document_Type__c as document_type,
    s.ECM_App_ID__c as document_reference,
    s.File_Name__c as file_name,
    s.Object_ID__c as object_id,
    s.Repository__c as repository_id,
    CASE 
        WHEN s.CreatedDate IS NOT NULL AND s.CreatedDate != '' 
        THEN TRY_CAST(s.CreatedDate AS datetimeoffset)
        ELSE GETDATE() 
    END as created_on,
    CASE 
        WHEN s.LastModifiedDate IS NOT NULL AND s.LastModifiedDate != '' 
        THEN TRY_CAST(s.LastModifiedDate AS datetimeoffset)
        ELSE GETDATE() 
    END as modified_on
FROM [case-migration].dbo.stg_case_document_reference s WITH (NOLOCK)
INNER JOIN [case-management].dbo.case_transaction ct WITH (NOLOCK)
    ON s.Case__c = ct.case_number
    AND ct.is_migration = 1
WHERE s.migration_lot IS NOT NULL
    AND s.record_status = 'ACTIVE'
OPTION (MAXDOP 4, OPTIMIZE FOR UNKNOWN);

-- Re-enable constraints and indexes
ALTER INDEX ALL ON [case-management].dbo.case_transaction_document_reference REBUILD;
ALTER TABLE [case-management].dbo.case_transaction_document_reference WITH CHECK CHECK CONSTRAINT ALL;

-- Reset database recovery model
ALTER DATABASE [case-management] SET RECOVERY FULL;

-- Display migration summary
SELECT 
    'Case Transaction Document Reference Migration Complete' as Status,
    COUNT(*) as Records_Migrated,
    GETDATE() as Migration_Completed_At
FROM [case-management].dbo.case_transaction_document_reference d
INNER JOIN [case-management].dbo.case_transaction ct ON d.case_id = ct.case_id
WHERE ct.is_migration = 1;
