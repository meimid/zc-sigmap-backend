-- liquibase formatted sql

-- changeset sigmap:1757950258-1
ALTER TABLE dyna_docs ADD COLUMN IF NOT EXISTS is_aao TINYINT(1) DEFAULT 0 NOT NULL;

-- changeset sigmap:1757950258-2
UPDATE dyna_docs SET is_aao = 0 WHERE is_aao IS NULL;