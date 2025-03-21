INSERT INTO stock_mouvement (ingredient_id, mouvement_type, quantity, unity, mouvement_date)
VALUES (3, 'IN', 100, 'U', '2025-02-01 08:00:00');

INSERT INTO stock_mouvement (ingredient_id, mouvement_type, quantity, unity, mouvement_date)
VALUES (4, 'IN', 50, 'U', '2025-02-01 08:00:00');

INSERT INTO stock_mouvement (ingredient_id, mouvement_type, quantity, unity, mouvement_date)
VALUES (1, 'IN', 10000, 'G', '2025-02-01 08:00:00');

INSERT INTO stock_mouvement (ingredient_id, mouvement_type, quantity, unity, mouvement_date)
VALUES (2, 'IN', 20, 'L', '2025-02-01 08:00:00');

INSERT INTO stock_mouvement (id, ingredient_id, mouvement_type, quantity, unity, mouvement_date) VALUES
                                                                                                     (5, 3, 'OUT', 10, 'U', '2025-02-02 10:00:00'),
                                                                                                     (6, 3, 'OUT', 10, 'U', '2025-02-03 15:00:00'),
                                                                                                     (7, 4, 'OUT', 20, 'U', '2025-02-05 16:00:00');