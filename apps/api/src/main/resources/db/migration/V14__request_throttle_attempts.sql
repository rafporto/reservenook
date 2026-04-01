CREATE TABLE request_throttle_attempts (
    id BIGSERIAL PRIMARY KEY,
    scope VARCHAR(64) NOT NULL,
    bucket_key VARCHAR(255) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_request_throttle_scope_key_occurred_at
    ON request_throttle_attempts (scope, bucket_key, occurred_at);

CREATE INDEX idx_request_throttle_occurred_at
    ON request_throttle_attempts (occurred_at);
