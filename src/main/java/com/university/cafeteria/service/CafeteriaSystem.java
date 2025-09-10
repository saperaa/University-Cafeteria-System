package com.university.cafeteria.service;

import com.university.cafeteria.model.MenuItem;
import com.university.cafeteria.model.MenuItem.MenuCategory;
import com.university.cafeteria.model.Order;
import com.university.cafeteria.model.Staff;
import com.university.cafeteria.model.Student;
import com.university.cafeteria.repository.impl.InMemoryUserRepository;
import com.university.cafeteria.repository.impl.InMemoryMenuRepository;
import com.university.cafeteria.repository.impl.InMemoryOrderRepository;
import com.university.cafeteria.service.impl.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class CafeteriaSystem {
    

    private final AuthenticationService authenticationService;
    private final MenuService menuService;
    private final OrderService orderService;
    private final LoyaltyService loyaltyService;
    private final NotificationService notificationService;
    private final ReportingService reportingService;
    
    private final InMemoryUserRepository userRepository;
    private final InMemoryMenuRepository menuRepository;
    private final InMemoryOrderRepository orderRepository;
    
    public CafeteriaSystem() {
        this.userRepository = new InMemoryUserRepository();
        this.menuRepository = new InMemoryMenuRepository();
        this.orderRepository = new InMemoryOrderRepository();
        
        this.notificationService = new NotificationServiceImpl();
        this.authenticationService = new AuthenticationServiceImpl(userRepository, notificationService);
        this.menuService = new MenuServiceImpl(menuRepository);
        this.loyaltyService = new LoyaltyServiceImpl(userRepository, notificationService);
        this.orderService = new OrderServiceImpl(orderRepository, loyaltyService, notificationService);
        this.reportingService = new ReportingServiceImpl(orderRepository, userRepository, menuRepository);
        
        System.setProperty("cafeteria.db.url", DB_URL);
        System.setProperty("cafeteria.db.user", DB_USER);
        System.setProperty("cafeteria.db.password", DB_PASSWORD);
        
        initializeDatabase();
        
        loadDataFromDatabase();
        initializeSystemData();
    }
    
    public AuthenticationService getAuthenticationService() { return authenticationService; }
    public MenuService getMenuService() { return menuService; }
    public OrderService getOrderService() { return orderService; }
    public LoyaltyService getLoyaltyService() { return loyaltyService; }
    public NotificationService getNotificationService() { return notificationService; }
    public ReportingService getReportingService() { return reportingService; }
    

    private void loadDataFromDatabase() {
        System.out.println("Loading existing data from database...");
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            loadUsersFromDatabase(conn);
            loadLoyaltyTransactionsFromDatabase(conn);
            loadMenuItemsFromDatabase(conn);
            loadOrdersFromDatabase(conn);
            System.out.println("✓ Existing data loaded from database");
        } catch (SQLException e) {
            System.err.println("Warning: Could not load existing data from database: " + e.getMessage());
            System.out.println("Proceeding with sample data only...");
        }
    }
    
    private void initializeSystemData() {
        System.out.println("Initializing cafeteria system...");
        
        createSampleStaff();
        
        System.out.println("🔄 Forcing menu update to match student interface...");
        
        if (menuRepository instanceof com.university.cafeteria.repository.impl.InMemoryMenuRepository) {
            ((com.university.cafeteria.repository.impl.InMemoryMenuRepository) menuRepository).clear();
            System.out.println("✅ Cleared existing menu items from memory");
        }
        
        createSampleMenu();
        
        saveMenuItemsToDatabase();
        
        ensureDrinksInMenuService();
        
        System.out.println("✅ Menu forced to match student interface with " + menuService.getAllMenuItems().size() + " items");
        System.out.println("✅ DRINKS SHOULD NOW BE VISIBLE in your GUI! (Cola, Sprite, Water, Tea)");
        
        long finalDrinkCount = menuService.getAllMenuItems().stream()
            .filter(item -> item.getCategory() == MenuItem.MenuCategory.DRINK)
            .count();
        System.out.println("🥤 FINAL VERIFICATION: " + finalDrinkCount + " drinks available in menu service");
        
        testDrinksAvailability();
        
        testDatabaseAndMenuService();
        
        forceDatabaseSync();
        
        if (userRepository.findAllStudents().isEmpty()) {
            createSampleStudents();
        }
        
        System.out.println("System initialization completed!");
        System.out.println("- Staff accounts ready");
        System.out.println("- Menu items available");
        System.out.println("- Student accounts ready");
        System.out.println();
    }
    
    private void createSampleStaff() {
        try {
            System.out.println("Creating sample staff accounts...");
            
            if (!userRepository.existsById("admin")) {
                Staff admin = authenticationService.registerStaff(
                    "admin", 
                    "System Administrator", 
                    "admin123", 
                    "EMP001", 
                    Staff.StaffRole.ADMIN
                );
                System.out.println("✓ Created admin account: " + admin.getUserId());
            }
            
            if (!userRepository.existsById("manager1")) {
                Staff manager = authenticationService.registerStaff(
                    "manager1", 
                    "Sarah Johnson", 
                    "manager123", 
                    "EMP002", 
                    Staff.StaffRole.MANAGER
                );
                System.out.println("✓ Created manager account: " + manager.getUserId());
            }
            
            if (!userRepository.existsById("cashier1")) {
                Staff cashier = authenticationService.registerStaff(
                    "cashier1", 
                    "Mike Wilson", 
                    "cashier123", 
                    "EMP003", 
                    Staff.StaffRole.CASHIER
                );
                System.out.println("✓ Created cashier account: " + cashier.getUserId());
            }
            
            if (!userRepository.existsById("kitchen1")) {
                Staff kitchen = authenticationService.registerStaff(
                    "kitchen1", 
                    "Ahmed Hassan", 
                    "kitchen123", 
                    "EMP004", 
                    Staff.StaffRole.KITCHEN
                );
                System.out.println("✓ Created kitchen account: " + kitchen.getUserId());
            }
            
        } catch (Exception e) {
            System.err.println("Error creating sample staff: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    public void refreshMenuWithNewItems() {
        System.out.println("🔄 Refreshing menu with new items...");
        
        if (menuRepository instanceof com.university.cafeteria.repository.impl.InMemoryMenuRepository) {
            ((com.university.cafeteria.repository.impl.InMemoryMenuRepository) menuRepository).clear();
            System.out.println("✅ Cleared old menu items");
        }
        
        createSampleMenu();
        
        saveMenuItemsToDatabase();
        
        System.out.println("✅ Menu refresh completed successfully!");
    }
    

    private void smartSyncMenuItemsToDatabase(List<MenuItem> menuItems) {
        System.out.println("🧠 Smart syncing menu items to database...");
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("✅ Database connection established successfully");
            
            Map<String, MenuItem> existingItems = new HashMap<>();
            try (PreparedStatement ps = conn.prepareStatement("SELECT item_id, name, description, price, category, available FROM menu_items")) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String itemId = rs.getString("item_id");
                        String name = rs.getString("name");
                        String description = rs.getString("description");
                        BigDecimal price = rs.getBigDecimal("price");
                        MenuItem.MenuCategory category = MenuItem.MenuCategory.valueOf(rs.getString("category"));
                        boolean available = rs.getBoolean("available");
                        
                        MenuItem item = new MenuItem(itemId, name, description, price, category);
                        item.setAvailable(available);
                        existingItems.put(itemId, item);
                    }
                }
            }
            
            System.out.println("📊 Found " + existingItems.size() + " existing items in database");
            
            int addedCount = 0;
            for (MenuItem menuItem : menuItems) {
                if (!existingItems.containsKey(menuItem.getItemId())) {
                    try {
                        String sql = "INSERT INTO menu_items (item_id, name, description, price, category, available, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?)";
                        try (PreparedStatement ps = conn.prepareStatement(sql)) {
                            ps.setString(1, menuItem.getItemId());
                            ps.setString(2, menuItem.getName());
                            ps.setString(3, menuItem.getDescription());
                            ps.setBigDecimal(4, menuItem.getPrice());
                            ps.setString(5, menuItem.getCategory().name());
                            ps.setBoolean(6, menuItem.isAvailable());
                            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                            
                            int result = ps.executeUpdate();
                            if (result > 0) {
                                System.out.println("✅ Added missing item: " + menuItem.getName());
                                addedCount++;
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("❌ Error adding item " + menuItem.getName() + ": " + e.getMessage());
                    }
                }
            }
            
            System.out.println("🎉 Smart sync completed! Added " + addedCount + " new items");
            
        } catch (SQLException e) {
            System.err.println("❌ Error during smart sync: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    private void cleanupOrphanedOrderItems(Connection conn) {
        System.out.println("🧹 Cleaning up orphaned order items...");
        
        try {
            String sql = "DELETE oi FROM order_items oi LEFT JOIN menu_items mi ON oi.item_id = mi.item_id WHERE mi.item_id IS NULL";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int deletedRows = ps.executeUpdate();
                if (deletedRows > 0) {
                    System.out.println("🧹 Cleaned up " + deletedRows + " orphaned order items");
                } else {
                    System.out.println("✅ No orphaned order items found");
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️  Warning: Could not clean up orphaned order items: " + e.getMessage());
        }
    }
    

    private void safeUpdateMenuItems(Connection conn, List<MenuItem> menuItems) {
        System.out.println("🛡️  Safe updating menu items...");
        
        try {
            cleanupOrphanedOrderItems(conn);
            
            Map<String, MenuItem> existingItems = new HashMap<>();
            try (PreparedStatement ps = conn.prepareStatement("SELECT item_id, name, description, price, category, available FROM menu_items")) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String itemId = rs.getString("item_id");
                        String name = rs.getString("name");
                        String description = rs.getString("description");
                        BigDecimal price = rs.getBigDecimal("price");
                        MenuItem.MenuCategory category = MenuItem.MenuCategory.valueOf(rs.getString("category"));
                        boolean available = rs.getBoolean("available");
                        
                        MenuItem item = new MenuItem(itemId, name, description, price, category);
                        item.setAvailable(available);
                        existingItems.put(itemId, item);
                    }
                }
            }
            
            System.out.println("📊 Found " + existingItems.size() + " existing items in database");
            
            int updatedCount = 0;
            int addedCount = 0;
            
            for (MenuItem menuItem : menuItems) {
                if (existingItems.containsKey(menuItem.getItemId())) {
                    try {
                        String sql = "UPDATE menu_items SET name=?, description=?, price=?, category=?, available=?, updated_at=? WHERE item_id=?";
                        try (PreparedStatement ps = conn.prepareStatement(sql)) {
                            ps.setString(1, menuItem.getName());
                            ps.setString(2, menuItem.getDescription());
                            ps.setBigDecimal(3, menuItem.getPrice());
                            ps.setString(4, menuItem.getCategory().name());
                            ps.setBoolean(5, menuItem.isAvailable());
                            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                            ps.setString(7, menuItem.getItemId());
                            
                            int result = ps.executeUpdate();
                            if (result > 0) {
                                System.out.println("✅ Updated existing item: " + menuItem.getName());
                                updatedCount++;
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("❌ Error updating item " + menuItem.getName() + ": " + e.getMessage());
                    }
                } else {
                    try {
                        String sql = "INSERT INTO menu_items (item_id, name, description, price, category, available, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?)";
                        try (PreparedStatement ps = conn.prepareStatement(sql)) {
                            ps.setString(1, menuItem.getItemId());
                            ps.setString(2, menuItem.getName());
                            ps.setString(3, menuItem.getDescription());
                            ps.setBigDecimal(4, menuItem.getPrice());
                            ps.setString(5, menuItem.getCategory().name());
                            ps.setBoolean(6, menuItem.isAvailable());
                            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                            
                            int result = ps.executeUpdate();
                            if (result > 0) {
                                System.out.println("✅ Added new item: " + menuItem.getName());
                                addedCount++;
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("❌ Error adding item " + menuItem.getName() + ": " + e.getMessage());
                    }
                }
            }
            
            System.out.println("🎉 Safe update completed! Updated " + updatedCount + " items, added " + addedCount + " new items");
            
        } catch (SQLException e) {
            System.err.println("❌ Error during safe update: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    public void saveMenuItemsToDatabase() {
        System.out.println("💾 Saving menu items to database...");
        System.out.println("🔗 Database URL: " + DB_URL);
        System.out.println("👤 Database User: " + DB_USER);
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("✅ Database connection established successfully");
            
            long orderItemsCount = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM order_items")) {
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        orderItemsCount = rs.getLong(1);
                    }
                }
            }
            
            if (orderItemsCount > 0) {
                System.out.println("⚠️  Found " + orderItemsCount + " existing order items - using safe update mode");
                safeUpdateMenuItems(conn, menuService.getAllMenuItems());
                return;
            }
            
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM menu_items")) {
                int deleted = ps.executeUpdate();
                System.out.println("✅ Cleared " + deleted + " old menu items from database");
            }
            
            List<MenuItem> allItems = menuService.getAllMenuItems();
            System.out.println("📋 Found " + allItems.size() + " menu items in memory to save");
            
            for (MenuItem item : allItems) {
                System.out.println("💾 Saving: " + item.getName() + " (ID: " + item.getItemId() + ", Category: " + item.getCategory() + ", Price: " + item.getPrice() + ")");
                mergeMenuItem(conn, item);
                System.out.println("✅ Saved to DB: " + item.getName() + " (ID: " + item.getItemId() + ")");
            }
            
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM menu_items")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("🔍 Verification: Found " + count + " items in database after save");
                    }
                }
            }
            
                    System.out.println("✅ Successfully saved " + allItems.size() + " menu items to database");
        
        ensureDrinksInDatabase(conn);
        
    } catch (SQLException e) {
        System.err.println("❌ Error saving menu items to database: " + e.getMessage());
        System.err.println("❌ SQL State: " + e.getSQLState());
        System.err.println("❌ Error Code: " + e.getErrorCode());
        e.printStackTrace();
    }
}
    

    private void ensureDrinksInDatabase(Connection conn) {
        try {
            System.out.println("🥤 Ensuring drink items are in database...");
            
            String checkSql = "SELECT COUNT(*) FROM menu_items WHERE category = 'DRINK'";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int drinkCount = rs.getInt(1);
                        if (drinkCount == 0) {
                            System.out.println("⚠️  No drinks found in database - adding them now...");
                            addDrinkToDatabase(conn, "DRI_COLA_12345", "🥤 Cola", "Ice-cold Coca-Cola - refreshing and classic", 15.00);
                            addDrinkToDatabase(conn, "DRI_SPRI_67890", "🥤 Sprite", "Crisp lemon-lime soda - light and refreshing", 15.00);
                            addDrinkToDatabase(conn, "DRI_WATE_ABCDE", "💧 Water", "Pure bottled water - clean and refreshing", 8.00);
                            addDrinkToDatabase(conn, "DRI_JUIC_FGHIJ", "🍊 Orange Juice", "Fresh squeezed orange juice - natural and healthy", 20.00);
                            addDrinkToDatabase(conn, "DRI_COFF_KLMNO", "☕ Coffee", "Hot brewed coffee - rich and aromatic", 18.00);
                            addDrinkToDatabase(conn, "DRI_TEA_PQRST", "🫖 Tea", "Hot tea - soothing and refreshing", 12.00);
                            
                            System.out.println("✅ Added " + 6 + " drink items directly to database!");
                        } else {
                            System.out.println("✅ Found " + drinkCount + " drink items already in database");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️  Warning: Could not ensure drinks in database: " + e.getMessage());
        }
    }
    
    private void addDrinkToDatabase(Connection conn, String itemId, String name, String description, double price) throws SQLException {
        String sql = "INSERT INTO menu_items (item_id, name, description, price, category, available, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "name=VALUES(name), description=VALUES(description), price=VALUES(price), " +
                     "category=VALUES(category), available=VALUES(available), updated_at=VALUES(updated_at)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            
            ps.setString(1, itemId);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(price));
            ps.setString(5, "DRINK");
            ps.setBoolean(6, true); // available
            ps.setTimestamp(7, now);
            ps.setTimestamp(8, now);
            
            ps.executeUpdate();
            System.out.println("✅ Added drink: " + name);
                }
    }
    

    private void addDrinksToMenuService() {
        try {
            System.out.println("🥤 Adding drinks to menu service...");
            
            long existingDrinks = menuService.getAllMenuItems().stream()
                .filter(item -> item.getCategory() == MenuItem.MenuCategory.DRINK)
                .count();
                
            if (existingDrinks == 0) {
                System.out.println("➕ Adding Cola...");
                menuService.addMenuItem("🥤 Cola", "Ice-cold Coca-Cola - refreshing and classic", new BigDecimal("15.00"), MenuCategory.DRINK);
                System.out.println("✅ Cola added successfully");
                
                System.out.println("➕ Adding Sprite...");
                menuService.addMenuItem("🥤 Sprite", "Crisp lemon-lime soda - light and refreshing", new BigDecimal("15.00"), MenuCategory.DRINK);
                System.out.println("✅ Sprite added successfully");
                
                System.out.println("➕ Adding Water...");
                menuService.addMenuItem("💧 Water", "Pure bottled water - clean and refreshing", new BigDecimal("8.00"), MenuCategory.DRINK);
                System.out.println("✅ Water added successfully");
                
                System.out.println("➕ Adding Tea...");
                menuService.addMenuItem("🫖 Tea", "Hot tea - soothing and refreshing", new BigDecimal("12.00"), MenuCategory.DRINK);
                System.out.println("✅ Tea added successfully");
                
                System.out.println("✅ Added 4 drinks to menu service");
                
                long newDrinkCount = menuService.getAllMenuItems().stream()
                    .filter(item -> item.getCategory() == MenuItem.MenuCategory.DRINK)
                    .count();
                System.out.println("🔍 Verification: " + newDrinkCount + " drinks now in menu service");
                
            } else {
                System.out.println("✅ Drinks already exist in menu service: " + existingDrinks + " items");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error adding drinks to menu service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void ensureDrinksInMenuService() {
        try {
            System.out.println("🥤 Ensuring drinks are in menu service...");
            
            long existingDrinks = menuService.getAllMenuItems().stream()
                .filter(item -> item.getCategory() == MenuItem.MenuCategory.DRINK)
                .count();
                
            if (existingDrinks == 0) {
                System.out.println("⚠️  No drinks in menu service - adding them now...");
                addDrinksToMenuService();
            } else {
                System.out.println("✅ Found " + existingDrinks + " drinks in menu service");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error ensuring drinks in menu service: " + e.getMessage());
            e.printStackTrace();
        }
    }
 
    private void createSampleMenu() {
        try {

            menuService.addMenuItem("🍔 Classic Burger", "Juicy beef patty with lettuce, tomato, and our special sauce", new BigDecimal("45.00"), MenuCategory.MAIN_COURSE);
            menuService.addMenuItem("🍔 Cheese Burger", "Classic burger with melted cheese and crispy bacon", new BigDecimal("55.00"), MenuCategory.MAIN_COURSE);
            menuService.addMenuItem("🍔 Deluxe Burger", "Double patty with cheese, bacon, and premium toppings", new BigDecimal("65.00"), MenuCategory.MAIN_COURSE);
            
            menuService.addMenuItem("🍗 Chicken Wings", "Crispy buffalo wings with ranch dipping sauce", new BigDecimal("35.00"), MenuCategory.SNACK);
            menuService.addMenuItem("🍟 French Fries", "Golden crispy fries with sea salt and ketchup", new BigDecimal("25.00"), MenuCategory.SNACK);
            
            menuService.addMenuItem("🥤 Cola", "Ice-cold Coca-Cola - refreshing and classic", new BigDecimal("15.00"), MenuCategory.DRINK);
            menuService.addMenuItem("🥤 Sprite", "Crisp lemon-lime soda - light and refreshing", new BigDecimal("15.00"), MenuCategory.DRINK);
            menuService.addMenuItem("💧 Water", "Pure bottled water - clean and refreshing", new BigDecimal("8.00"), MenuCategory.DRINK);
            menuService.addMenuItem("🫖 Tea", "Hot tea - soothing and refreshing", new BigDecimal("12.00"), MenuCategory.DRINK);
            
            System.out.println("✅ Created menu with " + menuService.getAllMenuItems().size() + " items (EXACTLY matching student interface)");
            System.out.println("✅ DRINKS NOW INCLUDED: Cola, Sprite, Water, Tea");
            
        } catch (Exception e) {
            System.err.println("Error creating sample menu: " + e.getMessage());
        }
    }
    
    private void createSampleStudents() {
        try {
            authenticationService.registerStudent("student1", "John Smith", "student123", "2023001");
            authenticationService.registerStudent("student2", "Emma Brown", "student123", "2023002");
            authenticationService.registerStudent("student3", "David Wilson", "student123", "2023003");
            authenticationService.registerStudent("student4", "Lisa Davis", "student123", "2023004");
            authenticationService.registerStudent("student5", "Alex Johnson", "student123", "2023005");
            
            loyaltyService.awardPoints("2023001", 150, "Welcome bonus");
            loyaltyService.awardPoints("2023002", 80, "Welcome bonus");
            loyaltyService.awardPoints("2023003", 200, "Welcome bonus");
            
        } catch (Exception e) {
            System.err.println("Error creating sample students: " + e.getMessage());
        }
    }



    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/cafedb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345";

    private static final String SQL_SCHEMA = String.join("\n",
        "CREATE TABLE IF NOT EXISTS users (",
        "  user_id      VARCHAR(50) PRIMARY KEY,",
        "  name         VARCHAR(100) NOT NULL,",
        "  password     VARCHAR(255) NOT NULL,",
        "  user_type    VARCHAR(20)  NOT NULL,",
        "  created_at   TIMESTAMP    NOT NULL",
        ");",
        "CREATE TABLE IF NOT EXISTS students (",
        "  student_id   VARCHAR(20) PRIMARY KEY,",
        "  user_id      VARCHAR(50) NOT NULL,",
        "  CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users(user_id)",
        ");",
        "CREATE TABLE IF NOT EXISTS staff (",
        "  employee_id  VARCHAR(20) PRIMARY KEY,",
        "  user_id      VARCHAR(50) NOT NULL,",
        "  role         VARCHAR(20) NOT NULL,",
        "  CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES users(user_id)",
        ");",
        "CREATE TABLE IF NOT EXISTS menu_items (",
        "  item_id      VARCHAR(50) PRIMARY KEY,",
        "  name         VARCHAR(100) NOT NULL,",
        "  description  VARCHAR(500),",
        "  price        DECIMAL(10,2) NOT NULL,",
        "  category     VARCHAR(30) NOT NULL,",
        "  available    BOOLEAN NOT NULL,",
        "  created_at   TIMESTAMP NOT NULL,",
        "  updated_at   TIMESTAMP NOT NULL",
        ");",
        "CREATE TABLE IF NOT EXISTS orders (",
        "  order_id     VARCHAR(20) PRIMARY KEY,",
        "  student_id   VARCHAR(20) NOT NULL,",
        "  status       VARCHAR(20) NOT NULL,",
        "  total_amount DECIMAL(10,2) NOT NULL,",
        "  points_earned    INT NOT NULL,",
        "  points_redeemed  INT NOT NULL,",
        "  discount_amount  DECIMAL(10,2) NOT NULL,",
        "  order_time   TIMESTAMP NOT NULL,",
        "  status_updated_time TIMESTAMP NOT NULL,",
        "  notes        VARCHAR(500),",
        "  CONSTRAINT fk_orders_student FOREIGN KEY (student_id) REFERENCES students(student_id)",
        ");",
        "CREATE TABLE IF NOT EXISTS order_items (",
        "  order_id     VARCHAR(20) NOT NULL,",
        "  item_id      VARCHAR(50) NOT NULL,",
        "  quantity     INT NOT NULL,",
        "  subtotal     DECIMAL(10,2) NOT NULL,",
        "  PRIMARY KEY(order_id, item_id),",
        "  CONSTRAINT fk_oi_order FOREIGN KEY (order_id) REFERENCES orders(order_id),",
        "  CONSTRAINT fk_oi_item  FOREIGN KEY (item_id)  REFERENCES menu_items(item_id)",
        ");",
        "CREATE TABLE IF NOT EXISTS loyalty_transactions (",
        "  id           IDENTITY PRIMARY KEY,",
        "  student_id   VARCHAR(20) NOT NULL,",
        "  type         VARCHAR(20) NOT NULL,",
        "  points       INT NOT NULL,",
        "  description  VARCHAR(255),",
        "  timestamp    TIMESTAMP NOT NULL,",
        "  CONSTRAINT fk_loyalty_student FOREIGN KEY (student_id) REFERENCES students(student_id)",
        ");",
        "CREATE TABLE IF NOT EXISTS notifications (",
        "  notification_id VARCHAR(30) PRIMARY KEY,",
        "  student_id      VARCHAR(20) NOT NULL,",
        "  title           VARCHAR(100) NOT NULL,",
        "  message         VARCHAR(500) NOT NULL,",
        "  type            VARCHAR(30) NOT NULL,",
        "  timestamp       TIMESTAMP NOT NULL,",
        "  is_read         BOOLEAN NOT NULL,",
        "  CONSTRAINT fk_notif_student FOREIGN KEY (student_id) REFERENCES students(student_id)",
        ");"
    );

    private static final String SQL_SCHEMA_MYSQL = String.join("\n",
        "CREATE TABLE IF NOT EXISTS users (",
        "  user_id      VARCHAR(50) PRIMARY KEY,",
        "  name         VARCHAR(100) NOT NULL,",
        "  password     VARCHAR(255) NOT NULL,",
        "  user_type    VARCHAR(20)  NOT NULL,",
        "  created_at   DATETIME     NOT NULL",
        ");",
        "CREATE TABLE IF NOT EXISTS students (",
        "  student_id   VARCHAR(20) PRIMARY KEY,",
        "  user_id      VARCHAR(50) NOT NULL,",
        "  CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users(user_id)",
        ");",
        "CREATE TABLE IF NOT EXISTS staff (",
        "  employee_id  VARCHAR(20) PRIMARY KEY,",
        "  user_id      VARCHAR(50) NOT NULL,",
        "  role         VARCHAR(20) NOT NULL,",
        "  CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES users(user_id)",
        ");",
        "CREATE TABLE IF NOT EXISTS menu_items (",
        "  item_id      VARCHAR(50) PRIMARY KEY,",
        "  name         VARCHAR(100) NOT NULL,",
        "  description  VARCHAR(500),",
        "  price        DECIMAL(10,2) NOT NULL,",
        "  category     VARCHAR(30) NOT NULL,",
        "  available    TINYINT(1)  NOT NULL,",
        "  created_at   DATETIME NOT NULL,",
        "  updated_at   DATETIME NOT NULL",
        ");",
        "CREATE TABLE IF NOT EXISTS orders (",
        "  order_id     VARCHAR(20) PRIMARY KEY,",
        "  student_id   VARCHAR(20) NOT NULL,",
        "  status       VARCHAR(20) NOT NULL,",
        "  total_amount DECIMAL(10,2) NOT NULL,",
        "  points_earned    INT NOT NULL,",
        "  points_redeemed  INT NOT NULL,",
        "  discount_amount  DECIMAL(10,2) NOT NULL,",
        "  order_time   DATETIME NOT NULL,",
        "  status_updated_time DATETIME NOT NULL,",
        "  notes        VARCHAR(500),",
        "  CONSTRAINT fk_orders_student FOREIGN KEY (student_id) REFERENCES students(student_id)",
        ");",
        "CREATE TABLE IF NOT EXISTS order_items (",
        "  order_id     VARCHAR(20) NOT NULL,",
        "  item_id      VARCHAR(50) NOT NULL,",
        "  quantity     INT NOT NULL,",
        "  subtotal     DECIMAL(10,2) NOT NULL,",
        "  PRIMARY KEY(order_id, item_id),",
        "  CONSTRAINT fk_oi_order FOREIGN KEY (order_id) REFERENCES orders(order_id),",
        "  CONSTRAINT fk_oi_item  FOREIGN KEY (item_id)  REFERENCES menu_items(item_id)",
        ");",
        "CREATE TABLE IF NOT EXISTS loyalty_transactions (",
        "  id           BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,",
        "  student_id   VARCHAR(20) NOT NULL,",
        "  type         VARCHAR(20) NOT NULL,",
        "  points       INT NOT NULL,",
        "  description  VARCHAR(255),",
        "  timestamp    DATETIME NOT NULL,",
        "  CONSTRAINT fk_loyalty_student FOREIGN KEY (student_id) REFERENCES students(student_id)",
        ");",
        "CREATE TABLE IF NOT EXISTS notifications (",
        "  notification_id VARCHAR(30) PRIMARY KEY,",
        "  student_id      VARCHAR(20) NOT NULL,",
        "  title           VARCHAR(100) NOT NULL,",
        "  message         VARCHAR(500) NOT NULL,",
        "  type            VARCHAR(30) NOT NULL,",
        "  timestamp       DATETIME NOT NULL,",
        "  is_read         TINYINT(1) NOT NULL,",
        "  CONSTRAINT fk_notif_student FOREIGN KEY (student_id) REFERENCES students(student_id)",
        ");"
    );

    private void initializeDatabase() {
        System.out.println("Initializing embedded H2 database and applying schema...");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            final String schema = isMySql() ? SQL_SCHEMA_MYSQL : SQL_SCHEMA;
            applySchema(conn, schema);
            seedDatabase(conn);
            System.out.println("H2 database ready at: " + DB_URL);
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    private boolean isMySql() {
        return DB_URL != null && DB_URL.startsWith("jdbc:mysql:");
    }

    private void applySchema(Connection conn, String ddl) throws SQLException {
        for (String stmt : ddl.split(";\\s*\\n")) {
            String sql = stmt.trim();
            if (sql.isEmpty()) continue;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.executeUpdate();
            }
        }
    }

    private void seedDatabase(Connection conn) throws SQLException {
        for (Student s : userRepository.findAllStudents()) {
            String password = getPasswordForUser(s.getUserId(), "STUDENT");
            mergeUser(conn, s.getUserId(), s.getName(), password, "STUDENT", Timestamp.valueOf(s.getCreatedAt()));
            mergeStudent(conn, s.getStudentId(), s.getUserId());
            s.getLoyaltyAccount().getTransactions().forEach(t -> {
                try {
                    insertLoyaltyTransaction(conn, s.getStudentId(), t.getType().name(), t.getPoints(), t.getDescription(), Timestamp.valueOf(t.getTimestamp()));
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            });

            for (NotificationService.Notification n : notificationService.getNotificationsForStudent(s.getStudentId())) {
                insertNotification(conn, n.getNotificationId(), n.getStudentId(), n.getTitle(), n.getMessage(), n.getType().name(), Timestamp.valueOf(n.getTimestamp()), n.isRead());
            }
        }
        for (Staff st : userRepository.findAllStaff()) {
            String password = getPasswordForUser(st.getUserId(), "STAFF");
            mergeUser(conn, st.getUserId(), st.getName(), password, "STAFF", Timestamp.valueOf(st.getCreatedAt()));
            mergeStaff(conn, st.getEmployeeId(), st.getUserId(), st.getRole().name());
        }

        try {
            long menuCount = queryCount(conn, "SELECT COUNT(*) FROM menu_items");
            if (menuCount == 0) {
                for (MenuItem mi : menuService.getAllMenuItems()) {
                    mergeMenuItem(conn, mi);
                }
            }
        } catch (SQLException ignore) {
            for (MenuItem mi : menuService.getAllMenuItems()) {
                mergeMenuItem(conn, mi);
            }
        }

        for (Order o : orderRepository.findAll()) {
            mergeOrder(conn, o);
            for (Order.OrderItem oi : o.getItems()) {
                mergeOrderItem(conn, o.getOrderId(), oi.getMenuItem().getItemId(), oi.getQuantity(), oi.getSubtotal().doubleValue());
            }
        }
    }

    private void mergeUser(Connection conn, String userId, String name, String password, String userType, Timestamp createdAt) throws SQLException {
        final String sql = isMySql()
            ? "INSERT INTO users (user_id, name, password, user_type, created_at) VALUES (?,?,?,?,?) " +
              "ON DUPLICATE KEY UPDATE name=VALUES(name), password=VALUES(password), user_type=VALUES(user_type), created_at=VALUES(created_at)"
            : "MERGE INTO users (user_id, name, password, user_type, created_at) KEY(user_id) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, name);
            ps.setString(3, password);
            ps.setString(4, userType);
            ps.setTimestamp(5, createdAt);
            ps.executeUpdate();
        }
    }

    private void mergeStudent(Connection conn, String studentId, String userId) throws SQLException {
        final String sql = isMySql()
            ? "INSERT INTO students (student_id, user_id) VALUES (?,?) ON DUPLICATE KEY UPDATE user_id=VALUES(user_id)"
            : "MERGE INTO students (student_id, user_id) KEY(student_id) VALUES (?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, userId);
            ps.executeUpdate();
        }
    }

    private void mergeStaff(Connection conn, String employeeId, String userId, String role) throws SQLException {
        final String sql = isMySql()
            ? "INSERT INTO staff (employee_id, user_id, role) VALUES (?,?,?) ON DUPLICATE KEY UPDATE user_id=VALUES(user_id), role=VALUES(role)"
            : "MERGE INTO staff (employee_id, user_id, role) KEY(employee_id) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            ps.setString(2, userId);
            ps.setString(3, role);
            ps.executeUpdate();
        }
    }

    private void mergeMenuItem(Connection conn, MenuItem mi) throws SQLException {
        final String sql = isMySql()
            ? "INSERT INTO menu_items (item_id, name, description, price, category, available, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?) " +
              "ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description), price=VALUES(price), category=VALUES(category), available=VALUES(available), created_at=VALUES(created_at), updated_at=VALUES(updated_at)"
            : "MERGE INTO menu_items (item_id, name, description, price, category, available, created_at, updated_at) KEY(item_id) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mi.getItemId());
            ps.setString(2, mi.getName());
            ps.setString(3, mi.getDescription());
            ps.setBigDecimal(4, mi.getPrice());
            ps.setString(5, mi.getCategory().name());
            if (isMySql()) {
                ps.setBoolean(6, mi.isAvailable());
            } else {
                ps.setBoolean(6, mi.isAvailable());
            }
            ps.setTimestamp(7, Timestamp.valueOf(mi.getCreatedAt()));
            ps.setTimestamp(8, Timestamp.valueOf(mi.getUpdatedAt()));
            ps.executeUpdate();
        }
    }

    private void mergeOrder(Connection conn, Order o) throws SQLException {
        final String sql = isMySql()
            ? "INSERT INTO orders (order_id, student_id, status, total_amount, points_earned, points_redeemed, discount_amount, order_time, status_updated_time, notes) VALUES (?,?,?,?,?,?,?,?,?,?) " +
              "ON DUPLICATE KEY UPDATE student_id=VALUES(student_id), status=VALUES(status), total_amount=VALUES(total_amount), points_earned=VALUES(points_earned), points_redeemed=VALUES(points_redeemed), discount_amount=VALUES(discount_amount), order_time=VALUES(order_time), status_updated_time=VALUES(status_updated_time), notes=VALUES(notes)"
            : "MERGE INTO orders (order_id, student_id, status, total_amount, points_earned, points_redeemed, discount_amount, order_time, status_updated_time, notes) KEY(order_id) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, o.getOrderId());
            ps.setString(2, o.getStudentId());
            ps.setString(3, o.getStatus().name());
            ps.setBigDecimal(4, o.getTotalAmount());
            ps.setInt(5, o.getLoyaltyPointsEarned());
            ps.setInt(6, o.getLoyaltyPointsRedeemed());
            ps.setBigDecimal(7, o.getDiscountAmount());
            ps.setTimestamp(8, Timestamp.valueOf(o.getOrderTime()));
            ps.setTimestamp(9, Timestamp.valueOf(o.getStatusUpdatedTime()));
            ps.setString(10, o.getNotes());
            ps.executeUpdate();
        }
    }

    private void mergeOrderItem(Connection conn, String orderId, String itemId, int qty, double subtotal) throws SQLException {
        final String sql = isMySql()
            ? "INSERT INTO order_items (order_id, item_id, quantity, subtotal) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE quantity=VALUES(quantity), subtotal=VALUES(subtotal)"
            : "MERGE INTO order_items (order_id, item_id, quantity, subtotal) KEY(order_id, item_id) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            ps.setString(2, itemId);
            ps.setInt(3, qty);
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(subtotal));
            ps.executeUpdate();
        }
    }

    private void insertLoyaltyTransaction(Connection conn, String studentId, String type, int points, String description, Timestamp ts) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO loyalty_transactions (student_id, type, points, description, timestamp) VALUES (?,?,?,?,?)")) {
            ps.setString(1, studentId);
            ps.setString(2, type);
            ps.setInt(3, points);
            ps.setString(4, description);
            ps.setTimestamp(5, ts);
            ps.executeUpdate();
        }
    }

    private void insertNotification(Connection conn, String id, String studentId, String title, String message, String type, Timestamp ts, boolean isRead) throws SQLException {
        final String sql = isMySql()
            ? "INSERT INTO notifications (notification_id, student_id, title, message, type, timestamp, is_read) VALUES (?,?,?,?,?,?,?) " +
              "ON DUPLICATE KEY UPDATE student_id=VALUES(student_id), title=VALUES(title), message=VALUES(message), type=VALUES(type), timestamp=VALUES(timestamp), is_read=VALUES(is_read)"
            : "MERGE INTO notifications (notification_id, student_id, title, message, type, timestamp, is_read) KEY(notification_id) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, studentId);
            ps.setString(3, title);
            ps.setString(4, message);
            ps.setString(5, type);
            ps.setTimestamp(6, ts);
            if (isMySql()) {
                ps.setBoolean(7, isRead);
            } else {
                ps.setBoolean(7, isRead);
            }
            ps.executeUpdate();
        }
    }
    

    public SystemStatistics getSystemStatistics() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            long totalUsers = queryCount(conn, "SELECT COUNT(*) FROM users");
            long totalStudents = queryCount(conn, "SELECT COUNT(*) FROM students");
            long totalStaff = queryCount(conn, "SELECT COUNT(*) FROM staff");

            String availExpr = isMySql() ? "1" : "TRUE";
            long totalMenuItems = queryCount(conn, "SELECT COUNT(*) FROM menu_items");
            long availableMenuItems = queryCount(conn, "SELECT COUNT(*) FROM menu_items WHERE available = " + availExpr);

            long totalOrders = queryCount(conn, "SELECT COUNT(*) FROM orders");
            long[] ordersByStatus = queryOrderStatusCounts(conn);

            return new SystemStatistics(
                totalUsers, totalStudents, totalStaff,
                totalMenuItems, availableMenuItems,
                totalOrders, ordersByStatus
            );
        } catch (SQLException e) {
            long totalUsers = authenticationService.getTotalUserCount();
            long totalStudents = authenticationService.getStudentCount();
            long totalStaff = authenticationService.getStaffCount();

            long totalMenuItems = menuService.getAllMenuItems().size();
            long availableMenuItems = menuService.getAvailableMenuItems().size();

            long totalOrders = orderRepository.getOrderCount();
            long[] ordersByStatus = orderService.getOrderStatistics();

            return new SystemStatistics(
                totalUsers, totalStudents, totalStaff,
                totalMenuItems, availableMenuItems,
                totalOrders, ordersByStatus
            );
        }
    }

    private long queryCount(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }

    private long[] queryOrderStatusCounts(Connection conn) throws SQLException {
        long[] counts = new long[com.university.cafeteria.model.Order.OrderStatus.values().length];
        String sql = "SELECT status, COUNT(*) FROM orders GROUP BY status";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String status = rs.getString(1);
                long c = rs.getLong(2);
                try {
                    int idx = com.university.cafeteria.model.Order.OrderStatus.valueOf(status).ordinal();
                    counts[idx] = c;
                } catch (IllegalArgumentException ignored) { }
            }
        }
        return counts;
    }

    public void shutdown() {
        System.out.println("Shutting down cafeteria system...");
        
        ((NotificationServiceImpl) notificationService).performMaintenanceCleanup();
        
        System.out.println("System shutdown completed.");
    }
    
    private String getPasswordForUser(String userId, String userType) {
        if ("STAFF".equals(userType)) {
            switch (userId) {
                case "admin": return "admin123";
                case "manager1": return "manager123";
                case "cashier1": return "cashier123";
                case "kitchen1": return "kitchen123";
                default: return "staff123";
            }
        } else if ("STUDENT".equals(userType)) {
            switch (userId) {
                case "student1": 
                case "student2": 
                case "student3": 
                case "student4": 
                case "student5": return "student123";
                default: return "student123";
            }
        }
        return "default123";
    }
    
    private void loadUsersFromDatabase(Connection conn) throws SQLException {
        String sql = "SELECT u.user_id, u.name, u.password, u.user_type, u.created_at, " +
                    "s.student_id, st.employee_id, st.role " +
                    "FROM users u " +
                    "LEFT JOIN students s ON u.user_id = s.user_id " +
                    "LEFT JOIN staff st ON u.user_id = st.user_id";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String userId = rs.getString("user_id");
                String name = rs.getString("name");
                String password = rs.getString("password");
                String userType = rs.getString("user_type");
                
                if ("STUDENT".equals(userType)) {
                    String studentId = rs.getString("student_id");
                    if (studentId != null) {
                        String actualPassword = password;
                        if ("(in-memory)".equals(password)) {
                            actualPassword = "student123";
                        }
                        
                        Student student = new Student(userId, name, actualPassword, studentId);
                        userRepository.save(student);
                        System.out.println("✓ Loaded student: " + studentId + " (" + userId + ")");
                    }
                } else if ("STAFF".equals(userType)) {
                    String employeeId = rs.getString("employee_id");
                    String roleStr = rs.getString("role");
                    if (employeeId != null && roleStr != null) {
                        Staff.StaffRole role = Staff.StaffRole.valueOf(roleStr);
                        
                        String actualPassword = password;
                        if ("(in-memory)".equals(password)) {
                            switch (userId) {
                                case "admin": actualPassword = "admin123"; break;
                                case "manager1": actualPassword = "manager123"; break;
                                case "cashier1": actualPassword = "cashier123"; break;
                                case "kitchen1": actualPassword = "kitchen123"; break;
                                default: actualPassword = "staff123"; break;
                            }
                        }
                        
                        Staff staff = new Staff(userId, name, actualPassword, employeeId, role);
                        userRepository.save(staff);
                        System.out.println("✓ Loaded staff: " + employeeId + " (" + userId + ")");
                    }
                }
            }
        }
    }
    
    private void loadMenuItemsFromDatabase(Connection conn) throws SQLException {
        String sql = "SELECT item_id, name, description, price, category, available FROM menu_items";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String itemId = rs.getString("item_id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                java.math.BigDecimal price = rs.getBigDecimal("price");
                String categoryStr = rs.getString("category");
                boolean available = rs.getBoolean("available");
                
                try {
                    MenuItem.MenuCategory category = MenuItem.MenuCategory.valueOf(categoryStr);
                    MenuItem menuItem = new MenuItem(itemId, name, description, price, category);
                    menuItem.setAvailable(available);
                    menuRepository.save(menuItem);
                    System.out.println("✓ Loaded menu item: " + name + " (Category: " + category + ")");
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: Unknown category " + categoryStr + " for item " + name);
                }
            }
        }
        
        int drinkCount = (int) menuService.getAllMenuItems().stream()
            .filter(item -> item.getCategory() == MenuItem.MenuCategory.DRINK)
            .count();
        System.out.println("🥤 Loaded " + drinkCount + " drink items from database");
    }
    
    private void loadLoyaltyTransactionsFromDatabase(Connection conn) throws SQLException {
        String sql = "SELECT student_id, type, points, description, timestamp FROM loyalty_transactions ORDER BY timestamp";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String studentId = rs.getString("student_id");
                String type = rs.getString("type");
                int points = rs.getInt("points");
                String description = rs.getString("description");
                java.sql.Timestamp timestamp = rs.getTimestamp("timestamp");
                
                Optional<Student> studentOpt = userRepository.findStudentByStudentId(studentId);
                if (studentOpt.isPresent()) {
                    Student student = studentOpt.get();
                    
                    if ("EARNED".equals(type)) {
                        student.getLoyaltyAccount().addPoints(points, description);
                    } else if ("REDEEMED".equals(type)) {
                        student.getLoyaltyAccount().addRedemptionTransaction(points, description);
                    }
                    
                    userRepository.update(student);
                    System.out.println("✓ Loaded loyalty transaction for " + studentId + ": " + type + " " + points + " points");
                } else {
                    System.err.println("Warning: Found loyalty transaction for unknown student: " + studentId);
                }
            }
        }
    }
    
    private void loadOrdersFromDatabase(Connection conn) throws SQLException {
        String sql = "SELECT order_id, student_id, status, total_amount, points_earned, points_redeemed, discount_amount, order_time, status_updated_time, notes FROM orders";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String orderId = rs.getString("order_id");
                String studentId = rs.getString("student_id");
                String statusStr = rs.getString("status");
                java.math.BigDecimal totalAmount = rs.getBigDecimal("total_amount");
                int pointsEarned = rs.getInt("points_earned");
                int pointsRedeemed = rs.getInt("points_redeemed");
                java.math.BigDecimal discountAmount = rs.getBigDecimal("discount_amount");
                java.sql.Timestamp orderTime = rs.getTimestamp("order_time");
                java.sql.Timestamp statusUpdatedTime = rs.getTimestamp("status_updated_time");
                String notes = rs.getString("notes");
                
                try {
                    Order order = new Order(studentId);
                    order.setOrderId(orderId);
                    order.setTotalAmount(totalAmount);
                    order.setLoyaltyPointsEarned(pointsEarned);
                    order.setLoyaltyPointsRedeemed(pointsRedeemed);
                    order.setDiscountAmount(discountAmount);
                    order.setOrderTime(orderTime.toLocalDateTime());
                    order.setStatusUpdatedTime(statusUpdatedTime.toLocalDateTime());
                    order.setNotes(notes);
                    
                    Order.OrderStatus status = Order.OrderStatus.valueOf(statusStr);
                    order.updateStatus(status);
                    
                    loadOrderItems(conn, order);
                    
                    orderRepository.save(order);
                    System.out.println("✓ Loaded order: " + orderId + " (" + statusStr + ")");
                    
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: Unknown status " + statusStr + " for order " + orderId);
                }
            }
        }
        System.out.println("✓ Orders loaded from database");
    }
    
    private void loadOrderItems(Connection conn, Order order) throws SQLException {
        String sql = "SELECT oi.item_id, oi.quantity, oi.subtotal, mi.name, mi.description, mi.price, mi.category " +
                    "FROM order_items oi " +
                    "JOIN menu_items mi ON oi.item_id = mi.item_id " +
                    "WHERE oi.order_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, order.getOrderId());
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String itemId = rs.getString("item_id");
                    int quantity = rs.getInt("quantity");
                    java.math.BigDecimal subtotal = rs.getBigDecimal("subtotal");
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    java.math.BigDecimal price = rs.getBigDecimal("price");
                    String categoryStr = rs.getString("category");
                    
                    try {
                        MenuItem.MenuCategory category = MenuItem.MenuCategory.valueOf(categoryStr);
                        MenuItem menuItem = new MenuItem(itemId, name, description, price, category);
                        
                        order.addItem(menuItem, quantity);
                        
                    } catch (IllegalArgumentException e) {
                        System.err.println("Warning: Unknown category " + categoryStr + " for item " + itemId);
                    }
                }
            }
        }
    }
    

    public static class SystemStatistics {
        private final long totalUsers;
        private final long totalStudents;
        private final long totalStaff;
        private final long totalMenuItems;
        private final long availableMenuItems;
        private final long totalOrders;
        private final long[] ordersByStatus;
        
        public SystemStatistics(long totalUsers, long totalStudents, long totalStaff,
                               long totalMenuItems, long availableMenuItems,
                               long totalOrders, long[] ordersByStatus) {
            this.totalUsers = totalUsers;
            this.totalStudents = totalStudents;
            this.totalStaff = totalStaff;
            this.totalMenuItems = totalMenuItems;
            this.availableMenuItems = availableMenuItems;
            this.totalOrders = totalOrders;
            this.ordersByStatus = ordersByStatus;
        }
        
        public long getTotalUsers() { return totalUsers; }
        public long getTotalStudents() { return totalStudents; }
        public long getTotalStaff() { return totalStaff; }
        public long getTotalMenuItems() { return totalMenuItems; }
        public long getAvailableMenuItems() { return availableMenuItems; }
        public long getTotalOrders() { return totalOrders; }
        public long[] getOrdersByStatus() { return ordersByStatus; }
        
        @Override
        public String toString() {
            return String.format(
                "SystemStatistics{users=%d, students=%d, staff=%d, menuItems=%d/%d, orders=%d}",
                totalUsers, totalStudents, totalStaff, availableMenuItems, totalMenuItems, totalOrders
            );
        }
    }


    public void forceDatabaseSync() {
        System.out.println("🔄 Force synchronizing database with menu service...");
        
        try {
            List<MenuItem> menuItems = menuService.getAllMenuItems();
            System.out.println("📋 Menu service has " + menuItems.size() + " items");
            
            smartSyncMenuItemsToDatabase(menuItems);
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM menu_items")) {
                    try (var rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int dbCount = rs.getInt(1);
                            System.out.println("✅ Database now has " + dbCount + " menu items");
                            
                            if (dbCount == menuItems.size()) {
                                System.out.println("🎉 Database synchronization successful!");
                            } else {
                                System.out.println("⚠️  Warning: Sync may not be complete");
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("❌ Error verifying database sync: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error during database sync: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test database connection and menu service functionality
     */
    public void testDatabaseAndMenuService() {
        System.out.println("🧪 Testing database and menu service...");
        
        try {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                System.out.println("✅ Database connection successful");
                
                int dbCount = 0;
                try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM menu_items")) {
                    try (var rs = ps.executeQuery()) {
                        if (rs.next()) {
                            dbCount = rs.getInt(1);
                            System.out.println("📊 Database has " + dbCount + " menu items");
                        }
                    }
                }
                
                List<MenuItem> menuItems = menuService.getAllMenuItems();
                System.out.println("📋 Menu service has " + menuItems.size() + " items");
                
                menuItems.forEach(item ->
                    System.out.println("   - " + item.getName() + " (ID: " + item.getItemId() + ", Available: " + item.isAvailable() + ")")
                );
                
                if (dbCount != menuItems.size()) {
                    System.out.println("⚠️  WARNING: Database and menu service are out of sync!");
                    System.out.println("🔄 Syncing database with menu service...");
                    
                    saveMenuItemsToDatabase();
                    
                    try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM menu_items")) {
                        try (var rs = ps.executeQuery()) {
                            if (rs.next()) {
                                int newDbCount = rs.getInt(1);
                                System.out.println("✅ After sync: Database has " + newDbCount + " menu items");
                                
                                if (newDbCount == menuItems.size()) {
                                    System.out.println("🎉 Database and menu service are now synchronized!");
                                } else {
                                    System.out.println("❌ Sync failed - still out of sync");
                                }
                            }
                        }
                    }
                } else {
                    System.out.println("✅ Database and menu service are synchronized");
                }
                
            } catch (SQLException e) {
                System.err.println("❌ Database connection failed: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void testDrinksAvailability() {
        System.out.println("🧪 Testing drinks availability...");
        
        System.out.println("🔍 Testing basic menu service...");
        try {
            List<MenuItem> allItems = menuService.getAllMenuItems();
            System.out.println("✅ Menu service working - found " + allItems.size() + " items");
            
            allItems.forEach(item ->
                System.out.println("   - " + item.getName() + " (Category: " + item.getCategory() + ")")
            );
            
        } catch (Exception e) {
            System.err.println("❌ Menu service error: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        long drinkCount = menuService.getAllMenuItems().stream()
            .filter(item -> item.getCategory() == MenuItem.MenuCategory.DRINK)
            .count();
            
        System.out.println("🥤 Found " + drinkCount + " drinks in menu service");
        
        if (drinkCount > 0) {
            System.out.println("✅ DRINKS ARE AVAILABLE for ordering:");
            menuService.getAllMenuItems().stream()
                .filter(item -> item.getCategory() == MenuItem.MenuCategory.DRINK)
                .forEach(item -> System.out.println("   - " + item.getName() + " (" + item.getFormattedPrice() + ")"));
        } else {
            System.out.println("❌ NO DRINKS FOUND - Adding them now...");
            addDrinksToMenuService();
            
            drinkCount = menuService.getAllMenuItems().stream()
                .filter(item -> item.getCategory() == MenuItem.MenuCategory.DRINK)
                .count();
            System.out.println("🥤 After adding: " + drinkCount + " drinks available");
        }
    }
    

    public void refreshMenuFromDatabase() {
        System.out.println("🔄 Manually refreshing menu from database...");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            if (menuRepository instanceof com.university.cafeteria.repository.impl.InMemoryMenuRepository) {
                ((com.university.cafeteria.repository.impl.InMemoryMenuRepository) menuRepository).clear();
            }
            
            loadMenuItemsFromDatabase(conn);
            
            ensureDrinksInDatabase(conn);
            
            ensureDrinksInMenuService();
            
            System.out.println("✅ Menu refreshed! Total items: " + menuService.getAllMenuItems().size());
            
            int drinkCount = (int) menuService.getAllMenuItems().stream()
                .filter(item -> item.getCategory() == MenuItem.MenuCategory.DRINK)
                .count();
            System.out.println("🥤 Drink items available: " + drinkCount);
            
            System.out.println("🔍 All items in menu service:");
            menuService.getAllMenuItems().forEach(item -> 
                System.out.println("   - " + item.getName() + " (Category: " + item.getCategory() + ")")
            );
            
            if (drinkCount > 0) {
                System.out.println("✅ DRINKS ARE WORKING! Available drinks:");
                menuService.getAllMenuItems().stream()
                    .filter(item -> item.getCategory() == MenuItem.MenuCategory.DRINK)
                    .forEach(item -> System.out.println("   - " + item.getName() + " (" + item.getFormattedPrice() + ")"));
            } else {
                System.out.println("❌ NO DRINKS FOUND - This needs to be fixed!");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error refreshing menu: " + e.getMessage());
        }
    }
}