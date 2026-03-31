ALTER TABLE inactivity_notification_events
ADD COLUMN notification_type VARCHAR(64) NOT NULL DEFAULT 'INACTIVITY_NOTICE';
