package org.example.dao;

import org.example.entity.StockMouvement;
import org.example.entity.MouvementType;
import org.example.entity.Unity;
import org.example.db.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StockMouvementDAO {

    DataSource dataSource = new DataSource();

    // Méthode pour récupérer tous les mouvements de stock
    public List<StockMouvement> getAllStockMouvements() {
        List<StockMouvement> stockMouvements = new ArrayList<>();
        String query = "SELECT * FROM stock_mouvement";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                StockMouvement stockMouvement = mapResultSetToStockMouvement(rs);
                stockMouvements.add(stockMouvement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stockMouvements;
    }

    // Méthode pour récupérer les mouvements de stock par ingrédient
    public List<StockMouvement> getStockMouvementsByIngredient(int ingredientId) {
        List<StockMouvement> stockMouvements = new ArrayList<>();
        String query = "SELECT * FROM stock_mouvement WHERE ingredient_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, ingredientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StockMouvement stockMouvement = mapResultSetToStockMouvement(rs);
                stockMouvements.add(stockMouvement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stockMouvements;
    }

    // Méthode pour mapper un ResultSet à un objet StockMouvement
    private StockMouvement mapResultSetToStockMouvement(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int ingredientId = rs.getInt("ingredient_id");
        MouvementType mouvementType = MouvementType.valueOf(rs.getString("mouvement_type"));
        double quantity = rs.getDouble("quantity");
        Unity unity = Unity.valueOf(rs.getString("unity"));
        LocalDateTime mouvementDate = rs.getTimestamp("mouvement_date").toLocalDateTime();

        return new StockMouvement(id, ingredientId, mouvementType, quantity, unity, mouvementDate);
    }
}