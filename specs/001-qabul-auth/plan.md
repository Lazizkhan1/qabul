# Implementation Plan: Authentication

**Branch**: `master` | **Date**: 2026-05-06 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-qabul-auth/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Implement the authentication entry point for applicants, admins, and moderators in the existing Spring Boot service. Applicants register with phone number verification, a local-dev console OTP, password creation, and JWT access/refresh token issuance. Existing applicants log in with phone number and password. Staff log in with username and password, with the admin account upserted from environment configuration at startup.

The technical approach is a layered Spring Boot web service using Spring MVC controllers, validation DTO records, service-layer orchestration, Spring Security password encoding, Auth0 JWT signing/verification, PostgreSQL persistence through Spring Data JPA, and Flyway migrations for users, sessions, and OTP verification state.

## Technical Context

**Language/Version**: Java 25  
**Primary Dependencies**: Spring Boot 4.0.6, Spring Web MVC, Spring Security, Spring Data JPA, Spring Validation, Flyway, PostgreSQL JDBC, Lombok, Auth0 Java JWT library, Spring Security crypto/bcrypt  
**Storage**: PostgreSQL 18 with Flyway-managed schema migrations  
**Testing**: JUnit Platform with Spring Boot integration tests, Spring Security test, Spring MVC test; project requirements explicitly prefer integration tests only  
**Target Platform**: Linux server deployment via Docker Compose; local development through Gradle and application console OTP output  
**Project Type**: Backend web service / REST API  
**Performance Goals**: OTP generation and token issuance complete within 3 seconds; admin seed completes within 5 seconds of startup  
**Constraints**: Secrets and token lifetimes come from environment configuration; passwords stored only as bcrypt hashes; JWTs include `user_id` and `role`; user IDs use UUID v7; structured errors must not expose stack traces to callers  
**Scale/Scope**: Authentication foundation for applicant, moderator, and admin flows in the Qabul admissions system; one active OTP per phone number; session tracking for refresh tokens and pending verification

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The current constitution file still contains placeholder principles and does not define enforceable gates. This plan therefore applies the explicit project requirements in `technical-requirements.md` as the operative quality constraints:

- Layer-by-layer organization for controllers, services, repositories, DTOs, configuration, and persistence.
- Secrets and admin bootstrap credentials must come from environment variables.
- JWT access and refresh tokens must use Auth0 Java JWT and include `user_id` and `role`.
- Passwords must be encrypted with bcrypt.
- UUID v7 must be used for primary keys.
- Flyway must own database migrations.
- Integration tests are required; unit tests are not required by project policy.
- API callers receive structured errors; stack traces are logged only at DEBUG level.

**Initial Gate Result**: PASS. No constitution-defined violations found; project-level constraints are represented in the design.

## Project Structure

### Documentation (this feature)

```text
specs/001-qabul-auth/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── openapi.yaml
└── tasks.md             # Phase 2 output from /speckit-tasks; not created by /speckit-plan
```

### Source Code (repository root)

```text
src/main/java/uz/umft/qabul/
├── QabulApplication.java
├── auth/
│   ├── AuthController.java
│   ├── AuthService.java
│   ├── AdminBootstrapService.java
│   ├── JwtTokenService.java
│   └── dto/
├── config/
│   ├── SecurityConfig.java
│   ├── AuthProperties.java
│   └── JwtAuthenticationFilter.java
├── domain/
│   ├── entity/
│   │   ├── User.java
│   │   ├── Session.java
│   │   └── OtpChallenge.java
│   └── enums/
│       ├── Lang.java
│       ├── Role.java
│       └── SessionStatus.java
├── repository/
│   ├── UserRepository.java
│   ├── SessionRepository.java
│   └── OtpChallengeRepository.java
└── web/
    └── error/

src/main/resources/
├── application.yaml
└── db/migration/
    └── V001__auth_schema.sql

src/test/java/uz/umft/qabul/
├── auth/
│   └── AuthFlowIntegrationTest.java
└── QabulApplicationTests.java
```

**Structure Decision**: Use the existing single Spring Boot application layout and add feature-oriented `auth` code plus shared `config`, `repository`, `domain`, and `web/error` packages. Keep DTOs as Java records under `auth/dto` per project requirements.

## Complexity Tracking

No constitution violations require complexity justification.

## Phase 0: Research

Completed in [research.md](./research.md). All technical context questions were resolved from the feature spec, repository stack, and project technical requirements.

## Phase 1: Design & Contracts

Completed artifacts:

- [data-model.md](./data-model.md)
- [contracts/openapi.yaml](./contracts/openapi.yaml)
- [quickstart.md](./quickstart.md)

### Post-Design Constitution Check

**Post-Design Gate Result**: PASS. The design keeps the service layered, persists auth state in PostgreSQL through Flyway/JPA, uses bcrypt and JWT as required, exposes structured REST contracts, and scopes tests to integration coverage for the full authentication flows.
