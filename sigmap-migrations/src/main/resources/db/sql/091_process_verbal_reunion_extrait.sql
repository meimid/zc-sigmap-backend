-- liquibase formatted sql

-- changeset sigmap:1749671959092-1
CREATE SEQUENCE process_verbal_reunion_extrait_id_seq INCREMENT BY 1 START WITH 1;

-- changeset sigmap:1757950292-2

CREATE TABLE IF NOT EXISTS process_verbal_reunion_extrait (
    extrait_id INT NOT NULL ,
    pv_id      INT NOT NULL,
    ac_id      INT NOT NULL,
    ppm_id     INT NOT NULL,
    ppm_ver    INT NOT NULL,
    doc_id     INT NULL,

    CONSTRAINT PK_EXTRAIT
    PRIMARY KEY (extrait_id, pv_id, ac_id),
    CONSTRAINT FK_EXTRAIT_PV
    FOREIGN KEY (pv_id) REFERENCES process_verbal_reunion (pv_id),
    CONSTRAINT FK_EXTRAIT_PPM
    FOREIGN KEY (ppm_id,ppm_ver) REFERENCES ppm (ppm_id,ppm_ver),
    CONSTRAINT FK_EXTRAIT_AC
    FOREIGN KEY (ac_id) REFERENCES ac (ac_id),
    CONSTRAINT FK_EXTRAIT_DOC
    FOREIGN KEY (doc_id) REFERENCES docs (doc_id)
    );
