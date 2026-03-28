# Phase 1 Use Cases

## Purpose

This document defines the use cases for Phase 1: Core Tenant and Identity Foundation.

Phase 1 exists to establish:

- company registration
- company activation lifecycle
- login
- password reset
- tenant-aware access control
- initial platform admin visibility
- inactive-company lifecycle handling

This phase should validate the tenant model and identity model before booking logic begins.

## Phase 1 Scope

Included:

- public company registration
- registration confirmation
- email activation
- login and logout
- forgot password
- password reset
- tenant-aware company admin access
- platform admin access to company list
- platform-admin inactivity policy configuration
- inactive-company warning notifications
- scheduled deletion of inactive companies
- public language selection and language persistence across public pages

Excluded:

- booking flows
- company profile editing beyond what is needed for the initial slice
- billing provider integration
- advanced staff management
- provider or instructor invitations
- widget behavior

## Actors

- Visitor
- Company Admin
- Platform Admin
- System
- Scheduled Job Runner

## UC-01 Register Company

### Goal

A visitor creates a new company account and the initial company admin user.

### Primary Actor

Visitor

### Preconditions

- the visitor is not authenticated
- the chosen company slug is not already used
- the email address is not already used in a conflicting way

### Main Flow

1. the visitor opens the public website or a localized public route
2. the visitor navigates from the public website to the registration page
3. the selected public language is preserved
4. the visitor enters company name, business type, slug, email, password, plan selection, and default language or locale
5. the system validates the submitted data
6. the system creates the company in a non-active state
7. the system creates the initial user as company admin in a non-active state
8. the system creates the company subscription or trial period
9. the system creates an email verification or activation token
10. the system sends an activation email
11. the system shows a confirmation message

### Alternate Flows

- slug already exists: registration is rejected with a clear validation error
- email already exists: registration is rejected or routed to a safe recovery path
- activation email cannot be sent: registration is not silently accepted; the failure must be visible and recoverable

### Postconditions

- company exists
- initial company admin exists
- tenant linkage exists
- activation token exists
- company cannot yet use protected tenant features until activation is completed

## UC-02 Activate Company Account

### Goal

The initial company admin activates the company account using the email link.

### Primary Actor

Visitor

### Preconditions

- a valid activation token exists
- the company is still pending activation

### Main Flow

1. the user opens the activation link from email
2. the system validates the token
3. the system marks the user email as verified
4. the system activates the company if the registration state is valid
5. the system activates the initial company admin account
6. the system invalidates the token
7. the system redirects the user to login or a post-activation confirmation page

### Alternate Flows

- token expired: user is shown a recovery path to request a new activation email
- token invalid: activation is rejected safely
- account already active: system shows idempotent success or a safe redirect

### Postconditions

- company is active
- initial company admin is active
- verification token is no longer usable

## UC-03 Resend Activation Email

### Goal

A pending user requests a new activation email.

### Primary Actor

Visitor

### Preconditions

- account exists in pending or non-verified state

### Main Flow

1. the user enters the registration email on a resend activation page
2. the system validates whether a resend is allowed
3. the system creates or rotates an activation token
4. the system sends a new activation email
5. the system shows a neutral confirmation response

### Alternate Flows

- unknown email: system still returns a neutral success-style response
- resend rate limit reached: system blocks abuse and returns a safe message

### Postconditions

- a valid activation path exists without leaking whether an account exists

## UC-04 Login

### Goal

A verified user logs into the platform.

### Primary Actor

Company Admin or Platform Admin

### Preconditions

- user account exists
- email is verified where required
- account is active

### Main Flow

1. the user opens the login page
2. the user enters credentials
3. the system validates the credentials
4. the system creates an authenticated session
5. the system resolves the user role and tenant context
6. the system redirects the user to the correct area

Redirect rules:

- platform admin goes to platform admin area
- company admin goes to the company backoffice for the correct tenant

### Alternate Flows

- wrong credentials: login fails with a generic error
- unverified user: access is denied and the user is guided to activation recovery
- inactive company: login may succeed only to a restricted state if product policy allows it, otherwise it is denied

### Postconditions

- authenticated session exists
- tenant-aware access rules can now be enforced

## UC-05 Logout

### Goal

An authenticated user ends the current session.

### Primary Actor

Authenticated User

### Main Flow

1. the user chooses logout
2. the system invalidates the current session
3. the system redirects the user to a public page or login page

### Postconditions

- the session is no longer valid

## UC-06 Request Password Reset

### Goal

A user requests a password reset email.

### Primary Actor

Visitor

### Preconditions

- the user knows the account email

### Main Flow

1. the user opens forgot password
2. the user submits the email address
3. the system creates a password reset token if the account is eligible
4. the system sends a password reset email
5. the system returns a neutral confirmation response

### Alternate Flows

- unknown email: return the same neutral response
- account not eligible: return the same neutral response
- rate limit exceeded: block abuse without account disclosure

### Postconditions

- password reset path exists without leaking account existence

## UC-07 Reset Password

### Goal

A user sets a new password using a valid reset token.

### Primary Actor

Visitor

### Preconditions

- a valid password reset token exists

### Main Flow

1. the user opens the reset link
2. the system validates the token
3. the user enters a new password
4. the system validates password policy
5. the system updates the stored password securely
6. the system invalidates the reset token
7. the system optionally invalidates old sessions
8. the system redirects the user to login

### Alternate Flows

- invalid or expired token: reset is rejected and the user is guided back to forgot password
- weak password: reset is rejected with validation feedback

### Postconditions

- new password is active
- old token is no longer usable

## UC-08 Access Company Backoffice Within Tenant Scope

### Goal

A company admin accesses only the company data that belongs to that tenant.

### Primary Actor

Company Admin

### Preconditions

- the user is authenticated
- the user has a company membership with company admin role

### Main Flow

1. the user opens a protected company route
2. the system resolves the authenticated identity
3. the system resolves the tenant membership
4. the system authorizes access for the requested tenant scope
5. the system returns the requested backoffice page or data

### Alternate Flows

- no membership for tenant: access is denied
- user attempts to access another tenant: access is denied
- role insufficient: access is denied

### Postconditions

- only tenant-owned data for the authorized company is visible

## UC-09 Platform Admin Views Companies

### Goal

A platform admin views the registered companies and their high-level status.

### Primary Actor

Platform Admin

### Preconditions

- authenticated platform admin account exists

### Main Flow

1. the platform admin opens the dedicated admin URL
2. the system verifies platform-level authorization
3. the system returns the company list with read-only visibility

The list should include at least:

- company name
- business type
- activation status
- trial or paid status
- expiration information

### Alternate Flows

- non-platform user attempts access: access is denied

### Postconditions

- platform-level oversight exists without entering tenant-owned operational flows

## UC-10 Select Public Language And Preserve It Across Navigation

### Goal

A public user changes the site language and continues navigating in that same language.

### Primary Actor

Visitor

### Preconditions

- the user is on a public page

### Main Flow

1. the user opens the public site
2. the page is shown in English by default if no language is selected in the URL
3. the user uses the language selector shown in the top area
4. the system changes the current route to the selected language version
5. the user navigates to another public page
6. the system keeps the selected language in the URL and in rendered content

### Alternate Flows

- unsupported language in URL: system falls back safely according to routing policy
- public deep link already contains language: page opens directly in that language

### Postconditions

- the public navigation remains in the selected language until the user changes it

## UC-11 Resolve Post-Login Destination

### Goal

The system routes authenticated users to the correct landing area after login.

### Primary Actor

System

### Preconditions

- user is successfully authenticated

### Main Flow

1. the system inspects the user role assignments
2. the system determines whether the user is platform-level or company-level
3. the system redirects to the correct start page

### Postconditions

- authenticated users land in an area consistent with their role and tenant context

## UC-12 Platform Admin Configures Inactivity Policy

### Goal

The platform admin defines how long an inactive company may remain before deletion and how long before deletion the warning email must be sent.

### Primary Actor

Platform Admin

### Preconditions

- authenticated platform admin account exists

### Main Flow

1. the platform admin opens the platform policy area
2. the system shows the current inactivity policy
3. the platform admin sets the inactivity threshold
4. the platform admin sets the warning lead time before deletion
5. the system validates the values
6. the system stores the updated policy

### Alternate Flows

- invalid configuration such as warning lead time greater than inactivity threshold: the system rejects the update
- non-platform user attempts access: access is denied

### Postconditions

- the system has an active inactivity policy used for future lifecycle checks

## UC-13 Mark Company As Inactive

### Goal

The system identifies that a company has been inactive for the configured period.

### Primary Actor

Scheduled Job Runner

### Preconditions

- an inactivity policy exists
- the company meets the inactivity criteria defined by the product

### Main Flow

1. a scheduled process evaluates company activity state
2. the system compares the last relevant activity timestamp with the configured inactivity threshold
3. the system marks the company as inactive or pending deletion according to policy
4. the system records the relevant timestamps for warning and deletion handling

### Alternate Flows

- company becomes active again before state transition is finalized: inactivity transition is skipped or reversed

### Postconditions

- the company lifecycle state reflects inactivity

## UC-14 Notify Company About Inactivity

### Goal

The system informs the company that it is inactive.

### Primary Actor

System

### Preconditions

- the company has entered the inactive lifecycle state
- a reachable company admin contact exists

### Main Flow

1. the system identifies the target company admin recipients
2. the system sends an inactivity notification email
3. the system records the notification event

### Alternate Flows

- email delivery fails: the failure is recorded and retried according to policy

### Postconditions

- the company has been informed that the account is inactive

## UC-15 Warn Company About Pending Data Deletion

### Goal

The system notifies the company that its data will be deleted after the configured warning period.

### Primary Actor

Scheduled Job Runner

### Preconditions

- the company is inactive
- the configured warning window has been reached
- the company has not already been deleted

### Main Flow

1. the system determines that the deletion warning threshold has been reached
2. the system sends a warning email stating that company data will be deleted
3. the system records that the warning has been sent

### Alternate Flows

- company becomes active again before deletion: pending deletion is canceled according to policy
- warning was already sent: duplicate warning is prevented unless policy explicitly allows reminders

### Postconditions

- the company has been warned that deletion is approaching

## UC-16 Delete Inactive Company After Retention Period

### Goal

The system deletes the company and its related tenant-owned data after the configured inactive period has elapsed.

### Primary Actor

Scheduled Job Runner

### Preconditions

- the company is marked for deletion
- the configured deletion date has been reached
- no policy exception is active

### Main Flow

1. the scheduled process selects companies whose deletion date has been reached
2. the system verifies that deletion conditions are still valid
3. the system deletes or anonymizes tenant-owned data according to product and compliance policy
4. the system removes or deactivates the company record according to final deletion design
5. the system records the deletion event for auditability

### Alternate Flows

- company reactivated before deletion: deletion is canceled
- deletion cannot complete fully: the failure is recorded and the operation is retried safely

### Postconditions

- the inactive company data is no longer available in the platform according to policy

## Cross-Cutting Rules for Phase 1

- all tenant-owned records must be linked to a company
- password handling must be secure
- activation and reset tokens must be one-time or safely invalidated
- public account recovery flows must not leak sensitive account existence information
- authorization must be role-aware and tenant-aware
- audit-relevant account lifecycle actions should be designed so they can be logged cleanly
- inactivity and deletion policy must be explicit and configurable
- deletion warnings must be sent before irreversible company deletion

## Recommended First Implementation Order

1. company and user model foundations
2. role model and tenant membership model
3. public language routing baseline
4. registration flow
5. activation flow
6. login and session handling
7. password reset flow
8. company-admin protected area
9. platform-admin company list
10. inactivity policy configuration
11. inactivity detection and warning flow
12. scheduled company deletion flow

## Definition of Phase 1 Completion

Phase 1 is complete when:

- a company can register
- the company can activate through email
- the initial admin can log in
- password reset works
- company-admin access is tenant-safe
- platform admin can view companies
- public pages support language selection with persistence across navigation
- platform admin can configure inactivity and deletion policy
- inactive companies receive lifecycle warnings
- inactive companies can be deleted automatically according to policy
- all flows are covered by automated tests according to the project testing strategy
