update users
set phone_number = coalesce(phone_number, username)
where phone_number is null;

alter table users drop constraint if exists users_identifier_check;
alter table users drop column if exists username;
alter table users rename column phone_number to username;

alter table users alter column username set not null;
