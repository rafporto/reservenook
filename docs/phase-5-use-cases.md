# Phase 5 Use Cases

## Purpose

This document defines the use cases for Phase 5: Group Classes Module.

Security must ship with class scheduling and booking behavior, especially around capacity, public booking abuse, and instructor-scope access.

## Actors

- Anonymous Customer
- Company Admin
- Staff User
- Instructor
- System

## UC-43 Manage Class Types

### Goal

A company admin defines class templates such as yoga, spinning, or language lessons.

### Security Considerations

- admin-only writes
- tenant-scoped class-type identifiers
- audit coverage for configuration changes

## UC-44 Manage Instructors

### Goal

A company admin creates and updates instructor records and links them to tenant users where applicable.

### Security Considerations

- tenant ownership must be enforced on linked users
- cross-tenant instructor access is denied

## UC-45 Schedule Class Sessions

### Goal

An authorized actor schedules dated class sessions with instructor, start time, duration, and capacity.

### Security Considerations

- server-side validation for overlap, duration, and capacity
- authenticated writes require CSRF and recent-auth where applicable

## UC-46 View Public Class Availability

### Goal

An anonymous customer views upcoming class sessions and remaining capacity.

### Security Considerations

- public reads require abuse throttling
- responses must not leak internal scheduling data beyond bookable information

## UC-47 Book Class Session

### Goal

An anonymous customer books a place in a class session.

### Security Considerations

- capacity checks must be server-side and concurrency-safe
- duplicate-booking protection is required
- replay and automation protections are required

## UC-48 Manage Class Booking Outcomes

### Goal

Authorized tenant users confirm, cancel, waitlist, or mark attendance for class bookings.

### Security Considerations

- state transitions require authorization and auditability
- waitlist promotion logic must be safe under concurrency
