package org.example.entity;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class Order {
    private int id;
    private String reference;
    private LocalDateTime creationDate;
    private List<DishOrder> dishOrders;
    private List<OrderStatus> status;
    private LocalDateTime statusChange;

    public Order() {}

    public Order(int id, String reference, LocalDateTime creationDate, List<DishOrder> dishOrders, List<OrderStatus> status, LocalDateTime statusChange) {
        this.id = id;
        this.reference = reference;
        this.creationDate = creationDate;
        this.dishOrders = dishOrders;
        this.status = status;
        this.statusChange = statusChange;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public List<OrderStatus> getStatus() {
        return status;
    }

    public void setStatus(List<OrderStatus> status) {
        this.status = status;
    }

    public LocalDateTime getStatusChange() {
        return statusChange;
    }

    public void setStatusChange(LocalDateTime statusChange) {
        this.statusChange = statusChange;
    }

    public void setDishOrders(List<DishOrder> dishOrders) {
        this.dishOrders = dishOrders;
    }

    public List<DishOrder> getDishOrders() {
        return this.dishOrders;
    }

    public double getTotalAmount() {
        return dishOrders.stream()
                .mapToDouble(dishOrder -> dishOrder.getDish().getUnitPrice() * dishOrder.getQuantity())
                .sum();
    }

    public OrderStatus getOrderStatus() {
        if (status == null || status.isEmpty()) {
            return null;
        }
        return status.stream()
                .max(Comparator.comparing(OrderStatus :: getDateOrderStatus))
                .orElse(null);
    }

    public boolean isConfirmed() {
        if (status == null || status.isEmpty()) {
            return false;
        }
        OrderStatus latestStatus = status.stream()
                .max(Comparator.comparing(OrderStatus::getDateOrderStatus))
                .orElse(null);
        return latestStatus != null && latestStatus.getStatus() == StatusType.CONFIRMED;
    }
}