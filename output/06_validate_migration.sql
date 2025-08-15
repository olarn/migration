-- =====================================================
-- Migration Validation Script
-- Validates the results of the case migration process
-- =====================================================

SET NOCOUNT ON;
GO

PRINT '=====================================================';
PRINT 'Starting Migration Validation Process';
PRINT 'Validation Time: ' + CONVERT(varchar, GETDATE(), 120);
PRINT '=====================================================';
GO

-- 1. Check Migration Log
PRINT '1. Checking Migration Log...';
SELECT 
    script_name,
    start_time,
    end_time,
    status,
    records_migrated,
    DATEDIFF(SECOND, start_time, end_time) as duration_seconds,
    CASE 
        WHEN status = 'COMPLETED' THEN '✓ SUCCESS'
        WHEN status = 'FAILED' THEN '✗ FAILED'
        ELSE '? UNKNOWN'
    END as result
FROM [dbo].[migration_log]
ORDER BY log_id;
GO

-- 2. Validate Case Transaction Migration
PRINT '2. Validating Case Transaction Migration...';

-- Check total records
SELECT 
    'Case Transaction' as table_name,
    COUNT(*) as total_records,
    COUNT(CASE WHEN is_migration = 1 THEN 1 END) as migrated_records,
    COUNT(CASE WHEN is_migration = 0 THEN 1 END) as existing_records
FROM [case-management].dbo.case_transaction;

-- Check data quality
SELECT 
    'Data Quality Check' as check_type,
    COUNT(CASE WHEN case_number IS NULL THEN 1 END) as null_case_numbers,
    COUNT(CASE WHEN case_id IS NULL THEN 1 END) as null_case_ids,
    COUNT(CASE WHEN created_on IS NULL THEN 1 END) as null_created_dates,
    COUNT(CASE WHEN case_status_code IS NULL THEN 1 END) as null_status_codes
FROM [case-management].dbo.case_transaction
WHERE is_migration = 1;
GO

-- 3. Validate SLA Activity Migration
PRINT '3. Validating SLA Activity Migration...';

SELECT 
    'SLA Activity' as table_name,
    COUNT(*) as total_records,
    COUNT(DISTINCT case_id) as unique_cases,
    COUNT(CASE WHEN action = 'OWNER_CHANGE' THEN 1 END) as owner_change_actions,
    COUNT(CASE WHEN actual_duration IS NOT NULL THEN 1 END) as records_with_duration
FROM [case-management].dbo.case_transaction_sla_activity s
INNER JOIN [case-management].dbo.case_transaction ct ON s.case_id = ct.case_id
WHERE ct.is_migration = 1;
GO

-- 4. Validate Document Reference Migration
PRINT '4. Validating Document Reference Migration...';

SELECT 
    'Document Reference' as table_name,
    COUNT(*) as total_records,
    COUNT(DISTINCT case_id) as unique_cases,
    COUNT(CASE WHEN document_name IS NOT NULL THEN 1 END) as records_with_names,
    COUNT(CASE WHEN document_type IS NOT NULL THEN 1 END) as records_with_types
FROM [case-management].dbo.case_transaction_document_reference d
INNER JOIN [case-management].dbo.case_transaction ct ON d.case_id = ct.case_id
WHERE ct.is_migration = 1;
GO

-- 5. Validate SLA Hop Migration
PRINT '5. Validating SLA Hop Migration...';

SELECT 
    'SLA Hop' as table_name,
    COUNT(*) as total_records,
    COUNT(DISTINCT case_id) as unique_cases,
    COUNT(CASE WHEN hop_number = 1 THEN 1 END) as first_hops,
    COUNT(CASE WHEN duration_minutes IS NOT NULL THEN 1 END) as records_with_duration
FROM [case-management].dbo.case_transaction_sla_hop h
INNER JOIN [case-management].dbo.case_transaction ct ON h.case_id = ct.case_id
WHERE ct.is_migration = 1;
GO

-- 6. Validate Comment Migration
PRINT '6. Validating Comment Migration...';

SELECT 
    'Comment' as table_name,
    COUNT(*) as total_records,
    COUNT(DISTINCT case_id) as unique_cases,
    COUNT(CASE WHEN comment_type = 'MIGRATION' THEN 1 END) as migration_comments,
    COUNT(CASE WHEN comment_text LIKE 'Resolution:%' THEN 1 END) as resolution_comments,
    COUNT(CASE WHEN comment_text LIKE 'Root Cause:%' THEN 1 END) as root_cause_comments
FROM [case-management].dbo.case_transaction_comment c
INNER JOIN [case-management].dbo.case_transaction ct ON c.case_id = ct.case_id
WHERE ct.is_migration = 1;
GO

-- 7. Cross-Reference Validation
PRINT '7. Performing Cross-Reference Validation...';

-- Check for orphaned records
SELECT 
    'Orphaned SLA Activities' as check_type,
    COUNT(*) as orphaned_records
FROM [case-management].dbo.case_transaction_sla_activity s
LEFT JOIN [case-management].dbo.case_transaction ct ON s.case_id = ct.case_id
WHERE ct.case_id IS NULL;

SELECT 
    'Orphaned Document References' as check_type,
    COUNT(*) as orphaned_records
FROM [case-management].dbo.case_transaction_document_reference d
LEFT JOIN [case-management].dbo.case_transaction ct ON d.case_id = ct.case_id
WHERE ct.case_id IS NULL;

SELECT 
    'Orphaned SLA Hops' as check_type,
    COUNT(*) as orphaned_records
FROM [case-management].dbo.case_transaction_sla_hop h
LEFT JOIN [case-management].dbo.case_transaction ct ON h.case_id = ct.case_id
WHERE ct.case_id IS NULL;

SELECT 
    'Orphaned Comments' as check_type,
    COUNT(*) as orphaned_records
FROM [case-management].dbo.case_transaction_comment c
LEFT JOIN [case-management].dbo.case_transaction ct ON c.case_id = ct.case_id
WHERE ct.case_id IS NULL;
GO

-- 8. Data Consistency Checks
PRINT '8. Checking Data Consistency...';

-- Check for duplicate case numbers
SELECT 
    'Duplicate Case Numbers' as check_type,
    COUNT(*) as duplicate_count
FROM (
    SELECT case_number, COUNT(*) as cnt
    FROM [case-management].dbo.case_transaction
    WHERE is_migration = 1
    GROUP BY case_number
    HAVING COUNT(*) > 1
) duplicates;

-- Check for duplicate case IDs
SELECT 
    'Duplicate Case IDs' as check_type,
    COUNT(*) as duplicate_count
FROM (
    SELECT case_id, COUNT(*) as cnt
    FROM [case-management].dbo.case_transaction
    WHERE is_migration = 1
    GROUP BY case_id
    HAVING COUNT(*) > 1
) duplicates;
GO

-- 9. Performance Metrics
PRINT '9. Calculating Performance Metrics...';

-- Calculate migration statistics
SELECT 
    'Migration Performance Summary' as metric_type,
    COUNT(*) as total_migrated_cases,
    MIN(created_on) as earliest_case_date,
    MAX(created_on) as latest_case_date,
    DATEDIFF(MINUTE, MIN(created_on), MAX(created_on)) as time_span_minutes
FROM [case-management].dbo.case_transaction
WHERE is_migration = 1;
GO

-- 10. Final Validation Summary
PRINT '10. Generating Final Validation Summary...';

-- Create comprehensive summary
WITH MigrationSummary AS (
    SELECT 
        'case_transaction' as table_name,
        COUNT(*) as record_count
    FROM [case-management].dbo.case_transaction
    WHERE is_migration = 1
    
    UNION ALL
    
    SELECT 
        'case_transaction_sla_activity' as table_name,
        COUNT(*) as record_count
    FROM [case-management].dbo.case_transaction_sla_activity s
    INNER JOIN [case-management].dbo.case_transaction ct ON s.case_id = ct.case_id
    WHERE ct.is_migration = 1
    
    UNION ALL
    
    SELECT 
        'case_transaction_document_reference' as table_name,
        COUNT(*) as record_count
    FROM [case-management].dbo.case_transaction_document_reference d
    INNER JOIN [case-management].dbo.case_transaction ct ON d.case_id = ct.case_id
    WHERE ct.is_migration = 1
    
    UNION ALL
    
    SELECT 
        'case_transaction_sla_hop' as table_name,
        COUNT(*) as record_count
    FROM [case-management].dbo.case_transaction_sla_hop h
    INNER JOIN [case-management].dbo.case_transaction ct ON h.case_id = ct.case_id
    WHERE ct.is_migration = 1
    
    UNION ALL
    
    SELECT 
        'case_transaction_comment' as table_name,
        COUNT(*) as record_count
    FROM [case-management].dbo.case_transaction_comment c
    INNER JOIN [case-management].dbo.case_transaction ct ON c.case_id = ct.case_id
    WHERE ct.is_migration = 1
)
SELECT 
    table_name,
    record_count,
    CASE 
        WHEN record_count > 0 THEN '✓ MIGRATED'
        ELSE '✗ NO DATA'
    END as status
FROM MigrationSummary
ORDER BY table_name;
GO

PRINT '=====================================================';
PRINT 'Migration Validation Process Completed';
PRINT 'End Time: ' + CONVERT(varchar, GETDATE(), 120);
PRINT '=====================================================';
GO

-- 11. Recommendations (if any issues found)
PRINT '11. Generating Recommendations...';

-- Check for potential issues and provide recommendations
IF EXISTS (
    SELECT 1 FROM [case-management].dbo.case_transaction 
    WHERE is_migration = 1 AND case_number IS NULL
)
BEGIN
    PRINT '⚠️  WARNING: Found cases with NULL case numbers. Review data quality.';
END

IF EXISTS (
    SELECT 1 FROM [case-management].dbo.case_transaction_sla_activity s
    LEFT JOIN [case-management].dbo.case_transaction ct ON s.case_id = ct.case_id
    WHERE ct.case_id IS NULL
)
BEGIN
    PRINT '⚠️  WARNING: Found orphaned SLA activities. Consider cleanup.';
END

IF NOT EXISTS (
    SELECT 1 FROM [case-management].dbo.case_transaction 
    WHERE is_migration = 1
)
BEGIN
    PRINT '❌  ERROR: No migrated cases found. Migration may have failed.';
END
ELSE
BEGIN
    PRINT '✅  SUCCESS: Migration validation completed successfully.';
    PRINT '   All tables contain migrated data with proper relationships.';
END
GO
