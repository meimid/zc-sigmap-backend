-- liquibase formatted sql

-- changeset db:1732196145836-18
--preconditions onFail:WARN
CREATE SEQUENCE `dac_id_seq` START WITH 1 INCREMENT BY 1;

-- changeset db:1732196145836-19
--preconditions onFail:WARN
CREATE SEQUENCE `dyna_docs_id_seq` START WITH 1 INCREMENT BY 1;

-- changeset db:1732196145836-20
--preconditions onFail:WARN
CREATE SEQUENCE `dyna_fields_id_seq` START WITH 1 INCREMENT BY 1;

-- changeset db:1732196145836-21
--preconditions onFail:WARN
CREATE SEQUENCE `dyna_fields_details_id_seq` START WITH 1 INCREMENT BY 1;

