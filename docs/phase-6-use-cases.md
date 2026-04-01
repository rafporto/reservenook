# Phase 6 Use Cases

## Purpose

This document defines the use cases for Phase 6: Restaurant Module.

This phase is algorithmically heavier and must treat security, integrity, and concurrency as part of the first implementation.

## Actors

- Anonymous Customer
- Company Admin
- Restaurant Staff
- System

## UC-49 Manage Dining Areas

### Goal

A company admin manages dining areas such as patio, main hall, or bar.

### Security Considerations

- admin-only writes
- tenant-scoped identifiers and audit coverage

## UC-50 Manage Tables

### Goal

A company admin creates and updates restaurant tables with capacity and area assignments.

### Security Considerations

- capacity and assignment validation is server-side
- cross-tenant table writes are denied

## UC-51 Configure Combinable Tables

### Goal

A company admin defines which tables can be combined for larger parties.

### Security Considerations

- invalid or circular combination rules are rejected
- write actions are audited

## UC-52 Configure Service Periods And Reservation Rules

### Goal

A company admin defines reservation windows, durations, and party-size rules for service periods.

### Security Considerations

- only authorized actors can change operational reservation policy
- recent-auth and CSRF apply to sensitive writes

## UC-53 Compute Restaurant Availability

### Goal

The system computes party-size-aware availability using tables, combinations, and service rules.

### Security Considerations

- availability queries require query caps and abuse protection
- internal assignment logic should not leak unnecessary data publicly

## UC-54 Book Restaurant Reservation

### Goal

An anonymous customer books a restaurant reservation.

### Security Considerations

- public booking requires validation, replay safety, and anti-automation controls
- assignment checks must be concurrency-safe

## UC-55 Manage Reservation Outcomes

### Goal

Restaurant staff confirm, reseat, cancel, or mark arrivals for reservations.

### Security Considerations

- only authorized tenant staff can mutate reservations
- state changes must be audited

## UC-56 Restaurant Staff Views Operational Floorbook

### Goal

Authorized restaurant staff view the reservation floorbook for current and upcoming service periods.

### Security Considerations

- floorbook access is tenant-scoped
- data exposure follows least-privilege rules for customer details
