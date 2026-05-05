# Feature Specification: Online Examination

**Feature Branch**: `003-qabul-exam`  
**Created**: 2026-05-06  
**Status**: Draft

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Start an Exam (Priority: P1)

An applicant whose application is `ACCEPTED` initiates an exam. The system generates a set of questions from the question bank for their selected subject and starts a countdown timer.

**Why this priority**: Core admissions gate; contract and scoring depend on this flow.

**Independent Test**: Accept a test application, start an exam, verify questions are returned and a timer is active.

**Acceptance Scenarios**:

1. **Given** an applicant with an `ACCEPTED` application, **When** they start the exam, **Then** `N` questions are drawn from the bank for their subject and a countdown of the subject's configured duration begins.
2. **Given** an applicant without an `ACCEPTED` application, **When** they attempt to start, **Then** the request is rejected.
3. **Given** an applicant already in an `ONGOING` exam, **When** they call start again, **Then** the existing exam session is returned (no duplicate created).
4. **Given** the question bank has fewer questions than the subject's `total_questions`, **When** an exam is started, **Then** the system returns an error; no exam record is created.

---

### User Story 2 — Submit Exam Answers (Priority: P1)

During the exam the applicant submits their answers. When the timer expires the exam is auto-submitted.

**Why this priority**: Answers must be recorded and scored; without this no result exists.

**Independent Test**: Start an exam, submit answers, verify exam transitions to a terminal status and a score is calculated.

**Acceptance Scenarios**:

1. **Given** an `ONGOING` exam, **When** the applicant submits answers before the timer expires, **Then** the exam is scored and transitions to `PASSED` or `FAILED`.
2. **Given** an `ONGOING` exam where the timer expires, **When** auto-submission fires, **Then** answers submitted so far are scored; unanswered questions score 0.
3. **Given** a `PASSED` or `FAILED` exam, **When** a resubmission is attempted, **Then** the request is rejected.

---

### User Story 3 — View Exam Result (Priority: P1)

After the exam is finalised the applicant sees only their total score (range 56–189). Individual question results are not disclosed.

**Why this priority**: Transparency for the applicant; drives next steps (contract download).

**Independent Test**: Submit a completed exam, request results, verify only a single numeric score is returned (no question breakdown).

**Acceptance Scenarios**:

1. **Given** a finalised exam, **When** the applicant requests their result, **Then** only the final score is returned.
2. **Given** a score > 56.4, **When** the result is returned, **Then** the applicant is informed a contract will be available.
3. **Given** a score ≤ 56.4 and ≥ 40, **When** the result is returned, **Then** the applicant is informed a contract will be available (higher tuition scale).
4. **Given** a score < 40 (and auto-pass is `OFF`), **When** the result is returned, **Then** the applicant is informed the application is cancelled.

---

### User Story 4 — Auto-Pass Toggle (Priority: P2)

An admin or moderator can toggle the **auto-pass** flag at runtime via a dedicated privileged endpoint. When enabled, exam scores below 56.4 are overridden to a generated value in (56, 70] and a 1.0-scale contract is generated.

**Why this priority**: Explicit business requirement; affects contract generation for borderline applicants.

**Independent Test**: Enable auto-pass, run an exam expecting a score < 40, verify the stored score is in (56, 70] and a 1.0-scale contract is generated.

**Acceptance Scenarios**:

1. **Given** auto-pass is `ON` and a raw score < 56.4, **When** exam is finalised, **Then** the score is overridden to a value in (56, 70]; a 1.0-scale contract is generated.
2. **Given** auto-pass is `OFF`, **When** any exam is finalised, **Then** the raw score is used as-is.
3. **Given** a raw score ≥ 56.4 (regardless of auto-pass flag), **When** exam is finalised, **Then** no override occurs.
4. **Given** the flag is toggled, **When** applied, **Then** all subsequent finalised exams use the new value; in-progress exams are unaffected.
5. **Given** a non-admin/moderator user, **When** they attempt to toggle auto-pass, **Then** the request is rejected.

---

### Edge Cases

- What if an applicant loses connectivity mid-exam? The timer keeps running server-side; auto-submission fires on expiry.
- What if the generated auto-pass override value falls below 56? It MUST be regenerated until it is in (56, 70].
- What if an applicant's application is cancelled after an exam starts? Exam continues until submitted; result processing handles the new application state.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-EXAM-001**: Only applicants with `ACCEPTED` applications MUST be able to start an exam.
- **FR-EXAM-002**: The system MUST draw exactly `subject.total_questions` questions from the question bank for the applicant's subject, selected randomly.
- **FR-EXAM-003**: If the question bank has fewer questions than required, exam creation MUST fail with a clear error.
- **FR-EXAM-004**: Exam duration MUST equal `subject.exam_duration`; the timer MUST be enforced server-side.
- **FR-EXAM-005**: On timer expiry the system MUST auto-submit the exam with answers provided so far; unanswered questions score 0.
- **FR-EXAM-006**: The system MUST expose only the final score to the applicant; individual question outcomes MUST NOT be returned.
- **FR-EXAM-007**: Score thresholds:
  - > 56.4 → status `PASSED`, contract scale 1.0
  - ≤ 56.4 and ≥ 40 → status `PASSED`, contract scale 1.5
  - < 40 → status `FAILED`, application `CANCELED` (no contract, unless auto-pass is active)
- **FR-EXAM-008**: The system MUST expose a privileged runtime endpoint to toggle the auto-pass flag; only admins and moderators may access it.
- **FR-EXAM-009**: When auto-pass is `ON` and raw score < 56.4, the stored score MUST be overridden to a randomly generated value in (56, 70] and scale MUST be 1.0.
- **FR-EXAM-010**: The auto-pass flag MUST persist across service restarts.
- **FR-EXAM-011**: A second start request for an `ONGOING` exam MUST return the existing session without creating a duplicate.

### Key Entities

- **Exam**: Linked to an application; tracks `status` (PENDING / ONGOING / PASSED / FAILED), `duration`, `score`, `completed_at`.
- **ExamQuestion**: A question assigned to an exam session — `question_text`, `point`, `subject_id`.
- **ExamAnswer**: Possible answers per question — `answer_text`, `is_correct`.
- **Subject**: Reference entity defining `exam_duration` and `total_questions`.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Exam question generation and timer initialisation complete within 5 seconds of starting an exam.
- **SC-002**: Auto-submission fires within 10 seconds of timer expiry.
- **SC-003**: Score calculation and result availability complete within 10 seconds of exam submission.
- **SC-004**: Auto-pass flag changes take effect within 1 second of being toggled, without a service restart.
- **SC-005**: No exam allows more questions than `subject.total_questions`.

---

## Assumptions

- Questions are selected randomly from the subject's question bank; no weighting or adaptive logic is applied.
- The final score range is 56–189 (maximum); the minimum passing threshold is 56.4.
- Only one active exam is allowed per applicant at a time.
- The auto-pass override generates a uniform random value strictly greater than 56 and at most 70.
- Auto-pass and auto-accept are independent flags; toggling one does not affect the other.
