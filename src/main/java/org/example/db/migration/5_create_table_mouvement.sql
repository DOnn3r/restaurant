CREATE TABLE stock_mouvement (
                                 id SERIAL PRIMARY KEY,
                                 ingredient_id INT NOT NULL,
                                 mouvement_type mouvement NOT NULL,
                                 quantity DOUBLE PRECISION NOT NULL,
                                 unity unit NOT NULL,
                                 mouvement_date TIMESTAMP NOT NULL,
                                 FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
);