CREATE TABLE ingredient_price (
                                  id INT PRIMARY KEY,
                                  price int NOT NULL,
                                  date DATE NOT NULL,
                                  ingredient_id INT NOT NULL,
                                  FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
);