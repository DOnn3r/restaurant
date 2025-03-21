package org.example.entity;

import java.time.LocalDate;

public class IngredientPrice {
    private int id;
    private double price;
    private LocalDate date;
    private Ingredient ingredient;

    public IngredientPrice(double price, LocalDate date) {
        this.price = price;
        this.date = date;
    }

    public IngredientPrice(int id, double price, LocalDate date, Ingredient ingredient) {
        this.id = id;
        this.price = price;
        this.date = date;
        this.ingredient = ingredient;
    }

    public IngredientPrice(double price, LocalDate date, Ingredient ingredient) {
        this.price = price;
        this.date = date;
        this.ingredient = ingredient;
    }

    public IngredientPrice(int id, double price, LocalDate date) {
        this.id = id;
        this.price = price;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

}