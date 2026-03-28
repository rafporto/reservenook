# Architecture Overview

## Architectural Principle

The platform is built as:

**one platform, shared core, specialized booking modules**

This means:

- one product
- one codebase
- one infrastructure baseline
- one tenant model
- one shared authentication model
- one shared administration model
- separate domain logic for appointments, classes, and restaurant reservations

## System Shape

The recommended architecture is a modular monolith.

That means:

- one frontend application for the main product experience
- one backend application containing multiple domain modules
- one primary relational database
- strict modular boundaries inside the codebase

The project should avoid early distribution complexity and keep deployment simple while preserving internal separation between domains.

## Major Product Surfaces

The system contains four major UI surfaces:

### Public Main Website

This includes:

- landing pages
- feature descriptions
- pricing
- trial information
- registration
- login entry points
- legal pages

The public main website is the page served at the main web root such as `http://localhost:3000` in local development. It is the product marketing and entry page for public users.

Registration must be initiated from the public website.

The platform admin area must be reachable through a separate URL path that is not linked from the public website navigation.

### Company Public Booking Page

This includes:

- tenant-specific slug-based public pages
- business-type-specific booking flows
- localization based on company defaults
- responsive layout for desktop and mobile

### Company Backoffice

This includes:

- company configuration
- user and staff management
- booking configuration
- operational booking management
- role-limited access

### Platform Admin Area

This includes:

- platform-level company listing
- company status visibility
- plan and activation visibility
- future platform administration growth

## Backend Module Layout

The backend should be split into modules by business capability.

### Shared Core Modules

- companies
- tenant context
- users
- roles and permissions
- authentication
- subscriptions and plans
- localization
- notifications
- custom questions
- customer contacts
- audit logging
- widgets

### Booking-Specific Modules

- appointments
- classes
- restaurants

Each booking module owns its own business rules, validation rules, and persistence model while integrating with the shared core.

## Tenant Model

The platform is multi-tenant with one shared database.

Tenant isolation rules:

- each company is a tenant
- each tenant-owned row must carry a tenant identifier
- application services must always operate inside tenant scope
- authorization decisions must be tenant-aware
- cross-tenant reads and writes must be impossible by design

Deletion rules:

- deleting a company must delete or anonymize all tenant-owned data according to product policy and compliance requirements

## Suggested Layering

Each backend module should follow a consistent internal layering model:

- domain
- application
- infrastructure
- api

### Domain Layer

Contains:

- entities
- value objects
- domain services
- invariants
- domain events if needed

### Application Layer

Contains:

- use cases
- orchestration
- transaction boundaries
- authorization checks

### Infrastructure Layer

Contains:

- JPA mappings and repositories
- mail integration
- external service adapters
- persistence configuration

### API Layer

Contains:

- HTTP controllers
- request and response DTOs
- validation entry points

## Frontend Shape

The frontend should separate concerns by route and product surface rather than by arbitrary component type alone.

Suggested top-level areas:

- marketing
- auth
- tenant booking
- company backoffice
- platform admin

Frontend responsibilities:

- rendering public and authenticated pages
- handling client-side interactions and forms
- localization and formatting
- progressive loading of booking flows
- access control at the UI level in combination with backend enforcement

Public-page localization rules:

- the public site and related public flows should display a language selector in the top area
- English is the default language
- the selected language should persist when navigating to other public pages
- URL-based language routing is the preferred baseline so the selected language remains explicit and shareable

## Widget Strategy

The booking widget should be treated as a separate frontend deliverable, even if it shares design or domain concepts with the main web application.

Widget requirements:

- embeddable on third-party sites
- responsive
- tenant-aware
- business-type-aware
- aligned with backend availability rules

The widget should not duplicate backend business rules. All availability and booking validity must remain server-authoritative.

## Security Boundaries

Security requirements should be enforced centrally, not left to UI conventions.

Core security concerns:

- secure password storage
- email verification
- password reset
- session protection
- role-based access control
- tenant-aware authorization
- abuse protection on public booking endpoints
- validation of all public input

## Evolution Strategy

The architecture should be optimized for delayed decisions.

This means:

- keep deployment simple early
- keep domain boundaries strong
- avoid premature infrastructure complexity
- allow extraction of modules later only if needed

The system should remain a modular monolith until there is clear operational evidence that a distributed architecture is necessary.
