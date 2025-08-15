db:
	docker compose up mssql -d
mi:
	docker compose up mssql-tools
redis:
	docker compose up redis -d
down:
	docker compose down -v