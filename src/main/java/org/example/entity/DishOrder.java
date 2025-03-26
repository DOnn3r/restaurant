package org.example.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class DishOrder {
    private int id;
    private int orderId;
    private int dishId;
    private Dish dish; // Plat commandé
    private double quantity; // Quantité commandée
    private List<DishOrderStatus> dishStatus; // Liste des statuts du plat dans la commande
    private LocalDateTime statusChange; // Date du dernier changement de statut
    private Order order;

    public DishOrder(int id, Dish dish, double quantity, List<DishOrderStatus> dishStatus, LocalDateTime statusChange) {
        this.id = id;
        this.dish = dish;
        this.quantity = quantity;
        this.dishStatus = dishStatus;
        this.statusChange = statusChange;
    }

    public DishOrder(int id, int orderId, int dishId, Dish dish, double quantity, List<DishOrderStatus> dishStatus, LocalDateTime statusChange) {
        this.id = id;
        this.orderId = orderId;
        this.dishId = dishId;
        this.dish = dish;
        this.quantity = quantity;
        this.dishStatus = dishStatus;
        this.statusChange = statusChange;
    }

    public DishOrder(int id, int orderId, int dishId, double quantity, LocalDateTime statusChange) {
        this.id = id;
        this.orderId = orderId;
        this.dishId = dishId;
        this.quantity = quantity;
        this.statusChange = statusChange;
    }

    public DishOrder(int id, Order order, int dishId, double quantity, LocalDateTime statusChange) {
        this.id = id;
        this.order = order;
        this.dishId = dishId;
        this.quantity = quantity;
        this.statusChange = statusChange;
    }

    public DishOrder() {}

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public List<DishOrderStatus> getDishStatus() {
        return dishStatus;
    }

    public void setDishStatus(List<DishOrderStatus> dishStatus) {
        this.dishStatus = dishStatus;
    }

    public LocalDateTime getStatusChange() {
        return statusChange;
    }

    public void setStatusChange(LocalDateTime statusChange) {
        this.statusChange = statusChange;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getDishId() {
        return dishId;
    }

    public void setDishId(int dishId) {
        this.dishId = dishId;
    }

    public DishOrderStatus getActualStatus() {
        return dishStatus.stream()
                .max(Comparator.comparing(DishOrderStatus::getDateDishOrderStatus))
                .orElse(null);
    }
}