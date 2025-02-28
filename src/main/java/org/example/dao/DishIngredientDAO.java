package org.example.dao;

import org.example.db.DataSource;
import org.example.entity.Dish;
import org.example.entity.DishIngredient;
import org.example.entity.Ingredient;

import java.sql.SQLException;
import java.util.List;

public class DishIngredientDAO {
    private DataSource dataSource;
    private DishDAO dishDAO = new DishDAO();

    public DishIngredientDAO (DataSource dataSource){
        this.dataSource = dataSource;
    };

    public DishIngredientDAO(){}

    public Double calculateTotalCost(String name) throws SQLException {
        Dish dish = dishDAO.findByName(name);
        List<DishIngredient> dishIngredients = dishDAO.findIngredientsByDishId(dish.getId());

        double totalCost = dishIngredients.stream()
                .mapToDouble(dishIngredient -> {
                    Ingredient ingredient = dishIngredient.getIngredient();
                    double quantity = dishIngredient.getRequiredQuantity();
                    return ingredient.getUnitPrice() * quantity;
                })
                .sum();
        return totalCost;
    }
}