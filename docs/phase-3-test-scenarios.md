# Phase 3 Test Scenarios

## Purpose

This document defines the implementation and verification baseline for Phase 3: Shared Booking Infrastructure.

Security is not a follow-up track. Each use case must ship with functional tests and attack-oriented tests in the same delivery slice.

## Test Layers

- backend unit tests for validation, transitions, normalization, and policy rules
- backend integration tests for persistence, authorization, tenant scoping, and HTTP behavior
- frontend component tests for history filters, forms, validation, and feedback
- end-to-end tests for the most critical cross-screen booking-management journeys
- security tests for abuse controls, tenant isolation, CSRF, recent-auth, and audit behavior

## Cross-Cutting Security Baseline

Apply these checks to every public or authenticated Phase 3 endpoint:

- tenant identifiers cannot expose another tenant's records
- anonymous public endpoints are rate-limited and return neutral failure behavior where appropriate
- authenticated write endpoints require CSRF protection
- sensitive admin configuration writes require recent authentication
- audit events are created for high-impact booking changes

## UC-29 Manage Customer Contacts

### Backend Unit

- valid contact payload is accepted
- duplicate or normalized identity collisions are handled safely
- invalid email, phone, and oversized notes are rejected

### Backend Integration

- contact create and update persist within the current tenant
- cross-tenant contact reads and updates are denied

### Frontend

- contact form loads and validates correctly
- save success updates the contact list or detail view

### Security

- guessed contact identifiers from another tenant are rejected
- staff access follows configured role policy
- contact updates create audit records if required by policy

## UC-30 View Booking History

### Backend Integration

- history endpoint returns only current-tenant bookings
- unsupported filters are rejected safely

### Frontend

- history list renders booking status, customer, and timestamps
- filters update the results correctly

### Security

- cross-tenant history filters return no data and no leakage
- lower-privileged roles are denied if policy restricts access

## UC-31 Change Booking Status

### Backend Unit

- valid status transitions are accepted
- invalid or duplicate transitions are rejected or handled idempotently

### Backend Integration

- status change persists and is visible in history
- notifications are triggered only for allowed transitions

### Frontend

- status actions render only when allowed
- invalid transitions surface a clear error

### Security

- tenant scoping is enforced on status-change requests
- CSRF is required for status changes
- audit records include actor, action, and outcome

## UC-32 Configure Booking Notification Triggers

### Backend Unit

- supported trigger combinations are accepted
- invalid destinations or unsupported events are rejected

### Backend Integration

- configuration persists and is returned correctly
- non-admin access is denied

### Frontend

- trigger settings form renders and saves correctly

### Security

- write endpoint requires CSRF and recent authentication
- malformed destination values cannot bypass validation

## UC-33 View Booking Audit Trail

### Backend Integration

- booking audit list returns tenant-owned entries only
- role restrictions are enforced

### Frontend

- audit view renders actor, event, outcome, and timestamp data

### Security

- audit records cannot be modified through any endpoint
- privileged audit views do not expose sensitive token or secret values

## UC-34 Submit Public Booking Intake Data

### Backend Unit

- valid public intake payload is accepted
- malformed fields and unsupported combinations are rejected

### Backend Integration

- public intake endpoint accepts valid submissions for active tenants
- inactive or disabled tenant flows are rejected safely

### Frontend

- public intake form validates required fields
- neutral success or rejection state is shown correctly

### End-to-End

- anonymous customer submits valid intake data for an active tenant

### Security

- repeated public submissions are rate-limited
- input validation does not rely on the client only
- inactive-tenant and unknown-tenant responses do not leak more information than policy allows
