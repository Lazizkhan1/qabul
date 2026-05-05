# Research: Authentication

## Decision: Keep authentication inside the existing Spring Boot service

**Rationale**: The repository is already a single Spring Boot backend with JPA, Security, Validation, Flyway, and PostgreSQL dependencies. Authentication is a core cross-cutting capability for later applicant, exam, contract, and management flows, so keeping it in the same service avoids premature service boundaries and lets authorization reuse the existing domain model.

**Alternatives considered**:

- Separate auth service: rejected because the project is currently a single deployable application and there is no cross-service requirement.
- External identity provider: rejected because the spec requires custom applicant OTP registration, admin seeding from `.env`, and moderator accounts managed by admins.

## Decision: Use layered package organization

**Rationale**: `technical-requirements.md` requires layer-by-layer organization. The auth feature will use controllers for REST boundaries, DTO records for request/response payloads, services for flow orchestration, repositories for persistence, configuration classes for security/JWT properties, and domain entities/enums for persisted state.

**Alternatives considered**:

- Package only by entity: rejected because auth behavior spans multiple persistence records and security concerns.
- Put all auth code in one package: rejected because it would blur controller, service, configuration, and persistence responsibilities.

## Decision: Store users in one `users` table with separate identifier spaces

**Rationale**: The spec defines one `User` entity with applicant phone numbers and staff usernames. Phone number and username are separate identifier spaces, so applicants require a unique phone number and staff require a unique username. Role/type differentiates ADMIN, MODERATOR, and APPLICANT behavior.

**Alternatives considered**:

- Separate applicant and staff tables: rejected because the spec names a single User entity and JWT claims only need `user_id` and `role`.
- Require phone for staff: rejected because staff login explicitly uses username and password.

## Decision: Persist OTP challenges separately from sessions

**Rationale**: Session records have required statuses `ACTIVE` and `PENDING_VERIFICATION`, but OTP verification has its own lifecycle: requested, replaced, verified, expired. A dedicated `otp_challenges` table keeps passwordless registration state auditable and allows re-requesting an OTP to invalidate earlier challenges for the same phone number.

**Alternatives considered**:

- Store OTP directly on `users`: rejected because the user does not exist before successful first-time registration and repeated OTP requests should be independently tracked.
- Store OTP only in memory: rejected because service restarts would erase pending verification state and complicate integration tests.

## Decision: Use bcrypt via Spring Security `PasswordEncoder`

**Rationale**: The spec and project requirements require bcrypt. Spring Security already supplies stable bcrypt support and integrates naturally with authentication code.

**Alternatives considered**:

- Raw message digests: rejected because they do not provide password hashing work factors and salts appropriate for password storage.
- Argon2: rejected because the explicit requirement is bcrypt.

## Decision: Use Auth0 Java JWT for access and refresh tokens

**Rationale**: Project requirements explicitly call for JWT with the Auth0 library. Tokens must embed `user_id` and `role`; access tokens should be short-lived and refresh tokens longer-lived, with refresh token state backed by session records.

**Alternatives considered**:

- Spring OAuth2 resource server JWT stack: rejected because it does not satisfy the explicit Auth0 library requirement.
- Opaque server-side tokens only: rejected because the feature requires JWT claims.

## Decision: Track refresh tokens in `sessions`

**Rationale**: The feature requires session records to track token, status, user-agent, and expiry. Persisting refresh token identifiers or token hashes in sessions supports refresh validation, logout/revocation later, and expiry checks. Access tokens remain stateless and short-lived.

**Alternatives considered**:

- Store raw access tokens in sessions: rejected because access tokens should be short-lived and need not be stored for every request.
- Do not persist refresh token state: rejected because the required Session entity would not be meaningful and expired/revoked refresh token behavior would be harder to control.

## Decision: Admin account upsert runs at application startup

**Rationale**: The spec requires admin credentials from environment variables to create or update the admin record automatically at service startup. A startup service using application-ready lifecycle hooks can validate required config, bcrypt the configured password, and upsert by username.

**Alternatives considered**:

- Flyway seed with static credentials: rejected because `.env` updates must update the admin record.
- Manual admin creation endpoint: rejected because the admin must exist before staff management is possible.

## Decision: Use integration tests for the full auth flows

**Rationale**: Project requirements say to implement integration tests only. The highest-risk behavior crosses web endpoints, validation, persistence, password hashing, JWT, and startup seeding, so integration tests cover the right surface.

**Alternatives considered**:

- Unit tests for individual services: rejected for this project phase because the stated testing policy excludes unit tests.
- Manual-only Postman validation: rejected because regressions in registration and refresh flows need automated coverage.
