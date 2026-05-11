ALTER TABLE exam_questions
    ADD major_lang_id INTEGER;

ALTER TABLE major
    ADD subject1_id INTEGER;

ALTER TABLE major
    ADD subject2_id INTEGER;

ALTER TABLE major
    ADD subject3_id INTEGER;

ALTER TABLE exam_questions
    ADD CONSTRAINT FK_EXAM_QUESTIONS_ON_MAJOR_LANG FOREIGN KEY (major_lang_id) REFERENCES major_lang (id);

ALTER TABLE major
    ADD CONSTRAINT FK_MAJOR_ON_SUBJECT1 FOREIGN KEY (subject1_id) REFERENCES subjects (id);

ALTER TABLE major
    ADD CONSTRAINT FK_MAJOR_ON_SUBJECT2 FOREIGN KEY (subject2_id) REFERENCES subjects (id);

ALTER TABLE major
    ADD CONSTRAINT FK_MAJOR_ON_SUBJECT3 FOREIGN KEY (subject3_id) REFERENCES subjects (id);

ALTER TABLE tuition
    DROP COLUMN amount;

ALTER TABLE tuition
    ADD amount BIGINT NOT NULL;

ALTER TABLE certs
    DROP COLUMN score;

ALTER TABLE certs
    ADD score VARCHAR(255) NOT NULL;