# Stack Decision

## Purpose

This document defines the initial technology baseline for the Reservenook platform. The goal is to make early technical decisions explicit so the project can evolve without unnecessary drift.

The platform is intended to be:

- one product
- one codebase
- one shared tenant model
- one shared infrastructure baseline
- multiple business-specific booking modules

The initial implementation target is a modular monolith, not microservices.

## Chosen Stack

### Frontend

- Next.js
- React
- TypeScript
- MUI
- React Hook Form
- Zod
- TanStack Query
- next-intl

### Backend

- Kotlin
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- Bean Validation
- Flyway

### Data and Infrastructure

- PostgreSQL
- Redis when needed
- Docker Compose

### Testing

- Backend: JUnit 5, Kotest, MockK, Testcontainers
- Frontend: Vitest, React Testing Library, Playwright

## Why This Stack

### Frontend Rationale

Next.js is the preferred frontend framework because the product needs:

- a public marketing website
- SEO-friendly public booking pages
- authenticated company backoffice pages
- platform admin pages
- localization support
- a strong React ecosystem

MUI is selected to speed up delivery of backoffice screens, form-heavy interfaces, data tables, dialogs, and administrative workflows.

React Hook Form and Zod are selected to keep form state and validation explicit and maintainable.

TanStack Query is selected for server-state management, cache control, and mutation flows.

next-intl is selected because localization is a core product requirement from the start.

### Backend Rationale

Kotlin with Spring Boot is selected because the backend must handle:

- multi-tenant business logic
- authentication and role-based authorization
- complex booking rules
- email and notification flows
- modular domain design
- robust validation and transaction handling

Spring Boot provides a mature and well-supported foundation for this shape of application. Kotlin improves readability and domain modeling compared to a more verbose Java-first style.

### Data Rationale

PostgreSQL is selected as the primary database because the product needs:

- strong relational consistency
- transactional booking operations
- clear tenant scoping
- flexible indexing strategies
- support for future reporting queries

Redis is not a mandatory foundation dependency. It should only be introduced when there is a concrete need such as:

- rate limiting
- short-lived token storage
- caching expensive availability computations
- lightweight background coordination

### Deployment Rationale

Docker Compose is the initial deployment and local development baseline because it is sufficient for:

- local development environments
- CI service dependencies
- single-server production deployments
- reproducible application startup

The Phase 0 baseline must be strong enough to start the frontend, backend, database, and local email sandbox through Docker Compose.

The repository should provide example environment files so local configuration remains explicit and reproducible across frontend, backend, and infrastructure startup.

Terraform is intentionally excluded from the initial stack because infrastructure complexity does not yet justify it.

## Licensing Position

The project should remain buildable and publishable without requiring paid technologies.

The selected stack is acceptable under that constraint with the following practical rule:

- avoid features that require paid MUI tiers unless explicitly approved later
- use Linux server deployments with Docker Engine and Compose
- avoid dependence on commercial-only local tooling where possible

## Initial Project Structure Direction

The repository should be organized around a clear separation of frontend, backend, infrastructure, and documentation:

- `apps/web` for the Next.js application
- `apps/api` for the Spring Boot application
- `packages` for shared frontend packages when justified
- `infra` for container and environment-related assets
- `docs` for architectural and planning documentation

Initial placeholders such as `packages/ui` and `packages/config` may exist early, but they should only receive real implementation when there is clear reuse value.

## Initial Non-Goals

The following are intentionally not part of the initial baseline:

- microservices
- Kubernetes
- Terraform
- GraphQL
- event-driven distributed architecture
- separate databases per tenant

These may become relevant later, but they are not justified at the start of this project.
