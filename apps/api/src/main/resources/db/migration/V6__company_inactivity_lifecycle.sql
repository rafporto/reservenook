ALTER TABLE companies
    ADD COLUMN last_activity_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE companies
    ADD COLUMN inactive_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE companies
    ADD COLUMN deletion_scheduled_at TIMESTAMP WITH TIME ZONE;

UPDATE companies
SET last_activity_at = created_at
WHERE last_activity_at IS NULL;
