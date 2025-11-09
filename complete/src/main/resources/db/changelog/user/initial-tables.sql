CREATE TABLE yern.users (
    "id" bigserial primary key,
    "email" text unique not null,
    "first_name" varchar(20) NOT NULL,
    "last_name" varchar(20) NOT NULL,
    "created_at" timestamp without time zone,
"updated_at" timestamp without time zone 
);