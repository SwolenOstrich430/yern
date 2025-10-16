create table if not exists yern.roles (
    "id" bigserial primary key,
    "resource" yern.resource_type unique not null,
    "type" yern.role_type unique not null,
    "display_name" varchar(100) unique not null
);