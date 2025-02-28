package org.example.entity;

public class DishIngredient {
    private Ingredient ingredient;
    private double requiredQuantity;
    private Unity unity;

    public DishIngredient(Ingredient ingredient, double requiredQuantity, Unity unity) {
        this.ingredient = ingredient;
        this.requiredQuantity = requiredQuantity;
        this.unity = unity;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public double getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(double requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public Unity getUnity() {
        return unity;
    }

    public void setUnity(Unity unity) {
        this.unity = unity;
    }


}
