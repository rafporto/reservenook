CREATE TABLE appointment_services (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    duration_minutes INTEGER NOT NULL,
    buffer_minutes INTEGER NOT NULL DEFAULT 0,
    price_label VARCHAR(64),
    enabled BOOLEAN NOT NULL DEFAULT true,
    auto_confirm BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE appointment_providers (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    linked_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    display_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE appointment_provider_availability (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL REFERENCES appointment_providers(id) ON DELETE CASCADE,
    day_of_week VARCHAR(16) NOT NULL,
    opens_at TIME NOT NULL,
    closes_at TIME NOT NULL,
    display_order INTEGER NOT NULL
);

CREATE TABLE appointment_bookings (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    appointment_service_id BIGINT NOT NULL REFERENCES appointment_services(id) ON DELETE RESTRICT,
    provider_id BIGINT NOT NULL REFERENCES appointment_providers(id) ON DELETE RESTRICT,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_appointment_services_company_id ON appointment_services(company_id);
CREATE INDEX ix_appointment_providers_company_id ON appointment_providers(company_id);
CREATE INDEX ix_provider_availability_provider_id ON appointment_provider_availability(provider_id);
CREATE INDEX ix_appointment_bookings_company_starts_at ON appointment_bookings(company_id, starts_at);
