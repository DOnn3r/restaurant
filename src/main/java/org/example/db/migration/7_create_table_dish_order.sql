CREATE TABLE dish_order (
                            id SERIAL PRIMARY KEY,
                            order_id INT NOT NULL,
                            dish_id INT NOT NULL,
                            dish_status status NOT NULL,
                            status_change TIMESTAMP NOT NULL,
                            CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES "order"(id) ON DELETE CASCADE,
                            CONSTRAINT fk_dish FOREIGN KEY (dish_id) REFERENCES dish(id)
);
