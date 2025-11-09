create table if not exists yern.files (
    "id" bigserial primary key,
    "storage_provider" yern.storage_provider not null,
    "raw_path" text not null,
    "formatted_path" text,
    "public_url" text,
    "etag" text,
    "error" jsonb,
    "original_name" text not null,
    "created_at" timestamp without time zone,
    "updated_at" timestamp without time zone 
);

create table if not exists yern.file_access_control (
    "id" bigserial primary key,
    "user_id" bigint not null references users(id) on delete cascade,
    "file_id" bigint not null references files(id) on delete cascade,
    "role_id" bigint not null references roles(id) on delete cascade,
    "created_at" timestamp without time zone,
    "updated_at" timestamp without time zone 
);