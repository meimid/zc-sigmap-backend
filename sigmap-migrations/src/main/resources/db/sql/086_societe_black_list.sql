-- liquibase formatted sql

-- changeset sigmap:1757950287-1


CREATE TABLE IF NOT EXISTS societe_black_list (
    balck_list_id INT AUTO_INCREMENT NOT NULL,
    societe_id INT NOT NULL,
    nature_decision ENUM('TEMPORIARE', 'DEFINTIVE') NULL,
    reference VARCHAR(255) NULL,
    date_effet DATETIME NULL,
    delai DATETIME NULL,
    created_at   DATETIME DEFAULT NULL,
    created_by VARCHAR(100) NULL,
    updated_at DATETIME DEFAULT NULL NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at DATETIME DEFAULT NULL NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0 NOT NULL,
    PRIMARY KEY (balck_list_id,societe_id),
    CONSTRAINT FK_SOCIETE_TO_SOCIETE_BLAK_LIST FOREIGN KEY (societe_id) REFERENCES societe (societe_id));
