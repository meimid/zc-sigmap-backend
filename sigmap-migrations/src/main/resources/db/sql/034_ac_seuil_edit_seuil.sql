-- liquibase formatted sql

-- changeset sigmap:1749671959050-58
ALTER TABLE ac_seuil
    MODIFY seuil DECIMAL(20,3);