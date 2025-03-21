do
$$
begin
        if not exists(select from pg_type where typname = 'mouvement') then
create type "mouvement" as enum ('IN', 'OUT');
end if;
end
$$;