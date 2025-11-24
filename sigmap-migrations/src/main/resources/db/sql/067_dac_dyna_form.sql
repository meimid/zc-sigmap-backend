-- liquibase formatted sql

-- changeset sigmap:1757950268-1
CREATE TABLE IF NOT EXISTS dac_dyna_form (
    dac_id               INT          NOT NULL,
    dac_ver              INT          NOT NULL,
    dyna_form_id         INT          NOT NULL,
    dyna_form_ver        INT          NOT NULL,
    dyna_form_fields_id  INT          NOT NULL,
    display              TINYINT(1)   DEFAULT 0,

    CONSTRAINT PK_DAC_DYNA_FORM PRIMARY KEY (dac_id, dac_ver, dyna_form_id, dyna_form_ver, dyna_form_fields_id)
    );

-- changeset sigmap:1757950268-2
ALTER TABLE dac_dyna_form
    ADD CONSTRAINT FK_DAC_DYNA_FORM_DAC
        FOREIGN KEY (dac_id, dac_ver)
            REFERENCES dac (dac_id, dac_ver)
            ON DELETE CASCADE
            ON UPDATE CASCADE;

-- changeset sigmap:1757950268-3
ALTER TABLE dac_dyna_form
    ADD CONSTRAINT FK_DAC_DYNA_FORM_DYNA_FORM
        FOREIGN KEY (dyna_form_id, dyna_form_ver)
            REFERENCES dyna_form (dyna_form_id, dyna_form_ver)
            ON DELETE CASCADE
            ON UPDATE CASCADE;

-- changeset sigmap:1757950268-4
ALTER TABLE dac_dyna_form
    ADD CONSTRAINT FK_DAC_DYNA_FORM_DYNA_FORM_FIELD
        FOREIGN KEY (dyna_form_id, dyna_form_ver, dyna_form_fields_id)
            REFERENCES dyna_form_fields (dyna_form_id, dyna_form_ver, dyna_form_fields_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE;

-- changeset sigmap:1757950268-5
CREATE INDEX IDX_DAC_DYNA_FORM_DAC ON dac_dyna_form (dac_id, dac_ver);
CREATE INDEX IDX_DAC_DYNA_FORM_DYNA_FORM ON dac_dyna_form (dyna_form_id, dyna_form_ver);
CREATE INDEX IDX_DAC_DYNA_FORM_FIELD ON dac_dyna_form (dyna_form_id, dyna_form_ver, dyna_form_fields_id);