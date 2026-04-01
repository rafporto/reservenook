# Phase 4 Use Cases

## Purpose

This document defines the use cases for Phase 4: Appointment Module.

Phase 4 delivers the first specialized booking domain and should validate that the shared booking infrastructure supports a real operational workflow.

Security must be implemented together with each use case, especially for public booking, provider scheduling, and appointment-state mutations.

## Actors

- Anonymous Customer
- Company Admin
- Staff User
- Provider
- System

## UC-35 Manage Appointment Services

### Goal

A company admin defines the appointment services customers can book.

### Security Considerations

- admin-only write access
- server-side validation for duration, buffers, pricing, and availability flags
- audit coverage for service changes

## UC-36 Manage Providers

### Goal

A company admin creates and updates provider records and links them to staff accounts when applicable.

### Security Considerations

- provider identity must stay tenant-scoped
- linking a provider to a user must verify tenant ownership
- sensitive write operations require recent-auth and CSRF

## UC-37 Configure Provider Availability

### Goal

An authorized actor defines working hours, exceptions, and booking constraints for a provider.

### Security Considerations

- overlapping rules are rejected server-side
- cross-provider and cross-tenant updates are denied
- changes are audited

## UC-38 Generate Appointment Slots

### Goal

The system derives bookable appointment slots from service, provider, business-hour, and closure constraints.

### Security Considerations

- slot-generation queries must remain tenant-scoped
- invalid or extreme query ranges must be rejected
- performance and abuse guards must protect expensive availability calls

## UC-39 View Public Appointment Availability

### Goal

An anonymous customer views available appointment slots for a company.

### Security Considerations

- public availability endpoints require abuse throttling
- responses must not leak internal blocked periods beyond what the customer needs
- inactive or disabled tenant behavior should be neutral and safe

## UC-40 Book Appointment

### Goal

An anonymous customer submits an appointment booking request or completes a direct booking flow.

### Security Considerations

- public booking payloads require strong validation and anti-automation controls
- slot ownership and freshness must be verified server-side
- booking creation must be idempotent or replay-safe

## UC-41 Confirm Or Reject Appointment

### Goal

An authorized company actor manually confirms or rejects an appointment when manual confirmation is enabled.

### Security Considerations

- only authorized tenant actors can change confirmation state
- stale-state or duplicate confirmation attempts are rejected safely
- confirmation changes are audited and notification-safe

## UC-42 Provider Views Own Schedule

### Goal

A provider-linked user views only their own appointment schedule when provider access is enabled.

### Security Considerations

- provider-scoped access must not reveal other providers' schedules
- role and tenant checks must both apply
- schedule reads should avoid exposing unnecessary customer data
