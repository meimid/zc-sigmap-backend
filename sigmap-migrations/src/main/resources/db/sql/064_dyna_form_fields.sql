-- liquibase formatted sql

-- changeset sigmap:1757950265-1

CREATE SEQUENCE IF NOT EXISTS dyna_form_fields_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775806
    NOCACHE
    NOCYCLE;

-- changeset sigmap:1757950265-2
CREATE TABLE IF NOT EXISTS dyna_form_fields (
    dyna_form_id            INT          NOT NULL,
    dyna_form_ver           INT          NOT NULL,
    dyna_form_fields_id     INT          NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    label                   VARCHAR(255) NULL,
    description             TEXT         NULL,
    type_id                 INT          NULL,
    is_required             TINYINT(1)   NULL,
    dyna_form_field_order   INT          NULL,
    status                  VARCHAR(32)  NOT NULL,

    created_at              DATETIME     DEFAULT NULL,
    created_by              VARCHAR(100) DEFAULT NULL,
    updated_at              DATETIME     DEFAULT NULL,
    updated_by              VARCHAR(100) DEFAULT NULL,
    deleted_at              DATETIME     DEFAULT NULL,
    deleted_by              VARCHAR(100) DEFAULT NULL,
    is_deleted              TINYINT(1)   NOT NULL DEFAULT 0,

    CONSTRAINT PK_DYNA_FORM_FIELDS PRIMARY KEY (dyna_form_id, dyna_form_ver, dyna_form_fields_id),

    CONSTRAINT FK_DFF__DYNA_FORM
    FOREIGN KEY (dyna_form_id, dyna_form_ver)
    REFERENCES dyna_form (dyna_form_id, dyna_form_ver)
    ON DELETE CASCADE
    );