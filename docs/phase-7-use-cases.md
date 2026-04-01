# Phase 7 Use Cases

## Purpose

This document defines the use cases for Phase 7: Embedded Widget.

The widget exposes tenant booking flows outside the hosted web app, so security must be part of the implementation from the first slice.

## Actors

- Anonymous Customer
- Company Admin
- System
- External Website

## UC-57 Configure Embedded Widget Settings

### Goal

A company admin configures the embedded widget runtime settings for a tenant.

### Security Considerations

- allowed-domain validation is required
- admin-only writes require CSRF and recent-auth
- widget config changes are audited

## UC-58 Initialize Tenant Widget

### Goal

An external website loads a tenant widget with the allowed branding and booking configuration.

### Security Considerations

- initialization must validate tenant and origin policy
- unsupported or disallowed origins are rejected safely
- widget bootstrap responses must avoid exposing secrets

## UC-59 Execute Embedded Booking Flow

### Goal

An anonymous customer completes a booking journey through the embedded widget.

### Security Considerations

- widget booking flows must reuse the same backend validation and abuse protections as hosted flows
- cross-origin behavior must be explicitly controlled

## UC-60 Monitor Widget Usage

### Goal

A company admin reviews basic widget usage and operational health signals.

### Security Considerations

- analytics and operational views stay tenant-scoped
- exposed telemetry avoids leaking personal data beyond policy
