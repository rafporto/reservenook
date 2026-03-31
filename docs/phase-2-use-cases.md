# Phase 2 Use Cases

## Purpose

This document defines the use cases for Phase 2: Shared Company Configuration.

Phase 2 exists to establish:

- editable company profile data
- company branding and localization settings
- operating-calendar foundations
- notification preferences
- staff user management
- reusable customer intake configuration
- widget settings baseline for later public booking flows

This phase should validate the company backoffice as a real operational surface before shared booking infrastructure begins.

## Phase 2 Scope

Included:

- company profile management
- branding settings
- default language and locale settings
- business hours
- closure dates
- notification preferences
- staff user management
- custom customer questions
- widget settings foundation

Excluded:

- public booking flow execution
- provider calendars
- appointment slot generation
- class scheduling
- restaurant table configuration
- billing-provider integration
- advanced per-staff permissions beyond the initial shared backoffice baseline

## Actors

- Company Admin
- Staff User
- Platform Admin
- System

## UC-17 View Company Configuration Dashboard

### Goal

A company admin opens the backoffice and views the shared company configuration areas.

### Primary Actor

Company Admin

### Preconditions

- the user is authenticated
- the user belongs to the company
- the user has permission to view company configuration

### Main Flow

1. the company admin opens the protected company backoffice
2. the system resolves the tenant context
3. the system returns the shared configuration dashboard
4. the system shows the available areas such as profile, branding, hours, staff, questions, and widget settings

### Alternate Flows

- user lacks company membership: access is denied
- user belongs to another tenant: access is denied

### Postconditions

- the company admin can navigate to shared configuration areas within tenant scope

## UC-18 Update Company Profile

### Goal

A company admin updates the core business profile shown across the platform.

### Primary Actor

Company Admin

### Preconditions

- the company exists
- the user has permission to manage company settings

### Main Flow

1. the company admin opens the company profile section
2. the system shows the current profile values
3. the company admin updates fields such as company name, business description, primary email, phone, and address details
4. the system validates the submitted data
5. the system stores the updated company profile
6. the system shows the updated values

### Alternate Flows

- invalid field values: the system rejects the update with field-level feedback
- another tenant identifier is targeted: the update is denied

### Postconditions

- the company profile is updated and remains tenant-scoped

## UC-19 Configure Company Branding

### Goal

A company admin configures the company branding baseline used by future public booking surfaces.

### Primary Actor

Company Admin

### Preconditions

- the user has permission to manage company settings

### Main Flow

1. the company admin opens the branding section
2. the system shows the current branding settings
3. the company admin updates brand fields such as display name, logo reference, accent color, and support contact details
4. the system validates formatting and allowed branding values
5. the system stores the branding configuration
6. the system shows a preview or confirmation state

### Alternate Flows

- unsupported branding format or invalid color values: validation error is shown
- asset upload or reference cannot be saved: the failure is shown clearly

### Postconditions

- branding settings exist for future company public pages and widgets

## UC-20 Configure Company Language And Locale Defaults

### Goal

A company admin defines the default language and locale used for company-facing and future customer-facing experiences.

### Primary Actor

Company Admin

### Preconditions

- the company exists
- supported languages and locales are defined by product policy

### Main Flow

1. the company admin opens the localization settings
2. the system shows the current default language and locale
3. the company admin selects new default values
4. the system validates that the selected combination is supported
5. the system stores the new defaults
6. the system uses the updated values for future tenant-owned flows where applicable

### Alternate Flows

- unsupported language or locale: update is rejected
- incompatible language and locale combination: update is rejected according to policy

### Postconditions

- the company has active default language and locale settings

## UC-21 Configure Business Hours

### Goal

A company admin defines the normal weekly operating hours for the company.

### Primary Actor

Company Admin

### Preconditions

- the user has permission to manage company settings
- timezone policy for the company is already defined or assumed

### Main Flow

1. the company admin opens the business hours section
2. the system shows the current weekly schedule
3. the company admin defines opening and closing windows per day
4. the system validates that each time range is coherent
5. the system stores the weekly hours
6. the system shows the updated schedule

### Alternate Flows

- overlapping or invalid time ranges: update is rejected with validation feedback
- day is marked closed: the system stores no active time range for that day

### Postconditions

- the company has a valid weekly operating-hours baseline

## UC-22 Manage Closure Dates

### Goal

A company admin defines dates or periods when the business is not operating.

### Primary Actor

Company Admin

### Preconditions

- the company has access to the business calendar configuration

### Main Flow

1. the company admin opens the closure dates section
2. the system shows existing closure entries
3. the company admin adds, edits, or removes a closure date or date range
4. the system validates the submitted date period
5. the system stores the closure configuration

### Alternate Flows

- overlapping closure periods: handled according to merge or rejection policy
- invalid end date before start date: update is rejected

### Postconditions

- the company has an explicit closure-date baseline for future availability logic

## UC-23 Configure Notification Preferences

### Goal

A company admin defines which operational notifications the company wants to receive and where.

### Primary Actor

Company Admin

### Preconditions

- the company exists
- supported notification channels are defined by product policy

### Main Flow

1. the company admin opens notification preferences
2. the system shows the current notification settings
3. the company admin enables or disables supported notification types and configures destination details
4. the system validates the submitted values
5. the system stores the updated preferences

### Alternate Flows

- invalid destination such as malformed email address: update is rejected
- unsupported channel selection: update is rejected

### Postconditions

- the company has explicit notification preferences for supported shared events

## UC-24 List Staff Users

### Goal

A company admin views the staff users who belong to the tenant.

### Primary Actor

Company Admin

### Preconditions

- the user has permission to manage staff users

### Main Flow

1. the company admin opens the staff management area
2. the system returns the staff user list for the current tenant only
3. the system shows each staff user with status, role, and basic contact information

### Alternate Flows

- non-admin company user attempts access: access is denied
- cross-tenant data access attempt: access is denied

### Postconditions

- the company admin has tenant-scoped visibility into staff users

## UC-25 Create Staff User

### Goal

A company admin creates a new staff user for the company.

### Primary Actor

Company Admin

### Preconditions

- the user has permission to manage staff users
- the target email is valid for creation according to identity policy

### Main Flow

1. the company admin opens the create staff flow
2. the company admin enters the staff user details and role
3. the system validates the submitted data
4. the system creates the staff user in the current tenant
5. the system creates the company membership with the selected role
6. the system triggers the defined onboarding path such as activation or invitation
7. the system shows a success message

### Alternate Flows

- email already exists in a conflicting tenant or role context: creation is rejected safely
- invalid role selection: creation is rejected
- onboarding email cannot be sent: failure is visible and recoverable

### Postconditions

- a new staff user exists within the company tenant with the intended role

## UC-26 Update Staff User Status Or Role

### Goal

A company admin changes the role or active status of an existing staff user.

### Primary Actor

Company Admin

### Preconditions

- the staff user belongs to the same tenant
- the acting user has permission to manage staff roles

### Main Flow

1. the company admin opens a staff user record
2. the system shows the current role and status
3. the company admin updates the permitted fields
4. the system validates the change
5. the system stores the new membership role or status

### Alternate Flows

- attempted self-lockout or removal of the last required admin: change is rejected according to policy
- cross-tenant staff record is targeted: access is denied

### Postconditions

- the staff user role and status reflect the approved company configuration

## UC-27 Configure Custom Customer Questions

### Goal

A company admin defines custom intake questions to be reused by later booking flows.

### Primary Actor

Company Admin

### Preconditions

- the company has access to shared booking configuration

### Main Flow

1. the company admin opens the custom questions area
2. the system shows the current question list
3. the company admin adds, edits, reorders, enables, or disables questions
4. the system validates question type, label, and required-state rules
5. the system stores the question configuration

### Alternate Flows

- unsupported question type: update is rejected
- invalid required or option structure: update is rejected

### Postconditions

- the company has a reusable set of custom customer questions

## UC-28 Configure Widget Settings Baseline

### Goal

A company admin configures the baseline settings that will later control the embedded booking widget.

### Primary Actor

Company Admin

### Preconditions

- the company exists
- widget configuration is enabled for the tenant

### Main Flow

1. the company admin opens the widget settings section
2. the system shows the current widget baseline settings
3. the company admin updates settings such as primary call-to-action label, embed readiness, allowed domains placeholder, or visual options supported in this phase
4. the system validates the submitted settings
5. the system stores the widget configuration

### Alternate Flows

- invalid domain or unsupported visual option: validation error is shown
- widget feature disabled by product policy: system shows a restricted state

### Postconditions

- the company has a reusable widget settings baseline for later embedding work

## Cross-Cutting Rules for Phase 2

- all configuration data must remain tenant-owned and tenant-scoped
- company admins can manage company configuration only for their tenant
- company configuration changes should be auditable
- shared configuration must be structured for reuse by later public booking flows
- localization choices must use supported product values only
- staff management must not allow accidental loss of required company-admin coverage
- widget settings must not expose unvalidated or unsafe public configuration

## Recommended First Implementation Order

1. shared company configuration dashboard
2. company profile editing
3. language and locale defaults
4. business hours and closure dates
5. notification preferences
6. staff listing and creation
7. staff role and status management
8. custom customer questions
9. widget settings foundation
10. branding refinement and preview support

## Definition of Phase 2 Completion

Phase 2 is complete when:

- company admins can view a shared configuration dashboard
- company profile data can be edited safely
- default language and locale can be updated
- business hours and closure dates can be configured
- notification preferences can be configured
- staff users can be listed and managed within tenant scope
- custom customer questions can be configured
- widget baseline settings exist for future embedded flows
- all shared company configuration flows are covered by automated tests according to the project testing strategy
