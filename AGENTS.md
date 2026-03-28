# Repository Guidelines

## Project Structure & Module Organization

This repository currently contains planning and architecture documentation in [`docs/`](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs) plus the top-level [`README.md`](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\README.md). The intended application layout is:

- `apps/web` for the Next.js frontend
- `apps/api` for the Kotlin + Spring Boot backend
- `docs` for architecture, roadmap, domain, and testing documents

Keep tests close to the code they verify where practical, with integration and end-to-end tests grouped clearly by app.

## Build, Test, and Development Commands

Application commands are not available yet because the apps have not been scaffolded. When implementation begins, standardize around project-local commands and document them in `README.md`.

Expected examples:

- `npm run dev` in `apps/web` to start the frontend locally
- `npm test` in `apps/web` to run frontend unit/integration tests
- `./gradlew test` in `apps/api` to run backend tests
- `docker compose up` at the repo root to start local infrastructure

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
