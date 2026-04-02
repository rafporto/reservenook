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
- shared company configuration such as branding, support contacts, notification routing, and widget baseline settings
- operating-calendar foundations such as business hours and closure dates

Core concepts:

- Company
- CompanySlug
- BusinessType
- CompanyStatus
- CompanyActivityTimestamp
- ActivationPeriod
- CompanyBranding
- CompanyBusinessHours
- CompanyClosureCalendar
- CompanyWidgetSettings

### Users

Responsibilities:

- platform identity
- credentials
- membership in company scope
- invitations
- password lifecycle
- company-admin and staff backoffice participation

Core concepts:

- User
- CompanyMembership
- Invitation
- PasswordResetToken
- EmailVerificationToken
- StaffInvitation

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

## Phase 2 Shared Configuration Aggregates

The implemented Phase 2 baseline adds explicit tenant-owned configuration aggregates that are reused by later booking flows.

- company profile fields live on `Company` and represent the public-facing business identity
- branding fields live on `Company` and control display name, logo reference, accent color, and support contacts
- business hours are modeled as tenant-owned weekly entries
- closure dates are modeled as tenant-owned date-range entries
- customer questions are modeled as ordered tenant-owned configuration entries with question type and selectable options where applicable
- widget settings are modeled as tenant-owned embed configuration with domain allow-list validation
- staff management remains identity-plus-membership based, with `CompanyMembership` carrying the tenant role and the linked `User` carrying credential state

## Phase 3 Shared Booking Aggregates

The implemented Phase 3 baseline adds reusable tenant-owned booking concepts before the specialized appointment, class, and restaurant modules.

- `CustomerContact` stores tenant-owned customer identity, language preference, and operational notes
- `Booking` stores the shared booking request record with customer linkage, source, status, preferred date, and internal note fields
- `BookingStatus` is aligned across the shared booking baseline so later modules can reuse the same lifecycle vocabulary
- `BookingAuditEvent` stores immutable tenant-scoped operational history for booking creation and booking-status changes
- booking notification triggers live on `Company` and extend the Phase 2 notification-routing baseline with booking-specific event toggles
- the public booking intake flow reuses tenant customer questions and widget enablement to decide whether anonymous booking requests can be accepted

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
- CompanyDeletionEvent

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
## Phase 4 Appointment Module

The appointment specialization extends the shared booking core with four tenant-scoped entities:

- `AppointmentService` defines what can be booked, including duration, buffer, public availability, and manual-vs-auto confirmation behavior
- `AppointmentProvider` represents the person or resource delivering the appointment and may optionally link to a tenant staff user
- `AppointmentProviderAvailability` defines weekly provider working windows used to generate public slots
- `AppointmentBooking` links a shared `Booking` record to the chosen appointment service, provider, and concrete start/end time

Security and isolation rules for this module:

- appointment services and providers are always scoped to one company
- provider-to-user linking must stay inside the same tenant
- public slot generation only exposes enabled services and concrete bookable slots, not internal blocked windows
- provider self-schedule reads are limited to the linked provider identity rather than the full tenant booking dataset
