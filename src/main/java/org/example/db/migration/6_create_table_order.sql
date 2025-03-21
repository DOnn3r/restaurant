create table "order" (
    id serial primary key,
    dish_id int not null,
    dish_status status not null,
    status_change timestamp not null,
    constraint fk_dish foreign key (dish_id) references dish(id)
);