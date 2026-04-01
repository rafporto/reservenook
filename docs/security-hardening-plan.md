# Security Hardening Plan

## Purpose

This document defines the initial plan to test the already implemented use cases for security weaknesses and to fix the most relevant fragilities before further feature expansion.

The goal is not only to run ad hoc attack tests, but to create a repeatable hardening pass across the current product surface.

## Plan

### 1. Inventory Implemented Use Cases

Map each implemented use case to:

- entry points
- protected data
- actor permissions
- tenant boundaries
- email and token dependencies

### 2. Build a Threat Model

Review the current product against the main threat categories:

- authentication attacks
- session hijacking or fixation
- CSRF
- IDOR and tenant-isolation bypass
- privilege escalation
- account enumeration
- brute force and public form abuse
- activation and password-reset token abuse
- injection and malformed input
- lifecycle-job misuse or unsafe state transitions

### 3. Review Backend Security Controls

Inspect backend code and configuration for:

- authentication and session handling
- role-aware and tenant-aware authorization
- token creation, expiry, replay prevention, and invalidation
- password hashing and reset behavior
- validation and error-message leakage
- public endpoint abuse protection
- admin-only endpoint exposure

### 4. Review Frontend and Public-Flow Risks

Inspect frontend behavior for:

- unsafe redirects
- route and language-handling inconsistencies
- sensitive data leakage in client-visible errors
- protections enforced only in UI but not in backend
- indexing of token-based or private flows
- abuse opportunities in forms and fetch flows

### 5. Create a Security Test Matrix Per Use Case

For each implemented use case, define tests for:

- normal path
- unauthorized path
- cross-tenant abuse path
- malformed-input path
- repeated-request or brute-force path
- token replay or expired-token path
- privilege-escalation attempt

### 6. Execute Security Checks in Priority Order

Start with the highest-risk implemented flows:

- login and logout
- activation and resend activation
- forgot password and reset password
- company backoffice access
- platform admin access
- inactivity-policy update
- company profile update
- scheduled lifecycle flows

### 7. Triage Findings

Classify each finding by:

- severity
- exploitability
- blast radius
- ease of fix

### 8. Fix Highest-Risk Fragilities First

Apply fixes in this order:

- critical vulnerabilities
- high-risk authorization or token issues
- medium-risk hardening gaps
- lower-risk operational improvements

### 9. Add Regression Coverage

Every confirmed fragility should result in:

- backend unit or integration tests
- frontend tests where relevant
- documentation updates for the security scenario or checklist

### 10. Rerun Verification and Report Residual Risk

After fixes:

- rerun the full CI baseline
- rerun targeted abuse tests
- produce a residual-risk summary
- document recommended next hardening steps

## Recommended First Focus

The first hardening pass should prioritize:

- auth and session flows
- tenant isolation
- platform-admin authorization
- token-based flows
- public endpoint abuse protection

## Security Test Scope

The security review should explicitly describe which tests are being executed, not only the areas being reviewed.

The testing baseline should use OWASP-aligned categories, especially:

- authentication
- session management
- MFA where applicable in the future
- OAuth2 or federated login where applicable in the future
- ASVS-style verification for access control, recovery, logging, and transport protections

For the current system, organize testing into these groups:

- identity and login behavior
- session handling
- recovery and token flows
- authorization and tenant isolation
- API authentication and protected endpoints
- abuse controls and rate limiting
- edge-case input handling
- logging, monitoring, and auditability
- transport and browser security headers

## Concrete Test Checklist

### 1. Login and Account Enumeration

Test whether the system leaks whether an account exists.

Scenarios:

- wrong email and wrong password
- real email and wrong password
- disabled user
- inactive company user
- unverified user
- platform admin with wrong password

Verify:

- response message consistency
- response status-code consistency where policy requires neutral behavior
- response-time consistency where practical
- no different UI behavior that leaks account state more than intended

Current applicable use cases:

- UC-04 Login
- UC-11 Resolve Post-Login Destination

### 2. Rate Limiting and Anti-Automation

Test whether repeated requests are slowed, blocked, or at least observable.

Scenarios:

- repeated failed login attempts from one IP
- repeated failed login attempts against one account from multiple clients
- repeated login attempts across many usernames
- password reset request spam
- resend activation request spam
- activation token brute-force style attempts

Verify:

- rate limits, lockouts, or progressive delays if implemented
- whether missing protections represent a real gap
- whether abusive events are logged for review

Current applicable use cases:

- UC-03 Resend Activation Email
- UC-04 Login
- UC-06 Request Password Reset

### 3. Password Policy and Storage

Test whether weak or dangerous passwords are accepted and whether storage is safe.

Scenarios:

- very short password
- common or trivial password
- password same as email
- password with leading or trailing spaces
- very long password
- Unicode password handling
- password reset to a previously used value if policy exists later

Verify:

- frontend and backend validation are consistent
- stored password uses secure hashing rather than reversible storage
- password verification behavior is safe and stable

Current applicable use cases:

- UC-01 Register Company
- UC-07 Reset Password

### 4. Session and Token Handling

Test the authenticated session as if it were under attack.

Scenarios:

- login creates a new authenticated session
- logout invalidates the current session
- old session reuse after logout
- old session reuse after password reset if session invalidation policy applies
- cookie behavior on protected routes
- session behavior after account state changes such as company inactivity or disablement

Verify:

- session rotation or regeneration behavior after login
- logout invalidation
- no reuse of stale authenticated session identifiers
- secure cookie settings where applicable
- idle and absolute timeout policy, if implemented

Current applicable use cases:

- UC-04 Login
- UC-05 Logout
- UC-07 Reset Password
- UC-13 through UC-16 company lifecycle effects on access

### 5. Authorization After Authentication

Authentication and authorization must be tested separately.

Scenarios:

- company admin tries to access another tenant
- company admin tries to access platform-admin endpoints
- platform admin tries to access company-only flows where that should be denied
- lower-privileged company user attempts future admin-only configuration actions
- stale session replay after role or company-state change

Verify:

- tenant isolation
- role isolation
- no insecure direct object reference behavior
- no privilege escalation through crafted routes or identifiers

Current applicable use cases:

- UC-08 Access Company Backoffice Within Tenant Scope
- UC-09 Platform Admin Views Companies
- UC-12 Platform Admin Configures Inactivity Policy
- UC-17 View Company Configuration Dashboard
- UC-18 Update Company Profile

### 6. Password Reset and Account Recovery

Recovery flows should be tested as heavily as login.

Scenarios:

- reset token guessed or malformed
- reset token reused
- expired reset token
- old reset link after password change
- forgot-password request for unknown account
- forgot-password request for ineligible account
- repeated reset requests

Verify:

- reset links are single-use
- reset links expire correctly
- reset request does not disclose whether an account exists
- reset invalidates old token paths safely
- reset abuse is not trivial

Current applicable use cases:

- UC-06 Request Password Reset
- UC-07 Reset Password

### 7. Activation and Email Verification Hardening

Activation flows are also authentication-adjacent attack surfaces.

Scenarios:

- activation token reuse
- activation token expiration
- invalid activation token
- resend activation for unknown email
- resend activation for already active account
- activation path enumeration via UI differences

Verify:

- activation links are single-use or safely invalidated
- expired links offer recovery without leaking data
- resend flow remains neutral when appropriate

Current applicable use cases:

- UC-02 Activate Company Account
- UC-03 Resend Activation Email

### 8. API Authentication

Protected APIs must enforce authentication independently of UI.

Scenarios:

- missing session or token on protected endpoint
- invalid session on protected endpoint
- expired or revoked auth state if applicable
- authenticated request to wrong tenant identifier
- protected API over insecure assumptions in CORS or cookies

Verify:

- protected endpoints reject unauthenticated requests
- every protected API enforces the same backend rules as the UI
- authenticated browser flows do not expose APIs too broadly

Current applicable endpoints:

- login, logout, session
- company backoffice routes
- platform-admin routes
- profile update route

### 9. Edge-Case Input Tests

These often uncover logic flaws rather than direct injection.

Scenarios:

- email case sensitivity and normalization differences
- leading and trailing spaces in login and registration fields
- duplicate identities caused by normalization mismatch
- null, empty, or oversized values
- Unicode input in names, passwords, and descriptions
- concurrent reset requests or concurrent profile updates

Verify:

- frontend and backend validation align
- normalization rules are consistent
- concurrency does not create broken state

Current applicable use cases:

- UC-01 Register Company
- UC-04 Login
- UC-06 Request Password Reset
- UC-07 Reset Password
- UC-18 Update Company Profile

### 10. Logging, Alerting, and Auditability

A secure system should leave evidence of sensitive activity.

Verify whether these events are logged or otherwise auditable:

- failed login attempts
- rate-limit or abuse events
- password reset requests
- password changes
- activation and resend-activation events
- company profile updates
- platform-admin inactivity-policy updates
- lifecycle warnings and deletion actions

Current applicable use cases:

- UC-02 through UC-18, especially admin and lifecycle actions

### 11. Transport and Browser Security

Even correct auth logic is weaker without transport protections.

Verify:

- HTTPS-only deployment assumptions
- no mixed content on auth pages
- secure cookie use
- HttpOnly cookie behavior
- SameSite policy
- HSTS where applicable in deployed environments
- CSP and frame protections for login and public entry pages

This area should be reviewed in code, deployment config, and browser behavior together.

## Use-Case-Oriented Security Matrix

For each implemented use case, record:

- feature
- actor
- expected outcome
- observed outcome
- attack type or risk
- severity
- fix required

Suggested structure:

- Feature: login, reset password, activate account, company backoffice, platform admin, company profile
- Role: anonymous, company admin, platform admin, disabled user, inactive-company user
- Expected: allow, deny, neutral response, redirect, revoke, log
- Observed: actual behavior during test
- Risk: enumeration, token replay, privilege escalation, CSRF, IDOR, abuse, session reuse

## High-Value First Test Pass

If the review must start small, begin with:

- login error consistency
- rate limiting on login and reset flows
- session invalidation on logout and password reset
- reset-token expiry and one-time use
- activation-token expiry and one-time use
- company-admin to platform-admin authorization bypass attempts
- cross-tenant access attempts on company routes
- company profile update restricted to the authenticated tenant

## Expected Output of the Hardening Pass

At the end of the work, produce:

- the executed test matrix
- confirmed findings
- fixes applied
- regression tests added
- residual risks not yet addressed
- recommended next security improvements

## Implemented Baseline

The first hardening pass is now implemented with automated regression coverage.

Completed controls:

- public auth abuse throttling for login, forgot-password, and resend-activation requests
- generic login failure handling to reduce account-state enumeration
- CSRF protection for authenticated state-changing endpoints
- explicit authenticated CSRF token endpoint for the web client
- tighter session-cookie defaults with `HttpOnly` and `SameSite=Lax`
- password-reset-driven session revocation through credential-version checks on authenticated requests
- browser-facing defensive headers on API and web responses

Current regression coverage includes:

- repeated failed login attempts are blocked with `429 Too Many Requests`
- repeated forgot-password requests are blocked with `429 Too Many Requests`
- repeated resend-activation requests are blocked with `429 Too Many Requests`
- activation-blocked login attempts return the same generic invalid-credentials response as other login failures
- authenticated logout requires a valid CSRF token
- company profile updates require a valid CSRF token
- platform-admin inactivity-policy updates require a valid CSRF token
- password reset revokes authenticated sessions created before the password change
- old passwords fail after reset while the new password remains valid
- public auth responses emit defensive frame, content-type, referrer, and permissions headers
- Next.js route config emits the same baseline defensive browser headers for all web pages

Remaining high-priority items for the next pass:

- invalidate active sessions after other high-risk account changes beyond password reset
- broaden abuse controls beyond per-email or per-client combinations
- add security logging and audit coverage for abuse events
- review browser security headers such as CSP and frame protections
- extend hardening tests across the remaining Phase 2 configuration surfaces as they are implemented
