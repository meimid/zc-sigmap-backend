-- liquibase formatted sql

-- changeset db:1732196145836-30
CREATE UNIQUE INDEX ref_type_code_index ON ref_type (ref_code);
