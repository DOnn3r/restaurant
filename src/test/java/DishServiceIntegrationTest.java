import org.example.dao.*;
import org.example.db.DataSource;
import org.example.entity.*;
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
    private StockMouvementDAO stockMouvementDAO = new StockMouvementDAO();
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

    @Test
    public void testGetStockMouvementsByIngredient() {
        int ingredientId = 1; // Saucisse
        List<StockMouvement> mouvements = stockMouvementDAO.getStockMouvementsByIngredient(ingredientId);

        // Vérifier que la liste n'est pas vide
        assertFalse(mouvements.isEmpty(), "La liste des mouvements ne devrait pas être vide");

        // Vérifier le nombre de mouvements pour cet ingrédient
        assertEquals(1, mouvements.size(), "Il devrait y avoir 1 mouvement de stock pour l'ingrédient ID 1");

        // Vérifier les détails du mouvement
        StockMouvement mouvement = mouvements.get(0);
        assertEquals(3, mouvement.getId());
        assertEquals(1, mouvement.getIngredientId());
        assertEquals(MouvementType.IN, mouvement.getMouvementType());
        assertEquals(10000, mouvement.getQuantity());
        assertEquals(Unity.G, mouvement.getUnity());
        assertEquals(LocalDateTime.of(2025, 2, 1, 8, 0), mouvement.getMouvementDate());
    }

    @Test
    public void testGetAvailableQuantity_NoSorties() {
        // Créer des ingrédients avec des mouvements de stock (uniquement des entrées)
        Ingredient oeuf = new Ingredient(
                3, "Oeuf", LocalDateTime.now(), 1000, Unity.U,
                List.of(), // historicalPrices
                List.of(
                        new StockMouvement(1, 3, MouvementType.IN, 100, Unity.U, LocalDateTime.of(2025, 2, 1, 8, 0))
                )
        );

        Ingredient pain = new Ingredient(
                4, "Pain", LocalDateTime.now(), 1000, Unity.U,
                List.of(), // historicalPrices
                List.of(
                        new StockMouvement(2, 4, MouvementType.IN, 50, Unity.U, LocalDateTime.of(2025, 2, 1, 8, 0))
                )
        );

        Ingredient saucisse = new Ingredient(
                1, "Saucisse", LocalDateTime.now(), 20, Unity.G,
                List.of(), // historicalPrices
                List.of(
                        new StockMouvement(3, 1, MouvementType.IN, 10000, Unity.G, LocalDateTime.of(2025, 2, 1, 8, 0))
                )
        );

        Ingredient huile = new Ingredient(
                2, "Huile", LocalDateTime.now(), 10000, Unity.L,
                List.of(), // historicalPrices (vide pour cet exemple)
                List.of(
                        new StockMouvement(4, 2, MouvementType.IN, 20, Unity.L, LocalDateTime.of(2025, 2, 1, 8, 0))
                )
        );

        // Vérifier les quantités disponibles à la date du jour (2025-02-24)
        assertEquals(100, oeuf.getAvailableQuantity(LocalDate.of(2025, 2, 24)), 0.01);
        assertEquals(50, pain.getAvailableQuantity(LocalDate.of(2025, 2, 24)), 0.01);
        assertEquals(10000, saucisse.getAvailableQuantity(LocalDate.of(2025, 2, 24)), 0.01);
        assertEquals(20, huile.getAvailableQuantity(LocalDate.of(2025, 2, 24)), 0.01);
    }

    @Test
    public void testGetAvailableQuantity_WithSorties() {
        ingredientDAO.saveStockMouvements();

        // Vérifier les quantités disponibles à la date du jour (2025-02-24)
        assertEquals(80, oeuf.getAvailableQuantity(LocalDate.of(2025, 3, 1)), 0.01);
        assertEquals(30, pain.getAvailableQuantity(LocalDate.of(2025, 3, 1)), 0.01);
        assertEquals(10000, saucisse.getAvailableQuantity(LocalDate.of(2025, 3, 1)), 0.01);
        assertEquals(20, huile.getAvailableQuantity(LocalDate.of(2025, 3, 1)), 0.01);
    }

    @Test
    public void testAddStockMouvement() {
        Ingredient sel = new Ingredient(8, "Sel", LocalDateTime.now(), 2.5, Unity.G);
        Ingredient riz = new Ingredient(9, "Riz", LocalDateTime.now(), 3.5, Unity.G);

        sel.addStockMouvement(new StockMouvement(10, 8, MouvementType.IN, 1000, Unity.G, LocalDateTime.of(2025, 2, 24, 8, 0)));
        sel.addStockMouvement(new StockMouvement(11, 8, MouvementType.OUT, 200, Unity.G, LocalDateTime.of(2025, 2, 24, 10, 0)));

        riz.addStockMouvement(new StockMouvement(12, 9, MouvementType.IN, 5000, Unity.G, LocalDateTime.of(2025, 2, 24, 9, 0)));
        riz.addStockMouvement(new StockMouvement(13, 9, MouvementType.OUT, 1000, Unity.G, LocalDateTime.of(2025, 2, 24, 11, 0)));

        assertEquals(800, sel.getAvailableQuantity(LocalDate.of(2025, 2, 24)), 0.01); // 1000 - 200 = 800
        assertEquals(4000, riz.getAvailableQuantity(LocalDate.of(2025, 2, 24)), 0.01); // 5000 - 1000 = 4000
    }

    @Test
    public void testGetAvailableQuantity() {
        Ingredient saucisse = new Ingredient(
                1, "Saucisse", LocalDateTime.now(), 10000, Unity.G,
                List.of(),
                List.of(
                        new StockMouvement(1, 1, MouvementType.IN, 5000, Unity.G, LocalDateTime.of(2025, 1, 1, 8, 0)),
                        new StockMouvement(2, 1, MouvementType.OUT, 2000, Unity.G, LocalDateTime.of(2025, 1, 2, 10, 0))
                )
        );

        Ingredient pain = new Ingredient(
                4, "Pain", LocalDateTime.now(), 1000, Unity.U,
                List.of(),
                List.of(
                        new StockMouvement(3, 4, MouvementType.IN, 100, Unity.U, LocalDateTime.of(2025, 1, 1, 8, 0)),
                        new StockMouvement(4, 4, MouvementType.OUT, 50, Unity.U, LocalDateTime.of(2025, 1, 2, 10, 0))
                )
        );

        Ingredient fromage = new Ingredient(
                6, "Fromage", LocalDateTime.now(), 500, Unity.G,
                List.of(),
                List.of(
                        new StockMouvement(5, 6, MouvementType.IN, 300, Unity.G, LocalDateTime.of(2025, 1, 1, 8, 0)),
                        new StockMouvement(6, 6, MouvementType.OUT, 100, Unity.G, LocalDateTime.of(2025, 1, 2, 10, 0))
                )
        );

        Dish hotDog = new Dish(1, "Hot Dog", 15000, List.of(
                new DishIngredient(saucisse, 100, Unity.G),
                new DishIngredient(pain, 1, Unity.U),
                new DishIngredient(fromage, 50, Unity.G)
        ));
        assertEquals(4, hotDog.getAvailableQuantity(LocalDate.of(2025, 1, 3)));
    }

    @Test
    public void testDishOrderCreation() {
        Dish dish = new Dish(1, "Hot Dog", 15000, List.of());
        List<DishOrderStatus> statusList = List.of(
                new DishOrderStatus(1, 1, StatusType.CREATED, LocalDateTime.now())
        );
        DishOrder dishOrder = new DishOrder(1, dish, 2, statusList, LocalDateTime.now());

        assertEquals(1, dishOrder.getId());
        assertEquals(dish, dishOrder.getDish());
        assertEquals(2, dishOrder.getQuantity());
        assertEquals(statusList, dishOrder.getDishStatus());
    }

    @Test
    public void testGetActualStatus() {
        DishOrderStatus status1 = new DishOrderStatus(1, 1, StatusType.CREATED, LocalDateTime.of(2025, 1, 1, 10, 0));
        DishOrderStatus status2 = new DishOrderStatus(2, 1, StatusType.CONFIRMED, LocalDateTime.of(2025, 1, 1, 11, 0));
        List<DishOrderStatus> statusList = List.of(status1, status2);

        DishOrder dishOrder = new DishOrder(1, new Dish(), 2, statusList, LocalDateTime.now());

        assertEquals(StatusType.CONFIRMED, dishOrder.getActualStatus().getStatus());
    }

    @Test
    public void testDishOrderStatusCreation() {
        DishOrderStatus status = new DishOrderStatus(1, 1, StatusType.CREATED, LocalDateTime.now());

        assertEquals(1, status.getId());
        assertEquals(1, status.getDishId());
        assertEquals(StatusType.CREATED, status.getStatus());
        assertNotNull(status.getDateDishOrderStatus());
    }

    @Test
    public void testOrderCreation() {
        Dish dish = new Dish(1, "Hot Dog", 15000, List.of());
        DishOrder dishOrder = new DishOrder(1, dish, 2, List.of(), LocalDateTime.now());
        org.example.entity.Order order = new Order(1, "REF123", LocalDateTime.of(2025, 2, 1, 8, 0), List.of(dishOrder), List.of(), LocalDateTime.now());

        assertEquals(1, order.getId());
        assertEquals("REF123", order.getReference());
        assertEquals(1, order.getDishOrders().size());
    }

    @Test
    public void testGetTotalAmount() {
        Dish dish = new Dish(1, "Hot Dog", 15000, List.of());
        DishOrder dishOrder = new DishOrder(1, dish, 2, List.of(), LocalDateTime.now());
        org.example.entity.Order order = new org.example.entity.Order(1, "REF123", LocalDateTime.now(), List.of(dishOrder), List.of(), LocalDateTime.now());

        assertEquals(30000, order.getTotalAmount(), 0.01);
    }

    @Test
    public void testGetOrderStatus() {
        OrderStatus status1 = new OrderStatus(1, 1, StatusType.CREATED, LocalDateTime.of(2025, 1, 1, 10, 0));
        OrderStatus status2 = new OrderStatus(2, 1, StatusType.CONFIRMED, LocalDateTime.of(2025, 1, 1, 11, 0));
        org.example.entity.Order order = new org.example.entity.Order(1, "REF123", LocalDateTime.now(), List.of(), List.of(status1, status2), LocalDateTime.now());

        assertEquals(StatusType.CONFIRMED, order.getOrderStatus().getStatus());
    }

    @Test
    public void testFindByReference() throws SQLException {
        OrderDAO orderDAO = new OrderDAO(new DataSource());
        org.example.entity.Order order = orderDAO.findByReference("CMD123");

        assertNotNull(order);
        assertEquals("CMD123", order.getReference());
    }

    @Test
    public void testGetDishOrdersByOrderId() throws SQLException {
        OrderDAO orderDAO = new OrderDAO(new DataSource());
        List<DishOrder> dishOrders = orderDAO.getDishOrdersByOrderId(1);

        assertNotNull(dishOrders);
        assertFalse(dishOrders.isEmpty());
    }

    @Test
    public void testGetDishOrderStatusByDishOrderId() throws SQLException {
        DishOrderDAO dishOrderDAO = new DishOrderDAO();
        List<DishOrderStatus> statusList = dishOrderDAO.getDishOrderStatusByDishOrderId(3);

        assertNotNull(statusList);
        assertFalse(statusList.isEmpty());
    }

    @Test
    public void testSaveAllDishOrders() throws SQLException {
        DishOrderDAO dishOrderDAO = new DishOrderDAO();
        Order order = new Order(1, "REF123", LocalDateTime.of(2025, 2, 1, 8, 0), List.of(), List.of(), LocalDateTime.now());
        Dish dish = new Dish(1, "Hot Dog", 15000, List.of());
        DishOrder dishOrder = new DishOrder(1, 1, 1 , dish, 2, List.of(), LocalDateTime.now());

        List<DishOrder> dishOrders = List.of(dishOrder);

        List<DishOrder> savedOrders = dishOrderDAO.saveAll(dishOrders);

        assertNotNull(savedOrders);
        assertEquals(3, dishOrderDAO.getAll().size());
    }
}