# Phase 7 Test Scenarios

## Purpose

This document defines the implementation and verification baseline for Phase 7: Embedded Widget.

## Test Layers

- backend unit tests for widget policy and origin validation
- backend integration tests for widget bootstrap, booking reuse, and tenant enforcement
- frontend and runtime tests for widget initialization and rendering behavior
- end-to-end tests for embedded booking on an allowed host
- security tests for allowed-origin enforcement, clickjacking resistance, and abuse protection

## UC-57 Configure Embedded Widget Settings

### Security

- invalid allowed domains are rejected
- non-admin or stale-auth writes are denied

## UC-58 Initialize Tenant Widget

### Security

- disallowed origins cannot initialize the widget
- bootstrap payload excludes secrets and sensitive internal configuration

## UC-59 Execute Embedded Booking Flow

### Security

- embedded flow enforces the same throttling, validation, and tenant checks as hosted booking
- CORS and framing behavior are explicitly verified

## UC-60 Monitor Widget Usage

### Security

- tenant analytics access is isolated
- exported or displayed telemetry follows privacy policy
