DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'yern.storage_provider') THEN
        CREATE TYPE storage_provider AS ENUM ('GCS');
    END IF;
END
$$;