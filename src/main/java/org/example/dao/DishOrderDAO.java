package org.example.dao;

import org.example.db.DataSource;
import org.example.entity.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DishOrderDAO implements CrudOperation<DishOrder> {
    private DataSource dataSource;
    private DishDAO dishDAO = new DishDAO();

    public DishOrderDAO() {this.dataSource = new DataSource();}
    public DishOrderDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<DishOrder> getAll() throws SQLException {
        String sql = "SELECT * FROM dish_order";
        List<DishOrder> dishOrders = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    DishOrder dishOrder = new DishOrder();
                    dishOrder.setId(rs.getInt("id"));
                    dishOrder.setOrderId(rs.getInt("order_id"));
                    dishOrder.setDishId(rs.getInt("dish_id"));
                    dishOrder.setQuantity(rs.getDouble("quantity"));
                    dishOrder.setStatusChange(rs.getTimestamp("status_change").toLocalDateTime());
                    dishOrders.add(dishOrder);
                }
            }
        }

        return dishOrders;
    }

    public List<DishOrderStatus> getDishOrderStatusByDishOrderId(int dishOrderId) throws SQLException {
        String sql = "SELECT id, dish_order_id, status, status_change FROM dish_order_status WHERE dish_order_id = ?";
        List<DishOrderStatus> dishOrderStatusList = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, dishOrderId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    DishOrderStatus dishOrderStatus = new DishOrderStatus(
                            rs.getInt("id"),
                            rs.getInt("dish_order_id"),
                            StatusType.valueOf(rs.getString("status")),
                            rs.getTimestamp("status_change").toLocalDateTime() // Utilisez le bon nom de colonne
                    );
                    dishOrderStatusList.add(dishOrderStatus);
                }
            }
        }

        return dishOrderStatusList;
    }

    public List<DishOrder> saveAll(List<DishOrder> dishOrders) throws SQLException {
        List<DishOrder> orders = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            for (DishOrder dishOrder : dishOrders) {
                String sql = "INSERT INTO dish_order (id, order_id, dish_id, quantity, status_change) " +
                        "VALUES (?, ?, ?, ?, ?) " +
                        "ON CONFLICT (id) DO UPDATE " +
                        "SET order_id = EXCLUDED.order_id, " +
                        "    dish_id = EXCLUDED.dish_id, " +
                        "    quantity = EXCLUDED.quantity, " +
                        "    status_change = EXCLUDED.status_change;";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, dishOrder.getId());
                    statement.setInt(2, dishOrder.getOrderId());
                    statement.setInt(3, dishOrder.getDishId());
                    statement.setDouble(4, dishOrder.getQuantity()); // Quantité
                    statement.setTimestamp(5, Timestamp.valueOf(dishOrder.getStatusChange())); // Timestamp
                    statement.executeUpdate();
                }
            }
            connection.commit();
        }
        return orders;
    }

    @Override
    public DishOrder findByName(String name) {
        throw new Error("Not implemented");
    }

    public Dish getDishById(int id) throws SQLException {
        String sql = "SELECT * FROM dish_order WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    DishOrder dishOrder = new DishOrder();
                    dishOrder.setId(rs.getInt("id"));
                    dishOrder.setDishId(rs.getInt("dish_id"));
                    Dish dish = dishDAO.getDishById(rs.getInt("dish_id"));
                    dishOrder.setDish(dish);
                }
            }
        }
        return dishDAO.getDishById(id);
    }

    public Order getOrderReferenceByOrderId(int orderId) throws SQLException {
        String sql = "SELECT reference FROM 'order' inner join dish_order on 'order'.id = dish_order.order_id";
        Order order = new Order();
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1, orderId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    order.setId(rs.getInt("id"));
                    order.setReference(rs.getString("reference"));
                }
            }
        }
        return order;
    }
}
