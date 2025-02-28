import org.example.dao.DishDAO;
import org.example.dao.DishIngredientDAO;
import org.example.dao.IngredientDAO;
import org.example.db.DataSource;
import org.example.entity.*;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DishServiceIntegrationTest {

    private DishDAO dishDAO = new DishDAO();
    private IngredientDAO ingredientDAO = new IngredientDAO();
    private DishIngredientDAO dishIngredientDAO = new DishIngredientDAO();
    private DataSource dataSource = new DataSource();

    @Test
    public void getAllDishes() throws SQLException, ClassNotFoundException {
        List<Dish> dishes = dishDAO.getAll();
        assertEquals(dishes.size(), 3);
    }
    @Test
    public void getAllIngredients() throws SQLException, ClassNotFoundException {
        List<Ingredient> ingredients = ingredientDAO.getAll();
        assertEquals(ingredients.size(), 7);
    }
    @Test
    public void testGetTotalCostAtDate() {
        Ingredient saucisse = new Ingredient(1, "Saucisse", LocalDateTime.now(), 20, Unity.G, List.of(
                new IngredientPrice(20, LocalDate.of(2025, 1, 1)),
                new IngredientPrice(18, LocalDate.of(2024, 12, 1))
        ));
        Ingredient pain = new Ingredient(4, "Pain", LocalDateTime.now(), 1000, Unity.U, List.of(
                new IngredientPrice(1000, LocalDate.of(2025, 1, 1)),
                new IngredientPrice(900, LocalDate.of(2024, 12, 1))
        ));

        Dish hotDog = new Dish(1, "Hot Dog", 0, List.of(
                new DishIngredient(saucisse, 1, Unity.G),
                new DishIngredient(pain, 1, Unity.U)
        ));

        double expectedCostToday = 1020.0;
        assertEquals(expectedCostToday, hotDog.getTotalCostAtDate(LocalDate.of(2025, 1, 1)), 0.01);

        double expectedCostPast = 918.0;
        assertEquals(expectedCostPast, hotDog.getTotalCostAtDate(LocalDate.of(2024, 12, 15)), 0.01);
    }
    @Test
    void testFilterAndSortIngredients() throws SQLException {
        List<Criteria> criteria = Arrays.asList(new Criteria("unit_price", 1000.0, 10000.0));

        List<Ingredient> result = ingredientDAO.filterAndSortIngredients(criteria, 0, 10);

        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("Huile", result.get(0).getName()); // Vérifie le premier élément trié
        assertEquals(10000.0, result.get(0).getUnitPrice());
    }

    @Test
    void testSaveAll_ShouldInsertDishesAndIngredients() throws SQLException {
        // Given: A list of dishes with ingredients
        List<Dish> dishes = List.of(
                new Dish(2,"Pizza", 12000, List.of(
                        new DishIngredient(new Ingredient(5,"Tomate", LocalDateTime.now(), 2000, Unity.G), 0.5, Unity.G),
                        new DishIngredient(new Ingredient(6,"Fromage", LocalDateTime.now(), 500, Unity.G), 0.2, Unity.G)
                )),
                new Dish(3, "Salade", 4000, List.of(
                        new DishIngredient(new Ingredient(7,"Laitue", LocalDateTime.now(), 1500, Unity.U), 2, Unity.U)
                ))
        );

        // When: Saving all dishes
        dishDAO.saveAll(dishes);

        // Then: Verify dishes are saved
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM dish");
             ResultSet rs = stmt.executeQuery()) {
            assertTrue(rs.next());
            assertEquals(3, rs.getInt(1));
        }

        // Then: Verify ingredients are saved
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM ingredient");
             ResultSet rs = stmt.executeQuery()) {
            assertTrue(rs.next());
            assertEquals(7, rs.getInt(1));
        }

        // Then: Verify dish_ingredient relations
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM dish_ingredient");
             ResultSet rs = stmt.executeQuery()) {
            assertTrue(rs.next());
            assertEquals(8, rs.getInt(1));
        }
    }

    @Test
    public void testGetGrossMargin() {
        // Créer des ingrédients avec des prix historiques
        Ingredient saucisse = new Ingredient(1, "Saucisse", LocalDateTime.now(), 20, Unity.G, List.of(
                new IngredientPrice(20, LocalDate.of(2025, 1, 1)),   // Prix actuel
                new IngredientPrice(18, LocalDate.of(2024, 12, 1))  // Prix antérieur
        ));
        Ingredient pain = new Ingredient(4, "Pain", LocalDateTime.now(), 1000, Unity.U, List.of(
                new IngredientPrice(1000, LocalDate.of(2025, 1, 1)), // Prix actuel
                new IngredientPrice(900, LocalDate.of(2024, 12, 1))  // Prix antérieur
        ));

        // Créer un plat "Hot Dog" avec ces ingrédients
        Dish hotDog = new Dish(1, "Hot Dog", 1500, List.of(
                new DishIngredient(saucisse, 1, Unity.G),
                new DishIngredient(pain, 1, Unity.U)
        ));

        // Vérifier la marge brute à la date du jour (2025-01-01)
        double expectedMarginToday = 480.0; // 1500 (prix de vente) - (20 + 1000) = 480
        assertEquals(expectedMarginToday, hotDog.getGrossMarginAtDate(LocalDate.of(2025, 1, 1)), 0.01);

        // Vérifier la marge brute à une date antérieure (2024-12-01)
        double expectedMarginPast = 582.0; // 1500 (prix de vente) - (18 + 900) = 582
        assertEquals(expectedMarginPast, hotDog.getGrossMarginAtDate(LocalDate.of(2024, 12, 1)), 0.01);
    }
}