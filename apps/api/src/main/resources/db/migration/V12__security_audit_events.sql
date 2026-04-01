CREATE TABLE security_audit_events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    outcome VARCHAR(50) NOT NULL,
    actor_user_id BIGINT NULL,
    actor_email VARCHAR(255) NULL,
    company_slug VARCHAR(255) NULL,
    target_email VARCHAR(255) NULL,
    details VARCHAR(500) NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
