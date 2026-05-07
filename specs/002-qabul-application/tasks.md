# Tasks: Application Management

**Input**: Design documents from `/specs/002-qabul-application/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.yaml, quickstart.md

**Tests**: Integration tests are required by project requirements; test tasks are included and must be implemented first per story.

**Organization**: Tasks are grouped by user story so each story is independently implementable and testable.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Parallelizable (different files, no dependencies)
- **[Story]**: User story label (`[US1]`, `[US2]`, `[US3]`, `[US4]`)

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create schema and baseline wiring for application domain.

- [ ] T001 Create Flyway migration skeleton for application domain in `src/main/resources/db/migration/V003__application_management.sql`
- [ ] T002 [P] Add application status enum in `src/main/java/uz/umft/qabul/enums/ApplicationStatus.java`
- [ ] T003 [P] Add degree enum for tuition/application filters in `src/main/java/uz/umft/qabul/enums/Degree.java`
- [ ] T004 [P] Add request/response DTO package scaffold for application APIs in `src/main/java/uz/umft/qabul/controller/dto/application/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core shared entities/repositories/services needed by all stories.

**⚠️ CRITICAL**: Complete this phase before any user story work.

- [ ] T005 Implement migration tables/indexes for `applications`, `certs`, and `application_settings` in `src/main/resources/db/migration/V003__application_management.sql`
- [ ] T006 [P] Implement `Application` JPA entity in `src/main/java/uz/umft/qabul/entity/Application.java`
- [ ] T007 [P] Implement `Cert` JPA entity in `src/main/java/uz/umft/qabul/entity/Cert.java`
- [ ] T008 [P] Implement `ApplicationSetting` JPA entity in `src/main/java/uz/umft/qabul/entity/ApplicationSetting.java`
- [ ] T009 [P] Add `ApplicationRepository` with filter-ready query methods in `src/main/java/uz/umft/qabul/repository/ApplicationRepository.java`
- [ ] T010 [P] Add `CertRepository` in `src/main/java/uz/umft/qabul/repository/CertRepository.java`
- [ ] T011 [P] Add `ApplicationSettingRepository` in `src/main/java/uz/umft/qabul/repository/ApplicationSettingRepository.java`
- [ ] T012 Implement shared application mapper/model assembly helpers in `src/main/java/uz/umft/qabul/service/ApplicationMapper.java`
- [ ] T013 Implement shared validation helper for tuition/cert/category checks in `src/main/java/uz/umft/qabul/service/ApplicationValidationService.java`

**Checkpoint**: Foundational domain and persistence are ready.

---

## Phase 3: User Story 1 - Submit an Application (Priority: P1) 🎯 MVP

**Goal**: Allow applicants to submit one active application with certificate metadata.

**Independent Test**: Submit complete applicant payload and verify application is created with required status and certificate records.

### Tests for User Story 1 (Integration-first)

- [ ] T014 [P] [US1] Add integration test for successful applicant submission in `src/test/java/uz/umft/qabul/application/ApplicantSubmitApplicationIntegrationTest.java`
- [ ] T015 [P] [US1] Add integration test for required-field validation errors in `src/test/java/uz/umft/qabul/application/ApplicantSubmitApplicationValidationIntegrationTest.java`
- [ ] T016 [P] [US1] Add integration test for duplicate active application rejection in `src/test/java/uz/umft/qabul/application/ApplicantDuplicateApplicationIntegrationTest.java`

### Implementation for User Story 1

- [ ] T017 [US1] Implement create-application request DTOs in `src/main/java/uz/umft/qabul/controller/dto/application/CreateApplicationRequest.java`
- [ ] T018 [US1] Implement application/certificate response DTOs in `src/main/java/uz/umft/qabul/controller/dto/application/ApplicationResponse.java`
- [ ] T019 [US1] Implement applicant submission use case in `src/main/java/uz/umft/qabul/service/ApplicationSubmissionService.java`
- [ ] T020 [US1] Implement applicant submit endpoint in `src/main/java/uz/umft/qabul/controller/api/ApplicationController.java`
- [ ] T021 [US1] Wire structured field-level error mapping for submission DTO validation in `src/main/java/uz/umft/qabul/exception/GlobalExceptionHandler.java`

**Checkpoint**: US1 is independently functional.

---

## Phase 4: User Story 2 - Moderator Reviews Applications (Priority: P2)

**Goal**: Moderators/admins list, inspect, accept, and reject applications.

**Independent Test**: Moderator lists pending applications, accepts one, then verifies status transition; rejects non-terminal application and receives terminal-state rejection behavior.

### Tests for User Story 2 (Integration-first)

- [ ] T022 [P] [US2] Add integration test for paginated/filterable moderation list in `src/test/java/uz/umft/qabul/application/ModeratorListApplicationsIntegrationTest.java`
- [ ] T023 [P] [US2] Add integration test for moderator accept flow in `src/test/java/uz/umft/qabul/application/ModeratorAcceptApplicationIntegrationTest.java`
- [ ] T024 [P] [US2] Add integration test for moderator reject and terminal-state guard in `src/test/java/uz/umft/qabul/application/ModeratorRejectApplicationIntegrationTest.java`

### Implementation for User Story 2

- [ ] T025 [US2] Implement moderation list/detail query DTOs in `src/main/java/uz/umft/qabul/controller/dto/application/ApplicationListResponse.java`
- [ ] T026 [US2] Implement moderator review operations in `src/main/java/uz/umft/qabul/service/ApplicationModerationService.java`
- [ ] T027 [US2] Add list/detail/accept/reject endpoints with role guards in `src/main/java/uz/umft/qabul/controller/api/ApplicationController.java`
- [ ] T028 [US2] Add filtering + pagination query support in `src/main/java/uz/umft/qabul/repository/ApplicationRepository.java`

**Checkpoint**: US2 is independently functional.

---

## Phase 5: User Story 3 - Auto-Accept Toggle (Priority: P2)

**Goal**: Enable privileged runtime toggle that changes initial application status for new submissions.

**Independent Test**: Toggle auto-accept on/off and confirm new submissions switch between `ACCEPTED` and `PENDING` without restart.

### Tests for User Story 3 (Integration-first)

- [ ] T029 [P] [US3] Add integration test for privileged auto-accept toggle in `src/test/java/uz/umft/qabul/application/AutoAcceptToggleIntegrationTest.java`
- [ ] T030 [P] [US3] Add integration test for toggle effect on new submissions in `src/test/java/uz/umft/qabul/application/AutoAcceptSubmissionBehaviorIntegrationTest.java`
- [ ] T031 [P] [US3] Add integration test for unauthorized toggle access in `src/test/java/uz/umft/qabul/application/AutoAcceptAuthorizationIntegrationTest.java`

### Implementation for User Story 3

- [ ] T032 [US3] Implement toggle request/response DTOs in `src/main/java/uz/umft/qabul/controller/dto/application/AutoAcceptToggleDto.java`
- [ ] T033 [US3] Implement settings persistence and toggle service in `src/main/java/uz/umft/qabul/service/ApplicationSettingsService.java`
- [ ] T034 [US3] Integrate initial-status resolution into submission flow in `src/main/java/uz/umft/qabul/service/ApplicationSubmissionService.java`
- [ ] T035 [US3] Add auto-accept toggle endpoint with role guard in `src/main/java/uz/umft/qabul/controller/api/ApplicationController.java`

**Checkpoint**: US3 is independently functional.

---

## Phase 6: User Story 4 - Applicant Views Their Application (Priority: P3)

**Goal**: Allow applicants to fetch their own application details and status.

**Independent Test**: Applicant with application receives details; applicant without application gets not-found/empty response.

### Tests for User Story 4 (Integration-first)

- [ ] T036 [P] [US4] Add integration test for applicant self-view success in `src/test/java/uz/umft/qabul/application/ApplicantViewOwnApplicationIntegrationTest.java`
- [ ] T037 [P] [US4] Add integration test for applicant self-view not found case in `src/test/java/uz/umft/qabul/application/ApplicantViewOwnApplicationNotFoundIntegrationTest.java`

### Implementation for User Story 4

- [ ] T038 [US4] Implement applicant self-view service method in `src/main/java/uz/umft/qabul/service/ApplicationQueryService.java`
- [ ] T039 [US4] Add applicant self-view endpoint in `src/main/java/uz/umft/qabul/controller/api/ApplicationController.java`

**Checkpoint**: US4 is independently functional.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final contract alignment, docs updates, and full-suite validation.

- [ ] T040 [P] Update Postman collection for application endpoints in `Qabul_API.postman_collection.json`
- [ ] T041 [P] Align API contract examples with implemented payload/response details in `specs/002-qabul-application/contracts/openapi.yaml`
- [ ] T042 Add quickstart verification notes for final endpoint behavior in `specs/002-qabul-application/quickstart.md`
- [ ] T043 Run full integration suite and fix regressions across `src/test/java/uz/umft/qabul/application/`
- [ ] T044 Mark completed tasks and capture final implementation notes in `specs/002-qabul-application/tasks.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: start immediately
- **Phase 2 (Foundational)**: depends on Phase 1 and blocks all user stories
- **Phases 3-6 (User Stories)**: depend on Phase 2 completion
- **Phase 7 (Polish)**: depends on completion of stories being shipped

### User Story Dependencies

- **US1 (P1)**: independent after Foundational
- **US2 (P2)**: depends on US1-created application data but can be developed after Foundational in parallel with US3
- **US3 (P2)**: depends on submission flow (US1) for end-to-end behavior validation
- **US4 (P3)**: depends on US1 data model and query primitives

### Within Each User Story

- Integration tests first, then implementation
- DTO/entity changes before services
- Services before controller endpoints
- Endpoint + error handling completed before story sign-off

### Parallel Opportunities

- Tasks marked **[P]** can run concurrently when file paths differ
- US2 and US3 can be parallelized after US1 baseline submission path exists
- Test-writing tasks within each story can run in parallel

---

## Parallel Example: User Story 2

```bash
# Parallel tests
Task: "T022 [US2] moderation list integration test in src/test/java/uz/umft/qabul/application/ModeratorListApplicationsIntegrationTest.java"
Task: "T023 [US2] accept flow integration test in src/test/java/uz/umft/qabul/application/ModeratorAcceptApplicationIntegrationTest.java"
Task: "T024 [US2] reject flow integration test in src/test/java/uz/umft/qabul/application/ModeratorRejectApplicationIntegrationTest.java"

# Parallel implementation prep
Task: "T025 [US2] list/detail DTOs in src/main/java/uz/umft/qabul/controller/dto/application/ApplicationListResponse.java"
Task: "T028 [US2] filtering query support in src/main/java/uz/umft/qabul/repository/ApplicationRepository.java"
```

---

## Implementation Strategy

### MVP First (US1 only)

1. Complete Phases 1-2
2. Complete Phase 3 (US1)
3. Validate applicant submission flow independently

### Incremental Delivery

1. Ship US1 (MVP submission)
2. Add US2 (moderation)
3. Add US3 (auto-accept runtime control)
4. Add US4 (applicant self-view)
5. Execute Phase 7 polish before release
