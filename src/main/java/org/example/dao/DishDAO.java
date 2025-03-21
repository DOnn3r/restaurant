package org.example.dao;

import org.example.db.DataSource;
import org.example.entity.Dish;
import org.example.entity.DishIngredient;
import org.example.entity.Ingredient;
import org.example.entity.Unity;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DishDAO implements CrudOperation<Dish>{
    private DataSource dataSource;

    public DishDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DishDAO(){
        this.dataSource = new DataSource();
    };

    @Override
    public List<Dish> getAll(){
        String sql = "select d.id, d.name, d.price from dish d";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Dish> dishes = new ArrayList<>();
                while (resultSet.next() == true) {
                    Dish dish = new Dish();
                    dish.setId(resultSet.getInt("id"));
                    dish.setName(resultSet.getString("name"));
                    dish.setUnitPrice(resultSet.getDouble("price"));
                    dishes.add(dish);
                }
                return dishes;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public List<Dish> saveAll(List<Dish> entities) {
        String dishSql = "INSERT INTO dish (id, name, price) VALUES (?, ?, ?) ON CONFLICT (name) DO UPDATE SET price = EXCLUDED.price RETURNING id";
        String ingredientSql = "INSERT INTO ingredient (id, name, last_modification, unit_price, unity) VALUES (?, ?, ?, ?, ?) ON CONFLICT (name) DO NOTHING RETURNING id";
        String dishIngredientSql = "INSERT INTO dish_ingredient (dish_id, ingredient_id, required_quantity) VALUES (?, ?, ?) ON CONFLICT (dish_id, ingredient_id) DO UPDATE SET required_quantity = EXCLUDED.required_quantity";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement dishStmt = connection.prepareStatement(dishSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement ingredientStmt = connection.prepareStatement(ingredientSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement dishIngredientStmt = connection.prepareStatement(dishIngredientSql)) {

                for (Dish dish : entities) {
                    // Insert or update dish
                    dishStmt.setInt(1, dish.getId()); // Paramètre 1 : id (manuel)
                    dishStmt.setString(2, dish.getName()); // Paramètre 2 : name
                    dishStmt.setDouble(3, dish.getUnitPrice()); // Paramètre 3 : price
                    dishStmt.executeUpdate();

                    // Récupérer l'`id` généré (si applicable)
                    try (ResultSet rs = dishStmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            dish.setId(rs.getInt(1)); // Met à jour l'`id` de l'objet Dish
                        }
                    }

                    // Insert or update ingredients
                    for (DishIngredient dishIngredient : dish.getIngredients()) {
                        Ingredient ingredient = dishIngredient.getIngredient();
                        ingredientStmt.setInt(1, ingredient.getId()); // Paramètre 1 : id (manuel)
                        ingredientStmt.setString(2, ingredient.getName()); // Paramètre 2 : name
                        ingredientStmt.setTimestamp(3, Timestamp.valueOf(ingredient.getLastModification())); // Paramètre 3 : last_modification
                        ingredientStmt.setDouble(4, ingredient.getUnitPrice()); // Paramètre 4 : unit_price
                        ingredientStmt.setObject(5, ingredient.getUnity().name(), Types.OTHER); // Paramètre 5 : unity (ENUM)
                        ingredientStmt.executeUpdate();

                        // Récupérer l'`id` généré (si applicable)
                        int ingredientId = -1;
                        try (ResultSet rs = ingredientStmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                ingredientId = rs.getInt(1); // Met à jour l'`id` de l'objet Ingredient
                            }
                        }

                        // Si l'`id` n'a pas été généré (cas d'un conflit ON CONFLICT DO NOTHING)
                        if (ingredientId == -1) {
                            try (PreparedStatement findIngredientStmt = connection.prepareStatement("SELECT id FROM ingredient WHERE name = ?")) {
                                findIngredientStmt.setString(1, ingredient.getName());
                                try (ResultSet rs = findIngredientStmt.executeQuery()) {
                                    if (rs.next()) {
                                        ingredientId = rs.getInt("id"); // Récupère l'`id` existant
                                    }
                                }
                            }
                        }

                        // Insert or update dish_ingredient
                        if (ingredientId != -1) {
                            dishIngredientStmt.setInt(1, dish.getId()); // Paramètre 1 : dish_id
                            dishIngredientStmt.setInt(2, ingredientId); // Paramètre 2 : ingredient_id
                            dishIngredientStmt.setDouble(3, dishIngredient.getRequiredQuantity()); // Paramètre 3 : required_quantity
                            dishIngredientStmt.executeUpdate();
                        }
                    }
                }
                connection.commit();
                return entities;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Dish findByName(String name) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("select d.id, d.name, d.price from dish d where name ilike '%" + name + "%'")) {
                Dish dish = new Dish();
                while (resultSet.next()) {
                    dish.setId(resultSet.getInt("id"));
                    dish.setName(resultSet.getString("name"));
                    dish.setUnitPrice(resultSet.getDouble("price"));
                }
                return dish;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<DishIngredient> findIngredientsByDishId(int dishId) throws SQLException {
        String sql = "SELECT i.*, di.required_quantity, i.unity " +
                "FROM Ingredient i " +
                "JOIN Dish_Ingredient di ON i.id = di.ingredient_id " +
                "WHERE di.dish_id = ?";
        List<DishIngredient> dishIngredients = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dishId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Ingredient ingredient = new Ingredient(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getTimestamp("last_modification").toLocalDateTime(),
                            rs.getDouble("unit_price"),
                            Unity.valueOf(rs.getString("unity"))
                    );
                    double requiredQuantity = rs.getDouble("required_quantity");
                    Unity unity = Unity.valueOf(rs.getString("unity"));
                    DishIngredient dishIngredient = new DishIngredient(ingredient, requiredQuantity, unity);
                    dishIngredients.add(dishIngredient);
                }
            }
        }
        return dishIngredients;
    }

    public Dish getDishById(int dishId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("select d.id, d.name, d.price from dish where id = " + dishId)) {
                Dish dish = new Dish();
                while (resultSet.next()) {
                    dish.setId(resultSet.getInt("id"));
                    dish.setName(resultSet.getString("name"));
                    dish.setUnitPrice(resultSet.getDouble("price"));
                }
                return dish;
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Dish findByNameWithIngredients(String name) throws SQLException {
        String sql = "SELECT d.id, d.name, d.price " +
                "FROM dish d " +
                "WHERE d.name ILIKE ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, "%" + name + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Dish dish = new Dish();
                    dish.setId(rs.getInt("id"));
                    dish.setName(rs.getString("name"));
                    dish.setUnitPrice(rs.getDouble("price"));
                    dish.setIngredients(findIngredientsByDishId(dish.getId()));
                    return dish;
                }
            }
        }
        return null;
    }
}