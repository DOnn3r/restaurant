package org.example.entity;

import java.time.LocalDateTime;
import java.util.Date;

public class OrderStatus {
    private int id;
    private int orderId; // Référence à la commande
    private StatusType status; // Statut de la commande
    private LocalDateTime dateOrderStatus; // Date du changement de statut

    public OrderStatus(int id, int orderId, StatusType status, LocalDateTime dateOrderStatus) {
        this.id = id;
        this.orderId = orderId;
        this.status = status;
        this.dateOrderStatus = dateOrderStatus;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public StatusType getStatus() {
        return status;
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }

    public LocalDateTime getDateOrderStatus() {
        return dateOrderStatus;
    }

    public void setDateOrderStatus(LocalDateTime dateOrderStatus) {
        this.dateOrderStatus = dateOrderStatus;
    }
}