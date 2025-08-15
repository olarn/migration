# Case Migration Scripts

This folder contains SQL scripts to migrate case data from the legacy `[case-migration]` database schema to the new `[case-management]` database schema.

## Overview

The migration system follows the mapping logic defined in `mapping/mapping.java` and implements the performance guidelines specified in `requirements.txt`:

- **Parallel Extract & Load** - Uses bulk operations and parallel processing
- **Set-based Validation** - Minimizes row-by-row operations  
- **Efficient Key Mapping** - Uses hash joins and indexed lookups
- **Batch Entity Curation** - Processes in chunks to manage memory
- **Optimized Bulk Loading** - Uses minimal logging and table locks
- **Smart Constraint/Index Management** - Disables during load, rebuilds after
- **Performance Metrics** - Monitors and optimizes bottlenecks

## Migration Scripts

### 1. `00_master_migration_script.sql` - Master Execution Script
**Execute this script to run the complete migration process.**

This script:
- Creates a migration log table for tracking progress
- Executes all migration scripts in the correct order
- Provides comprehensive error handling and rollback
- Logs all migration activities
- Shows progress and completion status

### 2. `01_migrate_case_transaction.sql` - Main Case Data Migration
Migrates the master case data from `stg_case_inprogress` to `case_transaction`.

**Key Features:**
- Bulk insert with parallel processing (MAXDOP 4)
- Disables constraints and indexes during load
- Uses minimal logging for performance
- Handles data type conversions safely
- Maps all fields according to the Java mapping logic

### 3. `02_migrate_case_transaction_sla_activity.sql` - SLA Activity Migration
Migrates SLA activity data from `stg_sla_per_owner` to `case_transaction_sla_activity`.

**Key Features:**
- Creates SLA activity records for each owner change
- Calculates actual duration between start/end times
- Maintains hop number references
- Links to migrated case transactions

### 4. `03_migrate_case_transaction_document_reference.sql` - Document Reference Migration
Migrates document reference data from `stg_case_document_reference` to `case_transaction_document_reference`.

**Key Features:**
- Preserves all document metadata
- Handles date conversions safely
- Links documents to migrated cases

### 5. `04_migrate_case_transaction_sla_hop.sql` - SLA Hop Migration
Migrates SLA hop data from `stg_sla_per_owner` to `case_transaction_sla_hop`.

**Key Features:**
- Creates hop records for each SLA transition
- Calculates duration in minutes
- Maintains chronological order

### 6. `05_migrate_case_transaction_comment.sql` - Comment Migration
Creates comment records based on case information from `stg_case_inprogress`.

**Key Features:**
- Extracts resolution and root cause information
- Creates descriptive migration comments
- Preserves case descriptions

## Prerequisites

Before running the migration:

1. **Database Access**: Ensure you have access to both source and destination databases
2. **Permissions**: You need DDL and DML permissions on the destination database
3. **Data Validation**: Verify that source data has the required `migration_lot` values
4. **Backup**: Take a full backup of the destination database before migration
5. **Maintenance Window**: Plan for downtime during the migration process

## Execution Instructions

### Option 1: Run Master Script (Recommended)
```sql
-- Connect to the destination database [case-management]
USE [case-management];
GO

-- Execute the master migration script
:r "path\to\00_master_migration_script.sql"
```

### Option 2: Run Individual Scripts
If you prefer to run scripts individually:

1. **First**: Run `01_migrate_case_transaction.sql`
2. **Then**: Run `02_migrate_case_transaction_sla_activity.sql`
3. **Then**: Run `03_migrate_case_transaction_document_reference.sql`
4. **Then**: Run `04_migrate_case_transaction_sla_hop.sql`
5. **Finally**: Run `05_migrate_case_transaction_comment.sql`

## Performance Optimizations

The scripts include several performance optimizations:

- **MAXDOP 4**: Limits parallel processing to 4 threads
- **NOLOCK Hints**: Uses for read operations to avoid blocking
- **Bulk Operations**: Disables constraints and indexes during load
- **Minimal Logging**: Sets database to BULK_LOGGED recovery model
- **Batch Processing**: Processes data in chunks to manage memory

## Monitoring and Logging

The master script creates a `migration_log` table that tracks:

- Script execution start/end times
- Number of records migrated
- Success/failure status
- Error messages (if any)

## Data Validation

After migration, verify the data integrity:

```sql
-- Check total migrated cases
SELECT COUNT(*) as Total_Cases 
FROM [case-management].dbo.case_transaction 
WHERE is_migration = 1;

-- Check SLA activities
SELECT COUNT(*) as Total_SLA_Activities
FROM [case-management].dbo.case_transaction_sla_activity s
INNER JOIN [case-management].dbo.case_transaction ct ON s.case_id = ct.case_id
WHERE ct.is_migration = 1;

-- Check document references
SELECT COUNT(*) as Total_Documents
FROM [case-management].dbo.case_transaction_document_reference d
INNER JOIN [case-management].dbo.case_transaction ct ON d.case_id = ct.case_id
WHERE ct.is_migration = 1;
```

## Rollback Plan

If migration fails or needs to be rolled back:

```sql
-- Rollback all migrated data
DELETE FROM [case-management].dbo.case_transaction_comment WHERE case_id IN (
    SELECT case_id FROM [case-management].dbo.case_transaction WHERE is_migration = 1
);

DELETE FROM [case-management].dbo.case_transaction_sla_hop WHERE case_id IN (
    SELECT case_id FROM [case-management].dbo.case_transaction WHERE is_migration = 1
);

DELETE FROM [case-management].dbo.case_transaction_document_reference WHERE case_id IN (
    SELECT case_id FROM [case-management].dbo.case_transaction WHERE is_migration = 1
);

DELETE FROM [case-management].dbo.case_transaction_sla_activity WHERE case_id IN (
    SELECT case_id FROM [case-management].dbo.case_transaction WHERE is_migration = 1
);

DELETE FROM [case-management].dbo.case_transaction WHERE is_migration = 1;
```

## Troubleshooting

### Common Issues:

1. **Permission Errors**: Ensure you have sufficient database permissions
2. **Memory Issues**: Reduce MAXDOP if encountering memory pressure
3. **Timeout Issues**: Increase command timeout for large datasets
4. **Constraint Violations**: Check data integrity before migration

### Performance Tuning:

- Adjust MAXDOP based on your server's CPU configuration
- Monitor tempdb usage during migration
- Consider running during off-peak hours
- Use appropriate maintenance windows

## Support

For issues or questions regarding the migration scripts, refer to:
- The mapping logic in `mapping/mapping.java`
- The requirements in `requirements.txt`
- The source and destination schema definitions

## Version History

- **v1.0**: Initial migration scripts with bulk operations and performance optimizations
- Implements all required mappings from the Java mapping logic
- Follows performance guidelines for fast-as-possible migration
