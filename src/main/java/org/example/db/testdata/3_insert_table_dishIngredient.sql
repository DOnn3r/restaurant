INSERT INTO Dish_Ingredient (dish_id, ingredient_id, required_quantity)
VALUES
    (1, (SELECT id FROM Ingredient WHERE name = 'Saucisse'),100),
    (1, (SELECT id FROM Ingredient WHERE name = 'Huile'), 0.15),
    (1, (SELECT id FROM Ingredient WHERE name = 'Oeuf'), 1),
    (1, (SELECT id xFROM Ingredient WHERE name = 'Pain'), 1);