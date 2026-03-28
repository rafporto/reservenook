# Reservenook

Reservenook is a multi-tenant booking platform for three business types in one product:

- appointment-based businesses
- group classes
- restaurants

The platform is planned as:

- one codebase
- one shared tenant model
- one shared core
- specialized booking modules per business type

## Technology Baseline

- Frontend: Next.js, React, TypeScript, MUI
- Backend: Kotlin, Spring Boot
- Data: PostgreSQL
- Infrastructure: Docker Compose

## Repository Structure

- `apps/web` for the frontend skeleton
- `apps/api` for the backend skeleton
- `packages` for shared packages
- `infra` for infrastructure-related assets
- `docs` for planning and architecture documentation

## Current App Scaffolds

- `apps/web` is now a real Next.js application scaffold with TypeScript, MUI, TanStack Query, and Vitest wiring
- `apps/api` is now a real Kotlin and Spring Boot scaffold with Web, Security, JPA, Flyway, and test dependencies

## Local Commands

- `npm run dev:web` to start the frontend locally once dependencies are installed
- `npm run test:web` to run frontend tests
- `npm run lint:web` to run frontend linting
- `npm run build:web` to build the frontend for production
- `docker compose up -d` to start PostgreSQL, Redis, and Mailpit
- `./gradlew :apps:api:test` to run backend tests once the Gradle wrapper is present

## Environment Templates

Use the example files as the local baseline:

- [/.env.example](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\.env.example) for shared local defaults
- [apps/api/.env.example](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\apps\api\.env.example) for backend variables
- [apps/web/.env.example](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\apps\web\.env.example) for frontend variables

Recommended local setup:

1. copy each example to a local `.env` file when needed
2. start infrastructure with `docker compose up -d`
3. run the backend with `.\gradlew.bat :apps:api:bootRun`
4. run the frontend with `npm run dev:web`

## CI Baseline

GitHub Actions runs the Phase 0 verification baseline on pushes to `main` and on pull requests:

- frontend tests
- frontend lint
- frontend production build
- backend tests

## Documentation

Project decisions and planning are documented in:

- [docs/stack.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\stack.md)
- [docs/architecture.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\architecture.md)
- [docs/roadmap.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\roadmap.md)
- [docs/testing-strategy.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\testing-strategy.md)
- [docs/domain-model.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\domain-model.md)
- [docs/conventions.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\conventions.md)

## Working Rule

Documentation is part of the codebase.

Any code change that affects architecture, domain behavior, stack decisions, delivery scope, testing policy, or operational assumptions must include updates to the relevant documentation files in the same change.

Code is not considered complete if the necessary documentation updates are missing.
