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

## SOLID Implementation Rule

New code and refactors must follow SOLID principles as a delivery requirement, not as an optional cleanup.

- prefer small focused services and components over multi-purpose orchestration classes
- split backend behavior by use case when a class starts owning unrelated responsibilities
- keep validation, policies, and state-transition rules reusable instead of duplicating them across flows
- favor composition and narrow collaborator contracts over broad dependencies on many repositories or feature areas
- do not grow existing god classes or god components when a new feature can be added by extending smaller collaborators

Practical expectations:

- controllers should stay thin
- application services should usually own one use-case area
- shared rules should move into validators, policies, or support objects
- frontend screens should act as composition shells, with section panels, hooks, and client helpers extracted as complexity grows
- security controls such as tenant checks, CSRF, recent-auth, throttling, and audit logging must stay explicit through the refactor

## Security Baseline Rule

Security hardening is part of feature delivery. It is not a later cleanup pass.

Every new use case and refactor must preserve and extend the current security baseline where applicable:

- tenant isolation and role checks must be enforced on the backend, not only in the UI
- authenticated state-changing requests must keep CSRF protection
- sensitive admin writes must keep recent-auth protection
- public entry points must consider abuse throttling and replay safety
- auth, recovery, admin, and lifecycle-sensitive actions must remain auditable
- session revocation rules must be preserved when credentials or tenant access state changes
- browser-facing routes must keep the existing defensive-header and CSP baseline
- HSTS should remain configurable for secure deployments

Practical expectations:

- implement security together with the use case, not after it
- add regression coverage for authorization, cross-tenant abuse, token misuse, CSRF, and rate-limit behavior when relevant
- do not introduce public or admin endpoints without explicitly checking enumeration, privilege escalation, and tenant-boundary risks
- future phases should extend the same baseline to booking, payment, customer, and other sensitive flows as they are added
- when a change introduces or expands operational risk, add monitoring or alerting with it instead of leaving operator visibility as a later follow-up
- when public-flow performance is materially affected, add or update an executable smoke or performance check rather than relying only on subjective manual testing

## UI, Localization, and SEO Expectations

Frontend implementation is not complete when it only works functionally. Every page should also be evaluated for:

- consistent UX with clear hierarchy, navigation, feedback, and mobile responsiveness
- SEO quality on public pages, including meaningful page structure, metadata, and crawl-friendly content where appropriate

Language behavior is a hard requirement:

- each page must follow the currently selected language when that page is part of a localized flow
- navigation between public pages must preserve the selected language
- new public pages should be designed so the language stays explicit in the route and reflected in the rendered UI

When implementing or updating UI:

- think beyond form submission and endpoint wiring
- consider usability, clarity, accessibility, and search discoverability as part of the deliverable
- keep protected or token-based pages out of SEO-oriented indexing when appropriate, while public marketing and entry pages should be structured for discoverability

## Testing Guidelines

Backend development should follow TDD by default. Frontend work must include automated tests for critical behavior.

- Backend: JUnit 5, Kotest, MockK, Testcontainers
- Frontend: Vitest, React Testing Library, Playwright

Name tests so the behavior is obvious, for example `CompanyRegistrationServiceTest` or `booking-form.test.tsx`.

## Commit & Pull Request Guidelines

There is no Git history yet, so use a simple convention from the start:

- Commit format: `type: short summary`
- Examples: `docs: add contributor guide`, `feat: add company registration endpoint`

Before every commit:

- run the local CI baseline that matches the changed areas
- fix failing checks before creating the commit
- do not commit known build, test, lint, or type errors

At minimum, use the current project baseline where applicable:

- `npm run test:web`
- `npm run lint:web`
- `npm run build:web`
- `./gradlew :apps:api:test`

PRs should include:

- a short problem/solution description
- linked issue or task when available
- test evidence
- updated documentation when behavior or decisions changed

## Documentation Rule

Documentation is part of the deliverable. Any code change that affects behavior, architecture, testing expectations, or operational assumptions must update the relevant files in [`docs/`](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs) in the same change.

Use [`docs/conventions.md`](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\conventions.md) as the implementation baseline before adding new features.
