create table users
(
    id            uuid primary key,
    phone_number  varchar(20) unique,
    username      varchar(100) unique,
    password_hash varchar(255) not null,
    type          varchar(30)  not null,
    lang          varchar(10)  not null default 'UZ',
    created_at    timestamp,
    updated_at    timestamp,
    constraint users_identifier_check check (phone_number is not null or username is not null)
);

create table sessions
(
    id         uuid primary key,
    user_id    uuid      not null references users (id),
    token      text      not null,
    status     varchar(30) not null,
    user_agent varchar(500),
    expired_at timestamp not null,
    created_at timestamp,
    updated_at timestamp
);

create index idx_sessions_token_status on sessions (status);
create index idx_sessions_user_status on sessions (user_id, status);

create table otp_challenges
(
    id           uuid primary key,
    phone_number varchar(20)  not null,
    otp_hash     varchar(255) not null,
    status       varchar(30)  not null,
    expires_at   timestamp    not null,
    verified_at  timestamp,
    created_at   timestamp,
    updated_at   timestamp
);

create index idx_otp_challenges_phone_status on otp_challenges (phone_number, status);
