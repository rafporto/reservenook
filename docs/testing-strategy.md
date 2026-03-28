# Testing Strategy

## Purpose

This project treats testing as a core engineering requirement, not as optional cleanup after implementation.

Two explicit rules apply:

- backend development should follow TDD as the default approach
- frontend development must include automated tests for critical behavior

Documentation maintenance is also a project requirement:

- any code change that affects behavior, architecture, module boundaries, delivery assumptions, or testing expectations must update the relevant documentation files in the same change

## Testing Philosophy

The platform contains high-risk business logic in areas such as:

- tenant isolation
- booking constraints
- overlapping booking prevention
- capacity handling
- role-based authorization
- password and activation flows
- public form validation

These areas should not depend on manual verification alone.

The test strategy therefore emphasizes:

- fast feedback for domain logic
- realistic integration tests for persistence and security
- targeted frontend tests for user-critical behavior
- end-to-end verification of the most important journeys

## Backend Testing Policy

### Default Approach

Backend features should be developed using TDD whenever they involve:

- domain rules
- application services
- validation behavior
- authorization behavior
- scheduling logic
- availability logic

The intended cycle is:

1. write a failing test
2. implement the smallest change that makes it pass
3. refactor while keeping tests green

### Backend Test Types

#### Unit Tests

Use unit tests for:

- domain entities
- value objects
- booking policy rules
- overlap detection
- capacity calculations
- tenant-aware permission checks

These tests should be fast and should not require Spring context startup unless there is a real reason.

#### Integration Tests

Use integration tests for:

- repository behavior
- Flyway migrations
- transaction behavior
- security rules
- controller behavior
- tenant scoping at API level

These tests should use real infrastructure where practical.

#### Testcontainers

Use Testcontainers for:

- PostgreSQL-backed integration tests
- Redis-backed integration tests if Redis is introduced

The objective is to verify behavior against realistic dependencies, not mocks pretending to be infrastructure.

### Backend Tooling

Recommended backend test stack:

- JUnit 5
- Kotest
- MockK
- Spring Boot Test
- Testcontainers

## Frontend Testing Policy

### Core Rule

Frontend code should have automated tests where behavior matters.

This includes:

- booking flow progression
- form validation
- auth flows
- conditional rendering based on roles
- localization-sensitive display behavior
- mutation success and failure handling

### What Should Be Tested

Use automated tests for:

- form submission behavior
- required field enforcement
- dynamic step progression
- loading and error states
- business-rule-driven UI branching
- accessibility-critical interactions

### What Does Not Need Heavy Testing

Avoid over-investing in tests for:

- purely decorative components
- simple layout wrappers
- trivial MUI composition with no meaningful behavior

The goal is not test quantity. The goal is risk reduction.

### Frontend Test Types

#### Component and Integration Tests

Use these for:

- forms
- dialogs
- route-level flows
- booking step behavior
- localized rendering concerns

Recommended tooling:

- Vitest
- React Testing Library
- user-event

#### End-to-End Tests

Use end-to-end tests for the main user journeys:

- company registration
- login
- password reset
- tenant admin basic configuration
- public booking creation

Recommended tooling:

- Playwright

Keep the end-to-end suite focused and stable. It should cover critical journeys, not every screen permutation.

## Test Pyramid for This Project

The expected distribution is:

- many backend unit tests
- a healthy layer of backend integration tests
- targeted frontend component and integration tests
- a small but valuable set of end-to-end tests

This project should not rely primarily on end-to-end tests. Too much logic exists below the UI layer for that to be a good primary strategy.

## Definition of Done

A feature is not complete unless:

- backend logic is covered by appropriate tests
- frontend critical behavior is covered by tests when UI is involved
- integration tests exist where infrastructure or security behavior matters
- regressions can be detected automatically in CI
- affected documentation has been updated in the same change when implementation alters documented behavior or decisions

## CI Expectations

Continuous integration should eventually run at least:

- backend unit and integration tests
- frontend unit and integration tests
- selected end-to-end smoke tests
- linting and formatting checks

CI should fail on broken tests. Test failures should be treated as release blockers unless explicitly waived for a documented reason.
