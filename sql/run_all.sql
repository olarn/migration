:r /usr/src/app/sql/../source schema/[case-migration].dbo.stg_case_inprogress.sql
:r /usr/src/app/sql/../source schema/[case-migration].dbo.stg_sla_per_owner.sql
:r /usr/src/app/sql/../source schema/[case-migration].dbo.stg_case_document_reference.sql
:r /usr/src/app/sql/../destination schema/[case-management].dbo.case_transaction.sql
:r /usr/src/app/sql/../destination schema/[case-management].dbo.case_transaction_sla_activity.sql
:r /usr/src/app/sql/../destination schema/[case-management].dbo.case_transaction_document_reference.sql
:r /usr/src/app/sql/../destination schema/[case-management].dbo.case_transaction_sla_hop.sql
:r /usr/src/app/sql/../destination schema/[case-management].dbo.case_transaction_comment.sql
:r /usr/src/app/sql/../Output/00_seed_sample_data.sql
:r /usr/src/app/sql/../Output2/01_transform_and_migrate_case_transaction.sql
:r /usr/src/app/sql/../Output2/02_transform_and_migrate_sla_activity.sql
:r /usr/src/app/sql/../Output2/03_transform_and_migrate_document_reference.sql
:r /usr/src/app/sql/../Output2/04_transform_and_migrate_sla_hop.sql
:r /usr/src/app/sql/../Output2/05_transform_and_migrate_comment.sql
:r /usr/src/app/sql/../Output2/06_validate_transform.sql
