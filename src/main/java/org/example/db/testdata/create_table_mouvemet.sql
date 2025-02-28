CREATE TABLE stock_mouvement (
                                 id SERIAL PRIMARY KEY,
                                 mouvement_type mouvement NOT NULL,
                                 unity unit NOT NULL,
                                 mouvement_date DATE NOT NULL
);