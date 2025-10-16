CREATE TABLE yern.users (
    "id" bigserial,
    "email" text unique not null,
    "first_name" varchar(20) NOT NULL,
    "last_name" varchar(20) NOT NULL,
    "audit_timestamps" audit_timestamps
);