DDL_DIR := src/main/resources/db/changelog/DDL
DML_DIR := src/main/resources/db/changelog/DML

TIMESTAMP := $(shell date +%Y%m%d%H%M%S)
USERNAME := $(shell git config user.name)
SERVICE_NAME := "masterdataManagement"

create_ddl:
	@mkdir -p $(DDL_DIR)
	@echo "--liquibase formatted sql" > $(DDL_DIR)/$(TIMESTAMP)_$(sql).sql
	@echo "" >> $(DDL_DIR)/$(TIMESTAMP)_$(sql).sql
	@echo "--changeset $(USERNAME):$(TIMESTAMP)_$(sql) labels:$(SERVICE_NAME)" >> $(DDL_DIR)/$(TIMESTAMP)_$(sql).sql
	@echo "file $(DDL_DIR)/$(TIMESTAMP)_$(sql).sql created"

create_dml:
	@mkdir -p $(DML_DIR)
	@echo "--liquibase formatted sql" > $(DML_DIR)/$(TIMESTAMP)_$(sql).sql
	@echo "" >> $(DML_DIR)/$(TIMESTAMP)_$(sql).sql
	@echo "--changeset $(USERNAME):$(TIMESTAMP)_$(sql) labels:$(SERVICE_NAME)" >> $(DML_DIR)/$(TIMESTAMP)_$(sql).sql
	@echo "file $(DML_DIR)/$(TIMESTAMP)_$(sql).sql created"

setup-hooks:
	@echo "Configuring Git to use custom hooks..."
	git config core.hooksPath scripts/git-hooks
	@chmod +x scripts/git-hooks/pre-push
	@echo "Hooks setup complete. 'pre-push' is now executable and active."