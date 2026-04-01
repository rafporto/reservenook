# Phase 6 Test Scenarios

## Purpose

This document defines the implementation and verification baseline for Phase 6: Restaurant Module.

## Test Layers

- backend unit tests for area, table, combination, and reservation-rule validation
- backend integration tests for availability algorithms, reservation persistence, and authorization
- frontend tests for restaurant configuration and operational floorbook views
- end-to-end tests for reservation booking and staff handling
- security tests for concurrency, public abuse control, and least-privilege operational access

## UC-49 to UC-52 Configuration Flows

### Security

- non-admin writes are denied
- CSRF and recent-auth protect sensitive configuration changes
- audit events are recorded for operational policy changes

## UC-53 Compute Restaurant Availability

### Security

- high-cost availability queries are rate-limited or capped
- public responses do not expose unnecessary internal assignment details

## UC-54 Book Restaurant Reservation

### Functional

- valid reservation requests allocate a compatible table or combination
- invalid party sizes or unavailable periods are rejected

### Security

- duplicate submissions do not double-book inventory
- concurrent reservation attempts preserve consistency
- public abuse throttling is active

## UC-55 Manage Reservation Outcomes

### Security

- unauthorized outcome changes are denied
- reservation state changes are audited

## UC-56 Restaurant Staff Views Operational Floorbook

### Security

- staff can view only current-tenant floorbook data
- customer details shown follow role policy
