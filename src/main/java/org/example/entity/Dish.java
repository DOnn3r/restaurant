package org.example.entity;

import java.time.LocalDate;
import java.util.List;

public class Dish {
    private int id;
    private String name;
    private double unitPrice;
    private List<DishIngredient> ingredients;
    private DishOrderStatus status;

    public Dish(int id, String name, double unitPrice, List<DishIngredient> ingredients, DishOrderStatus status) {
        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
        this.ingredients = ingredients;
        this.status = status;
    }

    public Dish(int id, String name, double unitPrice, List<DishIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
        this.ingredients = ingredients;
    }

    public Dish(String name, double unitPrice, List<DishIngredient> ingredients) {
        this.name = name;
        this.unitPrice = unitPrice;
        this.ingredients = ingredients;
    }

    public Dish() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public List<DishIngredient> getIngredients() {
        return ingredients;
    }

    public DishOrderStatus getStatus() {
        return status;
    }

    public void setStatus(DishOrderStatus status) {
        this.status = status;
    }

    public void setIngredients(List<DishIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    public double getTotalCostAtDate(LocalDate date) {
        return ingredients.stream()
                .mapToDouble(dishIngredient -> {
                    double priceAtDate = dishIngredient.getIngredient().getPriceAtDate(date);
                    return priceAtDate * dishIngredient.getRequiredQuantity();
                })
                .sum();
    }

    public double getGrossMarginAtDate(LocalDate date){
        double totalCost = getTotalCostAtDate(date);
        return unitPrice - totalCost;
    }

    public double getGrossMargin() {
        return getTotalCostAtDate(LocalDate.now());
    }

    public int getAvailableQuantity(LocalDate date) {
        return ingredients.stream()
                .mapToInt(dishIngredient -> {
                    double availableQuantity = dishIngredient.getIngredient().getAvailableQuantity(date);
                    return (int) (availableQuantity / dishIngredient.getRequiredQuantity());
                })
                .min()
                .orElse(0);
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", unitPrice=" + unitPrice +
                ", ingredients=" + ingredients +
                '}';
    }
}
