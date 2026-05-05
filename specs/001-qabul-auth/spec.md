# Feature Specification: Authentication

**Feature Branch**: `001-qabul-auth`  
**Created**: 2026-05-06  
**Status**: Draft

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Applicant First-Time Registration (Priority: P1)

A new applicant enters their phone number. The system checks it is unknown, generates a 6-digit OTP (printed to console in local dev), and prompts the applicant to enter it. On success they set a password and receive tokens.

**Why this priority**: Entry gate to the entire system.

**Independent Test**: Register a new number, enter the console-printed OTP, set a password, verify tokens are issued.

**Acceptance Scenarios**:

1. **Given** an unknown phone number, **When** submitted, **Then** an OTP is generated and printed to console (local dev); applicant is prompted for OTP.
2. **Given** a valid OTP within its window, **When** submitted, **Then** applicant is prompted to create a password; access + refresh tokens issued on success.
3. **Given** an expired or wrong OTP, **When** submitted, **Then** an error is returned; no tokens issued.
4. **Given** a known phone number, **When** submitted on the registration screen, **Then** applicant is redirected to the password login flow (User Story 2).

---

### User Story 2 — Applicant Login (Priority: P1)

A registered applicant enters phone number + password and receives JWT tokens.

**Why this priority**: Required for every authenticated applicant action.

**Independent Test**: Log in with a registered number + correct password; verify token claims include `user_id` and `role = APPLICANT`.

**Acceptance Scenarios**:

1. **Given** a registered applicant with the correct password, **When** they log in, **Then** access and refresh tokens are issued.
2. **Given** a wrong password, **When** submitted, **Then** an error is returned; no tokens issued.
3. **Given** a valid refresh token, **When** used to refresh, **Then** a new access token is issued.
4. **Given** an expired refresh token, **When** used, **Then** the request is rejected; applicant must log in again.

---

### User Story 3 — Admin & Moderator Login (Priority: P1)

Admins and moderators authenticate via username + password. The admin account is seeded from environment configuration at startup.

**Why this priority**: Staff cannot manage applications or reference data without authentication.

**Independent Test**: Use the env-configured admin credentials to log in; verify token claims include `role = ADMIN`.

**Acceptance Scenarios**:

1. **Given** the admin credentials from `.env`, **When** used to log in, **Then** an admin-role token is issued.
2. **Given** a moderator account created by admin, **When** the moderator logs in with their credentials, **Then** a moderator-role token is issued.
3. **Given** invalid credentials for any staff account, **When** submitted, **Then** an error is returned.
4. **Given** the service starts, **When** the admin account does not yet exist in the database, **Then** it is created/upserted from environment variables automatically.

---

### Edge Cases

- What if an OTP is requested multiple times for the same number? Latest OTP is valid; previous are invalidated.
- What if a phone number is registered as both applicant and staff? Not allowed — phone and username are separate identifier spaces.
- What if the `.env` admin credentials change and the service restarts? Admin record is upserted (updated) to match the new env values.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-AUTH-001**: The system MUST check whether a phone number exists before deciding the login vs. registration flow.
- **FR-AUTH-002**: For new applicants, the system MUST generate a 6-digit OTP; in local development, OTP MUST be printed to the application console.
- **FR-AUTH-003**: OTPs MUST be time-limited (reasonable default: 5 minutes).
- **FR-AUTH-004**: On valid OTP, first-time applicants MUST be prompted to set a password before tokens are issued.
- **FR-AUTH-005**: Passwords MUST be stored encrypted with bcrypt.
- **FR-AUTH-006**: The system MUST issue JWT access and refresh tokens on successful login; tokens MUST embed `user_id` and `role` claims.
- **FR-AUTH-007**: The system MUST support token refresh via a dedicated endpoint.
- **FR-AUTH-008**: Admin credentials (username + raw password) MUST be read from environment variables at startup and upserted into the database.
- **FR-AUTH-009**: All user primary keys MUST use UUID v7.
- **FR-AUTH-010**: Session records MUST track user-agent, token, status (`ACTIVE` / `PENDING_VERIFICATION`), and expiry.

### Key Entities

- **User**: Holds `phone_number` (applicant) or `username` (staff), `password_hash`, `type` (ADMIN / MODERATOR / APPLICANT), `language`.
- **Session**: Tracks each auth session — `user_id`, `token`, `status`, `user_agent`, `expired_at`.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: OTP generation and token issuance each complete within 3 seconds of request.
- **SC-002**: A new applicant can complete the full registration flow (phone → OTP → password → token) in under 2 minutes.
- **SC-003**: Invalid credentials always return a structured error response; no stack traces are exposed to the caller.
- **SC-004**: Admin account is seeded within 5 seconds of service startup.

---

## Assumptions

- OTP SMS delivery is mocked for the entire scope of this spec; real SMS provider integration is a separate feature.
- A single OTP per phone number is valid at any time; re-requesting invalidates the previous one.
- Token expiry durations use secure industry defaults (e.g., access token: 15 min, refresh token: 7 days) unless overridden via environment config.
- Staff (admin/moderator) login uses username, not phone number.
