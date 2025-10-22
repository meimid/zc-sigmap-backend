-- liquibase formatted sql

-- changeset sigmap:1757950264-1

CREATE SEQUENCE IF NOT EXISTS dyna_form_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775806
    NOCACHE
    NOCYCLE;

-- changeset sigmap:1757950264-2
CREATE TABLE IF NOT EXISTS dyna_form (
    dyna_form_id        INT          NOT NULL,
    dyna_form_ver       INT          NOT NULL,
    name                VARCHAR(255) NOT NULL,
    dyna_form_order     INT          NULL,
    status              VARCHAR(32)  NOT NULL,
    type_dossier        VARCHAR(32)  NOT NULL,

    created_at          DATETIME     DEFAULT NULL,
    created_by          VARCHAR(100) DEFAULT NULL,
    updated_at          DATETIME     DEFAULT NULL,
    updated_by          VARCHAR(100) DEFAULT NULL,
    deleted_at          DATETIME     DEFAULT NULL,
    deleted_by          VARCHAR(100) DEFAULT NULL,
    is_deleted          TINYINT(1)   NOT NULL DEFAULT 0,

    CONSTRAINT PK_DYNA_FORM PRIMARY KEY (dyna_form_id, dyna_form_ver)
    );