DROP TABLE IF EXISTS contracts;

CREATE TABLE contracts
(
    id              UUID             NOT NULL,
    exam_session_id UUID             NOT NULL,
    scale           DOUBLE PRECISION NOT NULL,
    contract_url    VARCHAR(255),
    created_at      TIMESTAMP WITHOUT TIME ZONE,
    updated_at      TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_contracts PRIMARY KEY (id),
    CONSTRAINT uc_contracts_exam_session UNIQUE (exam_session_id),
    CONSTRAINT fk_contracts_on_exam_session FOREIGN KEY (exam_session_id) REFERENCES exam_sessions (id)
);
