select create_enum('resource_type', '{FILE}');
select create_enum('role_type', '{OWNER, EDITOR, READER}');
select create_enum('permission', '{OWN, AUTHORIZE, DELETE, UPDATE, READ}');