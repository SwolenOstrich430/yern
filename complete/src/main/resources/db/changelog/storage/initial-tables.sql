DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'yern.storage_provider') THEN
        CREATE TYPE storage_provider AS ENUM ('GCS');
    END IF;
END
$$;

create table if not exists yern.files (
    "id" bigserial primary key,
    "storage_provider" yern.storage_provider NOT NULL,
    "raw_path" text not null,
    "formatted_path" text,
    "public_url" text,
    "etag" text,
    "error" jsonb,
    "created_at" timestamp not null default cast(
        now() as timestamp without time zone
    ),
    "updated_at" timestamp not null default cast(
        now() as timestamp without time zone
    )
);