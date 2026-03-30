# Phase 1 Test Scenarios

## Purpose

This document defines the test scenarios for the Phase 1 use cases.

Its goal is to turn the Phase 1 use-case specification into an implementation-ready testing baseline for backend TDD, frontend behavior tests, and a small number of end-to-end checks.

## Test Layers

Use the following test layers by default:

- backend unit tests for domain rules, policy logic, token handling, lifecycle transitions, and authorization decisions
- backend integration tests for persistence, security, HTTP endpoints, and tenant scoping
- frontend component and integration tests for forms, validation, error states, and redirects
- end-to-end tests only for the most critical cross-system journeys

## UC-01 Register Company

### Backend Unit

- registration succeeds with valid data
- registration rejects duplicate slug
- registration rejects invalid business type
- registration rejects invalid email
- registration rejects weak password
- registration creates company in pending or inactive state
- registration creates initial company admin role assignment
- registration creates trial or paid activation period correctly
- registration prepares activation token

### Backend Integration

- registration endpoint persists company, user, membership, and token
- duplicate slug returns validation error
- duplicate email follows defined conflict behavior
- registration failure does not leave partial tenant records
- activation email dispatch is triggered

### Frontend

- registration form shows required field validation
- registration form submits valid data successfully
- duplicate slug error is rendered clearly
- invalid email and weak password feedback are shown
- success state instructs the user to check email

### End-to-End

- visitor registers a company and sees confirmation

## UC-02 Activate Company Account

### Backend Unit

- valid token activates company and admin user
- expired token is rejected
- invalid token is rejected
- already-used token cannot be reused

### Backend Integration

- activation endpoint updates company and user state
- activation invalidates token after success
- already-active account is handled safely

### Frontend

- activation success page is displayed for valid token
- expired token page offers recovery path
- invalid token page does not expose sensitive details

### End-to-End

- user activates account from email token and reaches login-ready state

## UC-03 Resend Activation Email

### Backend Unit

- resend creates a fresh valid activation token
- resend is rate limited correctly
- resend returns neutral behavior for unknown email

### Backend Integration

- resend endpoint dispatches activation email for eligible account
- resend endpoint returns neutral response for unknown or ineligible account

### Frontend

- resend form submits successfully
- neutral confirmation is shown regardless of account visibility

## UC-04 Login

### Backend Unit

- valid credentials create authenticated session
- wrong password is rejected
- unverified account is rejected
- inactive account behavior follows product policy
- redirect target resolution follows role and tenant context

### Backend Integration

- login endpoint authenticates valid user
- protected session is created
- company admin resolves to company scope
- platform admin resolves to platform scope

### Frontend

- login form validates required fields
- wrong credentials show generic error
- successful login redirects correctly
- unverified account shows activation recovery guidance

### End-to-End

- company admin logs in and lands in company backoffice
- platform admin logs in and lands in platform admin area

## UC-05 Logout

### Backend Integration

- logout invalidates current session
- protected endpoint access fails after logout

### Frontend

- logout action redirects to public or login page

## UC-06 Request Password Reset

### Backend Unit

- valid account creates reset token
- unknown account returns neutral response
- request is rate limited correctly

### Backend Integration

- forgot-password endpoint dispatches reset email
- response does not reveal account existence

### Frontend

- forgot-password form validates email input
- neutral success message is displayed

## UC-07 Reset Password

### Backend Unit

- valid token allows password change
- expired token is rejected
- invalid token is rejected
- weak password is rejected
- old token cannot be reused
- existing sessions are invalidated if policy requires it

### Backend Integration

- reset endpoint updates stored password
- reset endpoint invalidates token
- user can log in with new password after reset

### Frontend

- reset form validates password rules
- reset form blocks submission when token is missing
- invalid or expired token state is shown correctly
- successful reset redirects to login

### End-to-End

- user completes password reset and logs in with new password

## UC-08 Access Company Backoffice Within Tenant Scope

### Backend Unit

- membership grants access to the correct tenant
- missing membership denies access
- wrong tenant access is denied
- insufficient role is denied

### Backend Integration

- protected company endpoints return only tenant-owned data
- cross-tenant requests are denied

### Frontend

- unauthorized tenant route access redirects or shows access denied
- company-scoped views only load tenant-safe data

### End-to-End

- company admin cannot access another company scope

## UC-09 Platform Admin Views Companies

### Backend Unit

- platform role grants access to company listing
- non-platform role is denied

### Backend Integration

- company list endpoint returns required summary fields
- non-platform access receives forbidden response

### Frontend

- company list renders activation and plan status
- access denied behavior is handled for non-platform users

## UC-10 Select Public Language And Preserve It Across Navigation

### Frontend

- public root page renders in English by default
- language selector is visible in the top area of public pages
- changing language updates the route to the selected language
- navigating from public home to registration keeps the selected language
- navigating between other public pages keeps the selected language
- unsupported language route falls back safely

### End-to-End

- visitor changes language on the public site and reaches registration in the same language

## UC-11 Resolve Post-Login Destination

### Backend Unit

- platform admin redirect target is platform admin area
- company admin redirect target is company backoffice
- unsupported role state fails safely

### Frontend

- post-login routing behavior matches role result

## UC-12 Platform Admin Configures Inactivity Policy

### Backend Unit

- valid inactivity threshold is accepted
- valid warning lead time is accepted
- warning lead time greater than threshold is rejected
- policy update stores current values correctly

### Backend Integration

- policy endpoint is accessible only to platform admin
- persisted policy is returned correctly on subsequent reads

### Frontend

- policy form validates numeric or date-based rules correctly
- invalid combinations show explicit validation messages
- successful save shows updated values

## UC-13 Mark Company As Inactive

### Backend Unit

- company becomes inactive after configured threshold
- active company is not marked inactive prematurely
- recently reactivated company is not incorrectly marked inactive

### Backend Integration

- scheduled inactivity job updates company state correctly
- inactivity timestamps are recorded

## UC-14 Notify Company About Inactivity

### Backend Unit

- inactive company notification is generated for correct recipients
- duplicate notification logic follows policy

### Backend Integration

- notification dispatch occurs when inactivity state is entered
- delivery failure is recorded for retry or review

## UC-15 Warn Company About Pending Data Deletion

### Backend Unit

- warning trigger fires at configured lead time
- warning is not sent too early
- duplicate warning is prevented by default
- reactivated company does not receive stale deletion warning

### Backend Integration

- scheduled warning job dispatches warning email
- warning event is persisted

## UC-16 Delete Inactive Company After Retention Period

### Backend Unit

- deletion candidate is selected only after configured retention period
- reactivated company is removed from deletion candidates
- failed deletion is recorded for retry

### Backend Integration

- deletion job removes or anonymizes tenant-owned data according to policy
- deleted company is no longer accessible
- deletion event is auditable

### End-to-End

- not required in the first implementation pass unless lifecycle jobs are exposed through a controllable admin or test hook

## Cross-Cutting Security and Tenant Tests

These scenarios should exist independently of individual use cases:

- password hashes are never stored as plain text
- activation and reset tokens expire correctly
- activation and reset tokens cannot be reused
- public endpoints validate malformed input safely
- tenant-owned queries always stay within tenant scope
- platform-only endpoints reject company users
- company-only endpoints reject platform-only context when inappropriate

## Minimum End-to-End Suite for Phase 1

The first Phase 1 end-to-end suite should stay small:

- company registration
- public language switching
- account activation
- login
- password reset
- company admin tenant-scoped access
- platform admin company list access

Lifecycle job scenarios for inactivity and deletion should initially rely on backend unit and integration tests unless there is a deterministic test harness for scheduled execution.
