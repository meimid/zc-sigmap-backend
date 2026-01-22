-- liquibase formatted sql

-- changeset sigmap:17496719590110-1
ALTER TABLE reunion
    ADD COLUMN year INT NULL;

-- changeset sigmap:17496719590109-2
UPDATE reunion
SET year = YEAR(reunion_date)
WHERE year IS NULL
  AND reunion_date IS NOT NULL
  AND status IN ('VALIDER', 'CLOTURER');

-- changeset sigmap:17496719590109-3
ALTER TABLE reunion
    ADD COLUMN reunion_num_com INT NULL;

-- changeset sigmap:17496719590109-4
UPDATE reunion r
    JOIN (
    SELECT
    reunion_id,
    ROW_NUMBER() OVER (
    PARTITION BY com_id, year
    ORDER BY reunion_date ASC, reunion_id ASC
    ) AS rn
    FROM reunion
    WHERE status IN ('VALIDER', 'CLOTURER')
    AND year IS NOT NULL
    ) t ON r.reunion_id = t.reunion_id
    SET r.reunion_num_com = t.rn;

-- changeset sigmap:17496719590109-5
ALTER TABLE reunion
    ADD CONSTRAINT uq_reunion_com_year_num
        UNIQUE (com_id, year, reunion_num_com);
