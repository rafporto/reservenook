CREATE INDEX ix_company_business_hours_company_day_order
    ON company_business_hours(company_id, day_of_week, display_order);

CREATE INDEX ix_company_closure_dates_company_starts_ends
    ON company_closure_dates(company_id, starts_on, ends_on);

CREATE INDEX ix_appointment_providers_company_active_created
    ON appointment_providers(company_id, active, created_at);

CREATE INDEX ix_provider_availability_provider_day_order
    ON appointment_provider_availability(provider_id, day_of_week, display_order);

CREATE INDEX ix_appointment_bookings_provider_starts_at
    ON appointment_bookings(provider_id, starts_at);

CREATE INDEX ix_class_sessions_company_type_status_starts_at
    ON class_sessions(company_id, class_type_id, status, starts_at);

CREATE INDEX ix_class_bookings_session_status
    ON class_bookings(class_session_id, status);

CREATE INDEX ix_restaurant_tables_company_active_created
    ON restaurant_tables(company_id, active, created_at);

CREATE INDEX ix_restaurant_table_combinations_company_created
    ON restaurant_table_combinations(company_id, created_at);

CREATE INDEX ix_restaurant_service_periods_company_day_active_opens
    ON restaurant_service_periods(company_id, day_of_week, active, opens_at);

CREATE INDEX ix_restaurant_reservations_company_reserved_window
    ON restaurant_reservations(company_id, reserved_at, reserved_until);

CREATE INDEX ix_widget_usage_events_company_created_at
    ON widget_usage_events(company_id, created_at DESC);
