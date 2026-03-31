# Domain Model Outline

## Purpose

This document provides an initial domain decomposition for the platform. It is not a final schema. Its purpose is to define ownership boundaries and shared concepts before implementation begins.

## Core Product Model

The system supports three business types within one platform:

- appointment-based businesses
- class-based businesses
- restaurants

All tenants share a common platform foundation while using business-type-specific booking capabilities.

## Shared Core Domains

### Companies

Responsibilities:

- tenant identity
- company lifecycle
- activity timestamps for lifecycle evaluation
- business type selection
- slug ownership
- activation status
- trial or paid status
- locale defaults

Core concepts:

- Company
- CompanySlug
- BusinessType
- CompanyStatus
- CompanyActivityTimestamp
- ActivationPeriod

### Users

Responsibilities:

- platform identity
- credentials
- membership in company scope
- invitations
- password lifecycle

Core concepts:

- User
- CompanyMembership
- Invitation
- PasswordResetToken
- EmailVerificationToken

### Roles and Permissions

Responsibilities:

- platform-level access
- company-level access
- domain-specific access restrictions

Core concepts:

- PlatformRole
- CompanyRole
- Permission
- RoleAssignment

### Plans and Subscription

Responsibilities:

- trial periods
- paid activation periods
- renewal visibility
- future billing integration points

Core concepts:

- Plan
- Subscription
- TrialPeriod
- ActivationStatus

### Localization

Responsibilities:

- language defaults
- locale defaults
- customer-facing formatting
- localized notifications

Core concepts:

- Language
- LocaleSetting

### Notifications

Responsibilities:

- customer email notifications
- staff notifications
- event-triggered messages

Core concepts:

- NotificationTemplate
- NotificationEvent
- DeliveryRequest
- InactivityNotificationEvent
- DeletionWarningEvent

### Customer Data and Questions

Responsibilities:

- customer contact data
- tenant-defined booking questions
- booking input data collection

Core concepts:

- CustomerContact
- CustomQuestion
- QuestionAnswer
- ConsentRecord

### Audit and Compliance

Responsibilities:

- relevant account action auditability
- deletion workflow support
- compliance-relevant activity tracking
- platform lifecycle policy configuration

Core concepts:

- AuditEntry
- DeletionRequest
- DeletionExecution
- InactivityPolicy

## Appointment Domain

Responsibilities:

- service definitions
- provider calendars
- appointment availability
- appointment booking

Core concepts:

- Service
- Provider
- ProviderCalendar
- AvailabilityRule
- AppointmentSlot
- AppointmentBooking

Key rules:

- service duration and slot step may differ
- providers may have different working hours
- overlapping appointments must be prevented where applicable
- confirmation may be automatic or manual

## Classes Domain

Responsibilities:

- class type management
- instructor assignment
- scheduled sessions
- capacity tracking
- class booking

Core concepts:

- ClassType
- Instructor
- Session
- SessionCapacity
- ClassBooking

Key rules:

- bookings reduce available capacity
- sold-out sessions must not accept further bookings
- booking windows may open and close based on business rules
- confirmation may be automatic or manual

## Restaurant Domain

Responsibilities:

- dining areas
- tables
- combinable table logic
- service periods
- reservation policies
- reservation assignment

Core concepts:

- DiningArea
- Table
- TableCombination
- ServicePeriod
- ReservationPolicy
- ReservationRequest
- Reservation
- TableAssignment

Key rules:

- reservations depend on party size, time, duration, and availability
- a valid booking may require one table or a compatible combination
- large-party rules may differ from normal reservations
- confirmation may be automatic or manual

## Shared Booking Concepts

Even though each booking domain has specialized logic, a few cross-domain concepts should remain aligned.

Shared concepts:

- BookingStatus
- ConfirmationMode
- CancellationReason
- CustomerInputSnapshot
- NotificationTrigger

These should be designed carefully to avoid forcing very different booking domains into an artificial common model.

## Tenant Ownership Rule

Every tenant-owned aggregate must be traceable to a company.

Examples:

- services belong to a company
- sessions belong to a company
- tables belong to a company
- bookings belong to a company
- staff memberships belong to a company

This rule must remain visible in both the code model and the database model.

## Domain Modeling Guidance

When implementation begins:

- prefer explicit value objects over stringly typed fields for important concepts
- model business rules in domain and application layers, not controllers
- keep module boundaries strict
- avoid premature generic abstractions across the three booking domains

The platform is unified, but the business logic is not identical. The model should reflect that reality.
