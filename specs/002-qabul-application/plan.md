# Implementation Plan: Application Management

**Branch**: `master` | **Date**: 2026-05-07 | **Spec**: `/specs/002-qabul-application/spec.md`  
**Input**: Feature specification from `/specs/002-qabul-application/spec.md`

## Summary

Implement end-to-end application management for authenticated applicants and moderators/admins: applicant submission with certificate metadata, moderator review actions (list/filter/detail/accept/reject), runtime auto-accept toggle, and applicant self-view. The implementation will extend the existing Spring Boot + Flyway + PostgreSQL backend with new entities, migrations, REST endpoints, and integration tests while preserving existing auth patterns and response conventions.

## Technical Context

**Language/Version**: Java 25 (OpenJDK toolchain via Gradle)  
**Primary Dependencies**: Spring Boot 4.0.6 (Web MVC, Security, Validation, Data JPA), Flyway, PostgreSQL driver, Lombok  
**Storage**: PostgreSQL 18 with Flyway migrations  
**Testing**: Spring Boot integration tests via `./gradlew test` (JUnit Platform)  
**Target Platform**: Linux server deployment via Docker Compose  
**Project Type**: Single backend web-service  
**Performance Goals**: Submission <= 5s, listing <= 3s on up to 100k records, auto-accept toggle effect <= 1s  
**Constraints**: UUID v7 IDs, structured validation errors, one active (non-`CANCELED`) application per applicant, role-guarded privileged endpoints, no application persistence on certificate upload failure  
**Scale/Scope**: Up to 100k applications with pagination/filtering by status and tuition dimensions

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Constitution file (`.specify/memory/constitution.md`) is currently an unfilled template with placeholder principles and no ratified enforceable gates.
- Gate result (pre-design): **PASS** (no active constitutional constraints to violate).
- Gate result (post-design): **PASS** (design remains within repository technical requirements and spec scope).

## Project Structure

### Documentation (this feature)

```text
specs/002-qabul-application/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── openapi.yaml
└── tasks.md
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/uz/umft/qabul/
│   │   ├── config/
│   │   ├── controller/
│   │   │   ├── api/
│   │   │   └── dto/
│   │   ├── entity/
│   │   ├── enums/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   └── resources/
│       ├── application.yaml
│       └── db/migration/
└── test/
    ├── java/uz/umft/qabul/
    │   ├── auth/
    │   └── application/        # new integration tests for this feature
    └── resources/application-test.yaml
```

**Structure Decision**: Continue with the existing layered single-service structure and add application-domain classes under current `entity/repository/service/controller` packages, plus Flyway SQL migrations and integration tests under `src/test/java/uz/umft/qabul/application`.

## Complexity Tracking

No constitution violations identified; this section is intentionally empty.
