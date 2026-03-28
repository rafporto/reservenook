# Development Conventions

## Purpose

This document defines the implementation rules for the Reservenook codebase. Its purpose is to reduce structural drift before feature development begins.

These conventions apply to frontend, backend, testing, configuration, and documentation work.

## General Rules

- prefer explicitness over clever abstractions
- keep module boundaries clear
- do not introduce infrastructure complexity without documented justification
- update documentation in the same change when implementation affects documented behavior or decisions

## Backend Conventions

The backend is a modular monolith. New code should respect domain boundaries.

Each backend module should follow this internal structure where applicable:

- `domain`
- `application`
- `infrastructure`
- `api`

Rules:

- business rules belong in domain or application code, not controllers
- controllers should validate input, delegate work, and map responses
- persistence concerns should stay in infrastructure code
- avoid leaking JPA entities directly through API responses
- prefer value objects over raw strings for important business concepts

Do not create generic booking abstractions too early across appointments, classes, and restaurants. Shared code should exist only when real duplication is proven.

## Frontend Conventions

The frontend should be organized by feature and product surface, not by technical layer alone.

Use:

- `src/app` for routes and layouts
- `src/features` for business-oriented UI flows
- `src/components` for reusable UI building blocks
- `src/lib` for utilities, providers, and shared client-side helpers

Rules:

- keep route files thin
- keep business-oriented UI logic inside feature modules
- avoid placing API details directly inside presentational components
- prefer server-authoritative flows for booking and validation logic

## Naming and Structure

- React components: `PascalCase`
- hooks: `useSomething`
- Kotlin classes: `PascalCase`
- Kotlin methods and variables: `camelCase`
- documentation files: lowercase kebab-case
- test files should describe behavior clearly

Examples:

- `CompanyRegistrationServiceTest`
- `booking-form.test.tsx`
- `restaurant-reservation-policy.md`

## Testing Rules

- backend work should follow TDD by default
- frontend work must include automated tests for critical behavior
- test business logic before wiring it into controllers or screens
- integration tests should cover security, persistence, and tenant scoping
- do not merge backend business logic without tests

## Configuration Rules

- use example environment files as the baseline for local setup
- add new variables only when they are actually required
- document every new environment variable in the relevant `.env.example` file
- avoid hidden configuration in IDE settings or personal machine setup

## Documentation Rules

The following files must be kept aligned with implementation when affected:

- `docs/stack.md`
- `docs/architecture.md`
- `docs/roadmap.md`
- `docs/testing-strategy.md`
- `docs/domain-model.md`
- `docs/conventions.md`

If code changes invalidate a documented assumption, the documentation must be updated in the same change.

## Delivery Discipline

- keep commits focused
- keep pull requests scoped to one coherent change
- keep CI green before merging
- do not start Phase 1 feature work by bypassing Phase 0 conventions

The project should optimize for clarity, maintainability, and controlled evolution, not speed through accidental complexity.
