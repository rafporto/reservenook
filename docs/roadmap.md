# Delivery Roadmap

## Purpose

This roadmap defines a practical order of delivery for the platform. It is intended to reduce architectural drift and avoid starting feature work before the system foundations are stable.

The sequence favors:

- stable tenant isolation first
- authentication and company lifecycle early
- shared platform capabilities before specialized modules
- one booking domain at a time
- testing and deployability from the beginning

## Phase 0: Foundation and Decisions

Goals:

- establish repository structure
- establish coding standards
- establish architecture rules
- establish testing strategy
- establish Docker-based local environment

Deliverables:

- frontend and backend application skeletons
- PostgreSQL container setup
- development profiles and environment strategy
- CI baseline
- documentation baseline

## Phase 1: Core Tenant and Identity Foundation

Goals:

- support company registration
- support company activation lifecycle
- support login and password flows
- establish tenant scoping

Deliverables:

- company entity and tenant model
- user model
- role model
- platform admin role
- company admin role
- registration flow
- email verification flow
- forgot password and reset password flow
- protected sessions

This phase is a hard prerequisite for almost every later feature.

## Phase 2: Shared Company Configuration

Goals:

- allow a company to configure shared platform settings
- establish reusable backoffice patterns

Deliverables:

- company profile management
- branding settings
- default language and locale settings
- business hours
- closure dates
- notification preferences
- staff user management
- custom customer questions
- widget settings foundation

## Phase 3: Shared Booking Infrastructure

Goals:

- introduce generic booking concepts that are reused across domains
- build common operational screens

Deliverables:

- customer contact model
- booking status model
- notification triggers
- common booking history concepts
- common audit trail patterns
- public input validation and abuse protection baseline

## Phase 4: Appointment Module

Goals:

- deliver the first specialized business module

Deliverables:

- services or appointment types
- provider calendars
- working hours
- slot generation rules
- appointment booking flow
- provider accounts
- manual and automatic confirmation behavior

Reason for priority:

Appointment booking has high overlap with the platform core and is a good first domain to stabilize the architecture.

## Phase 5: Group Classes Module

Goals:

- add group session scheduling and capacity handling

Deliverables:

- class types
- instructors
- scheduled sessions
- capacities
- class booking flow
- sold-out handling
- manual and automatic confirmation behavior

## Phase 6: Restaurant Module

Goals:

- add table-based reservation logic

Deliverables:

- dining areas
- tables
- combinable tables
- service periods
- reservation duration rules
- party-size-aware availability
- reservation booking flow
- role-specific restaurant staff access

Reason for later placement:

Restaurant table assignment and combination logic is likely the most algorithmically complex of the three modules.

## Phase 7: Embedded Widget

Goals:

- allow tenants to embed booking flows on external websites

Deliverables:

- embeddable widget runtime
- tenant-aware initialization
- branding configuration support
- booking-flow integration with hosted backend rules

## Phase 8: Hardening and Operational Maturity

Goals:

- prepare the platform for broader real-world usage

Deliverables:

- audit improvements
- abuse prevention improvements
- deletion and retention workflows
- performance testing of availability logic
- accessibility review
- localization completeness review
- monitoring and alerting baseline

## Work Sequencing Rules

The following delivery rules should guide implementation:

- do not start a specialized booking module before core auth and tenant scoping are stable
- do not treat UI progress as complete without backend rule support
- do not merge significant backend features without automated tests
- do not consider public booking flows complete without responsive behavior and validation coverage

## First Execution Slice

The first real implementation slice should prove the platform baseline end to end.

Recommended first slice:

- company registration
- email activation
- login
- tenant-aware company admin access
- editable company profile
- basic platform admin company listing

This slice validates the most important architectural assumptions before booking-domain complexity begins.
