# Repository Guidelines

## Project Structure & Module Organization

This repository currently contains planning and architecture documentation in [`docs/`](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs) plus the top-level [`README.md`](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\README.md). The intended application layout is:

- `apps/web` for the Next.js frontend
- `apps/api` for the Kotlin + Spring Boot backend
- `docs` for architecture, roadmap, domain, and testing documents

Keep tests close to the code they verify where practical, with integration and end-to-end tests grouped clearly by app.

## Build, Test, and Development Commands

Use project-local commands from the repository root where possible:

- `npm run dev:web` to start the frontend locally
- `npm run test:web` to run frontend unit/integration tests
- `npm run lint:web` to run frontend linting
- `npm run build:web` to run the frontend production build
- `./gradlew :apps:api:test` to run backend tests
- `docker compose up` at the repo root to start local infrastructure

These commands are also the current CI baseline and should stay green before changes are merged.

Use the local environment templates before introducing new configuration:

- `.env.example`
- `apps/api/.env.example`
- `apps/web/.env.example`

## Coding Style & Naming Conventions

Use 2 spaces for frontend files and 4 spaces for Kotlin files. Prefer clear, descriptive names over abbreviations.

- React components: `PascalCase`
- React hooks: `useSomething`
- Kotlin classes: `PascalCase`
- Kotlin functions and variables: `camelCase`
- Docs: lowercase kebab-case filenames such as `testing-strategy.md`

Adopt formatting and linting early and keep them automated.

## Testing Guidelines

Backend development should follow TDD by default. Frontend work must include automated tests for critical behavior.

- Backend: JUnit 5, Kotest, MockK, Testcontainers
- Frontend: Vitest, React Testing Library, Playwright

Name tests so the behavior is obvious, for example `CompanyRegistrationServiceTest` or `booking-form.test.tsx`.

## Commit & Pull Request Guidelines

There is no Git history yet, so use a simple convention from the start:

- Commit format: `type: short summary`
- Examples: `docs: add contributor guide`, `feat: add company registration endpoint`

PRs should include:

- a short problem/solution description
- linked issue or task when available
- test evidence
- updated documentation when behavior or decisions changed

## Documentation Rule

Documentation is part of the deliverable. Any code change that affects behavior, architecture, testing expectations, or operational assumptions must update the relevant files in [`docs/`](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs) in the same change.
