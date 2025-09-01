-- liquibase formatted sql

-- changeset sigmap:1749671959050-110

CREATE TABLE model_agpm (
                           doc_id   INT NOT NULL,
                           agpm_id   INT NOT NULL,
                           agpm_ver  INT NOT NULL,

                           created_at   DATETIME DEFAULT NULL,
                           created_by   VARCHAR(100) NULL,
                           updated_at   DATETIME DEFAULT NULL,
                           updated_by   VARCHAR(100) NULL,
                           deleted_at   DATETIME DEFAULT NULL,
                           deleted_by   VARCHAR(100) NULL,
                           is_deleted   TINYINT(1) DEFAULT 0 NOT NULL,

                           CONSTRAINT PK_MODEL_AGPM PRIMARY KEY (doc_id, agpm_id, agpm_ver),

                           CONSTRAINT UQ_MODEL_AGPM__DOC UNIQUE (doc_id),
                           CONSTRAINT UQ_MODEL_AGPM__AGPM UNIQUE (agpm_id, agpm_ver),

                           CONSTRAINT FK_MODEL_AGPM_DOC
                               FOREIGN KEY (doc_id) REFERENCES docs (doc_id) ON DELETE CASCADE,

                           CONSTRAINT FK_MODEL_AGPM_AGPM
                               FOREIGN KEY (agpm_id, agpm_ver) REFERENCES paa (agpm_id, agpm_ver) ON DELETE CASCADE
);