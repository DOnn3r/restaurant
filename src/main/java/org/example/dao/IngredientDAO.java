package org.example.dao;

import org.example.db.DataSource;
import org.example.entity.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IngredientDAO implements CrudOperation<Ingredient> {
    private DataSource dataSource;
    private StockMouvementDAO stockMouvementDAO = new StockMouvementDAO();

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
            connection.setAutoCommit(false);
            for (Ingredient entityToSave : entities) {
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
                saveHistoricalPrices(connection, entityToSave.getId(), entityToSave.getHistoricalPrices());

                saveStockMouvements(connection, entityToSave.getId(), entityToSave.getStockMouvements());

                savedIngredients.add(findById(entityToSave.getId()));
            }

            connection.commit();
        } catch (SQLException e) {
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

    public void saveStockMouvements(Connection connection, int ingredientId, List<StockMouvement> stockMouvements) throws SQLException {
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

    public Ingredient findById(int id) {
        String sql = "SELECT * FROM ingredient WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Créer l'ingrédient avec les informations de base
                    Ingredient ingredient = new Ingredient(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getTimestamp("last_modification").toLocalDateTime(),
                            resultSet.getDouble("unit_price"),
                            Unity.valueOf(resultSet.getString("unity"))
                    );

                    // Charger l'historique des prix
                    List<IngredientPrice> historicalPrices = loadHistoricalPrices(connection, ingredient.getId());
                    ingredient.setHistoricalPrices(historicalPrices);

                    // Charger les mouvements de stock en utilisant StockMouvementDAO
                    List<StockMouvement> stockMouvements = stockMouvementDAO.getStockMouvementsByIngredient(ingredient.getId());
                    ingredient.setStockMouvements(stockMouvements);

                    return ingredient;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'ingrédient par nom", e);
        }
        return null; // Retourner null si aucun ingrédient n'est trouvé
    }

    private List<IngredientPrice> loadHistoricalPrices(Connection connection, int ingredientId) throws SQLException {
        String sql = "SELECT * FROM ingredient_price WHERE ingredient_id = ? ORDER BY date ASC";
        List<IngredientPrice> historicalPrices = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ingredientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    historicalPrices.add(new IngredientPrice(
                            rs.getInt("id"),
                            rs.getDouble("price"),
                            rs.getDate("date").toLocalDate()
                    ));
                }
            }
        }

        return historicalPrices;
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
