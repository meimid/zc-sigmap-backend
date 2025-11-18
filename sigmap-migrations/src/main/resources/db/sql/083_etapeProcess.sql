-- liquibase formatted sql

-- changeset sigmap:1757950284-1
ALTER TABLE paa ADD COLUMN etape_process VARCHAR(255) NULL;

-- changeset sigmap:1757950284-2
ALTER TABLE ppm ADD COLUMN etape_process VARCHAR(255) NULL;

-- changeset sigmap:1757950284-3
ALTER TABLE dac ADD COLUMN etape_process VARCHAR(255) NULL;

-- changeset sigmap:1757950284-4
ALTER TABLE agpm ADD COLUMN etape_process VARCHAR(255) NULL;
