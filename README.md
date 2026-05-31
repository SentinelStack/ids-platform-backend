# ids-platform-backend

Backend foundation for a bachelor thesis project: an OpenWrt-based IDS/IPS platform.

## Purpose

This service stores and exposes **processed security telemetry** produced by the OpenWrt edge agent:
- alerts
- device heartbeats
- traffic statistics
- packet/forensics metadata

The router performs first-level traffic processing. This backend is designed as a telemetry and incident API layer, **not** as a raw packet processor.

## Architecture

- Kotlin + Spring Boot + Maven
- Layered design per module:
  - Controller
  - Service (interface + implementation)
  - Repository
  - Entity
  - DTO
  - Mapper
  - Assembler (HATEOAS links)
- Spring Data JPA
- Validation (`jakarta.validation`)
- Centralized exception handling with `@RestControllerAdvice`
- HATEOAS hypermedia links (`spring-boot-starter-hateoas`)

Project package root:

`src/main/kotlin/ro/puk3p/sentinel`

Modules:
- `device`
- `alert`
- `traffic`
- `forensics`
- `common` (errors + response wrappers)
- `config`

## Current Modules

### 1. Device Module
Manages OpenWrt routers/agents and heartbeat status.

Endpoints:
- `POST /api/devices/register`
- `POST /api/devices/{deviceId}/heartbeat`
- `GET /api/devices`
- `GET /api/devices/{deviceId}`
- `GET /api/devices/{deviceId}/status`

### 2. Alert Module
Stores and serves anomalies from edge detection.

Endpoints:
- `POST /api/alerts`
- `GET /api/alerts`
- `GET /api/alerts/{alertId}`
- `GET /api/alerts/latest`
- `GET /api/alerts/by-device/{deviceId}`
- `PATCH /api/alerts/{alertId}/acknowledge`

Filters for `GET /api/alerts`:
- `severity`
- `protocol`
- `deviceId`
- `sourceIp`
- `from`
- `to`

### 3. Traffic Module
Stores aggregated traffic windows for dashboard charts.

Endpoints:
- `POST /api/traffic/stats`
- `GET /api/traffic/stats/latest`
- `GET /api/traffic/stats/by-device/{deviceId}`
- `GET /api/traffic/summary`

### 4. Forensics Module
Stores packet-level metadata (not full PCAP files).

Endpoints:
- `POST /api/forensics/packets`
- `GET /api/forensics/packets`
- `GET /api/forensics/by-alert/{alertId}`
- `GET /api/forensics/timeline`

## Database

Supported drivers are included:
- PostgreSQL
- MariaDB

Default local config in `application.yml` uses PostgreSQL placeholders.
Switch to MariaDB by setting:
- `DB_URL`
- `DB_DRIVER=org.mariadb.jdbc.Driver`
- `DB_USERNAME`
- `DB_PASSWORD`

## HATEOAS

Responses follow HATEOAS principles: every resource carries a `links` array so
clients can discover related resources and available actions instead of
hardcoding URLs.

- **Entry point:** `GET /api` returns the top-level links (devices, alerts,
  traffic, forensics, health). A client can start there and navigate the whole
  API by following links.
- **Resource links:** each item links to itself and its relations — e.g. a
  device links to its `alerts`, `traffic`, `status`, and `heartbeat`; an alert
  links to its `device` and `forensics`.
- **State-driven actions:** affordances reflect resource state. An alert exposes
  an `acknowledge` link only while it is un-acknowledged; the link disappears
  once acknowledged.
- **Pagination:** paged collections include `self`/`first`/`prev`/`next`/`last`
  links for traversal.

Implementation: `spring-boot-starter-hateoas`, per-resource
`RepresentationModelAssembler`s under each module's `assembler` package, links
built with `WebMvcLinkBuilder`, and the `PageLinks` helper for pagination. The
existing `ApiResponse` envelope is preserved, so the `data` field still carries
each resource (now enriched with `links`). The OpenWrt agent, which only reads
`data.deviceId` and the HTTP status, is unaffected.

## CORS

Browser clients (the Angular dashboard) are allowed to call `/api/**` via a
`WebMvcConfigurer` in `config/CorsConfig.kt`. Allowed origins are configured
with `app.cors.allowed-origins` (comma-separated), defaulting to the Angular dev
server `http://localhost:4200`. Override per environment:

```bash
export CORS_ALLOWED_ORIGINS=http://localhost:4200,https://dashboard.example.com
```

The OpenWrt edge agent talks to the API server-to-server and is unaffected by CORS.

## Run Locally

1. Configure environment variables (or edit `application.yml`):

```bash
export DB_URL=jdbc:postgresql://localhost:5432/ids_platform
export DB_USERNAME=ids_user
export DB_PASSWORD=ids_password
export DB_DRIVER=org.postgresql.Driver
```

2. Build and run:

```bash
mvn clean spring-boot:run
```

Service starts on `http://localhost:8080`.

## Git Hook Flow

Repository hooks are stored in `.githooks` and enabled via:

```bash
./scripts/setup-git-hooks.sh
```

Active checks:
- `commit-msg`: enforces Conventional Commit prefix (`feat:`, `fix:`, `chore:`, `docs:`, etc.)
- `pre-commit`: runs local quality checks
- `pre-push`: validates outgoing commit messages, then runs SpotBugs + Checkstyle + PMD

## Example API Requests

Register device:

```bash
curl -X POST http://localhost:8080/api/devices/register \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Router Lab A",
    "ipAddress":"192.168.1.1",
    "firmwareVersion":"OpenWrt 23.05",
    "model":"TP-Link Archer"
  }'
```

Create alert:

```bash
curl -X POST http://localhost:8080/api/alerts \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId":"<device-uuid>",
    "timestamp":"2026-05-04T10:00:00Z",
    "type":"UDP_FLOOD_SUSPECTED",
    "severity":"HIGH",
    "protocol":"UDP",
    "sourceIp":"10.10.10.2",
    "destinationIp":"192.168.1.10",
    "sourcePort":5353,
    "destinationPort":443,
    "packetCount":12000,
    "bytesCount":945000,
    "windowSeconds":60,
    "description":"Traffic burst above baseline"
  }'
```

Create traffic stats:

```bash
curl -X POST http://localhost:8080/api/traffic/stats \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId":"<device-uuid>",
    "timestamp":"2026-05-04T10:01:00Z",
    "totalPackets":34000,
    "tcpPackets":21000,
    "udpPackets":13000,
    "totalBytes":28000000,
    "tcpBytes":21000000,
    "udpBytes":7000000,
    "windowSeconds":60
  }'
```

## Roadmap (Next Phases)

- AI summarization service for incident context
- MISP integration for threat intelligence enrichment
- Kafka events for streaming pipelines
- deeper web dashboard integration
- iOS app integration
