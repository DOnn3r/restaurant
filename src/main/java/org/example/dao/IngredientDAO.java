package org.example.dao;

import org.example.db.DataSource;
import org.example.entity.Criteria;
import org.example.entity.Ingredient;
import org.example.entity.Unity;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IngredientDAO implements CrudOperation<Ingredient> {
    private DataSource dataSource;

    public IngredientDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public IngredientDAO(){
        this.dataSource = new DataSource();
    };

    @Override
    public List<Ingredient> getAll() {
        String sql = "select i.id, i.name, i.last_modification, i.unit_price, i.unity from ingredient i     ";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Ingredient> ingredients = new ArrayList<>();
                while (resultSet.next() == true) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(resultSet.getInt("id"));
                    ingredient.setName(resultSet.getString("name"));
                    ingredient.setUnitPrice(resultSet.getDouble("unit_price"));
                    ingredient.setLastModification(resultSet.getTimestamp("last_modification").toLocalDateTime());
                    ingredient.setUnity(Unity.valueOf(resultSet.getString("unity")));
                    ingredients.add(ingredient);
                }
                return ingredients;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Ingredient> saveAll(List<Ingredient> entities) {
        List<Ingredient> ingredients = new ArrayList<>();
        try(Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()){
            for (Ingredient entityToSave : entities) {
                try {
                    String sql = statement.executeQuery(
                            "insert into ingredient values ('" + entityToSave.getId() + "',"
                                    + " '" + entityToSave.getName() + "',"
                                    + "'" + entityToSave.getLastModification().toLocalDate() + "',"
                                    + "'" + entityToSave.getUnitPrice() + "',"
                                    + "'" + entityToSave.getUnity() + "')")
                            + "on conflict (id) do update set name = excluded.name, last_modification = excluded.last_modification, unit_price = exluded.unit_price, unity= excluded.unity;";
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                ingredients.add(findById(entityToSave.getId()));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ingredients;
    }

    @Override
    public Ingredient findByName(String name) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("select i.id, i.name, i.last_modification, i.unit_price, i.unity from ingredient i where name = '" + name + "'")) {
                Ingredient ingredient = new Ingredient();
                while (resultSet.next()) {
                    ingredient.setId(resultSet.getInt("id"));
                    ingredient.setName(resultSet.getString("name"));
                    ingredient.setUnitPrice(resultSet.getDouble("price"));
                }
                return ingredient;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Ingredient findById(int id) throws SQLException {
        String sql = "SELECT * FROM Ingredient WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Ingredient(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getTimestamp("last_modified").toLocalDateTime(),
                            rs.getDouble("price"),
                            Unity.valueOf(rs.getString("unity"))
                    );
                }
            }
        }
        return null;
    }

    public List<Ingredient> findByDishId(int dishId) throws SQLException {
        String sql = "SELECT i.* FROM Ingredient i " +
                "JOIN Dish_Ingredient di ON i.id = di.ingredient_id " +
                "WHERE di.dish_id = ?";
        List<Ingredient> ingredients = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dishId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ingredients.add(new Ingredient(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getTimestamp("last_modified").toLocalDateTime(),
                            rs.getDouble("price"),
                            Unity.valueOf(rs.getString("unity"))
                    ));
                }
            }
        }
        return ingredients;
    }

    public List<Ingredient> filterAndSortIngredients(List<Criteria> criteria, int page, int size) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM ingredient WHERE 1=1");
        List<Object> parameters = new ArrayList<>();


        for (Criteria c : criteria) {
            switch (c.getField()) {
                case "name":
                    sql.append(" AND name ILIKE ?");
                    parameters.add("%" + c.getValue() + "%");
                    break;
                case "unity":
                    sql.append(" AND unity = ?");
                    parameters.add(c.getValue());
                    break;
                case "unit_price":
                    sql.append(" AND unit_price BETWEEN ? AND ?");
                    parameters.add(c.getMinValue());
                    parameters.add(c.getMaxValue());
                    break;
                case "last_modification":
                    sql.append(" AND last_modification BETWEEN ? AND ?");
                    parameters.add(c.getMinDate());
                    parameters.add(c.getMaxDate());
                    break;
            }
        }

        sql.append(" ORDER BY name ASC LIMIT ? OFFSET ?");

        try (Connection conn= dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            for (Object param : parameters) {
                if (param instanceof String) {
                    stmt.setString(index++, (String) param);
                } else if (param instanceof Double) {
                    stmt.setDouble(index++, (Double) param);
                } else if (param instanceof LocalDateTime) {
                    stmt.setTimestamp(index++, Timestamp.valueOf((LocalDateTime) param));
                }
            }

            stmt.setInt(index++, size);  // Limite
            stmt.setInt(index++, page * size);  // Offset pour la pagination

            try (ResultSet rs = stmt.executeQuery()) {
                List<Ingredient> ingredients = new ArrayList<>();
                while (rs.next()) {
                    Ingredient ingredient = new Ingredient(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getTimestamp("last_modification").toLocalDateTime(),
                            rs.getDouble("unit_price"),
                            Unity.valueOf(rs.getString("unity"))
                    );
                    ingredients.add(ingredient);
                }
                return ingredients;
            }
        }
    }
}
