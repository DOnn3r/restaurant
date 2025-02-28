package org.example.entity;


import java.time.LocalDateTime;

public class StockMouvement {
    private int id;
    private int ingredientId;
    private MouvementType mouvementType;
    private double quantity;
    private Unity unity;
    private LocalDateTime mouvementDate;

    public StockMouvement(int id, int ingredientId, MouvementType mouvementType, double quantity, Unity unity, LocalDateTime mouvementDate) {
        this.id = id;
        this.ingredientId = ingredientId;
        this.mouvementType = mouvementType;
        this.quantity = quantity;
        this.unity = unity;
        this.mouvementDate = mouvementDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public MouvementType getMouvementType() {
        return mouvementType;
    }

    public void setMouvementType(MouvementType mouvementType) {
        this.mouvementType = mouvementType;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public Unity getUnity() {
        return unity;
    }

    public void setUnity(Unity unity) {
        this.unity = unity;
    }

    public LocalDateTime getMouvementDate() {
        return mouvementDate;
    }

    public void setMouvementDate(LocalDateTime mouvementDate) {
        this.mouvementDate = mouvementDate;
    }

    @Override
    public String toString() {
        return "StockMouvement{" +
                "id=" + id +
                ", ingredientId=" + ingredientId +
                ", mouvementType=" + mouvementType +
                ", quantity=" + quantity +
                ", unity=" + unity +
                ", mouvementDate=" + mouvementDate +
                '}';
    }
}
