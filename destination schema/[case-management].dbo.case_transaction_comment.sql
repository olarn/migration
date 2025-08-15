CREATE TABLE case_transaction_comment (
    comment_id uniqueidentifier NOT NULL,
    case_id uniqueidentifier NULL,
    team_id uniqueidentifier NULL,
    comment_desc nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    employee_id nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    team_name_th nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    team_name_en nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    status_code int NULL,
    created_on datetimeoffset NULL,
    created_by_id uniqueidentifier NULL,
    modified_on datetimeoffset NULL,
    modified_by_id uniqueidentifier NULL,
    CONSTRAINT PK__case_tra__E795768790B6986D PRIMARY KEY (comment_id)
);