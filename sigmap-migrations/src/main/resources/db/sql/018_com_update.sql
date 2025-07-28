-- liquibase formatted sql

-- changeset sigmap:1749671959050-44
CREATE TABLE com_doc (
                         com_id INT NOT NULL,
                         doc_id INT NOT NULL,
                         CONSTRAINT PK_COM_DOC PRIMARY KEY (com_id, doc_id)
);
-- changeset sigmap:1749671959050-45
ALTER TABLE com_doc ADD CONSTRAINT FK_COMDOC_TO_COM
    FOREIGN KEY (com_id) REFERENCES com (com_id);

-- changeset sigmap:1749671959050-46
ALTER TABLE com_doc ADD CONSTRAINT FK_COMDOC_TO_DOCS
    FOREIGN KEY (doc_id) REFERENCES docs (doc_id);

-- changeset sigmap:1749671959050-47
ALTER TABLE com ADD COLUMN tutel_id INT NULL;

-- changeset sigmap:1749671959050-48
ALTER TABLE com ADD COLUMN logo VARCHAR(100) NULL;

-- changeset sigmap:1749671959050-49
ALTER TABLE com ADD COLUMN adresse_id INT NULL;

-- changeset sigmap:1749671959050-50
ALTER TABLE com ADD COLUMN president_id VARCHAR(100) NULL;

-- changeset sigmap:1749671959050-51
INSERT INTO adresse (
    email, tel1, adresse, created_at, created_by
)
SELECT
    REGEXP_SUBSTR(contact, '[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}') AS email,

    REGEXP_SUBSTR(contact, '(\\+\\d{1,3}[\\s\\d]{4,}|0\\d{1,3}[\\s\\d]{4,})') AS tel1,

    adresse,
    created_at,
    created_by
FROM com;

-- changeset sigmap:1749671959050-52
UPDATE com c
    JOIN (
    SELECT a.adresse_id, a.adresse, a.created_at
    FROM adresse a
    ) AS sub
ON c.adresse = sub.adresse AND c.created_at = sub.created_at
    SET c.adresse_id = sub.adresse_id;

-- changeset sigmap:1749671959050-53
ALTER TABLE com ADD CONSTRAINT FK_COM_TO_ADRESSE
    FOREIGN KEY (adresse_id) REFERENCES adresse (adresse_id);




