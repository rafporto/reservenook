# Phase 5 Test Scenarios

## Purpose

This document defines the implementation and test baseline for Phase 5: Group Classes Module.

## Test Layers

- backend unit tests for class-template, session, capacity, and waitlist rules
- backend integration tests for persistence, concurrency, and authorization
- frontend tests for class-management screens and public class booking forms
- end-to-end tests for class discovery and booking
- security tests for capacity abuse, duplicate booking, tenant isolation, and instructor-scope access

## UC-43 Manage Class Types

### Security

- non-admin writes are denied
- class-type updates require audit records

## UC-44 Manage Instructors

### Security

- cross-tenant instructor access is denied
- linking instructor records to users validates tenant membership

## UC-45 Schedule Class Sessions

### Security

- invalid overlaps or impossible capacities are rejected server-side
- CSRF and recent-auth apply to sensitive schedule writes

## UC-46 View Public Class Availability

### Security

- repeated scraping requests are rate-limited
- inactive tenants do not expose bookable sessions

## UC-47 Book Class Session

### Functional

- booking consumes available capacity
- sold-out sessions block further bookings

### Security

- duplicate submissions do not overbook capacity
- replay and high-rate public booking attempts are blocked or throttled
- concurrency tests prove capacity safety

## UC-48 Manage Class Booking Outcomes

### Security

- unauthorized booking-state changes are denied
- booking outcome changes are audited
- waitlist promotion remains safe under concurrent updates
