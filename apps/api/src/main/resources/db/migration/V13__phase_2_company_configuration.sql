ALTER TABLE users ADD COLUMN full_name VARCHAR(255);

ALTER TABLE companies ADD COLUMN brand_display_name VARCHAR(255);
ALTER TABLE companies ADD COLUMN brand_logo_url VARCHAR(1000);
ALTER TABLE companies ADD COLUMN brand_accent_color VARCHAR(7);
ALTER TABLE companies ADD COLUMN support_email VARCHAR(255);
ALTER TABLE companies ADD COLUMN support_phone VARCHAR(64);
ALTER TABLE companies ADD COLUMN notification_destination_email VARCHAR(255);
ALTER TABLE companies ADD COLUMN notify_on_new_booking BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE companies ADD COLUMN notify_on_cancellation BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE companies ADD COLUMN notify_daily_summary BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE companies ADD COLUMN widget_cta_label VARCHAR(80);
ALTER TABLE companies ADD COLUMN widget_enabled BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE companies ADD COLUMN widget_allowed_domains VARCHAR(2000);
ALTER TABLE companies ADD COLUMN widget_theme_variant VARCHAR(32) NOT NULL DEFAULT 'minimal';

CREATE TABLE company_business_hours (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    day_of_week VARCHAR(16) NOT NULL,
    opens_at TIME NOT NULL,
    closes_at TIME NOT NULL,
    display_order INTEGER NOT NULL
);

CREATE TABLE company_closure_dates (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    label VARCHAR(255),
    starts_on DATE NOT NULL,
    ends_on DATE NOT NULL
);

CREATE TABLE company_customer_questions (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    label VARCHAR(255) NOT NULL,
    question_type VARCHAR(32) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT false,
    enabled BOOLEAN NOT NULL DEFAULT true,
    display_order INTEGER NOT NULL,
    options_text VARCHAR(4000)
);
