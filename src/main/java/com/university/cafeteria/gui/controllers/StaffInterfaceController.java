package com.university.cafeteria.gui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import com.university.cafeteria.service.CafeteriaSystem;
import com.university.cafeteria.model.User;
import com.university.cafeteria.model.Student;
import com.university.cafeteria.model.Staff;
import com.university.cafeteria.model.Order;
import com.university.cafeteria.model.MenuItem;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Controller for the staff interface
 * Handles order management, menu management, and reporting based on staff role
 */
public class StaffInterfaceController {

    private CafeteriaSystem cafeteriaSystem;
    private Stage primaryStage;
    private Staff staff;
    private ObservableList<Order> orders;
    private ObservableList<MenuItem> menuItems;

    public StaffInterfaceController(CafeteriaSystem cafeteriaSystem, Stage primaryStage, Staff staff) {
        this.cafeteriaSystem = cafeteriaSystem;
        this.primaryStage = primaryStage;
        this.staff = staff;
        this.orders = FXCollections.observableArrayList();
        this.menuItems = FXCollections.observableArrayList();
        loadData();
    }

    public Scene createStaffScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Top bar
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // Main content with tabs based on role
        TabPane tabPane = createTabPane();
        root.setCenter(tabPane);

        return new Scene(root, 1400, 900);
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15));
        topBar.setStyle("-fx-background-color: #FF9800;");

        Label welcomeLabel = new Label("Welcome, " + staff.getName() + " (" + staff.getRole() + ")");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        welcomeLabel.setTextFill(Color.WHITE);

        Label shiftLabel = new Label("Role: " + staff.getRole().getDescription());
        shiftLabel.setFont(Font.font("Arial", 14));
        shiftLabel.setTextFill(Color.WHITE);
        shiftLabel.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 15; -fx-padding: 5 15;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        refreshButton.setOnAction(e -> refreshData());

        Button backToLoginButton = new Button("← Back to Login");
        backToLoginButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        backToLoginButton.setOnAction(e -> backToLogin());

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        logoutButton.setOnAction(e -> logout());

        topBar.getChildren().addAll(welcomeLabel, shiftLabel, spacer, refreshButton, backToLoginButton, logoutButton);
        return topBar;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Order Management tab (all roles)
        Tab ordersTab = new Tab("Order Management");
        ordersTab.setContent(createOrderManagementTab());
        tabPane.getTabs().add(ordersTab);

        // Menu Management tab (Admin, Manager)
        if (staff.getRole() == Staff.StaffRole.ADMIN || staff.getRole() == Staff.StaffRole.MANAGER) {
            Tab menuTab = new Tab("Menu Management");
            menuTab.setContent(createMenuManagementTab());
            tabPane.getTabs().add(menuTab);
        }

        // Reports tab (Admin, Manager)
        if (staff.getRole() == Staff.StaffRole.ADMIN || staff.getRole() == Staff.StaffRole.MANAGER) {
            Tab reportsTab = new Tab("Reports & Analytics");
            reportsTab.setContent(createReportsTab());
            tabPane.getTabs().add(reportsTab);
        }

        // User Management tab (Admin only)
        if (staff.getRole() == Staff.StaffRole.ADMIN) {
            Tab usersTab = new Tab("User Management");
            usersTab.setContent(createUserManagementTab());
            tabPane.getTabs().add(usersTab);
        }

        return tabPane;
    }

    private VBox createOrderManagementTab() {
        VBox orderContent = new VBox(15);
        orderContent.setPadding(new Insets(20));

        Label orderLabel = new Label("Order Management");
        orderLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        // Filter section
        HBox filterBox = new HBox(15);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label statusLabel = new Label("Filter by Status:");
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "PENDING", "CONFIRMED", "PREPARING", "READY", "COMPLETED", "CANCELLED");
        statusFilter.setValue("All");
        statusFilter.setOnAction(e -> filterOrders(statusFilter.getValue()));

        filterBox.getChildren().addAll(statusLabel, statusFilter);

        // Orders table
        TableView<Order> ordersTable = createOrdersTable();

        orderContent.getChildren().addAll(orderLabel, filterBox, ordersTable);
        return orderContent;
    }

    private TableView<Order> createOrdersTable() {
        TableView<Order> table = new TableView<>();
        table.setItems(orders);

        TableColumn<Order, String> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderIdCol.setPrefWidth(120);

        TableColumn<Order, String> customerCol = new TableColumn<>("Customer ID");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        customerCol.setPrefWidth(100);

        TableColumn<Order, String> dateCol = new TableColumn<>("Date & Time");
        dateCol.setCellValueFactory(cellData -> {
            String formattedDate = cellData.getValue().getOrderTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return new javafx.beans.property.SimpleStringProperty(formattedDate);
        });
        dateCol.setPrefWidth(130);

        TableColumn<Order, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(cellData -> {
            Order order = cellData.getValue();
            BigDecimal total = order.getTotalAmount();
            BigDecimal discount = order.getDiscountAmount();

            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                // Show original total and discount
                BigDecimal originalTotal = total.add(discount);
                return new javafx.beans.property.SimpleStringProperty(
                        String.format("%.2f (-%.2f)", originalTotal, discount));
            } else {
                return new javafx.beans.property.SimpleStringProperty(String.format("%.2f", total));
            }
        });
        totalCol.setPrefWidth(100);

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        TableColumn<Order, String> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(cellData -> {
            Order order = cellData.getValue();
            List<Order.OrderItem> items = order.getItems();
            StringBuilder itemsText = new StringBuilder();

            for (Order.OrderItem item : items) {
                String itemName = item.getMenuItem().getName();
                int quantity = item.getQuantity();

                // Check if this item was free due to loyalty redemption
                if (order.getLoyaltyPointsRedeemed() > 0 &&
                        order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0 &&
                        item.getSubtotal().compareTo(BigDecimal.ZERO) == 0) {
                    itemsText.append("💧 ").append(itemName).append(" (Free via Loyalty) (").append(quantity).append("), ");
                } else {
                    itemsText.append(itemName).append(" (").append(quantity).append("), ");
                }
            }

            return new javafx.beans.property.SimpleStringProperty(
                    itemsText.length() > 0 ? itemsText.substring(0, itemsText.length() - 2) : "");
        });
        itemsCol.setPrefWidth(250);

        // Loyalty Redemption column
        TableColumn<Order, String> loyaltyCol = new TableColumn<>("Loyalty");
        loyaltyCol.setCellValueFactory(cellData -> {
            Order order = cellData.getValue();
            int pointsRedeemed = order.getLoyaltyPointsRedeemed();
            BigDecimal discount = order.getDiscountAmount();

            if (pointsRedeemed > 0 && discount.compareTo(BigDecimal.ZERO) > 0) {
                return new javafx.beans.property.SimpleStringProperty(
                        String.format("%d pts (-%.2f)", pointsRedeemed, discount));
            } else {
                return new javafx.beans.property.SimpleStringProperty("-");
            }
        });
        loyaltyCol.setPrefWidth(100);

        TableColumn<Order, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(200);
        actionCol.setCellFactory(col -> new TableCell<Order, Void>() {
            private final HBox actionBox = new HBox(5);
            private final Button updateStatusButton = new Button("Update Status");
            private final Button viewDetailsButton = new Button("Details");

            {
                updateStatusButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 10px;");
                viewDetailsButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 10px;");

                updateStatusButton.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    showUpdateStatusDialog(order);
                });

                viewDetailsButton.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    showOrderDetails(order);
                });

                actionBox.getChildren().addAll(updateStatusButton, viewDetailsButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });

        table.getColumns().addAll(orderIdCol, customerCol, dateCol, totalCol, statusCol, itemsCol, loyaltyCol, actionCol);
        return table;
    }

    private VBox createMenuManagementTab() {
        VBox menuContent = new VBox(15);
        menuContent.setPadding(new Insets(20));

        Label menuLabel = new Label("Menu Management");
        menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER_LEFT);

        Button addItemButton = new Button("Add New Item");
        addItemButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        addItemButton.setOnAction(e -> showAddItemDialog());

        Button refreshMenuButton = new Button("Refresh Menu");
        refreshMenuButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        refreshMenuButton.setOnAction(e -> loadMenuItems());

        Button updateMenuButton = new Button("Update with New Items");
        updateMenuButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");
        updateMenuButton.setOnAction(e -> updateMenuWithNewItems());

        Button saveToDbButton = new Button("Save Menu to Database");
        saveToDbButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        saveToDbButton.setOnAction(e -> {
            try {
                cafeteriaSystem.saveMenuItemsToDatabase();
                showAlert("Success", "Menu items have been saved to database successfully!");
            } catch (Exception ex) {
                showAlert("Error", "Failed to save menu items to database: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        Button syncDbButton = new Button("🔄 Sync Database");
        syncDbButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");
        syncDbButton.setOnAction(e -> {
            try {
                cafeteriaSystem.forceDatabaseSync();
                loadMenuItems(); // Reload after sync
                showAlert("Success", "Database synchronized successfully!");
            } catch (Exception ex) {
                showAlert("Error", "Failed to sync database: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        actionButtons.getChildren().addAll(addItemButton, refreshMenuButton, updateMenuButton, saveToDbButton, syncDbButton);

        // Menu items table
        TableView<MenuItem> menuTable = createMenuManagementTable();

        menuContent.getChildren().addAll(menuLabel, actionButtons, menuTable);
        return menuContent;
    }

    private TableView<MenuItem> createMenuManagementTable() {
        TableView<MenuItem> table = new TableView<>();
        table.setItems(menuItems);

        TableColumn<MenuItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);

        TableColumn<MenuItem, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);

        TableColumn<MenuItem, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(250);

        TableColumn<MenuItem, BigDecimal> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(80);

        TableColumn<MenuItem, Boolean> availableCol = new TableColumn<>("Available");
        availableCol.setCellValueFactory(new PropertyValueFactory<>("available"));
        availableCol.setPrefWidth(80);

        TableColumn<MenuItem, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(200);
        actionCol.setCellFactory(col -> new TableCell<MenuItem, Void>() {
            private final HBox actionBox = new HBox(5);
            private final Button editButton = new Button("Edit");
            private final Button toggleButton = new Button("Toggle");
            private final Button deleteButton = new Button("Delete");

            {
                editButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 10px;");
                toggleButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 10px;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px;");

                editButton.setOnAction(e -> {
                    MenuItem item = getTableView().getItems().get(getIndex());
                    System.out.println("✏️  Edit button clicked for: " + item.getName() + " (ID: " + item.getItemId() + ")");
                    showEditItemDialog(item);
                });

                toggleButton.setOnAction(e -> {
                    MenuItem item = getTableView().getItems().get(getIndex());
                    System.out.println("🔄 Toggle button clicked for: " + item.getName() + " (ID: " + item.getItemId() + ")");
                    toggleItemAvailability(item);
                });

                deleteButton.setOnAction(e -> {
                    MenuItem item = getTableView().getItems().get(getIndex());
                    System.out.println("🗑️  Delete button clicked for: " + item.getName() + " (ID: " + item.getItemId() + ")");
                    deleteMenuItem(item);
                });

                actionBox.getChildren().addAll(editButton, toggleButton, deleteButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });

        table.getColumns().addAll(nameCol, categoryCol, descCol, priceCol, availableCol, actionCol);
        return table;
    }

    private VBox createReportsTab() {
        VBox reportsContent = new VBox(20);
        reportsContent.setPadding(new Insets(20));

        Label reportsLabel = new Label("Reports & Analytics");
        reportsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        // Report cards
        HBox reportCards = new HBox(20);
        reportCards.setAlignment(Pos.CENTER);

        // Get basic stats from available services (using pending orders as sample)
        List<Order> pendingOrders = cafeteriaSystem.getOrderService().getPendingOrders();
        int totalOrders = pendingOrders.size();
        BigDecimal totalRevenue = pendingOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgOrder = totalOrders > 0 ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;

        VBox salesCard = createReportCard("System Stats",
                "Total Orders: " + totalOrders,
                "Total Revenue: EGP " + totalRevenue,
                "Average Order: EGP " + avgOrder);

        VBox popularCard = createReportCard("Popular Items",
                "Check detailed reports",
                "for item statistics");

        VBox loyaltyCard = createReportCard("Loyalty Stats",
                "Feature available in",
                "detailed reports");

        reportCards.getChildren().addAll(salesCard, popularCard, loyaltyCard);

        // Generate detailed report button
        Button generateReportButton = new Button("Generate Detailed Report");
        generateReportButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-pref-width: 250px; -fx-pref-height: 40px;");
        generateReportButton.setOnAction(e -> showDetailedReport());

        VBox buttonContainer = new VBox(generateReportButton);
        buttonContainer.setAlignment(Pos.CENTER);

        reportsContent.getChildren().addAll(reportsLabel, reportCards, buttonContainer);
        return reportsContent;
    }

    private VBox createUserManagementTab() {
        VBox userContent = new VBox(15);
        userContent.setPadding(new Insets(20));

        Label userLabel = new Label("User Management");
        userLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        // User statistics - read from database
        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);

        // Get real-time counts from database
        long studentCount = cafeteriaSystem.getSystemStatistics().getTotalStudents();
        long staffCount = cafeteriaSystem.getSystemStatistics().getTotalStaff();

        VBox studentStats = createStatsCard("Students", String.valueOf(studentCount));
        VBox staffStats = createStatsCard("Staff", String.valueOf(staffCount));

        statsBox.getChildren().addAll(studentStats, staffStats);

        // No more "Add New Staff Member" button

        userContent.getChildren().addAll(userLabel, statsBox);
        return userContent;
    }

    private VBox createReportCard(String title, String... content) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPadding(new Insets(20));
        card.setPrefWidth(250);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.DARKBLUE);

        card.getChildren().add(titleLabel);

        for (String text : content) {
            Label contentLabel = new Label(text);
            contentLabel.setFont(Font.font("Arial", 14));
            contentLabel.setWrapText(true);
            card.getChildren().add(contentLabel);
        }

        return card;
    }

    private VBox createStatsCard(String title, String count) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #2196F3; -fx-background-radius: 10;");
        card.setPadding(new Insets(20));
        card.setPrefWidth(150);

        Label countLabel = new Label(count);
        countLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        countLabel.setTextFill(Color.WHITE);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.WHITE);

        card.getChildren().addAll(countLabel, titleLabel);
        return card;
    }

    private void loadData() {
        loadOrders();
        loadMenuItems();
    }

    private void loadOrders() {
        try {
            // Force database refresh to get updated order data with loyalty information
            System.out.println("🔄 Refreshing orders from database for staff interface");
            cafeteriaSystem.forceDatabaseSync();

            List<Order> allOrders = new java.util.ArrayList<>();

            // Try to get all types of orders
            List<Order> pendingOrders = cafeteriaSystem.getOrderService().getPendingOrders();
            List<Order> preparingOrders = cafeteriaSystem.getOrderService().getOrdersInPreparation();
            List<Order> readyOrders = cafeteriaSystem.getOrderService().getOrdersReadyForPickup();



            allOrders.addAll(pendingOrders);
            allOrders.addAll(preparingOrders);
            allOrders.addAll(readyOrders);

            System.out.println("Loaded " + allOrders.size() + " orders for staff interface");

            // Debug: Print loyalty information for each order
            for (Order order : allOrders) {
                System.out.println("Order " + order.getOrderId() + ": " +
                        "Total=" + order.getTotalAmount() +
                        ", Discount=" + order.getDiscountAmount() +
                        ", PointsRedeemed=" + order.getLoyaltyPointsRedeemed() +
                        ", PointsEarned=" + order.getLoyaltyPointsEarned());
            }

            orders.setAll(allOrders);

            // If no orders are found, we might need to add some test data
            if (allOrders.isEmpty()) {
                System.out.println("No orders found. Orders will appear once students place them through the cart.");
            }

        } catch (Exception e) {
            System.out.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
            orders.clear();
        }
    }

    private void loadMenuItems() {
        try {
            System.out.println("🔄 Loading menu items for staff interface...");

            // Force database sync to prevent foreign key constraint issues
            cafeteriaSystem.forceDatabaseSync();

            List<MenuItem> allItems = cafeteriaSystem.getMenuService().getAllMenuItems();
            menuItems.setAll(allItems);
            System.out.println("✅ Loaded " + allItems.size() + " menu items");

            // Debug: Show what items were loaded
            allItems.forEach(item ->
                    System.out.println("   - " + item.getName() + " (ID: " + item.getItemId() + ", Available: " + item.isAvailable() + ")")
            );

        } catch (Exception e) {
            System.err.println("❌ Error loading menu items: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void filterOrders(String status) {
        try {
            if ("All".equals(status)) {
                loadOrders(); // Load all orders
            } else {
                // Use specific status filtering
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status);
                List<Order> filtered = cafeteriaSystem.getOrderService().getOrdersByStatus(orderStatus);
                orders.setAll(filtered);
            }
        } catch (Exception e) {
            orders.clear();
        }
    }

    private void showUpdateStatusDialog(Order order) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Update Order Status");
        dialog.setHeaderText("Update status for Order #" + order.getOrderId());

        ComboBox<Order.OrderStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(Order.OrderStatus.values());
        statusCombo.setValue(order.getStatus());

        dialog.getDialogPane().setContent(statusCombo);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return statusCombo.getValue().toString();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(status -> {
            try {
                cafeteriaSystem.getOrderService().updateOrderStatus(order.getOrderId(), Order.OrderStatus.valueOf(status));
                loadOrders(); // Refresh
                showAlert("Success", "Order status updated successfully!");
            } catch (Exception e) {
                showAlert("Error", "Failed to update order status: " + e.getMessage());
            }
        });
    }

    private void showOrderDetails(Order order) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Details");
        alert.setHeaderText("Order #" + order.getOrderId());

        StringBuilder details = new StringBuilder();
        details.append("Customer: ").append(order.getStudentId()).append("\n");
        details.append("Date: ").append(order.getOrderTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        details.append("Status: ").append(order.getStatus()).append("\n");
        // Show total with loyalty discount if applicable
        BigDecimal total = order.getTotalAmount();
        BigDecimal discount = order.getDiscountAmount();

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal originalTotal = total.add(discount);
            details.append("Original Total: EGP ").append(originalTotal).append("\n");
            details.append("Loyalty Discount: -EGP ").append(discount).append("\n");
            details.append("Final Total: EGP ").append(total).append("\n");
        } else {
            details.append("Total: EGP ").append(total).append("\n");
        }

        details.append("Loyalty Points Earned: ").append(order.getLoyaltyPointsEarned()).append("\n");

        if (order.getLoyaltyPointsRedeemed() > 0) {
            details.append("Loyalty Points Redeemed: ").append(order.getLoyaltyPointsRedeemed()).append("\n");
        }

        details.append("\n");
        details.append("Items:\n");

        order.getItems().forEach(orderItem -> {
            String itemName = orderItem.getMenuItem().getName();
            int quantity = orderItem.getQuantity();
            BigDecimal subtotal = orderItem.getSubtotal();

            // Check if this item was free due to loyalty redemption
            if (order.getLoyaltyPointsRedeemed() > 0 &&
                    order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0 &&
                    subtotal.compareTo(BigDecimal.ZERO) == 0) {
                details.append("- 💧 ").append(itemName)
                        .append(" x").append(quantity)
                        .append(" (Free via Loyalty)\n");
            } else {
                details.append("- ").append(itemName)
                        .append(" x").append(quantity)
                        .append(" (EGP ").append(subtotal).append(")\n");
            }
        });

        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    private void showAddItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Menu Item");
        dialog.setHeaderText("Add a new item to the menu");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextField descField = new TextField();
        TextField priceField = new TextField();
        ComboBox<MenuItem.MenuCategory> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(MenuItem.MenuCategory.values());
        CheckBox availableCheck = new CheckBox();
        availableCheck.setSelected(true);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Price (EGP):"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryCombo, 1, 3);
        grid.add(new Label("Available:"), 0, 4);
        grid.add(availableCheck, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String name = nameField.getText();
                    String description = descField.getText();
                    BigDecimal price = new BigDecimal(priceField.getText());
                    MenuItem.MenuCategory category = categoryCombo.getValue();
                    boolean available = availableCheck.isSelected();

                    if (!name.isEmpty() && !description.isEmpty() && category != null) {
                        MenuItem newItem = cafeteriaSystem.getMenuService().addMenuItem(name, description, price, category);
                        if (newItem != null) {
                            // Set availability if needed
                            if (!available) {
                                cafeteriaSystem.getMenuService().setItemAvailability(newItem.getItemId(), available);
                            }

                            // Save to database
                            cafeteriaSystem.saveMenuItemsToDatabase();

                            // Reload menu items
                            loadMenuItems();

                            showAlert("Success", "Menu item '" + name + "' added successfully!");
                        } else {
                            showAlert("Error", "Failed to add menu item.");
                        }
                    }
                } catch (NumberFormatException e) {
                    showAlert("Error", "Invalid price format.");
                } catch (Exception e) {
                    showAlert("Error", "Failed to add menu item: " + e.getMessage());
                }
            }
        });
    }

    private void showEditItemDialog(MenuItem item) {
        Dialog<MenuItem> dialog = new Dialog<>();
        dialog.setTitle("Edit Menu Item");
        dialog.setHeaderText("Edit: " + item.getName());

        // Create form fields
        TextField nameField = new TextField(item.getName());
        TextArea descField = new TextArea(item.getDescription());
        descField.setPrefRowCount(3);
        TextField priceField = new TextField(item.getPrice().toString());
        ComboBox<MenuItem.MenuCategory> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(MenuItem.MenuCategory.values());
        categoryCombo.setValue(item.getCategory());

        // Create form layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Add buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Handle save button
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    BigDecimal newPrice = new BigDecimal(priceField.getText());
                    MenuItem updatedItem = cafeteriaSystem.getMenuService().updateMenuItem(
                            item.getItemId(),
                            nameField.getText(),
                            descField.getText(),
                            newPrice,
                            categoryCombo.getValue()
                    );

                    // Save to database
                    cafeteriaSystem.saveMenuItemsToDatabase();

                    // Reload menu items
                    loadMenuItems();

                    showAlert("Success", "Menu item updated successfully!");
                    return updatedItem;
                } catch (Exception e) {
                    showAlert("Error", "Failed to update menu item: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void toggleItemAvailability(MenuItem item) {
        try {
            boolean newAvailability = !item.isAvailable();
            boolean success = cafeteriaSystem.getMenuService().setItemAvailability(item.getItemId(), newAvailability);

            if (success) {
                // Save to database
                cafeteriaSystem.saveMenuItemsToDatabase();

                // Reload menu items
                loadMenuItems();

                String status = newAvailability ? "available" : "unavailable";
                showAlert("Success", "Item '" + item.getName() + "' is now " + status + "!");
            } else {
                showAlert("Error", "Failed to update item availability");
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to update availability: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteMenuItem(MenuItem item) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Menu Item");
        confirmation.setHeaderText("Are you sure you want to delete this item?");
        confirmation.setHeaderText("Delete: " + item.getName());
        confirmation.setContentText("This action cannot be undone. The item will be permanently removed from the menu.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = cafeteriaSystem.getMenuService().removeMenuItem(item.getItemId());

                    if (success) {
                        // Save to database
                        cafeteriaSystem.saveMenuItemsToDatabase();

                        // Reload menu items
                        loadMenuItems();

                        showAlert("Success", "Menu item '" + item.getName() + "' deleted successfully!");
                    } else {
                        showAlert("Error", "Failed to delete menu item. Item may not exist.");
                    }
                } catch (Exception e) {
                    showAlert("Error", "Failed to delete menu item: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void showDetailedReport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detailed Report");
        alert.setHeaderText("System Report");

        StringBuilder report = new StringBuilder();

        try {
            // Get orders from different statuses since getAllOrders() doesn't exist
            List<Order> allOrders = new java.util.ArrayList<>();
            allOrders.addAll(cafeteriaSystem.getOrderService().getPendingOrders());
            allOrders.addAll(cafeteriaSystem.getOrderService().getOrdersInPreparation());
            allOrders.addAll(cafeteriaSystem.getOrderService().getOrdersReadyForPickup());

            int totalOrders = allOrders.size();
            BigDecimal totalRevenue = allOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avgOrder = totalOrders > 0 ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;

            report.append("=== SYSTEM SALES REPORT ===\n");
            report.append("Total Orders: ").append(totalOrders).append("\n");
            report.append("Total Revenue: EGP ").append(totalRevenue).append("\n");
            report.append("Average Order Value: EGP ").append(avgOrder).append("\n\n");

            report.append("=== ORDER STATUS BREAKDOWN ===\n");
            java.util.Map<Order.OrderStatus, Long> statusCounts = allOrders.stream()
                    .collect(java.util.stream.Collectors.groupingBy(Order::getStatus, java.util.stream.Collectors.counting()));
            statusCounts.forEach((status, count) -> {
                report.append("- ").append(status).append(": ").append(count).append(" orders\n");
            });

            report.append("\n=== LOYALTY PROGRAM ===\n");
            int totalLoyaltyPoints = allOrders.stream()
                    .mapToInt(Order::getLoyaltyPointsEarned)
                    .sum();
            report.append("Total Loyalty Points Awarded: ").append(totalLoyaltyPoints).append("\n");

        } catch (Exception e) {
            report.append("Report generation failed: ").append(e.getMessage());
        }

        alert.setContentText(report.toString());
        alert.showAndWait();
    }

    private void showAddStaffDialog() {
        // Implementation for adding staff members
        showAlert("Info", "Add staff functionality not implemented in this demo.");
    }

    private void updateMenuWithNewItems() {
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Update Menu");
        confirmDialog.setHeaderText("Replace Menu with New Items");
        confirmDialog.setContentText("This will remove ALL existing menu items and replace them with a new modern menu. Are you sure?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Call the cafeteria system to refresh menu
                    cafeteriaSystem.refreshMenuWithNewItems();

                    // Reload the menu items in the interface
                    loadMenuItems();

                    showAlert("Success", "Menu updated successfully with new items!\n" +
                            "Old items removed and " + menuItems.size() + " new items added.\n" +
                            "Items have been saved to database for order processing.");

                } catch (Exception e) {
                    System.out.println("Error updating menu: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Error", "Failed to update menu: " + e.getMessage());
                }
            }
        });
    }

    private void refreshData() {
        loadData();
        refreshUserManagementTab();
        showAlert("Success", "Data refreshed successfully!");
    }

    private void refreshUserManagementTab() {
        // Refresh user counts from database
        try {
            long studentCount = cafeteriaSystem.getSystemStatistics().getTotalStudents();
            long staffCount = cafeteriaSystem.getSystemStatistics().getTotalStaff();

            // Update the user management tab content
            Parent root = primaryStage.getScene().getRoot();
            if (root instanceof BorderPane) {
                BorderPane borderPane = (BorderPane) root;
                Node centerNode = borderPane.getCenter();
                if (centerNode instanceof TabPane) {
                    TabPane tabPane = (TabPane) centerNode;
                    for (Tab tab : tabPane.getTabs()) {
                        if ("User Management".equals(tab.getText())) {
                            tab.setContent(createUserManagementTab());
                            break;
                        }
                    }
                }
            }

            System.out.println("✅ User management tab refreshed: " + studentCount + " students, " + staffCount + " staff");
        } catch (Exception e) {
            System.err.println("❌ Error refreshing user management tab: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void backToLogin() {
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Back to Login");
        confirmDialog.setHeaderText("Return to Login Screen");
        confirmDialog.setContentText("Are you sure you want to go back to the login screen? You will be logged out.");

        // Style the confirmation dialog
        confirmDialog.getDialogPane().setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5); " +
                        "-fx-padding: 20;"
        );

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                logout(); // Use existing logout functionality
            }
        });
    }

    private void logout() {
        LoginController loginController = new LoginController(cafeteriaSystem, primaryStage);
        Scene loginScene = loginController.createLoginScene();
        primaryStage.setScene(loginScene);
    }
}