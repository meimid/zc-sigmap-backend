-- changeset db:20250411-01
ALTER TABLE `lots`
    ADD COLUMN `created_at` datetime(0) NULL,
    ADD COLUMN `created_by` varchar(100) NULL,
    ADD COLUMN `updated_at` datetime(0) NULL,
    ADD COLUMN `updated_by` varchar(100) NULL,
    ADD COLUMN `deleted_at` datetime(0) NULL,
    ADD COLUMN `deleted_by` varchar(100) NULL,
    ADD COLUMN `is_deleted` tinyint(1) NULL;



