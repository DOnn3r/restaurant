CREATE TABLE stock_mouvement (
                                 id SERIAL PRIMARY KEY,
                                 ingredient_id INT NOT NULL,
                                 mouvement_type mouvement NOT NULL,
                                 quantity DOUBLE PRECISION NOT NULL,
                                 unity unit NOT NULL,
                                 mouvement_date TIMESTAMP NOT NULL,
                                 FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
);

-- Oeuf, 100 unités
INSERT INTO stock_mouvement (ingredient_id, mouvement_type, quantity, unity, mouvement_date)
VALUES (3, 'IN', 100, 'U', '2025-02-01 08:00:00');

-- Pain, 50 unités
INSERT INTO stock_mouvement (ingredient_id, mouvement_type, quantity, unity, mouvement_date)
VALUES (4, 'IN', 50, 'U', '2025-02-01 08:00:00');

-- Saucisse, 10 kg (10 000 g)
INSERT INTO stock_mouvement (ingredient_id, mouvement_type, quantity, unity, mouvement_date)
VALUES (1, 'IN', 10000, 'G', '2025-02-01 08:00:00');

-- Huile, 20 L
INSERT INTO stock_mouvement (ingredient_id, mouvement_type, quantity, unity, mouvement_date)
VALUES (2, 'IN', 20, 'L', '2025-02-01 08:00:00');

INSERT INTO stock_mouvement (id, ingredient_id, mouvement_type, quantity, unity, mouvement_date) VALUES
                                                                                                     (5, 3, 'OUT', 10, 'U', '2025-02-02 10:00:00'),
                                                                                                     (6, 3, 'OUT', 10, 'U', '2025-02-03 15:00:00'),
                                                                                                     (7, 4, 'OUT', 20, 'U', '2025-02-05 16:00:00');