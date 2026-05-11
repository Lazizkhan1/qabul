ALTER TABLE exam_questions
    DROP CONSTRAINT fk_exam_questions_on_exam;

ALTER TABLE tuition
    DROP CONSTRAINT fk_tuition_on_subject;

CREATE TABLE certificate_files
(
    id            UUID          NOT NULL,
    owner_user_id UUID          NOT NULL,
    category_id   INTEGER       NOT NULL,
    relative_path VARCHAR(1024) NOT NULL,
    original_name VARCHAR(255)  NOT NULL,
    content_type  VARCHAR(255)  NOT NULL,
    size_bytes    BIGINT        NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE,
    updated_at    TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_certificate_files PRIMARY KEY (id)
);

ALTER TABLE major_type
    ADD active INTEGER DEFAULT 1;

ALTER TABLE major_type
    ALTER COLUMN active SET NOT NULL;

ALTER TABLE certificate_files
    ADD CONSTRAINT FK_CERTIFICATE_FILES_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES cert_category (id);

ALTER TABLE certificate_files
    ADD CONSTRAINT FK_CERTIFICATE_FILES_ON_OWNER_USER FOREIGN KEY (owner_user_id) REFERENCES users (id);

ALTER TABLE exam_questions
    DROP COLUMN exam_id;

ALTER TABLE tuition
    DROP COLUMN subject_id;

ALTER TABLE cert_category
    DROP COLUMN type;

ALTER TABLE cert_category
    ADD type VARCHAR(255) NOT NULL;