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

## Documentation

Project decisions and planning are documented in:

- [docs/stack.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\stack.md)
- [docs/architecture.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\architecture.md)
- [docs/roadmap.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\roadmap.md)
- [docs/testing-strategy.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\testing-strategy.md)
- [docs/domain-model.md](C:\Users\rafael.portorodrigue\IdeaProjects\reservenook\docs\domain-model.md)

## Working Rule

Documentation is part of the codebase.

Any code change that affects architecture, domain behavior, stack decisions, delivery scope, testing policy, or operational assumptions must include updates to the relevant documentation files in the same change.

Code is not considered complete if the necessary documentation updates are missing.
