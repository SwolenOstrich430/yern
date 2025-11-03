create or replace function create_enum(
    enum_name TEXT,
    enum_values TEXT[]
)
returns void AS $$
declare
    value_list text;
    single_value text;
begin
    -- Construct the value list for CREATE TYPE
    value_list := '';
    for i in 1..array_length(enum_values, 1) loop
        if i > 1 then
            value_list := value_list || ', ';
        end if;
        value_list := value_list || quote_literal(enum_values[i]);
    end loop;

    execute format('create type %I as enum (%s)', enum_name, value_list);
end;
$$ language plpgsql;