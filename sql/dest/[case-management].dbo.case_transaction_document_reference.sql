CREATE TABLE case_transaction_document_reference (
    document_reference_id uniqueidentifier NOT NULL,
    case_id uniqueidentifier NULL,
    created_by_id uniqueidentifier NULL,
    modified_by_id uniqueidentifier NULL,
    emp_app_id nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    emp_ms_doc_type_key nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    file_name nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    object_document_id nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    repository nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    status_code int NULL,
    created_on datetimeoffset NULL,
    modified_on datetimeoffset NULL,
    CONSTRAINT PK__case_tra__090E3559C9769DFE PRIMARY KEY (document_reference_id)
);