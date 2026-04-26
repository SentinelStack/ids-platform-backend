# ids-platform-backend

Minimal Kotlin Spring Boot backend for the Sentinel IDS/IPS platform.

## Purpose

This module is currently used as a minimal integration step to verify that the backend can:

- start successfully as a Spring Boot application
- resolve and download the `ids-api-contract` artifact from Nexus
- load the exported OpenAPI contract from the dependency classpath
- expose simple test endpoints for validation

At this stage, the backend is intentionally minimal and does not yet contain the full domain logic for alerts, devices, reports or data processing.

## Tech Stack

- Kotlin
- Spring Boot
- Maven
- Nexus Repository

## Dependency on Contract Module

This backend depends on the published contract artifact:

- `ro.puk3p.sentinel:ids-api-contract`

The contract is retrieved from the private Nexus repository hosted at:

- `https://nexus.puk3p.online`

## Project Structure

```text
src/main/kotlin/ro/puk3p/sentinel/
├── SentinelBackendApplication.kt
└── web/
    └── ContractTestController.kt

src/main/resources/
└── application.yml