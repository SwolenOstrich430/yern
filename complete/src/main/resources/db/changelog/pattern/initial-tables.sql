create table if not exists yern.patterns(
    "id" bigserial primary key,
    "name" varchar(100) not null,
    "description" varchar(255) not null,
    "created_at" timestamp without time zone,
    "updated_at" timestamp without time zone 
);

create table if not exists yern.users_patterns (
  "user_id" bigint not null references users(id),
  "pattern_id" bigint not null references patterns(id),
  PRIMARY KEY ("user_id", "pattern_id")
);

create table if not exists yern.counters(
    "id" bigserial primary key,
    "value" bigint not null default 0,
    "is_dirty" bool not null default false,
    "last_reset_at" timestamp,
    "created_at" timestamp without time zone,
    "updated_at" timestamp without time zone 
);

create table if not exists yern.counter_logs(
    "id" uuid primary key default gen_random_uuid(),
    "counter_id" bigint not null,
    "external_id" text unique not null,
    "value" integer not null,
    "created_at" timestamp not null default now()
);

create table if not exists yern.sections(
    "id" bigserial primary key,
    "pattern_id" 
        bigint not null 
        references yern.patterns(id) 
        on delete cascade,
    "name" varchar(100) not null,
    "description" varchar(255),
    "notes" text,
    "sequence" integer not null,
    "file_id" 
        bigint 
        references yern.files(id) 
        on delete cascade,
    "counter_id" 
        bigint 
        references yern.counters(id) 
        on delete cascade,
    "created_at" timestamp without time zone,
    "updated_at" timestamp without time zone 
);

create table if not exists yern.counters(
    "id" bigserial primary key,
    "value" bigint not null default 0,
    "is_dirty" boolean not null default false,
    "last_reset_at" timestamp without time zone,
    "created_at" timestamp without time zone,
    "updated_at" timestamp without time zone 
);