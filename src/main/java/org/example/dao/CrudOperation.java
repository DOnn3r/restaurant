package org.example.dao;

import org.example.entity.Dish;
import org.example.entity.Ingredient;

import java.sql.SQLException;
import java.util.List;

public interface CrudOperation<E> {
    List<E> getAll() throws SQLException;

    List<E> saveAll(List<E> entities) throws SQLException;

    E findByName(String name) throws SQLException;
}
