-- liquibase formatted sql

-- changeset sigmap:1749671959050-108

CREATE TABLE model_ppm (
                           doc_id   INT NOT NULL,
                           ppm_id   INT NOT NULL,
                           ppm_ver  INT NOT NULL,

                           created_at   DATETIME DEFAULT NULL,
                           created_by   VARCHAR(100) NULL,
                           updated_at   DATETIME DEFAULT NULL,
                           updated_by   VARCHAR(100) NULL,
                           deleted_at   DATETIME DEFAULT NULL,
                           deleted_by   VARCHAR(100) NULL,
                           is_deleted   TINYINT(1) DEFAULT 0 NOT NULL,

                           CONSTRAINT PK_MODEL_PPM PRIMARY KEY (doc_id, ppm_id, ppm_ver),

                           CONSTRAINT UQ_MODEL_PPM__DOC UNIQUE (doc_id),
                           CONSTRAINT UQ_MODEL_PPM__PPM UNIQUE (ppm_id, ppm_ver),

                           CONSTRAINT FK_MODEL_PPM_DOC
                               FOREIGN KEY (doc_id) REFERENCES docs (doc_id) ON DELETE CASCADE,

                           CONSTRAINT FK_MODEL_PPM_PPM
                               FOREIGN KEY (ppm_id, ppm_ver) REFERENCES ppm (ppm_id, ppm_ver) ON DELETE CASCADE
);