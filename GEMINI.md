# Qabul Project Context

This document provides essential context and instructions for AI agents working on the **Qabul** (Admission System) project.

## Project Overview

**Qabul** is a university admission management system designed to handle applicant registrations, application submissions, reviews, examinations, and contract generation.

### Core Technologies
- **Language:** Java 25
- **Framework:** Spring Boot 4.0.6
- **Build Tool:** Gradle
- **Database:** PostgreSQL with Flyway migrations
- **ORM:** Hibernate 7.2.12.Final
- **Security:** Spring Security with JWT (`com.auth0:java-jwt`)
- **API Documentation:** SpringDoc / OpenAPI (Swagger)
- **Utilities:** Lombok, Actuator

### Architecture
The project follows a standard Spring Boot layered architecture:
- `controller/api`: REST controllers for external API access.
- `controller/dto`: Data Transfer Objects for API requests and responses.
- `service`: Business logic layer.
- `repository`: Data access layer using Spring Data JPA.
- `entity`: JPA entities mapping to the database schema.
- `enums`: Domain-specific enumerations (Roles, Statuses, etc.).
- `config`: Configuration classes (Security, JWT, File properties).
- `integration`: External service integrations (e.g., OTP Service).

## Key Workflows

### Authentication
- **Applicants:** Authenticate via phone number and OTP (currently mocked in local dev). Upon first login, they create a password. Subsequent logins use phone/password.
- **Staff (Admin/Moderator):** Authenticate via username (phone number) and password.

### Application Process
1. Applicant creates an application (`ApplicationController.submit`).
2. Moderators/Admins review applications (`accept` or `reject`).
3. **Auto-Accept:** An "auto-accept" feature can be toggled via `PATCH /api/v1/applications/settings/auto-accept`.

### Examination & Contracts
- Once an application is accepted, an exam is conducted.
- **Fake Scoring Requirement:** Per `SPEC.md`, if an applicant fails to reach the passing score (56), the system should "fakely" generate a result between 56 and 70 to allow them to proceed with a 1.0 tuition scale.
- **Contracts:** Generated in PDF format upon successful exam completion.

## Development & Operations

### Key Commands
- **Build Project:** `./gradlew build`
- **Run Application:** `./gradlew bootRun`
- **Run Tests:** `./gradlew test`
- **Clean Project:** `./gradlew clean`

### Configuration
Primary configuration is in `src/main/resources/application.yaml`. Environment variables are used for sensitive data and infrastructure overrides:
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `QABUL_AUTH_JWT_SECRET`
- `OTP_SERVICE_TOKEN`
- `QABUL_FILES_BASE_DIR`

### Testing Conventions
- **Integration Tests:** Located in `src/test/java/uz/umft/qabul`.
- **Base Support:** Use `ApplicationIntegrationTestSupport` for integration tests. It handles:
    - Database cleanup before each test.
    - Seeding reference data (School Years, Majors, Tuition, etc.).
    - MockMvc setup and authentication helpers.
- **Profile:** Use the `test` profile (`@ActiveProfiles("test")`).

### File Storage
Uploaded files (e.g., certificates) are stored in the directory defined by `qabul.files.base-dir` (default: `storage`). Allowed types: `pdf`, `png`, `jpg`, `jpeg`. Max size: 5MB.

## Specific Constraints & Instructions
- **Security:** Always use `@PreAuthorize` on controller methods to enforce role-based access control (`ROLE_ADMIN`, `ROLE_MODERATOR`, `ROLE_APPLICANT`).
- **Audit:** Entities like `Application` and `Cert` have `created_at` and `updated_at` timestamps.
- **Validation:** Use Jakarta Validation annotations in DTOs.
- **Flyway:** All schema changes must be implemented via Flyway migration scripts in `src/main/resources/db/migration`.
