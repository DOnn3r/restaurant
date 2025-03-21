package org.example.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ingredient {
    private int id;
    private String name;
    private LocalDateTime lastModification;
    private double unitPrice;
    private Unity unity;
    private List<IngredientPrice> historicalPrices;
    private List<StockMouvement> stockMouvements;

    public Ingredient(int id, String name, LocalDateTime lastModification, double unitPrice, Unity unity) {
        this.id = id;
        this.name = name;
        this.lastModification = lastModification;
        this.unitPrice = unitPrice;
        this.unity = unity;
    }

    public Ingredient(String name, LocalDateTime lastModification, double unitPrice, Unity unity) {
        this.name = name;
        this.lastModification = lastModification;
        this.unitPrice = unitPrice;
        this.unity = unity;
    }

    public Ingredient(int id, String name, LocalDateTime lastModification, double unitPrice, Unity unity, List<IngredientPrice> historicalPrices) {
        this.id = id;
        this.name = name;
        this.lastModification = lastModification;
        this.unitPrice = unitPrice;
        this.unity = unity;
        this.historicalPrices = historicalPrices;
    }

    public Ingredient(int id, String name, LocalDateTime lastModification, double unitPrice, Unity unity, List<IngredientPrice> historicalPrices, List<StockMouvement> stockMouvements) {
        this.id = id;
        this.name = name;
        this.lastModification = lastModification;
        this.unitPrice = unitPrice;
        this.unity = unity;
        this.historicalPrices = historicalPrices;
        this.stockMouvements = stockMouvements;
    }

    public Ingredient(String name, LocalDateTime lastModification) {
        this.name = name;
        this.lastModification = lastModification;
    }

    public Ingredient() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getLastModification() {
        return lastModification;
    }

    public void setLastModification(LocalDateTime lastModification) {
        this.lastModification = lastModification;
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

    public Unity getUnity() {
        return unity;
    }

    public void setUnity(Unity unity) {
        this.unity = unity;
    }

    public double getPriceAtDate(LocalDate date) {
        return historicalPrices.stream()
                .filter(price -> !price.getDate().isAfter(date))
                .max((p1, p2) -> p1.getDate().compareTo(p2.getDate()))
                .map(IngredientPrice::getPrice)
                .orElseThrow(() -> new RuntimeException("No price found for the given date"));
    }

    public double getAvailableQuantity(LocalDate date) {
        double availableQuantity = 0;

        for (StockMouvement mouvement : stockMouvements) {
            if (!mouvement.getMouvementDate().toLocalDate().isAfter(date)) {
                if (mouvement.getMouvementType() == MouvementType.IN) {
                    availableQuantity += mouvement.getQuantity();
                } else if (mouvement.getMouvementType() == MouvementType.OUT) {
                    availableQuantity -= mouvement.getQuantity();
                }
            }
        }

        return availableQuantity;
    }

    public double getAvailableQuantity() {
        return getAvailableQuantity(LocalDate.now());
    }

    public void addStockMouvement(StockMouvement mouvement) {
        if (this.stockMouvements == null) {
            this.stockMouvements = new ArrayList<>();
        }
        this.stockMouvements.add(mouvement);
    }

    public List<IngredientPrice> getHistoricalPrices() {
        return historicalPrices;
    }

    public void setHistoricalPrices(List<IngredientPrice> historicalPrices) {
        this.historicalPrices = historicalPrices;
    }

    public List<StockMouvement> getStockMouvements() {
        return stockMouvements;
    }

    public void setStockMouvements(List<StockMouvement> stockMouvements) {
        this.stockMouvements = stockMouvements;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", lastModification=" + lastModification +
                ", unitPrice=" + unitPrice +
                ", unity=" + unity +
                '}';
    }
}
