package org.example.entity;

import java.time.LocalDateTime;
import java.util.Date;

public class DishOrderStatus {
    private int id;
    private int dishId; // Référence au plat
    private StatusType status; // Statut du plat
    private LocalDateTime dateDishOrderStatus; // Date du changement de statut

    public DishOrderStatus(int id, int dishId, StatusType status, LocalDateTime dateDishOrderStatus) {
        this.id = id;
        this.dishId = dishId;
        this.status = status;
        this.dateDishOrderStatus = dateDishOrderStatus;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDishId() {
        return dishId;
    }

    public void setDishId(int dishId) {
        this.dishId = dishId;
    }

    public StatusType getStatus() {
        return status;
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }

    public LocalDateTime getDateDishOrderStatus() {
        return dateDishOrderStatus;
    }

    public void setDateDishOrderStatus(LocalDateTime dateDishOrderStatus) {
        this.dateDishOrderStatus = dateDishOrderStatus;
    }
}