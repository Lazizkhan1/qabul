# Feature Specification: Contract Generation & Download

**Feature Branch**: `004-qabul-contract`  
**Created**: 2026-05-06  
**Status**: Draft

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Auto-Generate Contract After Passing Exam (Priority: P1)

When an exam finalises with a qualifying score, the system automatically generates a PDF contract with the applicable tuition scale.

**Independent Test**: Complete a passing exam, verify a contract record is created with correct scale and a valid file URL.

**Acceptance Scenarios**:

1. **Given** score > 56.4, **When** results are processed, **Then** contract generated with scale 1.0.
2. **Given** score ≤ 56.4 and ≥ 40, **When** results are processed, **Then** contract generated with scale 1.5.
3. **Given** auto-pass active and score overridden, **When** results are processed, **Then** contract generated with scale 1.0.
4. **Given** score < 40 and auto-pass `OFF`, **When** results are processed, **Then** no contract generated.

---

### User Story 2 — Applicant Downloads Contract (Priority: P1)

An applicant with a generated contract downloads it via a secure, token-protected link.

**Independent Test**: Get a download token after a passing exam, use it to download the PDF, verify it's valid.

**Acceptance Scenarios**:

1. **Given** a valid download token, **When** the download is requested, **Then** the PDF is served.
2. **Given** an invalid or expired token, **When** used, **Then** request is rejected.
3. **Given** a used single-use token, **When** replayed, **Then** request is rejected.
4. **Given** an applicant with no contract, **When** they request a token, **Then** not-found is returned.

---

### Edge Cases

- PDF generation fails: exam result is saved; system retries generation; applicant not left in a broken state.
- Download requested before generation completes: return a pending status; applicant retries.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-CON-001**: System MUST auto-generate a PDF contract for qualifying exam results (score > 40 raw or auto-pass applied).
- **FR-CON-002**: Contract MUST store tuition scale (1.0 or 1.5) and link to the exam.
- **FR-CON-003**: Generated PDF URL MUST be stored in the contract record.
- **FR-CON-004**: Downloads MUST be protected by short-lived or single-use tokens.
- **FR-CON-005**: Token validation MUST be enforced before serving any file.
- **FR-CON-006**: Contract records MUST use UUID v7 and store `created_at`.
- **FR-CON-007**: Applicants MUST only download their own contract.

### Key Entities

- **Contract**: `contract_url`, `scale` (1.0 | 1.5), `exam_id`, `created_at`.

---

## Success Criteria *(mandatory)*

- **SC-001**: Contract PDF available within 10 seconds of exam result finalisation.
- **SC-002**: Invalid/expired token always returns an authorisation error; no file content leaked.
- **SC-003**: Contract generated for 100% of qualifying results under normal conditions.

---

## Assumptions

- PDF template content is agreed separately; out of scope.
- PDF stored in external object storage; this spec covers token-based proxying and URL metadata only.
- Download tokens expire after ~15 minutes or are single-use (config detail).
- Only one contract per exam; regeneration not supported in v1.
