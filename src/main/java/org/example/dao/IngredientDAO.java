package org.example.dao;

import org.example.db.DataSource;
import org.example.entity.*;

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
        List<Ingredient> savedIngredients = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            // Désactiver l'auto-commit pour gérer les transactions manuellement
            connection.setAutoCommit(false);

            // Sauvegarder chaque ingrédient
            for (Ingredient entityToSave : entities) {
                // Sauvegarder l'ingrédient dans la table `ingredient`
                String sql = "INSERT INTO ingredient (id, name, last_modification, unit_price, unity) " +
                        "VALUES (?, ?, ?, ?, ?) " +
                        "ON CONFLICT (id) DO UPDATE SET " +
                        "name = EXCLUDED.name, " +
                        "last_modification = EXCLUDED.last_modification, " +
                        "unit_price = EXCLUDED.unit_price, " +
                        "unity = EXCLUDED.unity;";

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, entityToSave.getId());
                    statement.setString(2, entityToSave.getName());
                    statement.setTimestamp(3, Timestamp.valueOf(entityToSave.getLastModification()));
                    statement.setDouble(4, entityToSave.getUnitPrice());
                    statement.setString(5, entityToSave.getUnity().toString());
                    statement.executeUpdate();
                }

                // Sauvegarder les prix historiques dans la table `ingredient_price`
                saveHistoricalPrices(connection, entityToSave.getId(), entityToSave.getHistoricalPrices());

                // Sauvegarder les mouvements de stock dans la table `mouvement_stock`
                saveStockMouvements(connection, entityToSave.getId(), entityToSave.getStockMouvements());

                // Ajouter l'ingrédient sauvegardé à la liste
                savedIngredients.add(findById(entityToSave.getId()));
            }

            // Valider la transaction
            connection.commit();
        } catch (SQLException e) {
            // En cas d'erreur, annuler la transaction
            try (Connection connection = dataSource.getConnection()) {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Erreur lors de l'annulation de la transaction", ex);
            }
            throw new RuntimeException("Erreur lors de la sauvegarde des ingrédients", e);
        }
        return savedIngredients;
    }

    private void saveHistoricalPrices(Connection connection, int ingredientId, List<IngredientPrice> historicalPrices) throws SQLException {
        String sql = "INSERT INTO ingredient_price (id, price, date, ingredient_id) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "price = EXCLUDED.price, " +
                "date = EXCLUDED.date, " +
                "ingredient_id = EXCLUDED.ingredient_id;";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (IngredientPrice price : historicalPrices) {
                statement.setInt(1, price.getId());
                statement.setDouble(2, price.getPrice());
                statement.setDate(3, Date.valueOf(price.getDate()));
                statement.setInt(4, ingredientId);
                statement.addBatch(); // Ajouter à la batch pour exécution groupée
            }
            statement.executeBatch(); // Exécuter tous les inserts en une seule fois
        }
    }

    private void saveStockMouvements(Connection connection, int ingredientId, List<StockMouvement> stockMouvements) throws SQLException {
        String sql = "INSERT INTO mouvement_stock (id, ingredient_id, mouvement_type, quantity, unity, mouvement_date) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "ingredient_id = EXCLUDED.ingredient_id, " +
                "mouvement_type = EXCLUDED.mouvement_type, " +
                "quantity = EXCLUDED.quantity, " +
                "unity = EXCLUDED.unity, " +
                "mouvement_date = EXCLUDED.mouvement_date;";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (StockMouvement mouvement : stockMouvements) {
                statement.setInt(1, mouvement.getId());
                statement.setInt(2, ingredientId);
                statement.setString(3, mouvement.getMouvementType().toString());
                statement.setDouble(4, mouvement.getQuantity());
                statement.setString(5, mouvement.getUnity().toString());
                statement.setTimestamp(6, Timestamp.valueOf(mouvement.getMouvementDate()));
                statement.addBatch(); // Ajouter à la batch pour exécution groupée
            }
            statement.executeBatch(); // Exécuter tous les inserts en une seule fois
        }
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
