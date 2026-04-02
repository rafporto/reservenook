<p align="left">
  <img src="apps/web/public/reservenook-logo.svg" alt="ReserveNook logo" width="280" />
</p>

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

- `apps/web` now includes a branded localized public marketing surface, improved auth/onboarding UX, SEO assets such as route metadata, `robots.txt`, and `sitemap.xml`, the completed Phase 2 tenant backoffice for profile, branding, localization, business hours, closure dates, notification preferences, staff management, customer questions, and widget settings, the Phase 3 shared booking baseline for customer contacts, booking history, booking audit visibility, booking trigger management, and a localized public booking-request page, the Phase 4 appointment module UI for appointment services, provider setup, provider availability, and public slot-based appointment booking, the Phase 5 group-classes UI for class types, instructors, class sessions, class booking outcomes, and public class-session booking, the Phase 6 restaurant backoffice panels for dining areas, tables, table combinations, service periods, and reservation outcomes, the public restaurant reservation flow with slot-based availability, the Phase 7 embedded widget loader and iframe-hosted booking experience with tenant theme support, origin-aware bootstrap, and widget usage visibility in the company backoffice, and the Phase 8 operational-maturity updates for locale-aware document language, company security audit visibility, and platform-admin operational monitoring, abuse-policy controls, and legal-hold management
- `apps/api` now includes the Phase 1 registration, auth, platform-admin policy, inactive-company lifecycle baseline, inactivity notification wiring, pending-deletion warning flow, automated company deletion, the completed Phase 2 company-configuration endpoints and persistence model, the Phase 3 shared booking infrastructure for customer contacts, bookings, booking audit events, booking notification triggers, and public booking intake, the Phase 4 appointment module for tenant-scoped services, providers, provider availability, slot generation, appointment booking, manual confirmation, and provider self-schedule reads, the Phase 5 group-classes module for tenant-scoped class types, instructors, scheduled sessions, capacity-safe public booking, waitlisting, and instructor schedule reads, the Phase 6 restaurant module for dining areas, tables, combinable table rules, service periods, table-assigned public reservations, reservation outcome management, and staff floorbook reads, the Phase 7 widget module for allowed-origin bootstrap, short-lived widget tokens, tenant-scoped widget usage monitoring, and embedded booking reuse across shared, appointment, class, and restaurant flows, and the Phase 8 operational-maturity baseline for persisted abuse-policy configuration, tenant-safe security-audit queries, legal-hold aware deletion workflows, operational monitoring summaries, and locale-aware document language handling

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
- reset password page: `http://localhost:3000/en/reset-password?token=...`
- public booking page: `http://localhost:3000/en/book/{company-slug}`
- widget loader endpoint: `http://localhost:3000/widget-loader.js`
- widget iframe route: `http://localhost:3000/widget/{company-slug}?token=...&locale=en`
- company backoffice route: `http://localhost:3000/app/company/{slug}`
- platform admin route: `http://localhost:3000/platform-admin`
- company backoffice placeholder: `http://localhost:3000/app/company/<company-slug>`
- platform admin placeholder: `http://localhost:3000/platform-admin`
- API health: `http://localhost:8080/api/public/ping`
- registration endpoint: `POST http://localhost:8080/api/public/companies/registration`
- activation endpoint: `POST http://localhost:8080/api/public/companies/activation/confirm`
- resend activation endpoint: `POST http://localhost:8080/api/public/companies/activation/resend`
- login endpoint: `POST http://localhost:8080/api/public/auth/login`
- forgot password endpoint: `POST http://localhost:8080/api/public/auth/forgot-password`
- reset password endpoint: `POST http://localhost:8080/api/public/auth/reset-password`
- company backoffice endpoint: `GET http://localhost:8080/api/app/company/{slug}/backoffice`
- company profile endpoint: `PUT http://localhost:8080/api/app/company/{slug}/profile`
- company branding endpoint: `PUT http://localhost:8080/api/app/company/{slug}/branding`
- company localization endpoint: `PUT http://localhost:8080/api/app/company/{slug}/localization`
- company business hours endpoint: `PUT http://localhost:8080/api/app/company/{slug}/business-hours`
- company closure dates endpoint: `PUT http://localhost:8080/api/app/company/{slug}/closure-dates`
- company notification preferences endpoint: `PUT http://localhost:8080/api/app/company/{slug}/notification-preferences`
- booking notification triggers endpoint: `PUT http://localhost:8080/api/app/company/{slug}/booking-notification-triggers`
- customer contacts endpoint: `GET|POST|PUT http://localhost:8080/api/app/company/{slug}/customer-contacts`
- bookings endpoint: `GET http://localhost:8080/api/app/company/{slug}/bookings`
- booking status endpoint: `PUT http://localhost:8080/api/app/company/{slug}/bookings/{bookingId}/status`
- booking audit endpoint: `GET http://localhost:8080/api/app/company/{slug}/booking-audit`
- public booking config endpoint: `GET http://localhost:8080/api/public/companies/{slug}/booking-intake-config`
- public booking intake endpoint: `POST http://localhost:8080/api/public/companies/{slug}/booking-intake`
- public class availability endpoint: `GET http://localhost:8080/api/public/companies/{slug}/classes/availability`
- public class booking endpoint: `POST http://localhost:8080/api/public/companies/{slug}/classes/book`
- public restaurant availability endpoint: `GET http://localhost:8080/api/public/companies/{slug}/restaurant/availability`
- public restaurant booking endpoint: `POST http://localhost:8080/api/public/companies/{slug}/restaurant/book`
- widget bootstrap endpoint: `GET http://localhost:8080/api/public/widget/{slug}/bootstrap`
- company staff list endpoint: `GET http://localhost:8080/api/app/company/{slug}/staff`
- company staff create endpoint: `POST http://localhost:8080/api/app/company/{slug}/staff`
- company staff update endpoint: `PUT http://localhost:8080/api/app/company/{slug}/staff/{membershipId}`
- company customer questions endpoint: `PUT http://localhost:8080/api/app/company/{slug}/customer-questions`
- company widget settings endpoint: `PUT http://localhost:8080/api/app/company/{slug}/widget-settings`
- company widget usage endpoint: `GET http://localhost:8080/api/app/company/{slug}/widget-usage`
- company security audit endpoint: `GET http://localhost:8080/api/app/company/{slug}/backoffice` and tenant security summary in the same payload
- class types endpoint: `GET|POST|PUT http://localhost:8080/api/app/company/{slug}/class-types`
- class instructors endpoint: `GET|POST|PUT http://localhost:8080/api/app/company/{slug}/class-instructors`
- class sessions endpoint: `GET|POST|PUT http://localhost:8080/api/app/company/{slug}/class-sessions`
- class bookings endpoint: `GET http://localhost:8080/api/app/company/{slug}/class-bookings`
- class booking outcome endpoint: `PUT http://localhost:8080/api/app/company/{slug}/class-bookings/{classBookingId}/status`
- instructor class schedule endpoint: `GET http://localhost:8080/api/app/company/{slug}/class-instructors/me/sessions`
- dining areas endpoint: `GET|POST|PUT http://localhost:8080/api/app/company/{slug}/dining-areas`
- restaurant tables endpoint: `GET|POST|PUT http://localhost:8080/api/app/company/{slug}/restaurant-tables`
- restaurant table combinations endpoint: `GET|PUT http://localhost:8080/api/app/company/{slug}/restaurant-table-combinations`
- restaurant service periods endpoint: `GET|POST|PUT http://localhost:8080/api/app/company/{slug}/restaurant-service-periods`
- restaurant reservations endpoint: `GET http://localhost:8080/api/app/company/{slug}/restaurant-reservations`
- restaurant reservation outcome endpoint: `PUT http://localhost:8080/api/app/company/{slug}/restaurant-reservations/{reservationId}/status`
- restaurant floorbook endpoint: `GET http://localhost:8080/api/app/company/{slug}/restaurant-floorbook`
- platform admin companies endpoint: `GET http://localhost:8080/api/platform-admin/companies`
- inactivity policy endpoints: `GET` and `PUT http://localhost:8080/api/platform-admin/inactivity-policy`
- abuse policy endpoints: `GET` and `PUT http://localhost:8080/api/platform-admin/abuse-policy`
- platform operations summary endpoint: `GET http://localhost:8080/api/platform-admin/operations-summary`
- platform company retention endpoint: `PUT http://localhost:8080/api/platform-admin/companies/{slug}/retention`
- session endpoint: `GET http://localhost:8080/api/auth/session`
- CSRF token endpoint: `GET http://localhost:8080/api/auth/csrf-token`
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
- [docs/phase-2-use-cases.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\phase-2-use-cases.md)
- [docs/phase-2-test-scenarios.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\phase-2-test-scenarios.md)
- [docs/phase-3-use-cases.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\phase-3-use-cases.md)
- [docs/phase-3-test-scenarios.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\phase-3-test-scenarios.md)
- [docs/phase-4-use-cases.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\phase-4-use-cases.md)
- [docs/phase-4-test-scenarios.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\phase-4-test-scenarios.md)
- [docs/phase-5-use-cases.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\phase-5-use-cases.md)
- [docs/phase-5-test-scenarios.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\phase-5-test-scenarios.md)
- [docs/phase-6-use-cases.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\phase-6-use-cases.md)
- [docs/phase-6-test-scenarios.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\phase-6-test-scenarios.md)
- [docs/phase-7-use-cases.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\phase-7-use-cases.md)
- [docs/phase-7-test-scenarios.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\phase-7-test-scenarios.md)
- [docs/phase-8-use-cases.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\phase-8-use-cases.md)
- [docs/phase-8-test-scenarios.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\phase-8-test-scenarios.md)

## Working Rule

Documentation is part of the codebase.

Any code change that affects architecture, domain behavior, stack decisions, delivery scope, testing policy, or operational assumptions must include updates to the relevant documentation files in the same change.

Code is not considered complete if the necessary documentation updates are missing.
