package org.example.dao;

import org.example.db.DataSource;
import org.example.entity.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OrderDAO implements CrudOperation<Order> {
    private DataSource dataSource;
    private DishOrderDAO dishOrderDAO = new DishOrderDAO();
    private DishDAO dishDAO = new DishDAO();

    public OrderDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Order findByReference(String reference) throws SQLException {
        String sql = "SELECT * FROM \"order\" where reference ilike ?";
        Order order = new Order();
        try(Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, "%"+reference+"%");
            try(ResultSet rs = statement.executeQuery()){
                if(rs.next()){
                    order.setId(rs.getInt("id"));
                    order.setReference(rs.getString("reference"));
                    order.setCreationDate(rs.getTimestamp("creation_date").toLocalDateTime());
                    order.setStatusChange(rs.getTimestamp("status_change").toLocalDateTime());
                    List<DishOrder> dishOrders = getDishOrdersByOrderId(order.getId());
                    order.setDishOrders(dishOrders);
                    List<OrderStatus> orderStatuses = getOrderStatusByOrderId(order.getId());
                    order.setStatus(orderStatuses);
                }
            }
        }
        return order;
    }

    @Override
    public List<Order> saveAll(List<Order> entities) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()){
            connection.setAutoCommit(false);
            for(Order order : entities){
                String sql = "insert into 'order' values(?,?,?,?,?,?,?,?)" +
                        "on conflict (id) do update" +
                        "set creation_date = excluded.creation_date" +
                        " total_amount = excluded.total_amount" +
                        " status_change = excluded.status_change";
                try(PreparedStatement statement = connection.prepareStatement(sql)){
                    statement.setInt(1, order.getId());
                    statement.setString(2, order.getReference());
                    statement.setTimestamp(3, Timestamp.valueOf(order.getCreationDate()));
                    statement.setTimestamp(4, java.sql.Timestamp.valueOf(order.getStatusChange()));
                    statement.executeUpdate();
                }
            }
            connection.commit();
        }
        return orders;
    }

    public Order saveOne(Order entities) throws SQLException {
        Order orders = new Order();
        try (Connection connection = dataSource.getConnection()){
            connection.setAutoCommit(false);
            String sql = "insert into 'order' values(?,?,?,?,?,?,?,?)" +
                    "on conflict (id) do update" +
                    "set creation_date = excluded.creation_date" +
                    " total_amount = excluded.total_amount" +
                    " status_change = excluded.status_change";
            try(PreparedStatement statement = connection.prepareStatement(sql)){
                statement.setInt(1, entities.getId());
                statement.setString(2, entities.getReference());
                statement.setTimestamp(3, Timestamp.valueOf(entities.getCreationDate()));
                statement.setTimestamp(4, java.sql.Timestamp.valueOf(entities.getStatusChange()));
                statement.executeUpdate();
            }
            connection.commit();
        }
        return orders;
    }

    public void addOrUpdateDishOrder(int orderId, DishOrder dishOrder) throws SQLException {
        Order order = findById(orderId);

        if (order != null && order.isConfirmed()) {
            throw new IllegalStateException("La commande est confirmée et ne peut plus être modifiée.");
        }

        String sql = "INSERT INTO dish_order (id, order_id, dish_id, quantity, status_change) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "quantity = EXCLUDED.quantity, " +
                "status_change = EXCLUDED.status_change";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, dishOrder.getId());
            statement.setInt(2, dishOrder.getOrderId());
            statement.setInt(3, dishOrder.getDishId());
            statement.setDouble(4, dishOrder.getQuantity());
            statement.setTimestamp(5, Timestamp.valueOf(dishOrder.getStatusChange()));
            statement.executeUpdate();
        }
    }

    @Override
    public Order findByName(String name) throws SQLException {
        return null;
    }

    public void save(DishOrder dishOrders, String reference, int orderId) throws SQLException {
        Order order = new Order(orderId, reference, LocalDateTime.now());
        saveOne(order);
        order.setDishOrders((List<DishOrder>) dishOrders);
        addOrUpdateDishOrder(orderId, dishOrders);
    }

    @Override
    public List<Order> getAll() throws SQLException {
        String sql = "SELECT * FROM " + "'order'";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                List<Order> orders = new ArrayList<>();
                while (rs.next() == true) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setReference(rs.getString("reference"));
                    order.setCreationDate(rs.getTimestamp("creation_date").toLocalDateTime());
                    order.setStatusChange(rs.getTimestamp("status_change").toLocalDateTime());
                    List<DishOrder> dishOrders = getDishOrdersByOrderId(order.getId());
                    order.setDishOrders(dishOrders);
                    List<OrderStatus> orderStatuses = getOrderStatusByOrderId(order.getId());
                    order.setStatus(orderStatuses);
                    orders.add(order);
                }
                return orders;
            }
        }
    }

    public List<DishOrder> getDishOrdersByOrderId(int orderId) throws SQLException {
        String sql = "SELECT * FROM dish_order WHERE order_id = ?";
        List<DishOrder> dishOrders = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    DishOrder dishOrder = new DishOrder();
                    dishOrder.setId(rs.getInt("id"));
                    dishOrder.setOrderId(rs.getInt("order_id"));
                    dishOrder.setDishId(rs.getInt("dish_id"));
                    dishOrderDAO.getDishById(dishOrder.getDishId());
                    dishOrder.setStatusChange(rs.getTimestamp("status_change").toLocalDateTime());
                    Dish dish = dishOrderDAO.getDishById(rs.getInt("dish_id"));
                    dishOrder.setDish(dish);

                    dishOrders.add(dishOrder);
                }
            }
        }

        return dishOrders;
    }

    private List<OrderStatus> getOrderStatusByOrderId(int orderId) throws SQLException {
        String sql = "SELECT * FROM order_status WHERE order_id = ?";
        List<OrderStatus> orderStatuses = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    OrderStatus orderStatus = new OrderStatus(
                            rs.getInt("id"),
                            rs.getInt("order_id"),
                            StatusType.valueOf(rs.getString("status")),
                            rs.getTimestamp("status_change").toLocalDateTime()
                    );
                    orderStatuses.add(orderStatus);
                }
            }
        }

        return orderStatuses;
    }

    public Order findById(int orderId) throws SQLException {
        String sql = "SELECT * FROM \"order\" WHERE id = ?";
        Order order = new Order();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    order.setId(rs.getInt("id"));
                    order.setReference(rs.getString("reference"));
                    order.setCreationDate(rs.getTimestamp("creation_date").toLocalDateTime());
                    order.setStatusChange(rs.getTimestamp("status_change").toLocalDateTime());

                    // Récupérer les plats associés à la commande
                    List<DishOrder> dishOrders = getDishOrdersByOrderId(order.getId());
                    order.setDishOrders(dishOrders);

                    // Récupérer les statuts de la commande
                    List<OrderStatus> orderStatuses = getOrderStatusByOrderId(order.getId());
                    order.setStatus(orderStatuses);
                }
            }
        }
        return order;
    }
}
