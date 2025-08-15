#!/usr/bin/env bash

set -euo pipefail
START_TIME=$(date +%s)

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

echo "[1/5] Starting Docker services..."
docker compose up -d

echo "[2/5] Preparing SQL directories inside container..."
docker compose exec -T mssql-tools /bin/bash -lc "mkdir -p /usr/src/app/sql/source /usr/src/app/sql/dest /usr/src/app/sql/out /usr/src/app/sql/out2"

echo "[3/5] Copying SQL files into container..."
docker cp "$ROOT_DIR/source schema/." mssql-tools:/usr/src/app/sql/source
docker cp "$ROOT_DIR/destination schema/." mssql-tools:/usr/src/app/sql/dest
docker cp "$ROOT_DIR/Output/." mssql-tools:/usr/src/app/sql/out
docker cp "$ROOT_DIR/Output2/." mssql-tools:/usr/src/app/sql/out2

echo "[4/5] Creating databases (if needed)..."
docker compose exec -T mssql-tools /bin/bash -lc \
  "/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P 'Str0ngP@ssw0rd!' -Q \"IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'case-migration') CREATE DATABASE [case-migration]; IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'case-management') CREATE DATABASE [case-management];\""

echo "[4.1/5] Creating/ensuring staging tables (ignoring if already exist)..."
docker compose exec -T mssql-tools /bin/bash -lc \
  "/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P 'Str0ngP@ssw0rd!' -d case-migration -i '/usr/src/app/sql/source/[case-migration].dbo.stg_case_inprogress.sql' || true"
docker compose exec -T mssql-tools /bin/bash -lc \
  "/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P 'Str0ngP@ssw0rd!' -d case-migration -i '/usr/src/app/sql/source/[case-migration].dbo.stg_sla_per_owner.sql' || true"
docker compose exec -T mssql-tools /bin/bash -lc \
  "/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P 'Str0ngP@ssw0rd!' -d case-migration -i '/usr/src/app/sql/source/[case-migration].dbo.stg_case_document_reference.sql' || true"

echo "[4.2/5] Creating/ensuring destination tables (ignoring if already exist)..."
docker compose exec -T mssql-tools /bin/bash -lc \
  "/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P 'Str0ngP@ssw0rd!' -i '/usr/src/app/sql/dest/[case-management].dbo.case_transaction.sql' || true"
docker compose exec -T mssql-tools /bin/bash -lc \
  "/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P 'Str0ngP@ssw0rd!' -i '/usr/src/app/sql/dest/[case-management].dbo.case_transaction_sla_activity.sql' || true"
docker compose exec -T mssql-tools /bin/bash -lc \
  "/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P 'Str0ngP@ssw0rd!' -i '/usr/src/app/sql/dest/[case-management].dbo.case_transaction_document_reference.sql' || true"
docker compose exec -T mssql-tools /bin/bash -lc \
  "/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P 'Str0ngP@ssw0rd!' -i '/usr/src/app/sql/dest/[case-management].dbo.case_transaction_sla_hop.sql' || true"
docker compose exec -T mssql-tools /bin/bash -lc \
  "/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P 'Str0ngP@ssw0rd!' -i '/usr/src/app/sql/dest/[case-management].dbo.case_transaction_comment.sql' || true"

echo "[4.3/5] Seeding sample data..."
docker compose exec -T mssql-tools /bin/bash -lc \
  "/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P 'Str0ngP@ssw0rd!' -i '/usr/src/app/sql/out/00_seed_sample_data.sql'"

echo "[5/5] Running Output2 migration scripts..."
docker compose exec -T mssql-tools /bin/bash -lc \
  "/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P 'Str0ngP@ssw0rd!' -i '/usr/src/app/sql/out2/00_delete_existing_destination_data.sql'"
docker compose exec -T mssql-tools /bin/bash -lc \
  "/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P 'Str0ngP@ssw0rd!' -i '/usr/src/app/sql/out2/01_transform_and_migrate_case_transaction.sql'"
docker compose exec -T mssql-tools /bin/bash -lc \
  "/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P 'Str0ngP@ssw0rd!' -i '/usr/src/app/sql/out2/02_transform_and_migrate_sla_activity.sql'"
docker compose exec -T mssql-tools /bin/bash -lc \
  "/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P 'Str0ngP@ssw0rd!' -i '/usr/src/app/sql/out2/03_transform_and_migrate_document_reference.sql'"
docker compose exec -T mssql-tools /bin/bash -lc \
  "/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P 'Str0ngP@ssw0rd!' -i '/usr/src/app/sql/out2/04_transform_and_migrate_sla_hop.sql'"
docker compose exec -T mssql-tools /bin/bash -lc \
  "/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P 'Str0ngP@ssw0rd!' -i '/usr/src/app/sql/out2/05_transform_and_migrate_comment.sql'"
docker compose exec -T mssql-tools /bin/bash -lc \
  "/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P 'Str0ngP@ssw0rd!' -i '/usr/src/app/sql/out2/06_validate_transform.sql'"

END_TIME=$(date +%s)
TOTAL_SEC=$((END_TIME - START_TIME))
printf "Migration completed in %02d:%02d:%02d (hh:mm:ss)\n" $((TOTAL_SEC/3600)) $(((TOTAL_SEC%3600)/60)) $((TOTAL_SEC%60))


