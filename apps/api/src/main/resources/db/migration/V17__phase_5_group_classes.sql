CREATE TABLE class_types (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    duration_minutes INTEGER NOT NULL,
    default_capacity INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    auto_confirm BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE class_instructors (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    linked_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    display_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE class_sessions (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    class_type_id BIGINT NOT NULL REFERENCES class_types(id) ON DELETE RESTRICT,
    instructor_id BIGINT NOT NULL REFERENCES class_instructors(id) ON DELETE RESTRICT,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    capacity INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE class_bookings (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    class_session_id BIGINT NOT NULL REFERENCES class_sessions(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    waitlist_position INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_class_types_company_id ON class_types(company_id);
CREATE INDEX ix_class_instructors_company_id ON class_instructors(company_id);
CREATE INDEX ix_class_sessions_company_starts_at ON class_sessions(company_id, starts_at);
CREATE INDEX ix_class_sessions_instructor_id ON class_sessions(instructor_id);
CREATE INDEX ix_class_bookings_company_session_status ON class_bookings(company_id, class_session_id, status);
