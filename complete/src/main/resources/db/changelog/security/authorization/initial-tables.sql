create table if not exists yern.roles (
    "id" bigserial primary key,
    "resource" yern.resource_type unique not null,
    "type" yern.role_type unique not null,
    "display_name" varchar(100) unique not null,
    "audit_timestamps" audit_timestamps
);

create table if not exists yern.roles_permissions (
    "role_id" bigint not null,
    "permission" yern.permission not null,
    "audit_timestamps" audit_timestamps,
    primary key("role_id", "permission")
);

create table if not exists yern.user_roles (
    "user_id" bigint not null,
    "role_id" bigint not null,
    "audit_timestamps" audit_timestamps
);