-- =====================================================
-- Case Transaction Comment Migration Script
-- Source: [case-migration].dbo.stg_case_inprogress (derived comments)
-- Destination: [case-management].dbo.case_transaction_comment
-- =====================================================

-- Disable constraints and indexes for faster bulk load
ALTER TABLE [case-management].dbo.case_transaction_comment NOCHECK CONSTRAINT ALL;
ALTER INDEX ALL ON [case-management].dbo.case_transaction_comment DISABLE;

-- Set minimal logging for bulk operations
ALTER DATABASE [case-management] SET RECOVERY BULK_LOGGED;

-- Bulk insert with parallel processing
INSERT INTO [case-management].dbo.case_transaction_comment (
    case_transaction_comment_id,
    case_id,
    comment_text,
    comment_type,
    created_by_id,
    created_by_name,
    created_on,
    modified_on
)
SELECT 
    NEWID() as case_transaction_comment_id,
    ct.case_id,
    CASE 
        WHEN s.Resolution__c IS NOT NULL AND s.Resolution__c != '' 
        THEN 'Resolution: ' + s.Resolution__c
        WHEN s.Root_Cause__c IS NOT NULL AND s.Root_Cause__c != '' 
        THEN 'Root Cause: ' + s.Root_Cause__c
        WHEN s.Description__c IS NOT NULL AND s.Description__c != '' 
        THEN 'Description: ' + s.Description__c
        ELSE 'Case migrated from legacy system - Case Number: ' + s.CaseNumber
    END as comment_text,
    'MIGRATION' as comment_type,
    NULL as created_by_id, -- Will be mapped later
    COALESCE(s.Created_Name__c, s.Staff_ID__c, 'SYSTEM') as created_by_name,
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
FROM [case-migration].dbo.stg_case_inprogress s WITH (NOLOCK)
INNER JOIN [case-management].dbo.case_transaction ct WITH (NOLOCK)
    ON s.CaseNumber = ct.case_number
    AND ct.is_migration = 1
WHERE s.migration_lot IS NOT NULL
    AND (
        s.Resolution__c IS NOT NULL OR 
        s.Root_Cause__c IS NOT NULL OR 
        s.Description__c IS NOT NULL
    )
OPTION (MAXDOP 4, OPTIMIZE FOR UNKNOWN);

-- Re-enable constraints and indexes
ALTER INDEX ALL ON [case-management].dbo.case_transaction_comment REBUILD;
ALTER TABLE [case-management].dbo.case_transaction_comment WITH CHECK CHECK CONSTRAINT ALL;

-- Reset database recovery model
ALTER DATABASE [case-management] SET RECOVERY FULL;

-- Display migration summary
SELECT 
    'Case Transaction Comment Migration Complete' as Status,
    COUNT(*) as Records_Migrated,
    GETDATE() as Migration_Completed_At
FROM [case-management].dbo.case_transaction_comment c
INNER JOIN [case-management].dbo.case_transaction ct ON c.case_id = ct.case_id
WHERE ct.is_migration = 1;
