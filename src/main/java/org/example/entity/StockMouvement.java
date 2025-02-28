package org.example.entity;

import java.time.LocalDateTime;

public final class StockMouvement {
    private final int id;
    private final int ingredientId;
    private final MouvementType mouvementType;
    private final double quantity;
    private final Unity unity;
    private final LocalDateTime mouvementDate;

    public StockMouvement(int id, int ingredientId, MouvementType mouvementType, double quantity, Unity unity, LocalDateTime mouvementDate) {
        this.id = id;
        this.ingredientId = ingredientId;
        this.mouvementType = mouvementType;
        this.quantity = quantity;
        this.unity = unity;
        this.mouvementDate = mouvementDate;
    }

    // Getters (pas de setters)
    public int getId() {
        return id;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public MouvementType getMouvementType() {
        return mouvementType;
    }

    public double getQuantity() {
        return quantity;
    }

    public Unity getUnity() {
        return unity;
    }

    public LocalDateTime getMouvementDate() {
        return mouvementDate;
    }
}