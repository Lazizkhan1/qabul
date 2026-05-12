# Full exam feature rewrite plan

## Problem summary

Current exam code is CRUD-only and not suitable for required behavior.  
The implementation will be rebuilt from scratch for exam flow (while keeping authentication and application modules
intact).

## Scope confirmed from your update

- Rewrite exam feature from scratch.
- No test implementation for now.
- Include Postman collection updates for all new/replaced endpoints.
- Session endpoints are applicant-only.
- One exam session total per application (no retakes).
- DOCX imports assume option **A** is always the correct answer.

## Proposed rewrite approach

Replace current exam domain endpoints/services with a session-driven exam lifecycle:

1. question bank import (DOCX),
2. exam session start and question delivery,
3. session completion with score response,
4. automatic completion of expired sessions.

## Planned work items

1. **Schema and model rewrite**
    - Add Flyway migration(s) to introduce/adjust:
        - `exam_subjects` join table for `Exam <-> Subject` many-to-many.
        - dedicated exam session tables (separate from auth `sessions`):
            - `exam_sessions`
            - `exam_session_answers`
        - required indexes/constraints for one active session per application and fast expired-session scanning.
    - Refactor entities:
        - `Exam` with `subjects` many-to-many relation.
        - new `ExamSession` + `ExamSessionAnswer` + status enum.
    - Refactor repositories to support session lifecycle queries.

2. **Exam duration from environment**
    - Add exam configuration binding for env `EXAM_DURATION` (minutes).
    - Use this value to set `expires_at` when session starts.
    - Wire into `application.yaml` with sane default fallback.

3. **DOCX upload/import for question creation**
    - Add admin endpoint for multipart DOCX upload (new exam management API).
    - Add Apache POI dependency for `.docx` parsing.
    - Parse strict text format:
        - one question text
        - exactly 4 answers
        - option A is always stored as the correct answer
    - Persist question + answers transactionally and return import summary.
    - Add sample `.docx` template file in repository.

4. **Exam session endpoints (for specific application)**
    - Start/create session endpoint for a target application.
    - Get questions endpoint for that active session/application.
    - Complete session endpoint:
        - accept submitted answer selections
        - compute achieved score
        - finalize session and exam status
        - return score/result payload.
    - Enforce one-session-only policy per application (no retake creation).

5. **Expired session scheduler**
    - Enable scheduling in app startup.
    - Add periodic scheduled job (minute-level) to:
        - find expired ongoing sessions
        - finalize them with submitted answers (or zero where missing)
        - persist final score and terminal status idempotently.

6. **Controller/service cleanup and replacement**
    - Replace current exam CRUD-focused controller/service paths with rewritten flow endpoints.
    - Keep role-based access via `@PreAuthorize`.
    - Restrict session lifecycle endpoints to `ROLE_APPLICANT`.
    - Keep error contract via existing `AuthException`/`GlobalExceptionHandler`.

7. **Postman collection update**
    - Update `Qabul_API.postman_collection.json`:
        - remove/mark deprecated old exam CRUD requests
        - add new management DOCX import request
        - add start/get/complete exam session requests
        - include example payloads and required auth headers.

## Risks / considerations

- Existing migrations already modified exam schema in multiple steps; new migration must account for current DB state
  safely.
- Rewrite should avoid coupling with auth `sessions` table.
- Score calculation and completion transitions must be deterministic and idempotent for scheduler retries.

## Confirmed product decisions

1. Only applicants can start and complete exam sessions.
2. Each application can have only one exam session total.
3. In DOCX imports, answer option A is always treated as correct.
