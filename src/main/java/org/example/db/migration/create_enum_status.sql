do
$$
begin
        if not exists(select from pg_type where typname = 'status') then
create type "status" as enum ('CREATED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'SERVED');
end if;
end
$$;