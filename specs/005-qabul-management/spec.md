# Feature Specification: Management, Reference Data & Observability

**Feature Branch**: `005-qabul-management`  
**Created**: 2026-05-06  
**Status**: Draft

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Moderator Account Management (Admin only) (Priority: P2)

Admin can create, update, and delete moderator accounts.

**Acceptance Scenarios**:

1. **Given** the admin is logged in, **When** they create a moderator with credentials, **Then** the moderator can log in and access moderator functions.
2. **Given** a moderator account, **When** admin deletes it, **Then** that account can no longer authenticate.
3. **Given** a non-admin user, **When** they attempt to create a moderator, **Then** rejected with authorisation error.

---

### User Story 2 — Reference Data CRUD (Priority: P2)

Moderators and admins can create, read, update, and delete all reference entities used in the system.

**Scope of entities**: school years, majors, major types, major languages, subjects (with exam duration & question count), exam questions & answers, tuition records, certificate categories.

**Independent Test**: Each entity can be independently created, read, updated, and deleted via the API.

**Acceptance Scenarios**:

1. **Given** a moderator or admin, **When** they create any reference entity, **Then** it is persisted and immediately available in lookups.
2. **Given** a school year set to `active`, **When** applicants submit applications, **Then** it is used as the current year context.
3. **Given** a subject with configured duration and question count, **When** an exam starts, **Then** the correct duration and question count are applied.
4. **Given** a reference entity deletion, **When** it is still referenced by active records (e.g., tuition → application), **Then** deletion is rejected with a clear error.

---

### User Story 3 — Application Statistics (Priority: P3)

Moderators and admins view application statistics, filterable by school year, major, major type, language, degree, and status.

**Acceptance Scenarios**:

1. **Given** a moderator, **When** they request stats with no filters, **Then** aggregate counts for all applications are returned.
2. **Given** a filter combination, **When** applied, **Then** only matching counts are returned.

---

### User Story 4 — Observability & Runtime Controls (Priority: P2)

The system exposes health-check, metrics, and logging endpoints. Admins can change the logging level at runtime without restarting the service. Daily log files are written to `logs/` and auto-deleted after 7 days.

**Acceptance Scenarios**:

1. **Given** the service is running, **When** the health endpoint is called, **Then** service status is returned.
2. **Given** an admin token, **When** they change the logging level, **Then** the new level takes effect immediately for all subsequent log entries.
3. **Given** a log file older than 7 days, **When** the cleanup runs, **Then** it is deleted automatically.

---

### Edge Cases

- Deleting a major that is referenced by active tuition records: rejected with a clear dependency error.
- Setting multiple school years to `active` simultaneously: only one can be active; the system enforces this constraint.

---

## Requirements *(mandatory)*

### Functional Requirements

**Moderator Management**
- **FR-MGT-001**: Admin MUST be able to create, update, and delete moderator accounts.
- **FR-MGT-002**: Only admins MUST be able to manage moderator accounts; moderators MUST NOT.

**Reference Data**
- **FR-MGT-003**: Admins and moderators MUST have full CRUD on: school years, majors, major types, major languages, subjects, exam questions/answers, tuition records, cert categories.
- **FR-MGT-004**: Only one school year may be `active` at a time.
- **FR-MGT-005**: Deletion of reference data referenced by active records MUST be rejected.
- **FR-MGT-006**: All list endpoints for reference data MUST support pagination (`page`, `limit`, `total`, `data`).

**Statistics**
- **FR-MGT-007**: Admins and moderators MUST be able to retrieve application counts grouped and filtered by: school year, major, major type, language, degree, status.

**Observability**
- **FR-MGT-008**: The system MUST expose health-check, metrics, and logging-level endpoints.
- **FR-MGT-009**: Admins MUST be able to change the active logging level at runtime via the logging endpoint.
- **FR-MGT-010**: Logs MUST be written to daily rolling files in `logs/`; files older than 7 days MUST be auto-deleted.
- **FR-MGT-011**: All exceptions MUST produce structured error responses; stack traces MUST be logged at DEBUG level only.

### Key Entities

- **SchoolYear**: `title`, `active` flag — only one active at a time.
- **Major / MajorType / MajorLang**: Lookup tables for offering dimensions.
- **Subject**: `title`, `exam_duration`, `total_questions`.
- **ExamQuestion / ExamAnswer**: Question bank; answers include `is_correct` flag.
- **Tuition**: Composite of major + type + lang + degree + school year + fee amount.
- **CertCategory**: Certificate type (national or language).

---

## Success Criteria *(mandatory)*

- **SC-001**: Statistics queries return within 3 seconds for up to 100,000 application records.
- **SC-002**: Logging level changes take effect within 1 second of being applied, without a restart.
- **SC-003**: Log files older than 7 days are removed within 24 hours of their expiry.
- **SC-004**: All CRUD operations return structured errors on invalid input or constraint violations.

---

## Assumptions

- Flyway handles all database migrations; reference data seeding (initial majors, etc.) is done via migration scripts.
- Swagger UI and a Postman collection are generated from the API definition; this is an implementation concern, not a functional spec item.
- The admin controls the logging level via the actuator endpoint; no separate UI is required.
- Docker Compose is the deployment target; all secrets and configuration come from environment variables.
