-- liquibase formatted sql

-- changeset sigmap:1757950269-2

CREATE TABLE IF NOT EXISTS dyna_docs_model (
    dyna_docs_model_id INT NOT NULL AUTO_INCREMENT,
    description VARCHAR(255) NULL,
    created_at DATETIME DEFAULT NULL,
    created_by VARCHAR(100) DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    updated_by VARCHAR(100) DEFAULT NULL,
    deleted_at DATETIME DEFAULT NULL,
    deleted_by VARCHAR(100) DEFAULT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (dyna_docs_model_id)
)
