# Phase 3 Use Cases

## Purpose

This document defines the use cases for Phase 3: Shared Booking Infrastructure.

Phase 3 establishes the cross-domain booking baseline that appointment, class, restaurant, and widget flows will reuse.

Security is part of the phase scope. Every use case in this phase must ship with:

- tenant-scoped authorization
- abuse-resistant public inputs
- audit coverage for sensitive booking changes
- validation that does not trust frontend behavior alone

## Phase 3 Scope

Included:

- customer contact model
- booking status model
- notification trigger baseline
- booking history baseline
- booking audit trail baseline
- public validation and abuse-protection baseline

Excluded:

- appointment-specific slot generation
- class-specific capacity handling
- restaurant table assignment
- external payment capture

## Actors

- Anonymous Customer
- Company Admin
- Staff User
- System

## UC-29 Manage Customer Contacts

### Goal

Company users can create, view, and update tenant-owned customer contacts used by future booking flows.

### Primary Actor

Company Admin or Staff User

### Preconditions

- the user is authenticated in the tenant
- the company is active

### Main Flow

1. the actor opens the customer contacts area
2. the system shows the current tenant contacts
3. the actor creates or updates contact details such as name, email, phone, language, and notes
4. the system validates the submitted data and normalizes identity fields
5. the system stores the contact under the current tenant
6. the system returns the updated contact view

### Alternate Flows

- invalid email or phone: the update is rejected
- duplicate contact according to merge policy: the system links or rejects safely
- another tenant identifier is targeted: access is denied

### Security Considerations

- contact identifiers must be tenant-scoped
- list and detail endpoints must not leak contacts across tenants
- malformed or oversized notes must be validated server-side
- create and update operations must be auditable

### Postconditions

- the company has a reusable customer contact record

## UC-30 View Booking History

### Goal

Company users can view tenant booking history across shared booking records.

### Primary Actor

Company Admin or Staff User

### Preconditions

- the user is authenticated in the tenant
- booking history records exist

### Main Flow

1. the actor opens booking history
2. the system resolves the current tenant
3. the system returns booking history entries with status, customer, timestamps, and source metadata
4. the actor filters or searches the list

### Alternate Flows

- unauthorized tenant scope: access is denied
- unsupported filter value: request is rejected

### Security Considerations

- history queries must enforce tenant scope on every filter path
- searches must be rate-aware if exposed publicly later
- sensitive notes or metadata must follow role policy

### Postconditions

- the actor can review booking activity for the current tenant

## UC-31 Change Booking Status

### Goal

Authorized company users can transition a booking through shared lifecycle states.

### Primary Actor

Company Admin or Staff User

### Preconditions

- the booking exists in the current tenant
- the requested transition is allowed by policy

### Main Flow

1. the actor opens a booking
2. the actor requests a status change such as confirm, cancel, no-show, or complete
3. the system validates the transition and actor permission
4. the system stores the new status
5. the system records audit and history entries
6. the system triggers any configured notifications

### Alternate Flows

- invalid status transition: rejected
- stale or deleted booking: rejected
- unauthorized actor: denied

### Security Considerations

- transition rules must be enforced server-side
- repeated status-change requests must be idempotent or safely rejected
- high-impact changes must be audited with actor identity

### Postconditions

- the booking reflects the new valid status

## UC-32 Configure Booking Notification Triggers

### Goal

A company admin defines which booking lifecycle events trigger operational notifications.

### Primary Actor

Company Admin

### Preconditions

- notification channels exist
- the user has configuration permission

### Main Flow

1. the company admin opens booking notification settings
2. the system shows supported trigger types
3. the company admin enables or disables trigger combinations
4. the system validates the submitted configuration
5. the system stores the trigger settings

### Alternate Flows

- unsupported trigger combination: rejected
- malformed notification destination: rejected

### Security Considerations

- admin-only authorization is required
- write operations require CSRF and recent-auth protections
- destinations must be validated to prevent header or payload abuse

### Postconditions

- the tenant has active booking-notification trigger rules

## UC-33 View Booking Audit Trail

### Goal

Authorized company users can inspect booking audit entries for operational accountability.

### Primary Actor

Company Admin

### Preconditions

- booking audit data exists
- the actor has permission to review operational audit records

### Main Flow

1. the company admin opens booking audit history
2. the system returns actor, action, timestamp, and outcome data for tenant bookings
3. the company admin filters or reviews the records

### Alternate Flows

- user lacks permission: access is denied
- another tenant scope is targeted: access is denied

### Security Considerations

- audit records must not be mutable from the UI
- tenant isolation must apply to audit reads as well as booking reads
- privileged audit views should avoid leaking secrets or tokens

### Postconditions

- the tenant can review accountable booking-change history

## UC-34 Submit Public Booking Intake Data

### Goal

An anonymous customer submits public intake data that becomes the basis of a future booking request.

### Primary Actor

Anonymous Customer

### Preconditions

- the target tenant is active
- the public booking entry point is enabled

### Main Flow

1. the customer opens a public booking entry form
2. the system shows the required contact and booking intake fields
3. the customer submits contact and request details
4. the system validates the payload, abuse controls, and tenant availability rules
5. the system stores or forwards the intake according to product policy
6. the system returns a neutral confirmation or next-step state

### Alternate Flows

- malformed input: rejected with safe validation feedback
- rate limit exceeded: request is blocked
- inactive tenant or disabled public flow: request is denied safely

### Security Considerations

- anonymous entry points must include abuse throttling
- the flow must avoid account or tenant-state enumeration
- inputs must be validated and normalized server-side
- audit and fraud signals should be recorded for blocked abuse

### Postconditions

- a valid public intake record exists or a safe rejection is returned
