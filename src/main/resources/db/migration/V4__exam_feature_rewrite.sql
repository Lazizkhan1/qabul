CREATE TABLE exam_subjects
(
    exam_id    UUID    NOT NULL,
    subject_id INTEGER NOT NULL,
    CONSTRAINT pk_exam_subjects PRIMARY KEY (exam_id, subject_id),
    CONSTRAINT fk_exam_subjects_on_exam FOREIGN KEY (exam_id) REFERENCES exams (id),
    CONSTRAINT fk_exam_subjects_on_subject FOREIGN KEY (subject_id) REFERENCES subjects (id)
);

CREATE TABLE exam_sessions
(
    id             UUID                        NOT NULL,
    application_id UUID                        NOT NULL,
    status         VARCHAR(255)                NOT NULL,
    score          DOUBLE PRECISION,
    expires_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    completed_at   TIMESTAMP WITHOUT TIME ZONE,
    created_at     TIMESTAMP WITHOUT TIME ZONE,
    updated_at     TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_exam_sessions PRIMARY KEY (id),
    CONSTRAINT uc_exam_sessions_application UNIQUE (application_id),
    CONSTRAINT fk_exam_sessions_on_application FOREIGN KEY (application_id) REFERENCES applications (id)
);

CREATE TABLE exam_session_answers
(
    id          UUID    NOT NULL,
    session_id  UUID    NOT NULL,
    question_id INTEGER NOT NULL,
    answer_id   INTEGER,
    is_correct  BOOLEAN,
    CONSTRAINT pk_exam_session_answers PRIMARY KEY (id),
    CONSTRAINT fk_exam_session_answers_on_session FOREIGN KEY (session_id) REFERENCES exam_sessions (id),
    CONSTRAINT fk_exam_session_answers_on_question FOREIGN KEY (question_id) REFERENCES exam_questions (id),
    CONSTRAINT fk_exam_session_answers_on_answer FOREIGN KEY (answer_id) REFERENCES exam_answers (id)
);
