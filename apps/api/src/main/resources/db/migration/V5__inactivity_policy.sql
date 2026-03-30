CREATE TABLE inactivity_policies (
    id BIGSERIAL PRIMARY KEY,
    inactivity_threshold_days INTEGER NOT NULL,
    deletion_warning_lead_days INTEGER NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO inactivity_policies (id, inactivity_threshold_days, deletion_warning_lead_days, updated_at)
VALUES (1, 90, 14, CURRENT_TIMESTAMP);
