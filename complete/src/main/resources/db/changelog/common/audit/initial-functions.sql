create or replace function create_audit_timestamp()
returns trigger as $$
declare 
    curr_time timestamp without time zone;
begin
    curr_time := now()::timestamp;
    new.audit_timestamps = row(curr_time, curr_time);
    
    return new;
end;
$$ language plpgsql;

create or replace function update_audit_timestamp()
returns trigger as $$
begin
    new.audit_timestamps.updated_at = now()::timestamp;
    
    return new;
end;
$$ language plpgsql;

create or replace function set_create_audit_timestamp_trigger(
    schema_name text,
    table_name text
)
returns void as $$
begin 
    execute format(
        'create or replace trigger %s_create_audit_timestamp_trigger
        before insert on %s.%s
        for each row
        execute function create_audit_timestamp();',
        table_name,
        schema_name,
		table_name
    );
end;
$$ language plpgsql;

create or replace function set_update_audit_timestamp_trigger(
    schema_name text,
    table_name text
)
returns void as $$
begin 
    execute format(
        'create or replace trigger %s_update_audit_timestamp_trigger
        before update on %s.%s
        for each row
        execute function update_audit_timestamp();',
        table_name,
        schema_name,
		table_name
    );
end;
$$ language plpgsql;

create or replace function set_audit_timestamp_triggers(
    schema_name text,
    table_name text
)
returns void as $$
begin 
    perform set_create_audit_timestamp_trigger(schema_name, table_name);
    perform set_update_audit_timestamp_trigger(schema_name, table_name);
end;
$$ language plpgsql;

create or replace function set_all_audit_timestamp_triggers(
    schema_name text
)
returns void as $$
declare 
	result record;
begin 
    for result in (
        select 
            table_name
        from 
            information_schema.columns 
        where 
            table_schema = schema_name
        and 
            udt_name = 'audit_timestamps'
    ) loop
        perform set_audit_timestamp_triggers(
			schema_name, result.table_name
		);
    end loop;
end;
$$ language plpgsql;