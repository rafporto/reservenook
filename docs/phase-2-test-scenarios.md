# Phase 2 Test Scenarios

## Purpose

This document defines the test scenarios for the Phase 2 use cases.

Its goal is to turn the Phase 2 use-case specification into an implementation-ready testing baseline for backend TDD, frontend behavior tests, and selected end-to-end checks for the company backoffice.

## Test Layers

Use the following test layers by default:

- backend unit tests for company-configuration validation rules, staff-management rules, and widget-setting policy checks
- backend integration tests for persistence, authorization, tenant scoping, and HTTP endpoints
- frontend component and integration tests for forms, validation, save feedback, list rendering, and protected navigation
- end-to-end tests only for the most critical configuration journeys that prove the backoffice baseline works end to end

## UC-17 View Company Configuration Dashboard

### Backend Integration

- company configuration dashboard endpoint is accessible only to the current tenant
- non-member access is denied
- platform-admin-only or unrelated role access is denied when inappropriate

### Frontend

- configuration dashboard renders the expected sections
- unauthorized access redirects or shows access denied
- tenant-safe company context is displayed

### End-to-End

- company admin logs in and reaches the shared configuration dashboard

## UC-18 Update Company Profile

### Backend Unit

- valid company profile update is accepted
- invalid email or phone format is rejected according to policy
- required business fields cannot be cleared if policy forbids it

### Backend Integration

- profile update endpoint persists the new company values
- profile reads return the updated values
- cross-tenant profile update is denied

### Frontend

- profile form loads existing values
- invalid inputs show field-level feedback
- successful save shows confirmation and updated values

### End-to-End

- company admin updates profile data and sees it persist after reload

## UC-19 Configure Company Branding

### Backend Unit

- supported branding values are accepted
- invalid color values are rejected
- invalid logo reference or asset rules are rejected according to policy

### Backend Integration

- branding settings endpoint persists valid configuration
- tenant cannot update another tenant's branding

### Frontend

- branding form renders current values
- invalid branding values show clear validation feedback
- successful save updates preview or confirmation state

## UC-20 Configure Company Language And Locale Defaults

### Backend Unit

- supported language is accepted
- supported locale is accepted
- unsupported language is rejected
- unsupported locale is rejected
- invalid language and locale combination is rejected according to policy

### Backend Integration

- localization settings persist and are returned correctly
- tenant scoping is enforced for localization updates

### Frontend

- language and locale selectors render supported options
- invalid combinations are blocked with explicit feedback
- successful save updates the displayed defaults

### End-to-End

- company admin updates default language or locale and sees the new values after refresh

## UC-21 Configure Business Hours

### Backend Unit

- valid opening and closing windows are accepted
- overlapping intervals are rejected
- closing time before opening time is rejected
- fully closed day is stored correctly

### Backend Integration

- business-hours endpoint persists weekly schedule
- schedule read returns normalized time values
- tenant scoping is enforced

### Frontend

- business-hours editor loads existing schedule
- invalid time ranges show validation feedback
- save confirmation is shown for valid schedule updates

### End-to-End

- company admin updates weekly hours and sees the saved schedule after reload

## UC-22 Manage Closure Dates

### Backend Unit

- single closure date is accepted
- valid closure range is accepted
- end date before start date is rejected
- overlapping closure periods are merged or rejected according to policy

### Backend Integration

- closure-date endpoint persists add, edit, and remove operations
- tenant scoping is enforced for closure-date operations

### Frontend

- closure list renders existing entries
- invalid date range shows validation feedback
- create, edit, and delete actions update the screen correctly

## UC-23 Configure Notification Preferences

### Backend Unit

- supported notification preferences are accepted
- unsupported channel or event type is rejected
- invalid destination details are rejected

### Backend Integration

- notification preference endpoint persists valid configuration
- subsequent reads return the updated preferences
- tenant scoping is enforced

### Frontend

- notification preferences form renders current values
- toggling supported options updates the draft state correctly
- invalid destination details show validation feedback
- successful save shows confirmation

## UC-24 List Staff Users

### Backend Unit

- company admin role grants access to tenant staff list
- non-admin role is denied if policy requires admin-only staff management

### Backend Integration

- staff list endpoint returns only current-tenant users
- cross-tenant staff visibility is denied

### Frontend

- staff management screen renders tenant staff list
- unauthorized staff access is handled safely
- role and status values are visible in the list

### End-to-End

- company admin opens staff management and sees only users from the same tenant

## UC-25 Create Staff User

### Backend Unit

- valid staff creation is accepted
- invalid email is rejected
- invalid role selection is rejected
- conflicting identity state is handled safely
- onboarding email or activation path is prepared correctly

### Backend Integration

- create-staff endpoint persists user and membership records
- onboarding dispatch is triggered
- tenant scoping is enforced for creation

### Frontend

- create-staff form validates required fields
- backend conflict errors are rendered clearly
- successful creation updates the staff list or confirmation state

### End-to-End

- company admin creates a staff user and the new user appears in the tenant staff list

## UC-26 Update Staff User Status Or Role

### Backend Unit

- valid role change is accepted
- valid active-status change is accepted
- invalid role transition is rejected according to policy
- removing the last required company admin is rejected if policy forbids it

### Backend Integration

- staff update endpoint persists new role or status
- cross-tenant update is denied

### Frontend

- staff detail or inline editor shows current role and status
- invalid changes surface policy feedback
- successful update refreshes the displayed state

## UC-27 Configure Custom Customer Questions

### Backend Unit

- supported question types are accepted
- missing label is rejected
- invalid option set for selectable questions is rejected
- reorder operation preserves valid question ordering

### Backend Integration

- custom-question endpoint persists create, edit, reorder, enable, and disable operations
- tenant scoping is enforced for all question operations

### Frontend

- question list renders current configuration
- add and edit flows validate required fields
- reorder or enable-disable actions update the displayed state

### End-to-End

- company admin creates custom customer questions and sees them persist after reload

## UC-28 Configure Widget Settings Baseline

### Backend Unit

- supported widget settings are accepted
- invalid domain values are rejected according to policy
- unsupported widget options are rejected

### Backend Integration

- widget settings endpoint persists valid baseline configuration
- widget settings are returned correctly on subsequent reads
- tenant scoping is enforced

### Frontend

- widget settings form loads saved values
- invalid settings show validation feedback
- successful save shows confirmation state

## Cross-Cutting Security and Tenant Tests

These scenarios should exist independently of individual use cases:

- company configuration endpoints always enforce tenant scoping
- company users cannot read or modify another tenant's settings
- staff management endpoints reject unauthorized company roles
- platform-level context cannot bypass company-level authorization rules where not intended
- audit-relevant configuration changes can be recorded or traced according to policy
- unsupported localization or widget values are rejected centrally

## Minimum End-to-End Suite for Phase 2

The first Phase 2 end-to-end suite should stay selective:

- company admin reaches the shared configuration dashboard
- company profile update
- language or locale update
- business-hours configuration
- staff user creation
- custom customer question configuration

Branding, closure dates, notification preferences, and widget settings can initially rely more heavily on backend unit and integration tests unless they become critical entry flows for later public booking work.
