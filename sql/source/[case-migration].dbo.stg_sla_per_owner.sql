CREATE TABLE stg_sla_per_owner (
  Id bigint IDENTITY(1,1) NOT NULL,
  Case__c nvarchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  Owner_Team__c nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  Owner_Team_NEW nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  Name nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  Employee_ID__c nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  Start_Date_Time__c nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  End_Date_Time__c nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  Case_Status__c nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  migration_lot nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  mid bigint NULL,
  record_status nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  record_remark nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  record_lastdatetime datetimeoffset NULL,
  CONSTRAINT PK__stg_sla___3214EC07452C9B86 PRIMARY KEY (Id)
)