CREATE TABLE Dish_Ingredient (
                                 dish_id INT NOT NULL,
                                 ingredient_id INT NOT NULL,
                                 required_quantity numeric(10,2)
                                 PRIMARY KEY (dish_id, ingredient_id),
                                 CONSTRAINT fk_dish FOREIGN KEY (dish_id) REFERENCES Dish(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_ingredient FOREIGN KEY (ingredient_id) REFERENCES Ingredient(id) ON DELETE CASCADE
);

INSERT INTO Dish_Ingredient (dish_id, ingredient_id, required_quantity)
VALUES
    (1, (SELECT id FROM Ingredient WHERE name = 'Saucisse'),100),
    (1, (SELECT id FROM Ingredient WHERE name = 'Huile'), 0.15),
    (1, (SELECT id FROM Ingredient WHERE name = 'Oeuf'), 1),
    (1, (SELECT id xFROM Ingredient WHERE name = 'Pain'), 1);