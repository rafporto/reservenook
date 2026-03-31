CREATE TABLE company_deletion_events (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    company_name VARCHAR(120) NOT NULL,
    company_slug VARCHAR(120) NOT NULL,
    status VARCHAR(32) NOT NULL,
    failure_reason VARCHAR(500),
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_company_deletion_events_company_id
    ON company_deletion_events (company_id);
