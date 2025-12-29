-- liquibase formatted sql
-- changeset sigmap:17496719590105-1

UPDATE agpm
SET published_at = updated_at
WHERE status = 'PUBLIE'
  AND published_at IS NULL
  AND updated_at IS NOT NULL;

UPDATE dac
SET published_at = updated_at
WHERE status = 'PUBLIE'
  AND published_at IS NULL
  AND updated_at IS NOT NULL;

UPDATE paa
SET published_at = updated_at
WHERE status = 'PUBLIE'
  AND published_at IS NULL
  AND updated_at IS NOT NULL;

UPDATE ppm
SET published_at = updated_at
WHERE status = 'PUBLIE'
  AND published_at IS NULL
  AND updated_at IS NOT NULL;