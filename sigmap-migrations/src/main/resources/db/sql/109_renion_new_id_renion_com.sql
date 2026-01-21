-- liquibase formatted sql

-- changeset sigmap:17496719590110-1

ALTER TABLE reunion
    ADD COLUMN reunion_num_com INT NULL;

-- changeset sigmap:17496719590110-2

UPDATE reunion r
    JOIN (
    SELECT reunion_id,
    ROW_NUMBER() OVER (
    PARTITION BY com_id
    ORDER BY reunion_date ASC, reunion_id ASC
    ) AS rn
    FROM reunion
    ) t ON r.reunion_id = t.reunion_id
    SET r.reunion_num_com = t.rn;

-- changeset sigmap:17496719590110-3

ALTER TABLE reunion
    MODIFY reunion_num_com INT NOT NULL;

-- changeset sigmap:17496719590110-4

ALTER TABLE reunion
    ADD CONSTRAINT uq_reunion_com_num
        UNIQUE (com_id, reunion_num_com);

