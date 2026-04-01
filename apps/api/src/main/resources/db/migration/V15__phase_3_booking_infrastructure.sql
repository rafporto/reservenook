ALTER TABLE companies ADD COLUMN notify_on_booking_confirmed BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE companies ADD COLUMN notify_on_booking_completed BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE companies ADD COLUMN notify_on_booking_no_show BOOLEAN NOT NULL DEFAULT false;

CREATE TABLE customer_contacts (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    normalized_email VARCHAR(255) NOT NULL,
    phone VARCHAR(64),
    preferred_language VARCHAR(16),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX ux_customer_contacts_company_normalized_email
    ON customer_contacts(company_id, normalized_email);

CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    customer_contact_id BIGINT NOT NULL REFERENCES customer_contacts(id) ON DELETE CASCADE,
    status VARCHAR(64) NOT NULL,
    source VARCHAR(64) NOT NULL,
    request_summary VARCHAR(255),
    preferred_date DATE,
    internal_note TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_bookings_company_created_at ON bookings(company_id, created_at DESC);
CREATE INDEX ix_bookings_company_status ON bookings(company_id, status);

CREATE TABLE booking_audit_events (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    action_type VARCHAR(64) NOT NULL,
    actor_user_id BIGINT,
    actor_email VARCHAR(255),
    outcome VARCHAR(64) NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_booking_audit_events_company_created_at
    ON booking_audit_events(company_id, created_at DESC);
