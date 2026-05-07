# Research: Application Management

## Decision: Persist auto-accept as database-backed runtime configuration

**Rationale**: The auto-accept toggle must survive service restarts and apply to all new submissions immediately. A dedicated settings table (single row/key) in PostgreSQL provides durable state and supports transactional reads during submission handling.

**Alternatives considered**:

- In-memory flag: rejected because restart would reset behavior and violate persistence assumptions.
- Environment variable only: rejected because runtime toggling without restart is required.

## Decision: Enforce one active application per applicant at database level

**Rationale**: FR-APP-002 requires no more than one non-canceled application per applicant. Enforcing this via a partial unique index on `applications(user_id)` where `status <> 'CANCELED'` prevents race-condition duplicates and complements service-level checks.

**Alternatives considered**:

- Service-only validation: rejected because concurrent requests can bypass application-level checks.
- Hard unique key on `user_id`: rejected because canceled applications must allow re-submission.

## Decision: Keep certificate metadata in the same submission transaction

**Rationale**: The spec says a failed certificate upload must prevent application creation. Persisting `application` and `cert` metadata in one transaction after successful upload URL resolution guarantees all-or-nothing behavior.

**Alternatives considered**:

- Asynchronous certificate attachment: rejected because it can leave partially created applications.
- Independent cert API after submission: rejected because submission must include certificates atomically.

## Decision: Use explicit status transition guards in service layer

**Rationale**: Moderators can accept/reject only non-terminal records, and canceled applications must not be changed. Centralizing transition checks in service methods keeps controller logic thin and ensures consistent transition rules across endpoints.

**Alternatives considered**:

- Free-form status updates from API payloads: rejected as too error-prone and weakly constrained.
- Database triggers for all transitions: rejected to keep business rules observable in Java service layer.

## Decision: Implement listing with pageable filters mapped to tuition dimensions

**Rationale**: Required filters span application status and tuition attributes (school year, major, major type, language, degree). A pageable query with joins to tuition and optional filter predicates supports required response shape `{page, limit, total, data}` and scale target up to 100k records.

**Alternatives considered**:

- In-memory filtering after broad reads: rejected due to poor performance and memory cost.
- Multiple specialized list endpoints per filter: rejected because one composable query is simpler and easier to maintain.

## Decision: Publish contract-first REST endpoints under `/api/v1/applications`

**Rationale**: Existing auth APIs use versioned REST controllers and role guards. Extending that pattern with dedicated submit/self-view/moderation/toggle endpoints keeps API style consistent and simplifies integration test coverage.

**Alternatives considered**:

- Reusing auth controller namespace: rejected because application management is a separate domain boundary.
- Single mutation endpoint with action payload: rejected because accept/reject/toggle semantics are clearer as explicit operations.
