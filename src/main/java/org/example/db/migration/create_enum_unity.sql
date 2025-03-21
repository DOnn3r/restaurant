do
$$
    begin
        if not exists(select from pg_type where typname = 'unity') then
            create type "unity" as enum ('G','L','U');
        end if;
    end
$$;