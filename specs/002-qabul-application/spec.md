# Feature Specification: Application Management

**Feature Branch**: `002-qabul-application`  
**Created**: 2026-05-06  
**Status**: Draft

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Submit an Application (Priority: P1)

An authenticated applicant fills in their personal details, selects a tuition option (major + type + language + degree + school year), uploads supporting certificates, and submits. The application enters the review queue.

**Why this priority**: Core purpose of the system; all exam and contract flows depend on an accepted application.

**Independent Test**: Submit a complete application as a test applicant, verify it appears in the moderator list with status `PENDING`.

**Acceptance Scenarios**:

1. **Given** a logged-in applicant with no active application, **When** they submit a complete form, **Then** an application is created with status `PENDING`.
2. **Given** a submission missing required fields (firstname, lastname, passport series, jshshir, address, tuition), **When** submitted, **Then** field-level validation errors are returned; no record is created.
3. **Given** an applicant who already has a non-cancelled application, **When** they try to create another, **Then** the request is rejected.
4. **Given** a valid form with certificates, **When** submitted, **Then** certificate metadata (number, score, file URL, category) is persisted alongside the application.

---

### User Story 2 — Moderator Reviews Applications (Priority: P2)

Moderators see a paginated list of applications with filtering options. They can open any application to view full applicant details and accept or reject it.

**Why this priority**: Applications can't progress to exam without staff review.

**Independent Test**: Log in as moderator, find a `PENDING` application, accept it, verify its status changes to `ACCEPTED`.

**Acceptance Scenarios**:

1. **Given** a moderator is logged in, **When** they request the application list, **Then** all applications are returned paginated (`page`, `limit`, `total`, `data`).
2. **Given** filters applied (e.g., status, school year, major), **When** the list is requested, **Then** only matching records are returned.
3. **Given** a `PENDING` or `IN_REVIEW` application, **When** a moderator accepts it, **Then** status transitions to `ACCEPTED`.
4. **Given** any non-terminal application, **When** a moderator rejects it, **Then** status transitions to `CANCELED`.
5. **Given** an already `CANCELED` application, **When** a moderator tries to change its status, **Then** the request is rejected.

---

### User Story 3 — Auto-Accept Toggle (Priority: P2)

An admin or moderator can toggle an auto-accept flag at runtime via a dedicated endpoint. When enabled, newly submitted applications are automatically moved to `ACCEPTED` without manual review.

**Why this priority**: Reduces moderator bottleneck during high-volume intake periods.

**Independent Test**: Enable auto-accept, submit a test application, verify it immediately has status `ACCEPTED` without moderator action.

**Acceptance Scenarios**:

1. **Given** auto-accept is `ON`, **When** a new application is submitted, **Then** it is automatically set to `ACCEPTED`.
2. **Given** auto-accept is `OFF`, **When** a new application is submitted, **Then** it remains `PENDING`.
3. **Given** the flag is toggled, **When** the change is applied, **Then** all subsequent submissions use the new value; existing applications are unaffected.
4. **Given** a non-admin/moderator user, **When** they attempt to toggle the flag, **Then** the request is rejected with an authorisation error.

---

### User Story 4 — Applicant Views Their Application (Priority: P3)

An applicant can view the current status and details of their own application.

**Acceptance Scenarios**:

1. **Given** a logged-in applicant with an application, **When** they request it, **Then** their application details and current status are returned.
2. **Given** a logged-in applicant with no application, **When** they request it, **Then** an appropriate empty/not-found response is returned.

---

### Edge Cases

- What if an applicant submits while the referenced tuition record is inactive or deleted? Submission is rejected with a clear error.
- What if two moderators accept/reject the same application simultaneously? Last writer wins; the final status is whichever completes last (optimistic concurrency is acceptable for this use case).
- What if certificate file upload fails? Application is not saved; the applicant is informed and can retry.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-APP-001**: An authenticated applicant MUST be able to submit an application containing personal details (firstname, lastname, middlename, birth_date, gender, jshshir, passport_series, address, additional_phone) and a selected tuition reference.
- **FR-APP-002**: An applicant MUST NOT have more than one active (non-`CANCELED`) application at a time.
- **FR-APP-003**: The system MUST support attaching one or more certificates to an application (cert_number, score, file_url, cert_category).
- **FR-APP-004**: Applications MUST begin with status `PENDING` unless auto-accept is enabled, in which case they MUST begin as `ACCEPTED`.
- **FR-APP-005**: Moderators and admins MUST be able to list all applications with pagination and filtering by: status, school year, major, major type, language, degree.
- **FR-APP-006**: Moderators and admins MUST be able to accept (`ACCEPTED`) or reject (`CANCELED`) any non-terminal application.
- **FR-APP-007**: The system MUST expose a privileged runtime endpoint to toggle the auto-accept flag; only admins and moderators may access it.
- **FR-APP-008**: All application and certificate records MUST use UUID v7 primary keys.
- **FR-APP-009**: Application records MUST track `created_at` and `updated_at` timestamps.

### Key Entities

- **Application**: Central record — personal info, selected tuition, status lifecycle, timestamps.
- **Cert**: Supporting document per application — number, score, file URL, linked category.
- **CertCategory**: Reference type for certificates — national or language certificate types.
- **Tuition**: The offering the applicant selected (major + type + language + degree + school year + fee amount).

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Application submission completes and persists within 5 seconds under normal network conditions.
- **SC-002**: The applications list returns results within 3 seconds for data sets up to 100,000 records.
- **SC-003**: Auto-accept flag changes take effect for all new submissions within 1 second of being toggled, without a service restart.
- **SC-004**: All validation errors are returned as structured, field-level responses; no unhandled exceptions reach the caller.

---

## Assumptions

- The applicant selects a `tuition` record that already exists in the system; reference data management is covered in spec `005-qabul-management`.
- Certificate file uploads are stored externally (object storage); this spec covers only metadata and URL persistence.
- Disability field defaults to `0` (no disability) if not provided.
- The auto-accept flag persists across service restarts.
- Pagination uses `{ page, limit, total, data }` for all list endpoints.
