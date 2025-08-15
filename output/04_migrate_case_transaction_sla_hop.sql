-- =====================================================
-- Case Transaction SLA Hop Migration Script
-- Source: [case-migration].dbo.stg_sla_per_owner
-- Destination: [case-management].dbo.case_transaction_sla_hop
-- =====================================================

-- Disable constraints and indexes for faster bulk load
ALTER TABLE [case-management].dbo.case_transaction_sla_hop NOCHECK CONSTRAINT ALL;
ALTER INDEX ALL ON [case-management].dbo.case_transaction_sla_hop DISABLE;

-- Set minimal logging for bulk operations
ALTER DATABASE [case-management] SET RECOVERY BULK_LOGGED;

-- Bulk insert with parallel processing
INSERT INTO [case-management].dbo.case_transaction_sla_hop (
    case_transaction_sla_hop_id,
    case_id,
    hop_number,
    owner_team_name,
    owner_employee_id,
    owner_name,
    start_date,
    end_date,
    duration_minutes,
    case_status,
    created_on,
    modified_on
)
SELECT 
    NEWID() as case_transaction_sla_hop_id,
    ct.case_id,
    ROW_NUMBER() OVER (PARTITION BY s.Case__c ORDER BY s.Start_Date_Time__c) as hop_number,
    s.Owner_Team_NEW as owner_team_name,
    s.Employee_ID__c as owner_employee_id,
    s.Name as owner_name,
    CASE 
        WHEN s.Start_Date_Time__c IS NOT NULL AND s.Start_Date_Time__c != '' 
        THEN TRY_CAST(s.Start_Date_Time__c AS datetimeoffset)
        ELSE NULL 
    END as start_date,
    CASE 
        WHEN s.End_Date_Time__c IS NOT NULL AND s.End_Date_Time__c != '' 
        THEN TRY_CAST(s.End_Date_Time__c AS datetimeoffset)
        ELSE NULL 
    END as end_date,
    CASE 
        WHEN s.End_Date_Time__c IS NOT NULL AND s.Start_Date_Time__c IS NOT NULL 
        AND s.End_Date_Time__c != '' AND s.Start_Date_Time__c != ''
        THEN DATEDIFF(MINUTE, TRY_CAST(s.Start_Date_Time__c AS datetimeoffset), TRY_CAST(s.End_Date_Time__c AS datetimeoffset))
        ELSE NULL 
    END as duration_minutes,
    s.Case_Status__c as case_status,
    CASE 
        WHEN s.Start_Date_Time__c IS NOT NULL AND s.Start_Date_Time__c != '' 
        THEN TRY_CAST(s.Start_Date_Time__c AS datetimeoffset)
        ELSE GETDATE() 
    END as created_on,
    CASE 
        WHEN s.End_Date_Time__c IS NOT NULL AND s.End_Date_Time__c != '' 
        THEN TRY_CAST(s.End_Date_Time__c AS datetimeoffset)
        ELSE GETDATE() 
    END as modified_on
FROM [case-migration].dbo.stg_sla_per_owner s WITH (NOLOCK)
INNER JOIN [case-management].dbo.case_transaction ct WITH (NOLOCK)
    ON s.Case__c = ct.case_number
    AND ct.is_migration = 1
WHERE s.migration_lot IS NOT NULL
    AND s.record_status = 'ACTIVE'
OPTION (MAXDOP 4, OPTIMIZE FOR UNKNOWN);

-- Re-enable constraints and indexes
ALTER INDEX ALL ON [case-management].dbo.case_transaction_sla_hop REBUILD;
ALTER TABLE [case-management].dbo.case_transaction_sla_hop WITH CHECK CHECK CONSTRAINT ALL;

-- Reset database recovery model
ALTER DATABASE [case-management] SET RECOVERY FULL;

-- Display migration summary
SELECT 
    'Case Transaction SLA Hop Migration Complete' as Status,
    COUNT(*) as Records_Migrated,
    GETDATE() as Migration_Completed_At
FROM [case-management].dbo.case_transaction_sla_hop h
INNER JOIN [case-management].dbo.case_transaction ct ON h.case_id = ct.case_id
WHERE ct.is_migration = 1;
