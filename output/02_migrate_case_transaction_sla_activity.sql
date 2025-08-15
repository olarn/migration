-- =====================================================
-- Case Transaction SLA Activity Migration Script
-- Source: [case-migration].dbo.stg_sla_per_owner
-- Destination: [case-management].dbo.case_transaction_sla_activity
-- =====================================================

-- Disable constraints and indexes for faster bulk load
ALTER TABLE [case-management].dbo.case_transaction_sla_activity NOCHECK CONSTRAINT ALL;
ALTER INDEX ALL ON [case-management].dbo.case_transaction_sla_activity DISABLE;

-- Set minimal logging for bulk operations
ALTER DATABASE [case-management] SET RECOVERY BULK_LOGGED;

-- Bulk insert with parallel processing
INSERT INTO [case-management].dbo.case_transaction_sla_activity (
    case_sla_activity_id,
    case_id,
    action,
    actual_duration,
    cancel_reason_code,
    cancel_reason_comment,
    cancel_reason_value,
    change_owner_reason,
    change_service_type_matrix_reason,
    change_service_type_matrix_reason_code,
    change_service_type_matrix_reason_comment,
    change_service_type_matrix_reason_value,
    created_by_id,
    created_by_name,
    created_on,
    end_date,
    hop_number_ref,
    modified_by_id,
    modified_by_name,
    modified_on,
    owner_id,
    owner_name,
    previous_reason_code,
    previous_reason_comment,
    previous_reason_value,
    resolution_list_code,
    resolution_list_comment,
    resolution_list_value,
    root_cause_list_code,
    root_cause_list_comment,
    root_cause_list_value,
    service_type_matrix_code,
    service_type_matrix_iteration,
    start_date,
    status_code,
    team_id,
    team_name_en,
    team_name_th
)
SELECT 
    NEWID() as case_sla_activity_id,
    ct.case_id,
    'OWNER_CHANGE' as action,
    CASE 
        WHEN s.End_Date_Time__c IS NOT NULL AND s.Start_Date_Time__c IS NOT NULL 
        AND s.End_Date_Time__c != '' AND s.Start_Date_Time__c != ''
        THEN DATEDIFF(MINUTE, TRY_CAST(s.Start_Date_Time__c AS datetimeoffset), TRY_CAST(s.End_Date_Time__c AS datetimeoffset))
        ELSE NULL 
    END as actual_duration,
    NULL as cancel_reason_code,
    NULL as cancel_reason_comment,
    NULL as cancel_reason_value,
    'MIGRATION_OWNER_CHANGE' as change_owner_reason,
    NULL as change_service_type_matrix_reason,
    NULL as change_service_type_matrix_reason_code,
    NULL as change_service_type_matrix_reason_comment,
    NULL as change_service_type_matrix_reason_value,
    NULL as created_by_id, -- Will be mapped later
    s.Name as created_by_name,
    CASE 
        WHEN s.Start_Date_Time__c IS NOT NULL AND s.Start_Date_Time__c != '' 
        THEN TRY_CAST(s.Start_Date_Time__c AS datetimeoffset)
        ELSE NULL 
    END as created_on,
    CASE 
        WHEN s.End_Date_Time__c IS NOT NULL AND s.End_Date_Time__c != '' 
        THEN TRY_CAST(s.End_Date_Time__c AS datetimeoffset)
        ELSE NULL 
    END as end_date,
    ROW_NUMBER() OVER (PARTITION BY s.Case__c ORDER BY s.Start_Date_Time__c) as hop_number_ref,
    NULL as modified_by_id, -- Will be mapped later
    s.Name as modified_by_name,
    CASE 
        WHEN s.End_Date_Time__c IS NOT NULL AND s.End_Date_Time__c != '' 
        THEN TRY_CAST(s.End_Date_Time__c AS datetimeoffset)
        ELSE NULL 
    END as modified_on,
    NULL as owner_id, -- Will be mapped later
    s.Name as owner_name,
    NULL as previous_reason_code,
    NULL as previous_reason_comment,
    NULL as previous_reason_value,
    NULL as resolution_list_code,
    NULL as resolution_list_comment,
    NULL as resolution_list_value,
    NULL as root_cause_list_code,
    NULL as root_cause_list_comment,
    NULL as root_cause_list_value,
    NULL as service_type_matrix_code,
    1 as service_type_matrix_iteration,
    CASE 
        WHEN s.Start_Date_Time__c IS NOT NULL AND s.Start_Date_Time__c != '' 
        THEN TRY_CAST(s.Start_Date_Time__c AS datetimeoffset)
        ELSE NULL 
    END as start_date,
    CASE 
        WHEN s.Case_Status__c IS NOT NULL 
        THEN TRY_CAST(s.Case_Status__c AS int)
        ELSE 1 
    END as status_code,
    NULL as team_id, -- Will be mapped later
    s.Owner_Team_NEW as team_name_en,
    s.Owner_Team_NEW as team_name_th
FROM [case-migration].dbo.stg_sla_per_owner s WITH (NOLOCK)
INNER JOIN [case-management].dbo.case_transaction ct WITH (NOLOCK)
    ON s.Case__c = ct.case_number
    AND ct.is_migration = 1
WHERE s.migration_lot IS NOT NULL
    AND s.record_status = 'ACTIVE'
OPTION (MAXDOP 4, OPTIMIZE FOR UNKNOWN);

-- Re-enable constraints and indexes
ALTER INDEX ALL ON [case-management].dbo.case_transaction_sla_activity REBUILD;
ALTER TABLE [case-management].dbo.case_transaction_sla_activity WITH CHECK CHECK CONSTRAINT ALL;

-- Reset database recovery model
ALTER DATABASE [case-management] SET RECOVERY FULL;

-- Display migration summary
SELECT 
    'Case Transaction SLA Activity Migration Complete' as Status,
    COUNT(*) as Records_Migrated,
    GETDATE() as Migration_Completed_At
FROM [case-management].dbo.case_transaction_sla_activity s
INNER JOIN [case-management].dbo.case_transaction ct ON s.case_id = ct.case_id
WHERE ct.is_migration = 1;
