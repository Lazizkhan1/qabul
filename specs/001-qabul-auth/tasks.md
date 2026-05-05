# Tasks: Authentication

**Input**: Design documents from `/specs/001-qabul-auth/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.yaml, quickstart.md

**Tests**: Integration tests are included because the feature specification defines mandatory independent tests and `technical-requirements.md` requires integration tests only.

**Organization**: Tasks are grouped by user story so applicant registration, applicant login/refresh, and staff login can be implemented and tested as independent increments after the shared foundation is complete.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel because it touches different files and has no dependency on incomplete tasks in the same phase.
- **[Story]**: Maps the task to a user story from `spec.md`.
- Every task includes an exact file path.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add required dependencies, config surfaces, and test configuration for the authentication feature.

- [X] T001 Add Auth0 Java JWT and Testcontainers PostgreSQL integration-test dependencies in `build.gradle.kts`
- [X] T002 [P] Add authentication environment properties for JWT, OTP, and admin seed credentials in `src/main/resources/application.yaml`
- [X] T003 [P] Create integration-test datasource and auth property overrides in `src/test/resources/application-test.yaml`
- [X] T004 [P] Create authentication API examples for all contract endpoints in `specs/001-qabul-auth/quickstart.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core persistence, security, error, and token infrastructure that must exist before any user story implementation.

**CRITICAL**: No user story work can begin until this phase is complete.

- [X] T005 Create Flyway migration for `users`, `sessions`, and `otp_challenges` auth schema in `src/main/resources/db/migration/V001__auth_schema.sql`
- [X] T006 Update existing `User` entity with username, nullable applicant phone, required role/type, language default, and timestamps in `src/main/java/uz/umft/qabul/domain/entity/User.java`
- [X] T007 [P] Create `Session` JPA entity with UUID v7, token, status, user-agent, expiry, and user relation in `src/main/java/uz/umft/qabul/domain/entity/Session.java`
- [X] T008 [P] Create `SessionStatus` enum with `ACTIVE` and `PENDING_VERIFICATION` in `src/main/java/uz/umft/qabul/domain/enums/SessionStatus.java`
- [X] T009 [P] Create `OtpChallengeStatus` enum with `ACTIVE`, `VERIFIED`, `EXPIRED`, and `REPLACED` in `src/main/java/uz/umft/qabul/domain/enums/OtpChallengeStatus.java`
- [X] T010 [P] Create `UserRepository` lookup methods for phone number, username, and role in `src/main/java/uz/umft/qabul/repository/UserRepository.java`
- [X] T011 [P] Create `SessionRepository` lookup methods for token and active user sessions in `src/main/java/uz/umft/qabul/repository/SessionRepository.java`
- [X] T012 [P] Create `AuthProperties` configuration properties for JWT secret, token TTLs, OTP TTL, and admin credentials in `src/main/java/uz/umft/qabul/config/AuthProperties.java`
- [X] T013 Create `JwtTokenService` for Auth0 JWT access/refresh creation and verification with `user_id` and `role` claims in `src/main/java/uz/umft/qabul/auth/JwtTokenService.java`
- [X] T014 Create `SecurityConfig` with password encoder, public auth endpoints, and stateless security defaults in `src/main/java/uz/umft/qabul/config/SecurityConfig.java`
- [X] T015 [P] Create request error response DTO in `src/main/java/uz/umft/qabul/web/error/ErrorResponse.java`
- [X] T016 Create global exception handler for validation, invalid credentials, expired token, and duplicate identifier errors in `src/main/java/uz/umft/qabul/web/error/GlobalExceptionHandler.java`

**Checkpoint**: Foundation ready. User story implementation can now begin in parallel.

---

## Phase 3: User Story 1 - Applicant First-Time Registration (Priority: P1) MVP

**Goal**: A new applicant submits an unknown phone number, receives a local-dev console OTP, verifies the latest OTP, sets a password, and receives JWT tokens.

**Independent Test**: Register a new number, use the console/logged OTP in the verification endpoint, set a password, and verify access and refresh tokens are issued.

### Tests for User Story 1

- [X] T017 [P] [US1] Create applicant registration integration test for OTP start, OTP replacement, OTP verification, password creation, token issuance, and invalid OTP rejection in `src/test/java/uz/umft/qabul/auth/ApplicantRegistrationIntegrationTest.java`

### Implementation for User Story 1

- [X] T018 [P] [US1] Create `OtpChallenge` JPA entity with phone number, OTP hash, status, expiry, verification timestamp, and timestamps in `src/main/java/uz/umft/qabul/domain/entity/OtpChallenge.java`
- [X] T019 [P] [US1] Create `OtpChallengeRepository` lookup and replacement methods for active phone challenges in `src/main/java/uz/umft/qabul/repository/OtpChallengeRepository.java`
- [X] T020 [P] [US1] Create applicant registration DTO records for start, verify OTP, set password, auth flow response, verification response, and token response in `src/main/java/uz/umft/qabul/auth/dto/ApplicantRegistrationDtos.java`
- [X] T021 [US1] Implement OTP generation, console logging, latest-OTP invalidation, expiry checks, and verification token issuance in `src/main/java/uz/umft/qabul/auth/ApplicantRegistrationService.java`
- [X] T022 [US1] Implement applicant password creation, applicant user creation, bcrypt hashing, pending-to-active session creation, and token issuance in `src/main/java/uz/umft/qabul/auth/ApplicantRegistrationService.java`
- [X] T023 [US1] Implement applicant registration endpoints `/api/v1/auth/applicant/start`, `/verify-otp`, and `/set-password` in `src/main/java/uz/umft/qabul/auth/ApplicantRegistrationController.java`
- [X] T024 [US1] Add structured error mapping for wrong OTP, expired OTP, replaced OTP, and duplicate applicant phone in `src/main/java/uz/umft/qabul/web/error/GlobalExceptionHandler.java`

**Checkpoint**: User Story 1 is fully functional and independently testable.

---

## Phase 4: User Story 2 - Applicant Login (Priority: P1)

**Goal**: A registered applicant logs in with phone number and password, receives JWT tokens, and can refresh access tokens with a valid refresh token.

**Independent Test**: Log in with a registered number and correct password; verify token claims include `user_id` and `role=APPLICANT`; refresh returns a new access token; expired refresh is rejected.

### Tests for User Story 2

- [X] T025 [P] [US2] Create applicant login and refresh integration test for correct password, wrong password, valid refresh, and expired refresh in `src/test/java/uz/umft/qabul/auth/ApplicantLoginIntegrationTest.java`

### Implementation for User Story 2

- [X] T026 [P] [US2] Create applicant login and refresh DTO records in `src/main/java/uz/umft/qabul/auth/dto/ApplicantLoginDtos.java`
- [X] T027 [US2] Implement applicant credential validation, bcrypt password matching, active session creation, and JWT issuance in `src/main/java/uz/umft/qabul/auth/ApplicantLoginService.java`
- [X] T028 [US2] Implement refresh token verification, session lookup, expiry rejection, and new access token creation in `src/main/java/uz/umft/qabul/auth/RefreshTokenService.java`
- [X] T029 [US2] Implement applicant login endpoint `/api/v1/auth/applicant/login` in `src/main/java/uz/umft/qabul/auth/ApplicantLoginController.java`
- [X] T030 [US2] Implement refresh endpoint `/api/v1/auth/refresh` in `src/main/java/uz/umft/qabul/auth/RefreshTokenController.java`
- [X] T031 [US2] Add structured error mapping for invalid applicant password, missing applicant phone, invalid refresh token, and expired refresh token in `src/main/java/uz/umft/qabul/web/error/GlobalExceptionHandler.java`

**Checkpoint**: User Stories 1 and 2 are independently functional.

---

## Phase 5: User Story 3 - Admin & Moderator Login (Priority: P1)

**Goal**: Admin and moderator users authenticate with username and password; the admin account is upserted from environment configuration during startup.

**Independent Test**: Start the service with admin env credentials, log in as admin, and verify token claims include `role=ADMIN`; log in as a seeded moderator and verify `role=MODERATOR`.

### Tests for User Story 3

- [X] T032 [P] [US3] Create staff login and admin bootstrap integration test for admin upsert, admin token claims, moderator token claims, and invalid staff credentials in `src/test/java/uz/umft/qabul/auth/StaffLoginIntegrationTest.java`

### Implementation for User Story 3

- [X] T033 [P] [US3] Create staff login DTO records in `src/main/java/uz/umft/qabul/auth/dto/StaffLoginDtos.java`
- [X] T034 [US3] Implement admin startup upsert from `QABUL_AUTH_ADMIN_USERNAME` and `QABUL_AUTH_ADMIN_PASSWORD` with bcrypt hashing in `src/main/java/uz/umft/qabul/auth/AdminBootstrapService.java`
- [X] T035 [US3] Implement staff credential validation, role restriction to ADMIN/MODERATOR, active session creation, and JWT issuance in `src/main/java/uz/umft/qabul/auth/StaffAuthService.java`
- [X] T036 [US3] Implement staff login endpoint `/api/v1/auth/staff/login` in `src/main/java/uz/umft/qabul/auth/StaffAuthController.java`
- [X] T037 [US3] Add structured error mapping for missing admin config, invalid staff credentials, and applicant use of staff login in `src/main/java/uz/umft/qabul/web/error/GlobalExceptionHandler.java`

**Checkpoint**: All authentication user stories are independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Validate the contract, quickstart, and operational quality across all authentication flows.

- [X] T038 [P] Align response field names and status codes with OpenAPI contract in `specs/001-qabul-auth/contracts/openapi.yaml`
- [X] T039 [P] Add authentication logging messages without raw passwords, raw OTP hashes, or full token values in `src/main/java/uz/umft/qabul/auth/ApplicantRegistrationService.java`
- [X] T040 [P] Add authentication logging messages without raw passwords or full token values in `src/main/java/uz/umft/qabul/auth/ApplicantLoginService.java`
- [X] T041 [P] Add authentication logging messages without raw passwords or full token values in `src/main/java/uz/umft/qabul/auth/StaffAuthService.java`
- [X] T042 Run and document `./gradlew test` validation results in `specs/001-qabul-auth/quickstart.md`
- [X] T043 Verify no task placeholders or unresolved clarification markers remain in `specs/001-qabul-auth/tasks.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion; blocks all user stories.
- **User Stories (Phase 3-5)**: Depend on Foundational completion; can proceed in parallel if separate implementers own separate story files.
- **Polish (Phase 6)**: Depends on completion of the selected user stories.

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational; no dependency on other stories.
- **User Story 2 (P1)**: Can start after Foundational; tests may seed an applicant directly and do not require US1 to be complete.
- **User Story 3 (P1)**: Can start after Foundational; no dependency on applicant flows.

### Within Each User Story

- Write the integration test first and confirm it fails.
- Create DTO/entity/repository files before services that use them.
- Implement services before controllers.
- Add endpoint-specific error handling before marking the story complete.
- Run the story's integration test before moving to the next priority or polish phase.

---

## Parallel Opportunities

- Setup tasks T002, T003, and T004 can run in parallel after T001 is assigned separately.
- Foundational tasks T007 through T012 and T015 can run in parallel after migration design T005 is agreed.
- After Phase 2, US1, US2, and US3 can be implemented in parallel because they use separate controller, DTO, service, and test files.
- Polish logging tasks T039, T040, and T041 can run in parallel after their corresponding services exist.

---

## Parallel Example: User Story 1

```text
Task: "T017 [US1] Create applicant registration integration test in src/test/java/uz/umft/qabul/auth/ApplicantRegistrationIntegrationTest.java"
Task: "T018 [US1] Create OtpChallenge entity in src/main/java/uz/umft/qabul/domain/entity/OtpChallenge.java"
Task: "T019 [US1] Create OtpChallengeRepository in src/main/java/uz/umft/qabul/repository/OtpChallengeRepository.java"
Task: "T020 [US1] Create applicant registration DTO records in src/main/java/uz/umft/qabul/auth/dto/ApplicantRegistrationDtos.java"
```

## Parallel Example: User Story 2

```text
Task: "T025 [US2] Create applicant login integration test in src/test/java/uz/umft/qabul/auth/ApplicantLoginIntegrationTest.java"
Task: "T026 [US2] Create applicant login DTO records in src/main/java/uz/umft/qabul/auth/dto/ApplicantLoginDtos.java"
```

## Parallel Example: User Story 3

```text
Task: "T032 [US3] Create staff login integration test in src/test/java/uz/umft/qabul/auth/StaffLoginIntegrationTest.java"
Task: "T033 [US3] Create staff login DTO records in src/main/java/uz/umft/qabul/auth/dto/StaffLoginDtos.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational.
3. Complete Phase 3: Applicant First-Time Registration.
4. Stop and validate US1 independently with `ApplicantRegistrationIntegrationTest`.
5. Demonstrate the phone to OTP to password to token flow from `quickstart.md`.

### Incremental Delivery

1. Complete Setup and Foundational phases.
2. Deliver US1 applicant registration as the MVP.
3. Deliver US2 applicant login and refresh.
4. Deliver US3 staff login and admin bootstrap.
5. Complete Polish phase and run the full integration suite.

### Parallel Team Strategy

1. Team completes Setup and Foundational phases together.
2. One implementer owns US1 files under applicant registration.
3. One implementer owns US2 files under applicant login and refresh.
4. One implementer owns US3 files under staff auth and admin bootstrap.
5. Merge only after each story's integration test passes independently.

---

## Notes

- `[P]` tasks are limited to different files with no direct dependency on incomplete tasks in the same phase.
- `[US1]`, `[US2]`, and `[US3]` labels map directly to the three P1 user stories in `spec.md`.
- Integration tests should be committed with the story they validate.
- Avoid storing raw passwords, raw OTP hashes, or full token values in logs.
