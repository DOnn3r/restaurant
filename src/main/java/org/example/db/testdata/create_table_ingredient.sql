    create table ingredient(
        id int primary key,
        name varchar,
        last_modification timestamp,
        unit_price decimal,
        unity unit
    );

INSERT INTO Ingredient
VALUES
    (1,'Saucisse','2025-01-01 00:00', 20 ,'G'),
    (2,'Huile', '2025-01-01 00:00 ', 10000, 'L'),
    (3,'Oeuf', '2025-01-01 00:00 ',1000,'U'),
    (4,'Pain','2025-01-01 00:00 ',1000, 'U');