-- liquibase formatted sql

-- changeset sigmap:1749671959050-108

ALTER TABLE reunion_ppm ADD COLUMN observation VARCHAR(255) NULL;

-- changeset sigmap:1749671959050-109

ALTER TABLE reunion_ppm ADD COLUMN decision VARCHAR(255) NULL;

-- changeset sigmap:1749671959050-110

ALTER TABLE reunion_dac ADD COLUMN observation VARCHAR(255) NULL;

-- changeset sigmap:1749671959050-111

ALTER TABLE reunion_dac ADD COLUMN decision VARCHAR(255) NULL;

-- changeset sigmap:1749671959050-112

CREATE TABLE process_verbal_reunion (
                                        pv_id INT AUTO_INCREMENT PRIMARY KEY,
                                        observation TEXT,
                                        created_at DATETIME NULL,
                                        created_by VARCHAR(100) NULL,
                                        updated_at DATETIME NULL,
                                        updated_by VARCHAR(100) NULL,
                                        deleted_at DATETIME NULL,
                                        deleted_by VARCHAR(100) NULL,
                                        is_deleted BOOLEAN DEFAULT FALSE NOT NULL

);
-- changeset sigmap:1749671959050-113

ALTER TABLE reunion ADD COLUMN pv_id INT NULL;

-- changeset sigmap:1749671959050-114
ALTER TABLE reunion
    ADD CONSTRAINT FK_REUNION_TO_PROCESS_VERBAL
        FOREIGN KEY (pv_id) REFERENCES process_verbal_reunion (pv_id);

-- changeset sigmap:1749671959050-115
CREATE TABLE process_verbal_reunion_doc (
                pv_id INT NOT NULL,
                doc_id INT NOT NULL,
                pv BOOLEAN NOT NULL,

                CONSTRAINT PK_PROCESS_VERBAL_REUNION_DOC PRIMARY KEY (pv_id, doc_id),

                CONSTRAINT FK_PV_DOC_TO_PV
                    FOREIGN KEY (pv_id) REFERENCES process_verbal_reunion (pv_id),

                CONSTRAINT FK_PV_DOC_TO_DOCS
                    FOREIGN KEY (doc_id) REFERENCES docs (doc_id)
);


-- changeset sigmap:1749671959050-116

CREATE TABLE reunion_dac_doc (
                                            reunion_id INT NOT NULL,
                                            dac_id INT NOT NULL,
                                            dac_ver INT NOT NULL,
                                            doc_id INT NOT NULL,

                                            CONSTRAINT PK_REUNION_DAC_DOC PRIMARY KEY (reunion_id, dac_id, dac_ver, doc_id),
                                            CONSTRAINT FK_REUNION_DAC_DOC_TO_REUNION FOREIGN KEY (reunion_id) REFERENCES reunion (reunion_id),
                                            CONSTRAINT FK_REUNION_DAC_DOC_TO_REUNION_DAC FOREIGN KEY (dac_id, dac_ver) REFERENCES reunion_dac (dac_id, dac_ver),
                                            CONSTRAINT FK_REUNION_DAC_DOC_TO_DOCS FOREIGN KEY (doc_id) REFERENCES docs (doc_id)
);
-- changeset sigmap:1749671959050-117

CREATE TABLE reunion_ppm_doc (
                                 reunion_id INT NOT NULL,
                                 ppm_id INT NOT NULL,
                                 ppm_ver INT NOT NULL,
                                 doc_id INT NOT NULL,

                                 CONSTRAINT PK_REUNION_PPM_DOC PRIMARY KEY (reunion_id, ppm_id, ppm_ver, doc_id),
                                 CONSTRAINT FK_REUNION_PPM_DOC_TO_REUNION FOREIGN KEY (reunion_id) REFERENCES reunion (reunion_id),
                                 CONSTRAINT FK_REUNION_PPM_DOC_TO_REUNION_PPM FOREIGN KEY (ppm_id, ppm_ver) REFERENCES reunion_ppm (ppm_id, ppm_ver),
                                 CONSTRAINT FK_REUNION_PPM_DOC_TO_DOCS FOREIGN KEY (doc_id) REFERENCES docs (doc_id)
);