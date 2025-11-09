-- CREATE DEFAULT ROLES
with role_types as (
	select 
		unnest(
			enum_range(NULL::yern.role_type)
		) AS role_enum_val
),
resources as (
	select 
		unnest(
			enum_range(NULL::yern.resource_type)
		) AS resource_enum_val
)
insert into yern.roles(
	resource, 
	"type", 
	display_name
)
select 
	rs.resource_enum_val as resource,
	rt.role_enum_val as type, 
	concat_ws(
		' ',
		initcap(rs.resource_enum_val::text), 
		initcap(rt.role_enum_val::text)
	) as display_name
from 
	resources rs
cross join 
	role_types rt
where 
	not exists(
		select 
			1
		from 
			yern.roles r 
		where 
			r.type = rt.role_enum_val
		and 
			r.resource = rs.resource_enum_val
	);

-- ADD DEFAULT PERMISSIONS TO THOSE ROLES 
-- JUST DOING THIS FOR NOW, DEFINITELY WILL CHANGE 
-- TODO: this is bad rn, need to find a way to connect this and 
--       the business rules in the app
with initial_roles_permissions(role_type, permissions) as (
	values 
		('OWNER', '{OWN,AUTHORIZE,DELETE,UPDATE,READ}'::yern.permission[]),
		('EDITOR', '{UPDATE,READ}'::yern.permission[]),
		('READER', '{READ}'::yern.permission[])
)
insert into yern.roles_permissions(
	role_id,
	permission
)
select 
	r.id as role_id,
	unnest(irp.permissions) as permission 
from 
	initial_roles_permissions irp 
join 
	yern.roles r 
on 
	irp.role_type::yern.role_type = r.type
where 
	not exists (
		select 
			1 
		from 	
			yern.roles_permissions rp 
		where 
			rp.role_id = r.id
		and 
			rp.permission = any(irp.permissions)
	)
