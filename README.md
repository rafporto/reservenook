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

- `apps/web` now includes the localized public product page and the Phase 1 company registration UI
- `apps/api` now includes the Phase 1 company registration endpoint with persistence, activation-token creation, and activation email dispatch wiring

## Local Commands

- `npm run dev:web` to start the frontend locally once dependencies are installed
- `npm run test:web` to run frontend tests
- `npm run lint:web` to run frontend linting
- `npm run build:web` to build the frontend for production
- `docker compose up --build` to start the full Docker stack
- `docker compose up -d postgres redis mailpit` to start infrastructure only
- `./gradlew :apps:api:test` to run backend tests once the Gradle wrapper is present

## Environment Templates

Use the example files as the local baseline:

- [/.env.example](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\.env.example) for shared local defaults
- [apps/api/.env.example](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\apps\api\.env.example) for backend variables
- [apps/web/.env.example](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\apps\web\.env.example) for frontend variables

Recommended local setup:

1. copy each example to a local `.env` file when needed
2. start the full stack with `docker compose up --build`
3. open the frontend at `http://localhost:3000`
4. open the API health endpoint at `http://localhost:8080/api/public/ping`
5. open Mailpit at `http://localhost:8025`

Alternative local setup:

1. copy each example to a local `.env` file when needed
2. start infrastructure with `docker compose up -d postgres redis mailpit`
3. run the backend with `.\gradlew.bat :apps:api:bootRun`
4. run the frontend with `npm run dev:web`

## Docker Setup

The repository can run the full baseline stack through Docker Compose.

Services started by [docker-compose.yml](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docker-compose.yml):

- `web` on `http://localhost:3000`
- `api` on `http://localhost:8080`
- `postgres` on port `5432`
- `redis` on port `6379`
- `mailpit` UI on `http://localhost:8025` and SMTP on port `1025`

Main commands:

- `docker compose up --build` to build and start the full stack
- `docker compose up -d` to start it in detached mode
- `docker compose down` to stop the stack
- `docker compose logs -f` to follow container logs

Useful checks:

- frontend: `http://localhost:3000` redirects to `http://localhost:3000/en`
- registration page: `http://localhost:3000/en/register`
- activation page: `http://localhost:3000/en/activate?token=...`
- resend activation page: `http://localhost:3000/en/resend-activation`
- login page: `http://localhost:3000/en/login`
- forgot password page: `http://localhost:3000/en/forgot-password`
- reset password placeholder: `http://localhost:3000/en/reset-password?token=...`
- company backoffice placeholder: `http://localhost:3000/app/company/<company-slug>`
- platform admin placeholder: `http://localhost:3000/platform-admin`
- API health: `http://localhost:8080/api/public/ping`
- registration endpoint: `POST http://localhost:8080/api/public/companies/registration`
- activation endpoint: `POST http://localhost:8080/api/public/companies/activation/confirm`
- resend activation endpoint: `POST http://localhost:8080/api/public/companies/activation/resend`
- login endpoint: `POST http://localhost:8080/api/public/auth/login`
- forgot password endpoint: `POST http://localhost:8080/api/public/auth/forgot-password`
- session endpoint: `GET http://localhost:8080/api/auth/session`
- logout endpoint: `POST http://localhost:8080/api/auth/logout`
- Mailpit UI: `http://localhost:8025`

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
- [docs/phase-1-use-cases.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\phase-1-use-cases.md)
- [docs/phase-1-test-scenarios.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\phase-1-test-scenarios.md)

## Working Rule

Documentation is part of the codebase.

Any code change that affects architecture, domain behavior, stack decisions, delivery scope, testing policy, or operational assumptions must include updates to the relevant documentation files in the same change.

Code is not considered complete if the necessary documentation updates are missing.
