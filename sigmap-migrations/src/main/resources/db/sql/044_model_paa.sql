-- liquibase formatted sql

-- changeset sigmap:1749671959050-109

CREATE TABLE model_paa (
                           doc_id   INT NOT NULL,
                           paa_id   INT NOT NULL,
                           paa_ver  INT NOT NULL,

                           created_at   DATETIME DEFAULT NULL,
                           created_by   VARCHAR(100) NULL,
                           updated_at   DATETIME DEFAULT NULL,
                           updated_by   VARCHAR(100) NULL,
                           deleted_at   DATETIME DEFAULT NULL,
                           deleted_by   VARCHAR(100) NULL,
                           is_deleted   TINYINT(1) DEFAULT 0 NOT NULL,

                           CONSTRAINT PK_MODEL_PAA PRIMARY KEY (doc_id, paa_id, paa_ver),

                           CONSTRAINT UQ_MODEL_PAA__DOC UNIQUE (doc_id),
                           CONSTRAINT UQ_MODEL_PAA__PAA UNIQUE (paa_id, paa_ver),

                           CONSTRAINT FK_MODEL_PAA_DOC
                               FOREIGN KEY (doc_id) REFERENCES docs (doc_id) ON DELETE CASCADE,

                           CONSTRAINT FK_MODEL_PAA_PAA
                               FOREIGN KEY (paa_id, paa_ver) REFERENCES paa (paa_id, paa_ver) ON DELETE CASCADE
);