create table yern.user_authentications (
  "id" bigserial primary key,
  "user_id" bigint not null references users(id) on delete cascade,
  "username" text not null,
  "password" text not null
);