-- liquibase formatted sql

-- changeset sigmap:1757950283-1

CREATE TABLE model_dac (
                           doc_id   INT NOT NULL,
                           dac_id   INT NOT NULL,
                           dac_ver  INT NOT NULL,

                           created_at   DATETIME DEFAULT NULL,
                           created_by   VARCHAR(100) NULL,
                           updated_at   DATETIME DEFAULT NULL,
                           updated_by   VARCHAR(100) NULL,
                           deleted_at   DATETIME DEFAULT NULL,
                           deleted_by   VARCHAR(100) NULL,
                           is_deleted   TINYINT(1) DEFAULT 0 NOT NULL,

                           CONSTRAINT PK_MODEL_DAC PRIMARY KEY (doc_id, dac_id, dac_ver),

                           CONSTRAINT UQ_MODEL_DAC__DOC UNIQUE (doc_id),
                           CONSTRAINT UQ_MODEL_DAC__DAC UNIQUE (dac_id, dac_ver),

                           CONSTRAINT FK_MODEL_DAC_DOC
                               FOREIGN KEY (doc_id) REFERENCES docs (doc_id) ON DELETE CASCADE,

                           CONSTRAINT FK_MODEL_DAC_DAC
                               FOREIGN KEY (dac_id, dac_ver) REFERENCES dac (dac_id, dac_ver) ON DELETE CASCADE
);
