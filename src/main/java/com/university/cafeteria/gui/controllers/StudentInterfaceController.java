package com.university.cafeteria.gui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import com.university.cafeteria.service.CafeteriaSystem;
import com.university.cafeteria.model.Student;
import com.university.cafeteria.model.MenuItem;
import com.university.cafeteria.model.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Food-themed student interface controller
 * Uses studentinterface.png background with overlay navigation buttons
 */
public class StudentInterfaceController {
    
    private CafeteriaSystem cafeteriaSystem;
    private Stage primaryStage;
    private Student student;
    private ObservableList<MenuItem> menuItems;
    private ObservableList<CartItem> cartItems;
    
    public StudentInterfaceController(CafeteriaSystem cafeteriaSystem, Stage primaryStage, Student student) {
        this.cafeteriaSystem = cafeteriaSystem;
        this.primaryStage = primaryStage;
        this.student = student;
        this.menuItems = FXCollections.observableArrayList();
        this.cartItems = FXCollections.observableArrayList();
        loadMenuItems();
    }
    
    public Scene createStudentScene() {
        // Create main container with background image
        StackPane root = new StackPane();
        root.setPrefSize(1000, 750);
        
        // Set a fallback background style (food-themed gradient)
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #FF6B35, #F7931E, #FFD23F); " +
            "-fx-background-radius: 0;"
        );
        
        // Load and set background image
        ImageView backgroundImage = createBackgroundImage();
        if (backgroundImage != null && backgroundImage.getImage() != null) {
            root.getChildren().add(backgroundImage);
        }
        
        // Create overlay with navigation buttons
        VBox overlay = createNavigationOverlay();
        root.getChildren().add(overlay);
        
        // Add logout button in top-right corner
        Button logoutButton = createLogoutButton();
        StackPane.setAlignment(logoutButton, Pos.TOP_RIGHT);
        StackPane.setMargin(logoutButton, new Insets(20));
        root.getChildren().add(logoutButton);
        
        // Add back to login button in top-left corner
        Button backToLoginButton = createBackToLoginButton();
        StackPane.setAlignment(backToLoginButton, Pos.TOP_LEFT);
        StackPane.setMargin(backToLoginButton, new Insets(20));
        root.getChildren().add(backToLoginButton);
        
        // Add fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
        
        Scene scene = new Scene(root, 1000, 750);
        return scene;
    }
    
    private ImageView createBackgroundImage() {
        ImageView imageView = new ImageView();
        Image backgroundImage = null;
        
        try {
            // Try loading from resources first
            backgroundImage = new Image(getClass().getResourceAsStream("/images/studentinterface.png"));
            System.out.println("Attempting to load from resources: /images/studentinterface.png");
            
            if (backgroundImage.isError()) {
                System.out.println("Resource loading failed, trying alternative paths...");
                // Fallback paths
                backgroundImage = new Image("file:src/main/resources/images/studentinterface.png");
                
                if (backgroundImage.isError()) {
                    backgroundImage = new Image("file:src/images/studentinterface.png");
                    
                    if (backgroundImage.isError()) {
                        backgroundImage = new Image("file:C:/Users/Data/Desktop/filedwithdatabase 3/filedwithdatabase/filed44/filed/src/main/resources/images/studentinterface.png");
                    }
                }
            }
            
            if (backgroundImage != null && !backgroundImage.isError()) {
                imageView.setImage(backgroundImage);
                System.out.println("Successfully loaded studentinterface.png image!");
            } else {
                System.out.println("All image loading attempts failed, using fallback");
                return null;
            }
            
            imageView.setFitWidth(1000);
            imageView.setFitHeight(750);
            imageView.setPreserveRatio(false);
            imageView.setSmooth(true);
            
        } catch (Exception e) {
            System.out.println("Exception loading image: " + e.getMessage());
            return null;
        }
        
        return imageView;
    }
    
    private VBox createNavigationOverlay() {
        VBox overlay = new VBox(20);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPadding(new Insets(50));
        
        // Position the buttons in the center portion of the screen
        VBox buttonContainer = new VBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setMaxWidth(500);
        
        // Create navigation buttons matching the design
        Button browseMenuButton = createNavigationButton("BROWSE MENUE");
        browseMenuButton.setOnAction(e -> showBrowseMenu());
        
        Button myCartButton = createNavigationButton("MY CART");
        myCartButton.setOnAction(e -> showMyCart());
        
        Button orderHistoryButton = createNavigationButton("ORDER HISTORY");
        orderHistoryButton.setOnAction(e -> showOrderHistory());
        
        Button profileButton = createNavigationButton("PROFILE AND LOYALTY");
        profileButton.setOnAction(e -> showProfile());
        
        buttonContainer.getChildren().addAll(browseMenuButton, myCartButton, orderHistoryButton, profileButton);
        
        // Add some spacing to position buttons correctly
        Region topSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        
        overlay.getChildren().addAll(topSpacer, buttonContainer);
        
        return overlay;
    }
    

    
    private Button createNavigationButton(String text) {
        Button button = new Button(text);
        
        // Style matching the golden/orange theme from the design
        String baseStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-font-family: 'Arial Black', 'Arial', sans-serif; " +
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 30; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 450px; " +
            "-fx-pref-height: 70px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);",
            "#FFB84D", // Golden orange color
            "#2C1810" // Dark brown text
        );
        
        button.setStyle(baseStyle);
        
        // Add hover effects
        button.setOnMouseEntered(e -> {
            String hoverStyle = baseStyle.replace("#FFB84D", "#FFA726") + 
                " -fx-scale-x: 1.05; -fx-scale-y: 1.05;";
            button.setStyle(hoverStyle);
            
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(baseStyle);
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
        
        return button;
    }
    
    private Button createLogoutButton() {
        Button logoutButton = new Button("← Logout");
        logoutButton.setStyle(
            "-fx-background-color: #D32F2F; " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 20; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 100px; " +
            "-fx-pref-height: 40px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
        );
        
        logoutButton.setOnAction(e -> logout());
        
        return logoutButton;
    }
    
    private Button createBackToLoginButton() {
        Button backButton = new Button("← Back to Login");
        backButton.setStyle(
            "-fx-background-color: rgba(255, 152, 0, 0.9); " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 20; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 140px; " +
            "-fx-pref-height: 40px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
        );
        
        // Add hover effect
        backButton.setOnMouseEntered(e -> {
            backButton.setStyle(
                "-fx-background-color: rgba(230, 130, 0, 0.9); " +
                "-fx-text-fill: white; " +
                "-fx-font-family: 'Arial', sans-serif; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 20; " +
                "-fx-cursor: hand; " +
                "-fx-pref-width: 140px; " +
                "-fx-pref-height: 40px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 3);" +
                "-fx-scale-x: 1.05; -fx-scale-y: 1.05;"
            );
        });
        
        backButton.setOnMouseExited(e -> {
            backButton.setStyle(
                "-fx-background-color: rgba(255, 152, 0, 0.9); " +
                "-fx-text-fill: white; " +
                "-fx-font-family: 'Arial', sans-serif; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 20; " +
                "-fx-cursor: hand; " +
                "-fx-pref-width: 140px; " +
                "-fx-pref-height: 40px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
            );
        });
        
        backButton.setOnAction(e -> backToLogin());
        
        return backButton;
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
    
    // Enhanced menu browsing with categories and add to cart functionality
    private void showBrowseMenu() {
        Stage menuStage = new Stage();
        menuStage.setTitle("🍔 Browse Menu - University Cafeteria");
        
        // Create main container
        StackPane root = new StackPane();
        root.setPrefSize(1000, 750);
        
        // Set background gradient
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #FF6B35, #F7931E, #FFD23F); " +
            "-fx-background-radius: 0;"
        );
        
        // Create menu content
        VBox menuContent = createMenuContent();
        root.getChildren().add(menuContent);
        
        // Add back button in top-left corner
        Button backButton = createBrowseMenuBackButton(menuStage);
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(20));
        root.getChildren().add(backButton);
        
        Scene menuScene = new Scene(root, 1000, 750);
        menuStage.setScene(menuScene);
        menuStage.show();
    }
    
    private VBox createMenuContent() {
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        
        // Title
        Label titleLabel = new Label("🍔 MENU");
        titleLabel.setStyle(
            "-fx-font-family: 'Arial Black', 'Arial', sans-serif; " +
            "-fx-font-size: 32px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: white; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 4, 0, 0, 2);"
        );
        
        // Create category buttons
        VBox categoryContainer = new VBox(20);
        categoryContainer.setAlignment(Pos.CENTER);
        categoryContainer.setMaxWidth(600);
        
        // Get availability counts for each category
        long sandwichesCount = menuItems.stream()
            .filter(item -> item.getCategory() == MenuItem.MenuCategory.MAIN_COURSE && item.isAvailable())
            .count();
        long appetizersCount = menuItems.stream()
            .filter(item -> item.getCategory() == MenuItem.MenuCategory.SNACK && item.isAvailable())
            .count();
        long drinksCount = menuItems.stream()
            .filter(item -> item.getCategory() == MenuItem.MenuCategory.DRINK && item.isAvailable())
            .count();
        
        Button sandwichesButton = createCategoryButton("🍔 SANDWICHES", 
            sandwichesCount + " Available Burger Options");
        sandwichesButton.setOnAction(e -> showCategoryMenu("SANDWICHES"));
        
        Button appetizersButton = createCategoryButton("🍗 APPETIZERS", 
            appetizersCount + " Available Snack Options");
        appetizersButton.setOnAction(e -> showCategoryMenu("APPETIZERS"));
        
        Button drinksButton = createCategoryButton("🥤 DRINKS", 
            drinksCount + " Available Drink Options");
        drinksButton.setOnAction(e -> showCategoryMenu("DRINKS"));
        
        categoryContainer.getChildren().addAll(sandwichesButton, appetizersButton, drinksButton);
        
        content.getChildren().addAll(titleLabel, categoryContainer);
        return content;
    }
    
    private Button createBrowseMenuBackButton(Stage menuStage) {
        Button backButton = new Button("← Back");
        backButton.setStyle(
            "-fx-background-color: rgba(211, 47, 47, 0.9); " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 20; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 100px; " +
            "-fx-pref-height: 40px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
        );
        
        // Add hover effect
        backButton.setOnMouseEntered(e -> {
            backButton.setStyle(
                "-fx-background-color: rgba(183, 28, 28, 0.9); " +
                "-fx-text-fill: white; " +
                "-fx-font-family: 'Arial', sans-serif; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 20; " +
                "-fx-cursor: hand; " +
                "-fx-pref-width: 100px; " +
                "-fx-pref-height: 40px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 3);" +
                "-fx-scale-x: 1.05; -fx-scale-y: 1.05;"
            );
        });
        
        backButton.setOnMouseExited(e -> {
            backButton.setStyle(
                "-fx-background-color: rgba(211, 47, 47, 0.9); " +
                "-fx-text-fill: white; " +
                "-fx-font-family: 'Arial', sans-serif; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 20; " +
                "-fx-cursor: hand; " +
                "-fx-pref-width: 100px; " +
                "-fx-pref-height: 40px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
            );
        });
        
        backButton.setOnAction(e -> menuStage.close());
        
        return backButton;
    }
    
    private Button createCategoryButton(String title, String description) {
        Button button = new Button();
        
        // Create button content
        VBox buttonContent = new VBox(5);
        buttonContent.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-family: 'Arial Black', 'Arial', sans-serif; " +
            "-fx-font-size: 20px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2C1810;"
        );
        
        Label descLabel = new Label(description);
        descLabel.setStyle(
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #2C1810; " +
            "-fx-wrap-text: true;"
        );
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(350);
        
        buttonContent.getChildren().addAll(titleLabel, descLabel);
        button.setGraphic(buttonContent);
        
        String baseStyle = 
            "-fx-background-color: #FFB84D; " +
            "-fx-background-radius: 25; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 400px; " +
            "-fx-pref-height: 80px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);";
        
        button.setStyle(baseStyle);
        
        // Hover effects
        button.setOnMouseEntered(e -> {
            button.setStyle(baseStyle.replace("#FFB84D", "#FFA726") + " -fx-scale-x: 1.03; -fx-scale-y: 1.03;");
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(baseStyle);
        });
        
        return button;
    }
    
    private void showCategoryMenu(String category) {
        Stage categoryStage = new Stage();
        categoryStage.setTitle("🍔 " + category + " - University Cafeteria");
        
        // Create main container
        StackPane root = new StackPane();
        root.setPrefSize(1000, 750);
        
        // Set background gradient
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #FF6B35, #F7931E, #FFD23F); " +
            "-fx-background-radius: 0;"
        );
        
        // Load background image if available
        ImageView backgroundImage = loadCategoryImage(category);
        if (backgroundImage != null) {
            root.getChildren().add(backgroundImage);
        }
        
        // Create category content overlay
        VBox categoryContent = createCategoryContent(category);
        root.getChildren().add(categoryContent);
        
        Scene categoryScene = new Scene(root, 1000, 750);
        categoryStage.setScene(categoryScene);
        categoryStage.show();
    }
    
    private ImageView loadCategoryImage(String category) {
        try {
            // Use the main menu image for all categories
            String imageName = "menue.png";
            
            Image image = new Image(getClass().getResourceAsStream("/images/" + imageName));
            if (!image.isError()) {
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(1000);
                imageView.setFitHeight(750);
                imageView.setPreserveRatio(false);
                imageView.setSmooth(true);
                System.out.println("Successfully loaded menue.png image for " + category);
                return imageView;
            } else {
                // Fallback: try alternative paths
                image = new Image("file:src/main/resources/images/" + imageName);
                if (!image.isError()) {
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(1000);
                    imageView.setFitHeight(750);
                    imageView.setPreserveRatio(false);
                    imageView.setSmooth(true);
                    return imageView;
                }
            }
        } catch (Exception e) {
            System.out.println("Could not load menue.png image: " + e.getMessage());
        }
        return null;
    }
    
    private VBox createCategoryContent(String category) {
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        
        // Category title
        Label categoryTitle = new Label(getEmoji(category) + " " + category);
        categoryTitle.setStyle(
            "-fx-font-family: 'Arial Black', 'Arial', sans-serif; " +
            "-fx-font-size: 28px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: white; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 6, 0, 0, 2);"
        );
        
        // Items container
        VBox itemsContainer = new VBox(15);
        itemsContainer.setAlignment(Pos.CENTER);
        itemsContainer.setMaxWidth(800);
        
        // Add items based on category from the loaded menu items
        List<MenuItem> categoryItems = menuItems.stream()
            .filter(item -> {
                switch (category) {
                    case "SANDWICHES": return item.getCategory() == MenuItem.MenuCategory.MAIN_COURSE;
                    case "APPETIZERS": return item.getCategory() == MenuItem.MenuCategory.SNACK;
                    case "DRINKS": return item.getCategory() == MenuItem.MenuCategory.DRINK;
                    default: return false;
                }
            })
            .toList();
        
        if (categoryItems.isEmpty()) {
            Label noItemsLabel = new Label("No items available in this category");
            noItemsLabel.setStyle(
                "-fx-font-family: 'Arial', sans-serif; " +
                "-fx-font-size: 16px; " +
                "-fx-text-fill: white; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 4, 0, 0, 2);"
            );
            itemsContainer.getChildren().add(noItemsLabel);
        } else {
            for (MenuItem item : categoryItems) {
                itemsContainer.getChildren().add(createMenuItemFromModel(item));
            }
        }
        
        // Back button
        Button backButton = new Button("← Back to Menu");
        backButton.setStyle(
            "-fx-background-color: #D32F2F; " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 20; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 150px; " +
            "-fx-pref-height: 40px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
        );
        backButton.setOnAction(e -> ((Stage) backButton.getScene().getWindow()).close());
        
        content.getChildren().addAll(categoryTitle, itemsContainer, backButton);
        return content;
    }
    
    /**
     * Create menu item from MenuItem model with availability status
     */
    private HBox createMenuItemFromModel(MenuItem item) {
        HBox itemBox = new HBox(20);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setMaxWidth(750);
        
        // Set style based on availability
        if (item.isAvailable()) {
            itemBox.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.95); " +
                "-fx-background-radius: 15; " +
                "-fx-padding: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 3);"
            );
        } else {
            itemBox.setStyle(
                "-fx-background-color: rgba(200, 200, 200, 0.8); " +
                "-fx-background-radius: 15; " +
                "-fx-padding: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);"
            );
        }
        
        // Item info
        VBox itemInfo = new VBox(8);
        itemInfo.setAlignment(Pos.CENTER_LEFT);
        
        // Add availability indicator to name
        String availabilityText = item.isAvailable() ? "" : " (UNAVAILABLE)";
        Label nameLabel = new Label(item.getName() + availabilityText);
        nameLabel.setStyle(
            "-fx-font-family: 'Arial Black', 'Arial', sans-serif; " +
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: " + (item.isAvailable() ? "#2C1810" : "#999999") + ";"
        );
        
        Label descLabel = new Label(item.getDescription());
        descLabel.setStyle(
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-text-fill: " + (item.isAvailable() ? "#666666" : "#999999") + "; " +
            "-fx-wrap-text: true;"
        );
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(400);
        
        Label priceLabel = new Label("EGP " + item.getPrice());
        priceLabel.setStyle(
            "-fx-font-family: 'Arial Black', 'Arial', sans-serif; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: " + (item.isAvailable() ? "#FF6B35" : "#999999") + ";"
        );
        
        itemInfo.getChildren().addAll(nameLabel, descLabel, priceLabel);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Add to cart button (disabled if unavailable)
        Button addButton = new Button(item.isAvailable() ? "Add to Cart" : "UNAVAILABLE");
        addButton.setDisable(!item.isAvailable());
        
        if (item.isAvailable()) {
            addButton.setStyle(
                "-fx-background-color: #4CAF50; " +
                "-fx-text-fill: white; " +
                "-fx-font-family: 'Arial', sans-serif; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand; " +
                "-fx-pref-width: 120px; " +
                "-fx-pref-height: 40px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 0, 2);"
            );
            
            // Add hover effect
            addButton.setOnMouseEntered(e -> {
                addButton.setStyle(addButton.getStyle().replace("#4CAF50", "#45a049"));
            });
            addButton.setOnMouseExited(e -> {
                addButton.setStyle(addButton.getStyle().replace("#45a049", "#4CAF50"));
            });
            
            // Add to cart functionality
            addButton.setOnAction(e -> {
                addToCart(item.getName(), item.getDescription(), item.getPrice());
                // Visual feedback
                addButton.setText("Added!");
                addButton.setStyle(addButton.getStyle().replace("#4CAF50", "#FF9800"));
                
                // Reset after 1 second
                javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(Duration.seconds(1), event -> {
                        addButton.setText("Add to Cart");
                        addButton.setStyle(addButton.getStyle().replace("#FF9800", "#4CAF50"));
                    })
                );
                timeline.play();
            });
        } else {
            addButton.setStyle(
                "-fx-background-color: #999999; " +
                "-fx-text-fill: white; " +
                "-fx-font-family: 'Arial', sans-serif; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: default; " +
                "-fx-pref-width: 120px; " +
                "-fx-pref-height: 40px;"
            );
        }
        
        itemBox.getChildren().addAll(itemInfo, spacer, addButton);
        return itemBox;
    }
    
    private HBox createMenuItem(String name, String description, String price) {
        HBox itemBox = new HBox(20);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setMaxWidth(750);
        itemBox.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.95); " +
            "-fx-background-radius: 15; " +
            "-fx-padding: 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 3);"
        );
        
        // Item info
        VBox itemInfo = new VBox(8);
        itemInfo.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(name);
        nameLabel.setStyle(
            "-fx-font-family: 'Arial Black', 'Arial', sans-serif; " +
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2C1810;"
        );
        
        Label descLabel = new Label(description);
        descLabel.setStyle(
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #666666; " +
            "-fx-wrap-text: true;"
        );
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(400);
        
        Label priceLabel = new Label("EGP " + price);
        priceLabel.setStyle(
            "-fx-font-family: 'Arial Black', 'Arial', sans-serif; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #FF6B35;"
        );
        
        itemInfo.getChildren().addAll(nameLabel, descLabel, priceLabel);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Add to cart button
        Button addButton = new Button("Add to Cart");
        addButton.setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 120px; " +
            "-fx-pref-height: 40px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 0, 2);"
        );
        
        // Add hover effect
        addButton.setOnMouseEntered(e -> {
            addButton.setStyle(addButton.getStyle().replace("#4CAF50", "#45a049"));
        });
        addButton.setOnMouseExited(e -> {
            addButton.setStyle(addButton.getStyle().replace("#45a049", "#4CAF50"));
        });
        
        // Add to cart functionality
        addButton.setOnAction(e -> {
            addToCart(name, description, new BigDecimal(price));
            // Visual feedback
            addButton.setText("Added!");
            addButton.setStyle(addButton.getStyle().replace("#4CAF50", "#FF9800"));
            
            // Reset after 1 second
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.seconds(1), event -> {
                    addButton.setText("Add to Cart");
                    addButton.setStyle(addButton.getStyle().replace("#FF9800", "#4CAF50"));
                })
            );
            timeline.play();
        });
        
        itemBox.getChildren().addAll(itemInfo, spacer, addButton);
        return itemBox;
    }
    
    private String getEmoji(String category) {
        switch (category) {
            case "SANDWICHES": return "🍔";
            case "APPETIZERS": return "🍗";
            case "DRINKS": return "🥤";
            default: return "🍽️";
        }
    }
    
    private void addToCart(String name, String description, BigDecimal price) {
        // Generate a simple ID for the item
        String itemId = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        
        // Check if item already in cart
        CartItem existingItem = cartItems.stream()
            .filter(cartItem -> cartItem.getItemId().equals(itemId))
            .findFirst()
            .orElse(null);
            
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + 1);
        } else {
            cartItems.add(new CartItem(itemId, name, price, 1));
        }
        
        System.out.println("Added to cart: " + name + " - EGP " + price);
    }
    
    private static Stage currentCartStage = null; // Static reference to prevent multiple cart windows
    
    private void showMyCart() {
        // Close existing cart window if it's open
        if (currentCartStage != null && currentCartStage.isShowing()) {
            currentCartStage.close();
        }
        
        // Refresh student's loyalty points from database
        int refreshedPoints = cafeteriaSystem.getLoyaltyService().getPointsBalance(student.getStudentId());
        // Update the loyalty account with the refreshed points
        int currentPointsInMemory = student.getLoyaltyPoints();
        if (refreshedPoints != currentPointsInMemory) {
            int difference = refreshedPoints - currentPointsInMemory;
            if (difference > 0) {
                student.addLoyaltyPoints(difference);
            } else if (difference < 0) {
                // If points decreased, we need to handle this differently since we can't set negative points
                // For now, just log the difference
                System.out.println("Points decreased by: " + Math.abs(difference));
            }
        }
        
        currentCartStage = new Stage();
        currentCartStage.setTitle("🛒 My Cart - University Cafeteria");
        
        // Create main container
        StackPane root = new StackPane();
        root.setPrefSize(800, 600);
        
        // Set background gradient
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #FF6B35, #F7931E, #FFD23F); " +
            "-fx-background-radius: 0;"
        );
        
        // Create cart content
        VBox cartContent = createCartContent(currentCartStage);
        root.getChildren().add(cartContent);
        
        Scene cartScene = new Scene(root, 800, 600);
        currentCartStage.setScene(cartScene);
        currentCartStage.show();
        
        // Set on close to clear the reference
        currentCartStage.setOnCloseRequest(e -> currentCartStage = null);
    }
    
    private VBox createCartContent(Stage cartStage) {
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));
        
        // Title
        Label titleLabel = new Label("🛒 MY CART");
        titleLabel.setStyle(
            "-fx-font-family: 'Arial Black', 'Arial', sans-serif; " +
            "-fx-font-size: 28px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: white; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 6, 0, 0, 2);"
        );
        
        // Cart items container
        VBox itemsContainer = new VBox(10);
        itemsContainer.setAlignment(Pos.CENTER);
        itemsContainer.setMaxWidth(700);
        itemsContainer.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.95); " +
            "-fx-background-radius: 15; " +
            "-fx-padding: 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 3);"
        );
        
        if (cartItems.isEmpty()) {
            Label emptyLabel = new Label("Your cart is empty");
            emptyLabel.setStyle(
                "-fx-font-family: 'Arial', sans-serif; " +
                "-fx-font-size: 16px; " +
                "-fx-text-fill: #666666;"
            );
            itemsContainer.getChildren().add(emptyLabel);
        } else {
            // Add cart items
            for (CartItem item : cartItems) {
                HBox itemRow = createCartItemRow(item);
                itemsContainer.getChildren().add(itemRow);
            }
            
            // Add separator
            javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
            separator.setStyle("-fx-background-color: #E0E0E0;");
            itemsContainer.getChildren().add(separator);
            
            // Total
            BigDecimal total = cartItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Check for active loyalty redemption
            BigDecimal redemptionAmount = BigDecimal.ZERO;
            String redemptionProperty = System.getProperty("loyalty.redemption.amount");
            if (redemptionProperty != null) {
                try {
                    redemptionAmount = new BigDecimal(redemptionProperty);
                } catch (NumberFormatException ex) {
                    System.err.println("Error parsing loyalty redemption amount: " + ex.getMessage());
                }
            }
            
            // Calculate final total after redemption
            BigDecimal finalTotal = total.subtract(redemptionAmount);
            if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
                finalTotal = BigDecimal.ZERO;
            }
            
            // Original total label
            Label totalLabel = new Label("Total: EGP " + String.format("%.2f", total));
            totalLabel.setStyle(
                "-fx-font-family: 'Arial Black', 'Arial', sans-serif; " +
                "-fx-font-size: 20px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #FF6B35;"
            );
            itemsContainer.getChildren().add(totalLabel);
            
            // Show redemption discount if applicable
            if (redemptionAmount.compareTo(BigDecimal.ZERO) > 0) {
                Label discountLabel = new Label("🎯 Loyalty Discount: -EGP " + String.format("%.2f", redemptionAmount));
                discountLabel.setStyle(
                    "-fx-font-family: 'Arial', sans-serif; " +
                    "-fx-font-size: 16px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-text-fill: #4CAF50;"
                );
                itemsContainer.getChildren().add(discountLabel);
                
                Label finalTotalLabel = new Label("Final Total: EGP " + String.format("%.2f", finalTotal));
                finalTotalLabel.setStyle(
                    "-fx-font-family: 'Arial Black', 'Arial', sans-serif; " +
                    "-fx-font-size: 22px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-text-fill: #FF6B35;"
                );
                itemsContainer.getChildren().add(finalTotalLabel);
            }
            
            // Simple Loyalty Redemption Button
            HBox loyaltyBox = createSimpleLoyaltyButton(finalTotal);
            itemsContainer.getChildren().add(loyaltyBox);
        }
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button clearButton = new Button("Clear Cart");
        clearButton.setStyle(
            "-fx-background-color: #f44336; " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 120px; " +
            "-fx-pref-height: 40px;"
        );
        clearButton.setOnAction(e -> {
            cartItems.clear();
            // Clear any active loyalty redemption when cart is cleared
            System.clearProperty("loyalty.redemption.amount");
            System.clearProperty("loyalty.redemption.points");
            cartStage.close();
            showMyCart(); // Refresh cart display
        });
        
        Button checkoutButton = new Button("Checkout");
        
        // Check if there's an active redemption to show in button text
        String checkoutRedemptionProperty = System.getProperty("loyalty.redemption.amount");
        if (checkoutRedemptionProperty != null) {
            try {
                BigDecimal checkoutRedemptionAmount = new BigDecimal(checkoutRedemptionProperty);
                if (checkoutRedemptionAmount.compareTo(BigDecimal.ZERO) > 0) {
                    checkoutButton.setText("Checkout (EGP " + String.format("%.2f", checkoutRedemptionAmount) + " off)");
                }
            } catch (NumberFormatException ex) {
                System.err.println("Error parsing redemption amount for button: " + ex.getMessage());
            }
        }
        
        checkoutButton.setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 120px; " +
            "-fx-pref-height: 40px;"
        );
                    checkoutButton.setOnAction(e -> {
                if (!cartItems.isEmpty()) {
                    try {
                        // Force database sync to prevent foreign key constraint issues
                        cafeteriaSystem.forceDatabaseSync();
                        
                        // Create a new order
                        Order newOrder = cafeteriaSystem.getOrderService().createOrder(student.getStudentId());
                    
                    // Add all cart items to the order (EXCEPT free water items)
                    int itemsAdded = 0;
                    for (CartItem cartItem : cartItems) {
                        // Skip free water items - they'll be handled separately
                        if (cartItem.getName().contains("Water (Free via Loyalty)") || 
                            cartItem.getName().contains("WATER_LOYALTY")) {
                            System.out.println("💧 Skipping free water cart item: " + cartItem.getName());
                            continue;
                        }
                        
                        // Find the corresponding menu item
                        MenuItem menuItem = findMenuItemByName(cartItem.getName());
                        if (menuItem != null) {
                            cafeteriaSystem.getOrderService().addItemToOrder(
                                newOrder.getOrderId(), 
                                menuItem, 
                                cartItem.getQuantity()
                            );
                            itemsAdded++;
                            System.out.println("Added to order: " + menuItem.getName() + " x" + cartItem.getQuantity());
                        } else {
                            System.out.println("ERROR: Could not add item to order: " + cartItem.getName());
                        }
                    }
                    
                    // Check if any items were actually added
                    if (itemsAdded == 0) {
                        throw new IllegalArgumentException("No valid items found in cart. Please make sure the menu items exist in the staff menu.");
                    }
                    
                    // Check for loyalty redemption BEFORE confirming order
                    BigDecimal redemptionAmount = BigDecimal.ZERO;
                    int pointsUsed = 0;
                    String redemptionProperty = System.getProperty("loyalty.redemption.amount");
                    String pointsProperty = System.getProperty("loyalty.redemption.points");
                    
                    if (redemptionProperty != null && pointsProperty != null) {
                        try {
                            redemptionAmount = new BigDecimal(redemptionProperty);
                            pointsUsed = Integer.parseInt(pointsProperty);
                            
                            System.out.println("Processing loyalty redemption: " + redemptionAmount + " EGP using " + pointsUsed + " points");
                        } catch (NumberFormatException ex) {
                            System.err.println("Error parsing loyalty redemption properties: " + ex.getMessage());
                        }
                    }
                    
                    // Calculate final total after redemption
                    BigDecimal total = cartItems.stream()
                        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    BigDecimal finalTotal = total.subtract(redemptionAmount);
                    if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
                        finalTotal = BigDecimal.ZERO;
                    }
                    
                    // For free water redemption, we need to handle the cart items correctly
                    if (pointsUsed == 100 && redemptionAmount.compareTo(BigDecimal.ZERO) == 0) {
                        // Remove the free water placeholder item
                        cartItems.removeIf(item -> item.getName().contains("WATER_LOYALTY") || 
                                                   item.getName().contains("💧 Water (Free via Loyalty)"));
                        
                        // For free water redemption, use the original water item but with 0.00 price
                        MenuItem waterMenuItem = findWaterMenuItem();
                        if (waterMenuItem != null) {
                            // Temporarily set the water price to 0.00 for this order
                            BigDecimal originalPrice = waterMenuItem.getPrice();
                            waterMenuItem.setPrice(BigDecimal.ZERO);
                            
                            cafeteriaSystem.getOrderService().addItemToOrder(
                                newOrder.getOrderId(), 
                                waterMenuItem, 
                                1
                            );
                            System.out.println("💧 Added FREE water to order: " + waterMenuItem.getName() + " x1 (Price: 0.00)");
                            
                            // Restore the original price after adding to order
                            waterMenuItem.setPrice(originalPrice);
                            System.out.println("💧 Restored water price to: " + originalPrice);
                        }
                    }
                    
                    // Apply loyalty discount to the order BEFORE confirming (when it's still in PENDING status)
                    if (redemptionAmount.compareTo(BigDecimal.ZERO) > 0 && pointsUsed > 0) {
                        try {
                            System.out.println("🔄 Attempting to apply loyalty discount:");
                            System.out.println("   - Order ID: " + newOrder.getOrderId());
                            System.out.println("   - Points to redeem: " + pointsUsed);
                            System.out.println("   - Expected discount: " + redemptionAmount + " EGP");
                            System.out.println("   - Order status: " + newOrder.getStatus());
                            
                            // Apply loyalty discount through the service while order is still PENDING
                            Order updatedOrder = cafeteriaSystem.getOrderService().applyLoyaltyDiscount(
                                newOrder.getOrderId(), 
                                pointsUsed
                            );
                            System.out.println("✅ Applied loyalty discount to order: " + redemptionAmount + " EGP, " + pointsUsed + " points");
                            
                            // Verify the discount was applied correctly
                            if (updatedOrder.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                                System.out.println("✅ Discount verified: " + updatedOrder.getDiscountAmount() + " EGP");
                                System.out.println("✅ Points redeemed: " + updatedOrder.getLoyaltyPointsRedeemed());
                                System.out.println("✅ Final total: " + updatedOrder.getTotalAmount());
                            } else {
                                System.out.println("⚠️  Warning: Discount not applied to order object");
                                System.out.println("   - Order discount amount: " + updatedOrder.getDiscountAmount());
                                System.out.println("   - Order points redeemed: " + updatedOrder.getLoyaltyPointsRedeemed());
                            }
                            
                            // Update the order reference
                            newOrder = updatedOrder;
                        } catch (Exception ex) {
                            System.err.println("❌ Failed to apply loyalty discount: " + ex.getMessage());
                            ex.printStackTrace();
                            // Continue with order creation even if discount fails
                        }
                    } else if (pointsUsed == 100 && redemptionAmount.compareTo(BigDecimal.ZERO) == 0) {
                        // For free water redemption, no discount needed - water is already free in cart
                        System.out.println("💧 Free water redemption: No discount applied to order - water is already free in cart");
                        
                        // Still need to record the loyalty redemption in the order
                        try {
                            // Manually set the loyalty redemption details for free water
                            newOrder.setLoyaltyPointsRedeemed(pointsUsed);
                            newOrder.setDiscountAmount(BigDecimal.ZERO);
                            System.out.println("💧 Free water: Set points redeemed = " + pointsUsed + ", discount = 0.00");
                            
                            // Force the order to recalculate totals with the new discount amount
                            newOrder.calculateTotals();
                            System.out.println("💧 Free water: Order totals recalculated");
                        } catch (Exception ex) {
                            System.err.println("❌ Failed to set free water loyalty details: " + ex.getMessage());
                        }
                    }
                    
                    // Confirm the order AFTER applying loyalty discount
                    Order confirmedOrder = cafeteriaSystem.getOrderService().confirmOrder(newOrder.getOrderId());
                    
                    // Award loyalty points for the order (based on original total spent, not discounted total)
                    int pointsEarned = cafeteriaSystem.getLoyaltyService().awardPointsFromOrder(
                        student.getStudentId(), 
                        total, 
                        confirmedOrder.getOrderId()
                    );
                    
                    // Clear the properties after successful order creation
                    if (redemptionProperty != null && pointsProperty != null) {
                        System.clearProperty("loyalty.redemption.amount");
                        System.clearProperty("loyalty.redemption.points");
                    }
                    
                    // Build success message
                    StringBuilder message = new StringBuilder();
                    message.append("Order ID: ").append(confirmedOrder.getOrderId()).append("\n");
                    message.append("Original Total: EGP ").append(String.format("%.2f", total)).append("\n");
                    
                    if (pointsUsed == 100 && redemptionAmount.compareTo(BigDecimal.ZERO) == 0) {
                        // Free water redemption
                        message.append("Loyalty Redemption: Free Water (100 points)\n");
                        message.append("Points Used: ").append(pointsUsed).append("\n");
                        message.append("Final Total: EGP ").append(String.format("%.2f", total)).append(" (Water is free)\n");
                    } else if (redemptionAmount.compareTo(BigDecimal.ZERO) > 0) {
                        // Regular discount redemption
                        message.append("Loyalty Redemption: -EGP ").append(String.format("%.2f", redemptionAmount)).append("\n");
                        message.append("Points Used: ").append(pointsUsed).append("\n");
                        message.append("Final Total: EGP ").append(String.format("%.2f", finalTotal)).append("\n");
                    } else {
                        message.append("Total: EGP ").append(String.format("%.2f", total)).append("\n");
                    }
                    
                    message.append("Status: ").append(confirmedOrder.getStatus()).append("\n");
                    message.append("🎯 Loyalty Points Earned: ").append(pointsEarned).append(" points\n");
                    message.append("Thank you for your order!");
                    
                    showAlert("🎉 Order Placed Successfully!", message.toString());
                    
                    cartItems.clear();
                    cartStage.close();
                    
                    System.out.println("Order created successfully: " + confirmedOrder.getOrderId());
                    
                } catch (Exception ex) {
                    System.out.println("Error creating order: " + ex.getMessage());
                    ex.printStackTrace();
                    showAlert("❌ Order Failed", "There was an error processing your order. Please try again.");
                }
            }
        });
        checkoutButton.setDisable(cartItems.isEmpty());
        
        Button backButton = new Button("← Back");
        backButton.setStyle(
            "-fx-background-color: #D32F2F; " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 80px; " +
            "-fx-pref-height: 40px;"
        );
        backButton.setOnAction(e -> cartStage.close());
        
        buttonBox.getChildren().addAll(backButton, clearButton, checkoutButton);
        
        content.getChildren().addAll(titleLabel, itemsContainer, buttonBox);
        return content;
    }
    
    /**
     * Create simple loyalty redemption button
     */
    private HBox createSimpleLoyaltyButton(BigDecimal total) {
        HBox loyaltyBox = new HBox(10);
        loyaltyBox.setAlignment(Pos.CENTER);
        loyaltyBox.setPadding(new Insets(8));
        loyaltyBox.setStyle(
            "-fx-background-color: rgba(255, 215, 0, 0.1); " +
            "-fx-background-radius: 6; " +
            "-fx-border-color: #FFD700; " +
            "-fx-border-radius: 6; " +
            "-fx-border-width: 1;"
        );
        
        // Get current loyalty points - refresh from database
        int currentPoints = cafeteriaSystem.getLoyaltyService().getPointsBalance(student.getStudentId());
        
        // Points display with refresh button
        HBox pointsBox = new HBox(5);
        pointsBox.setAlignment(Pos.CENTER_LEFT);
        
        Label pointsLabel = new Label("🎯 " + currentPoints + " pts");
        pointsLabel.setStyle(
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #8B4513;"
        );
        
        Button refreshButton = new Button("🔄");
        refreshButton.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #8B4513; " +
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 10px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 3; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 20px; " +
            "-fx-pref-height: 20px; " +
            "-fx-border-color: #8B4513; " +
            "-fx-border-radius: 3; " +
            "-fx-border-width: 1;"
        );
        
        // Check if there's an active redemption
        String loyaltyRedemptionProperty = System.getProperty("loyalty.redemption.amount");
        if (loyaltyRedemptionProperty != null) {
            try {
                BigDecimal loyaltyRedemptionAmount = new BigDecimal(loyaltyRedemptionProperty);
                if (loyaltyRedemptionAmount.compareTo(BigDecimal.ZERO) > 0) {
                    // Show active redemption info with remove button
                    HBox redemptionInfoBox = new HBox(5);
                    redemptionInfoBox.setAlignment(Pos.CENTER_LEFT);
                    
                    Label activeRedemptionLabel = new Label("✅ Active: EGP " + String.format("%.2f", loyaltyRedemptionAmount) + " off");
                    activeRedemptionLabel.setStyle(
                        "-fx-font-family: 'Arial', sans-serif; " +
                        "-fx-font-size: 10px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #4CAF50;"
                    );
                    
                    Button removeRedemptionButton = new Button("✕");
                    removeRedemptionButton.setStyle(
                        "-fx-background-color: #f44336; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Arial', sans-serif; " +
                        "-fx-font-size: 8px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 2; " +
                        "-fx-cursor: hand; " +
                        "-fx-pref-width: 16px; " +
                        "-fx-pref-height: 16px;"
                    );
                    removeRedemptionButton.setOnAction(e -> {
                        // Clear the redemption
                        System.clearProperty("loyalty.redemption.amount");
                        System.clearProperty("loyalty.redemption.points");
                        // Refresh the cart to show updated totals (will reuse existing window)
                        showMyCart();
                    });
                    
                    redemptionInfoBox.getChildren().addAll(activeRedemptionLabel, removeRedemptionButton);
                    pointsBox.getChildren().add(redemptionInfoBox);
                }
            } catch (NumberFormatException ex) {
                System.err.println("Error parsing active redemption amount: " + ex.getMessage());
            }
        }
        

        
        pointsBox.getChildren().addAll(pointsLabel, refreshButton);
        
        // Simple redemption button (only if student has enough points)
        Button redeemButton = new Button("Redeem Points");
        redeemButton.setDisable(currentPoints < 50);
        
        if (currentPoints >= 50) {
            redeemButton.setStyle(
                "-fx-background-color: #FF9800; " +
                "-fx-text-fill: white; " +
                "-fx-font-family: 'Arial', sans-serif; " +
                "-fx-font-size: 11px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 4; " +
                "-fx-cursor: hand; " +
                "-fx-pref-width: 100px; " +
                "-fx-pref-height: 28px;"
            );
            
            redeemButton.setOnAction(e -> {
                // Refresh points before showing dialog
                int refreshedPoints = cafeteriaSystem.getLoyaltyService().getPointsBalance(student.getStudentId());
                showSimpleLoyaltyDialog(refreshedPoints, total);
            });
        } else {
            redeemButton.setStyle(
                "-fx-background-color: #CCCCCC; " +
                "-fx-text-fill: #999999; " +
                "-fx-font-family: 'Arial', sans-serif; " +
                "-fx-font-size: 11px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 4; " +
                "-fx-pref-width: 100px; " +
                "-fx-pref-height: 28px;"
            );
        }
        
        // Now update the refresh button to also update the redeem button state
        final Button finalRedeemButton = redeemButton;
        refreshButton.setOnAction(e -> {
            // Force database sync to get latest points
            cafeteriaSystem.forceDatabaseSync();
            
            // Refresh points and update display
            int refreshedPoints = cafeteriaSystem.getLoyaltyService().getPointsBalance(student.getStudentId());
            // Update the loyalty account to reflect the new balance
            int currentPointsInMemory = student.getLoyaltyPoints();
            if (refreshedPoints != currentPointsInMemory) {
                int difference = refreshedPoints - currentPointsInMemory;
                if (difference > 0) {
                    student.addLoyaltyPoints(difference);
                }
            }
            pointsLabel.setText("🎯 " + refreshedPoints + " pts");
            
            // Update redeem button state
            finalRedeemButton.setDisable(refreshedPoints < 50);
            if (refreshedPoints >= 50) {
                finalRedeemButton.setStyle(
                    "-fx-background-color: #FF9800; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-family: 'Arial', sans-serif; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 4; " +
                    "-fx-cursor: hand; " +
                    "-fx-pref-width: 100px; " +
                    "-fx-pref-height: 28px;"
                );
            } else {
                finalRedeemButton.setStyle(
                    "-fx-background-color: #CCCCCC; " +
                    "-fx-text-fill: #999999; " +
                    "-fx-font-family: 'Arial', sans-serif; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 4; " +
                    "-fx-pref-width: 100px; " +
                    "-fx-pref-height: 28px;"
                );
            }
        });
        
        loyaltyBox.getChildren().addAll(pointsBox, redeemButton);
        return loyaltyBox;
    }
    
    /**
     * Show simple loyalty redemption dialog
     */
    private void showSimpleLoyaltyDialog(int currentPoints, BigDecimal total) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("🎯 Loyalty Points Redemption");
        dialogStage.setResizable(false);
        
        VBox dialogContent = new VBox(15);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(25));
        dialogContent.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);"
        );
        
        // Title
        Label titleLabel = new Label("🎯 Redeem Your Points");
        titleLabel.setStyle(
            "-fx-font-family: 'Arial Black', 'Arial', sans-serif; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #8B4513;"
        );
        
        // Current points info
        Label pointsInfoLabel = new Label("You have " + currentPoints + " points available");
        pointsInfoLabel.setStyle(
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #666666;"
        );
        
        // Redemption options based on FR4.2 requirements
        VBox optionsBox = new VBox(10);
        
        // Option 1: 50 points = EGP 10 discount (as per FR4.2)
        if (currentPoints >= 50) {
            HBox option1 = createRedemptionOption("50 points = EGP 10 discount", 50, new BigDecimal("10.00"), total, dialogStage);
            optionsBox.getChildren().add(option1);
        }
        
        // Option 2: 100 points = free water (as per FR4.2)
        if (currentPoints >= 100) {
            HBox option2 = createRedemptionOption("100 points = Free Water", 100, BigDecimal.ZERO, total, dialogStage);
            optionsBox.getChildren().add(option2);
        }
        
        // Close button
        Button closeButton = new Button("Cancel");
        closeButton.setStyle(
            "-fx-background-color: #999999; " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 80px; " +
            "-fx-pref-height: 30px;"
        );
        closeButton.setOnAction(e -> dialogStage.close());
        
        dialogContent.getChildren().addAll(titleLabel, pointsInfoLabel, optionsBox, closeButton);
        
        Scene dialogScene = new Scene(dialogContent, 280, 200);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }
    
    /**
     * Create redemption option button
     */
    private HBox createRedemptionOption(String description, int pointsCost, BigDecimal discountAmount, BigDecimal total, Stage dialogStage) {
        HBox option = new HBox(10);
        option.setAlignment(Pos.CENTER_LEFT);
        
        Label descLabel = new Label(description);
        descLabel.setStyle(
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 12px; " +
            "-fx-text-fill: #2C1810;"
        );
        
        Button useButton = new Button("Use");
        useButton.setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 11px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 4; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 50px; " +
            "-fx-pref-height: 25px;"
        );
        
        useButton.setOnAction(e -> {
            // Apply the redemption
            applyLoyaltyRedemption(pointsCost, discountAmount);
            dialogStage.close();
        });
        
        option.getChildren().addAll(descLabel, useButton);
        return option;
    }
    
    /**
     * Apply loyalty redemption
     */
    private void applyLoyaltyRedemption(int pointsCost, BigDecimal discountAmount) {
        try {
            // Double-check points balance before attempting redemption
            int currentBalance = cafeteriaSystem.getLoyaltyService().getPointsBalance(student.getStudentId());
            if (currentBalance < pointsCost) {
                showAlert("❌ Insufficient Points", 
                    "You only have " + currentBalance + " points, but need " + pointsCost + " points for this redemption.\n" +
                    "Please refresh your points balance or choose a different redemption option.");
                return;
            }
            
            // Deduct points from loyalty account (FR4.3)
            BigDecimal discount = cafeteriaSystem.getLoyaltyService().redeemPointsForDiscount(
                student.getStudentId(), 
                pointsCost, 
                "Loyalty redemption: " + (discountAmount.compareTo(BigDecimal.ZERO) == 0 ? "Free item" : "EGP " + discountAmount + " discount")
            );
            
            boolean success = discount.compareTo(BigDecimal.ZERO) >= 0;
            
            if (success) {
                // Handle free item redemption (100 points for free water)
                if (discountAmount.compareTo(BigDecimal.ZERO) == 0 && pointsCost == 100) {
                    // Check if free water is already in cart
                    boolean freeWaterAlreadyInCart = cartItems.stream()
                        .anyMatch(item -> item.getName().contains("Water (Free via Loyalty)"));
                    
                    if (!freeWaterAlreadyInCart) {
                        // Add free water to cart
                        addFreeWaterToCart();
                        // For free water, NO additional discount should be applied
                        // The water is already free in the cart
                        System.setProperty("loyalty.redemption.amount", "0.00");
                        System.setProperty("loyalty.redemption.points", String.valueOf(pointsCost));
                        System.out.println("💧 Free water redemption: Water is already free in cart, no additional discount needed");
                    } else {
                        // Free water already in cart, don't redeem points again
                        showAlert("💧 Already Redeemed", "Free water is already in your cart from a previous redemption.");
                        return;
                    }
                } else {
                    // Store redemption info for discount checkout
                    System.setProperty("loyalty.redemption.amount", discountAmount.toString());
                    System.setProperty("loyalty.redemption.points", String.valueOf(pointsCost));
                }
                
                // Update student's loyalty points in memory
                int newPoints = cafeteriaSystem.getLoyaltyService().getPointsBalance(student.getStudentId());
                // Update the loyalty account to reflect the new balance
                int currentPointsInMemory = student.getLoyaltyPoints();
                if (newPoints != currentPointsInMemory) {
                    int difference = newPoints - currentPointsInMemory;
                    if (difference > 0) {
                        student.addLoyaltyPoints(difference);
                    }
                }
                
                // Show success message
                String message = "Successfully redeemed " + pointsCost + " points for ";
                if (discountAmount.compareTo(BigDecimal.ZERO) == 0) {
                    message += "free water!";
                } else {
                    message += "EGP " + discountAmount + " discount!";
                }
                message += "\nNew balance: " + newPoints + " points";
                
                if (discountAmount.compareTo(BigDecimal.ZERO) == 0 && pointsCost == 100) {
                    message += "\n💧 Free water has been added to your cart!";
                }
                
                showAlert("🎉 Points Redeemed!", message);
                
                // Refresh cart to show updated info (will reuse existing window)
                showMyCart();
                
                // Also refresh the main interface loyalty display if it exists
                refreshMainInterfaceLoyaltyDisplay();
                
            } else {
                showAlert("❌ Failed", "Could not redeem points. Please try again.");
            }
        } catch (Exception ex) {
            System.err.println("Error redeeming loyalty points: " + ex.getMessage());
            showAlert("❌ Error", "Failed to redeem points: " + ex.getMessage());
        }
    }
    
    /**
     * Add free water to cart when redeemed with 100 points
     */
    private void addFreeWaterToCart() {
        // Check if free water is already in cart
        boolean freeWaterExists = cartItems.stream()
            .anyMatch(item -> item.getName().contains("Water (Free via Loyalty)"));
        
        if (!freeWaterExists) {
            // Add free water to cart using existing water menu item
            // We'll use a special name but with 0 price to indicate it's free
            CartItem freeWater = new CartItem("WATER_LOYALTY", "💧 Water (Free via Loyalty)", BigDecimal.ZERO, 1);
            cartItems.add(freeWater);
            System.out.println("Added free water to cart via loyalty redemption");
        } else {
            // Free water already exists, don't add more
            System.out.println("Free water already exists in cart, skipping duplicate addition");
        }
    }
    
    /**
     * Get the price of water from menu items
     */
    private BigDecimal getWaterPrice() {
        try {
            // Get all menu items and find water
            List<MenuItem> allItems = cafeteriaSystem.getMenuService().getAllMenuItems();
            Optional<MenuItem> waterItem = allItems.stream()
                .filter(item -> item.getName().toLowerCase().contains("water") || 
                               item.getName().toLowerCase().contains("💧"))
                .findFirst();
            
            if (waterItem.isPresent()) {
                return waterItem.get().getPrice();
            } else {
                // Default water price if not found
                return new BigDecimal("8.00");
            }
        } catch (Exception ex) {
            System.err.println("Error getting water price: " + ex.getMessage());
            // Default water price as fallback
            return new BigDecimal("8.00");
        }
    }
    
    /**
     * Find the water menu item
     */
    private MenuItem findWaterMenuItem() {
        try {
            List<MenuItem> allItems = cafeteriaSystem.getMenuService().getAllMenuItems();
            Optional<MenuItem> waterItem = allItems.stream()
                .filter(item -> item.getName().toLowerCase().contains("water") || 
                               item.getName().toLowerCase().contains("💧"))
                .findFirst();
            
            return waterItem.orElse(null);
        } catch (Exception ex) {
            System.err.println("Error finding water menu item: " + ex.getMessage());
            return null;
        }
    }
    

    
    /**
     * Refresh the main interface loyalty display
     */
    private void refreshMainInterfaceLoyaltyDisplay() {
        // Refresh student's points from database
        int refreshedPoints = cafeteriaSystem.getLoyaltyService().getPointsBalance(student.getStudentId());
        // Update the loyalty account to reflect the new balance
        int currentPointsInMemory = student.getLoyaltyPoints();
        if (refreshedPoints != currentPointsInMemory) {
            int difference = refreshedPoints - currentPointsInMemory;
            if (difference > 0) {
                student.addLoyaltyPoints(difference);
            }
        }
        
        // Update any loyalty displays in the main interface
        // This will be called after points are redeemed to keep everything in sync
        System.out.println("Main interface loyalty display refreshed. New balance: " + refreshedPoints + " points");
    }
    
    private HBox createCartItemRow(CartItem item) {
        HBox itemRow = new HBox(15);
        itemRow.setAlignment(Pos.CENTER_LEFT);
        itemRow.setPadding(new Insets(10));
        
        // Item info
        VBox itemInfo = new VBox(3);
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle(
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2C1810;"
        );
        
        Label priceLabel = new Label("EGP " + String.format("%.2f", item.getPrice()));
        priceLabel.setStyle(
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 12px; " +
            "-fx-text-fill: #666666;"
        );
        
        itemInfo.getChildren().addAll(nameLabel, priceLabel);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Quantity controls
        HBox quantityBox = new HBox(5);
        quantityBox.setAlignment(Pos.CENTER);
        
        Button minusButton = new Button("-");
        minusButton.setStyle(
            "-fx-background-color: #FF6B35; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 5; " +
            "-fx-pref-width: 30px; " +
            "-fx-pref-height: 30px;"
        );
        minusButton.setOnAction(e -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
            } else {
                cartItems.remove(item);
            }
            // Refresh display by reopening cart
            Stage currentStage = (Stage) minusButton.getScene().getWindow();
            currentStage.close();
            showMyCart();
        });
        
        Label quantityLabel = new Label(String.valueOf(item.getQuantity()));
        quantityLabel.setStyle(
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2C1810; " +
            "-fx-min-width: 30px; " +
            "-fx-alignment: center;"
        );
        
        Button plusButton = new Button("+");
        plusButton.setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 5; " +
            "-fx-pref-width: 30px; " +
            "-fx-pref-height: 30px;"
        );
        plusButton.setOnAction(e -> {
            item.setQuantity(item.getQuantity() + 1);
            // Refresh display by reopening cart
            Stage currentStage = (Stage) plusButton.getScene().getWindow();
            currentStage.close();
            showMyCart();
        });
        
        quantityBox.getChildren().addAll(minusButton, quantityLabel, plusButton);
        
        // Subtotal
        BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        Label subtotalLabel = new Label("EGP " + String.format("%.2f", subtotal));
        subtotalLabel.setStyle(
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #FF6B35; " +
            "-fx-min-width: 80px; " +
            "-fx-alignment: center-right;"
        );
        
        itemRow.getChildren().addAll(itemInfo, spacer, quantityBox, subtotalLabel);
        return itemRow;
    }
    
    private void showOrderHistory() {
        Stage orderHistoryStage = new Stage();
        orderHistoryStage.setTitle("📋 Order History - University Cafeteria");
        
        // Create main container
        StackPane root = new StackPane();
        root.setPrefSize(1000, 700);
        
        // Set background gradient
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #FF6B35, #F7931E, #FFD23F); " +
            "-fx-background-radius: 0;"
        );
        
        // Create order history content
        VBox orderHistoryContent = createOrderHistoryContent(orderHistoryStage);
        root.getChildren().add(orderHistoryContent);
        
        // Add back button in top-left corner
        Button backButton = createOrderHistoryBackButton(orderHistoryStage);
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(20));
        root.getChildren().add(backButton);
        
        // Add refresh button in top-right corner
        Button refreshButton = createOrderHistoryRefreshButton(orderHistoryStage);
        StackPane.setAlignment(refreshButton, Pos.TOP_RIGHT);
        StackPane.setMargin(refreshButton, new Insets(20));
        root.getChildren().add(refreshButton);
        
        Scene orderHistoryScene = new Scene(root, 1000, 700);
        orderHistoryStage.setScene(orderHistoryScene);
        orderHistoryStage.show();
    }
    
    private VBox createOrderHistoryContent(Stage orderHistoryStage) {
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        
        // Title
        Label titleLabel = new Label("📋 ORDER HISTORY");
        titleLabel.setStyle(
            "-fx-font-family: 'Arial Black', 'Arial', sans-serif; " +
            "-fx-font-size: 28px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: white; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 6, 0, 0, 2);"
        );
        
        // Create table container
        VBox tableContainer = new VBox(10);
        tableContainer.setAlignment(Pos.CENTER);
        tableContainer.setMaxWidth(900);
        tableContainer.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.95); " +
            "-fx-background-radius: 15; " +
            "-fx-padding: 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 3);"
        );
        
        // Create orders table
        TableView<Order> ordersTable = createOrdersTable();
        tableContainer.getChildren().add(ordersTable);
        
        // Load orders for this student
        loadStudentOrders(ordersTable);
        
        content.getChildren().addAll(titleLabel, tableContainer);
        return content;
    }
    
    private TableView<Order> createOrdersTable() {
        TableView<Order> table = new TableView<>();
        table.setPrefHeight(400);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Order ID column
        TableColumn<Order, String> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderIdCol.setMinWidth(100);
        
        // Date column
        TableColumn<Order, String> dateCol = new TableColumn<>("Date & Time");
        dateCol.setCellValueFactory(cellData -> {
            String formattedDate = cellData.getValue().getOrderTime()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return new javafx.beans.property.SimpleStringProperty(formattedDate);
        });
        dateCol.setMinWidth(130);
        
        // Status column
        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setMinWidth(100);
        
        // Items column
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
        itemsCol.setMinWidth(250);
        
        // Total column - show final amount after loyalty discount
        TableColumn<Order, String> totalCol = new TableColumn<>("Total (EGP)");
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
        totalCol.setMinWidth(120);
        
        // Loyalty Points column
        TableColumn<Order, Integer> loyaltyCol = new TableColumn<>("Points Earned");
        loyaltyCol.setCellValueFactory(new PropertyValueFactory<>("loyaltyPointsEarned"));
        loyaltyCol.setMinWidth(100);
        
        // Loyalty Redemption column
        TableColumn<Order, String> redemptionCol = new TableColumn<>("Loyalty Redemption");
        redemptionCol.setCellValueFactory(cellData -> {
            Order order = cellData.getValue();
            int pointsRedeemed = order.getLoyaltyPointsRedeemed();
            BigDecimal discount = order.getDiscountAmount();
            
            if (pointsRedeemed > 0 && discount.compareTo(BigDecimal.ZERO) > 0) {
                return new javafx.beans.property.SimpleStringProperty(
                    String.format("%d pts (-EGP %.2f)", pointsRedeemed, discount));
            } else {
                return new javafx.beans.property.SimpleStringProperty("-");
            }
        });
        redemptionCol.setMinWidth(150);
        
        table.getColumns().addAll(orderIdCol, dateCol, statusCol, itemsCol, totalCol, loyaltyCol, redemptionCol);
        
        // Style the table
        table.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-border-radius: 10;"
        );
        
        return table;
    }
    
    private void loadStudentOrders(TableView<Order> table) {
        try {
            // Force database refresh to get updated order data with loyalty information
            System.out.println("🔄 Refreshing orders from database for student: " + student.getStudentId());
            cafeteriaSystem.forceDatabaseSync(); // Crucial for refreshing data
            List<Order> studentOrders = cafeteriaSystem.getOrderService().getOrdersByStudent(student.getStudentId());
            System.out.println("Loading orders for student: " + student.getStudentId());
            System.out.println("Found " + studentOrders.size() + " orders");
            // Debug: Print loyalty information for each order
            for (Order order : studentOrders) {
                System.out.println("Order " + order.getOrderId() + ": " +
                    "Total=" + order.getTotalAmount() +
                    ", Discount=" + order.getDiscountAmount() +
                    ", PointsRedeemed=" + order.getLoyaltyPointsRedeemed() +
                    ", PointsEarned=" + order.getLoyaltyPointsEarned());
            }
            ObservableList<Order> orders = FXCollections.observableArrayList(studentOrders);
            table.setItems(orders);
        } catch (Exception e) {
            System.err.println("Error loading student orders: " + e.getMessage());
            e.printStackTrace();
            table.setItems(FXCollections.observableArrayList());
        }
    }
    
    /**
     * Overloaded version for refresh button that doesn't require table parameter
     */
    private void loadStudentOrders() {
        try {
            // Force database refresh to get updated order data with loyalty information
            System.out.println("🔄 Refreshing orders from database for student: " + student.getStudentId());
            cafeteriaSystem.forceDatabaseSync(); // Crucial for refreshing data
            List<Order> studentOrders = cafeteriaSystem.getOrderService().getOrdersByStudent(student.getStudentId());
            System.out.println("Loading orders for student: " + student.getStudentId());
            System.out.println("Found " + studentOrders.size() + " orders");
            // Debug: Print loyalty information for each order
            for (Order order : studentOrders) {
                System.out.println("Order " + order.getOrderId() + ": " +
                    "Total=" + order.getTotalAmount() +
                    ", Discount=" + order.getDiscountAmount() +
                    ", PointsRedeemed=" + order.getLoyaltyPointsRedeemed() +
                    ", PointsEarned=" + order.getLoyaltyPointsEarned());
            }
            // Note: This method is for refresh button, so we don't update any table
            // The table will be updated when the order history is shown again
        } catch (Exception e) {
            System.err.println("Error loading student orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    
    private Button createOrderHistoryBackButton(Stage orderHistoryStage) {
        Button backButton = new Button("← Back");
        backButton.setStyle(
            "-fx-background-color: rgba(211, 47, 47, 0.9); " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 20; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 100px; " +
            "-fx-pref-height: 40px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
        );
        
        // Add hover effect
        backButton.setOnMouseEntered(e -> {
            backButton.setStyle(
                "-fx-background-color: rgba(183, 28, 28, 0.9); " +
                "-fx-text-fill: white; " +
                "-fx-font-family: 'Arial', sans-serif; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 20; " +
                "-fx-cursor: hand; " +
                "-fx-pref-width: 100px; " +
                "-fx-pref-height: 40px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 3);" +
                "-fx-scale-x: 1.05; -fx-scale-y: 1.05;"
            );
        });
        
        backButton.setOnMouseExited(e -> {
            backButton.setStyle(
                "-fx-background-color: rgba(211, 47, 47, 0.9); " +
                "-fx-text-fill: white; " +
                "-fx-font-family: 'Arial', sans-serif; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 20; " +
                "-fx-cursor: hand; " +
                "-fx-pref-width: 100px; " +
                "-fx-pref-height: 40px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
            );
        });
        
        backButton.setOnAction(e -> orderHistoryStage.close());
        
        return backButton;
    }
    
    private Button createOrderHistoryRefreshButton(Stage orderHistoryStage) {
        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setStyle(
            "-fx-background-color: rgba(33, 150, 243, 0.9); " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 20; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 120px; " +
            "-fx-pref-height: 40px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
        );
        
        // Add hover effect
        refreshButton.setOnMouseEntered(e -> {
            refreshButton.setStyle(
                "-fx-background-color: rgba(21, 101, 192, 0.9); " +
                "-fx-text-fill: white; " +
                "-fx-font-family: 'Arial', sans-serif; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 20; " +
                "-fx-cursor: hand; " +
                "-fx-pref-width: 120px; " +
                "-fx-pref-height: 40px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
            );
        });
        
        refreshButton.setOnMouseExited(e -> {
            refreshButton.setStyle(
                "-fx-background-color: rgba(33, 150, 243, 0.9); " +
                "-fx-text-fill: white; " +
                "-fx-font-family: 'Arial', sans-serif; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 20; " +
                "-fx-cursor: hand; " +
                "-fx-pref-width: 120px; " +
                "-fx-pref-height: 40px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
            );
        });
        
        // Refresh the orders table
        refreshButton.setOnAction(e -> {
            // Simply reload orders with fresh data
            loadStudentOrders();
            
            // Show success message
            showAlert("Success", "Order history refreshed! Loyalty information should now be visible.");
        });
        
        return refreshButton;
    }
    
    private void showProfile() {
        Stage profileStage = new Stage();
        profileStage.setTitle("👤 Profile & Loyalty - University Cafeteria");
        
        // Create main container
        StackPane root = new StackPane();
        root.setPrefSize(800, 600);
        
        // Set background gradient
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #FF6B35, #F7931E, #FFD23F); " +
            "-fx-background-radius: 0;"
        );
        
        // Create profile content
        VBox profileContent = createProfileContent(profileStage);
        root.getChildren().add(profileContent);
        
        // Add back button in top-left corner
        Button backButton = createProfileBackButton(profileStage);
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(20));
        root.getChildren().add(backButton);
        
        Scene profileScene = new Scene(root, 800, 600);
        profileStage.setScene(profileScene);
        profileStage.show();
    }
    
    private VBox createProfileContent(Stage profileStage) {
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        
        // Title
        Label titleLabel = new Label("👤 PROFILE & LOYALTY");
        titleLabel.setStyle(
            "-fx-font-family: 'Arial Black', 'Arial', sans-serif; " +
            "-fx-font-size: 28px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: white; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 6, 0, 0, 2);"
        );
        
        // Profile information container
        VBox profileContainer = new VBox(20);
        profileContainer.setAlignment(Pos.CENTER);
        profileContainer.setMaxWidth(600);
        profileContainer.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.95); " +
            "-fx-background-radius: 15; " +
            "-fx-padding: 30; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 3);"
        );
        
        // Student info section
        VBox studentInfoSection = createInfoSection("📋 Student Information", 
            "Name: " + student.getName(),
            "Student ID: " + student.getStudentId(),
            "Account Type: Student"
        );
        
        // Get loyalty points (refresh from service)
        int loyaltyPoints = cafeteriaSystem.getLoyaltyService().getPointsBalance(student.getStudentId());
        System.out.println("Loading loyalty points for student " + student.getStudentId() + ": " + loyaltyPoints + " points");
        
        // Loyalty section
        VBox loyaltySection = createInfoSection("🎯 Loyalty Program", 
            "Current Points: " + loyaltyPoints + " points",
            "Point Value: 1 point = 0.20 EGP discount",
            "Earn Rate: 1 point per 10 EGP spent"
        );
        
        // Loyalty benefits section
        VBox benefitsSection = createInfoSection("🏆 Loyalty Benefits", 
            "✓ Earn points with every purchase",
            "✓ Redeem points for discounts",
            "✓ Special member-only offers",
            "✓ Birthday bonus points"
        );
        
        profileContainer.getChildren().addAll(studentInfoSection, loyaltySection, benefitsSection);
        
        // Refresh button
        Button refreshButton = new Button("🔄 Refresh Points");
        refreshButton.setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 150px; " +
            "-fx-pref-height: 40px;"
        );
        refreshButton.setOnAction(e -> {
            // Refresh the profile to show updated points
            profileStage.close();
            showProfile();
        });
        
        content.getChildren().addAll(titleLabel, profileContainer, refreshButton);
        return content;
    }
    
    private VBox createInfoSection(String title, String... infoLines) {
        VBox section = new VBox(10);
        section.setAlignment(Pos.CENTER_LEFT);
        
        // Section title
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-family: 'Arial Black', 'Arial', sans-serif; " +
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #FF6B35;"
        );
        
        section.getChildren().add(titleLabel);
        
        // Add info lines
        for (String line : infoLines) {
            Label infoLabel = new Label(line);
            infoLabel.setStyle(
                "-fx-font-family: 'Arial', sans-serif; " +
                "-fx-font-size: 14px; " +
                "-fx-text-fill: #2C1810; " +
                "-fx-padding: 2 0 2 20;"
            );
            section.getChildren().add(infoLabel);
        }
        
        return section;
    }
    
    private Button createProfileBackButton(Stage profileStage) {
        Button backButton = new Button("← Back");
        backButton.setStyle(
            "-fx-background-color: rgba(211, 47, 47, 0.9); " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Arial', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 20; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 100px; " +
            "-fx-pref-height: 40px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
        );
        
        // Add hover effect
        backButton.setOnMouseEntered(e -> {
            backButton.setStyle(
                "-fx-background-color: rgba(183, 28, 28, 0.9); " +
                "-fx-text-fill: white; " +
                "-fx-font-family: 'Arial', sans-serif; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 20; " +
                "-fx-cursor: hand; " +
                "-fx-pref-width: 100px; " +
                "-fx-pref-height: 40px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 3);" +
                "-fx-scale-x: 1.05; -fx-scale-y: 1.05;"
            );
        });
        
        backButton.setOnMouseExited(e -> {
            backButton.setStyle(
                "-fx-background-color: rgba(211, 47, 47, 0.9); " +
                "-fx-text-fill: white; " +
                "-fx-font-family: 'Arial', sans-serif; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 20; " +
                "-fx-cursor: hand; " +
                "-fx-pref-width: 100px; " +
                "-fx-pref-height: 40px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
            );
        });
        
        backButton.setOnAction(e -> profileStage.close());
        
        return backButton;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5); " +
            "-fx-padding: 20;"
        );
        alert.showAndWait();
    }
    
    private void logout() {
        LoginController loginController = new LoginController(cafeteriaSystem, primaryStage);
        Scene loginScene = loginController.createLoginScene();
        primaryStage.setScene(loginScene);
    }
    

    
    // Helper methods
    private void loadMenuItems() {
        System.out.println("🔄 Loading menu items for student interface...");
        
        // First, ensure drinks are available in the menu service
        cafeteriaSystem.ensureDrinksInMenuService();
        
        // Force database sync to prevent foreign key constraint issues
        cafeteriaSystem.forceDatabaseSync();
        
        // Load items from menu service
        List<MenuItem> allItems = cafeteriaSystem.getMenuService().getAllMenuItems();
        menuItems.setAll(allItems);
        
        // Debug: Check what items were loaded
        System.out.println("📋 Loaded " + allItems.size() + " menu items:");
        for (MenuItem item : allItems) {
            System.out.println("   - " + item.getName() + " (Category: " + item.getCategory() + ")");
        }
        
        // Check specifically for drinks
        long drinkCount = allItems.stream()
            .filter(item -> item.getCategory() == MenuItem.MenuCategory.DRINK)
            .count();
        System.out.println("🥤 Found " + drinkCount + " drink items in menu service");
        
        if (drinkCount == 0) {
            System.out.println("⚠️  WARNING: No drinks found! This will cause ordering issues!");
            
            // Try to force refresh the menu
            System.out.println("🔄 Attempting to force refresh menu...");
            try {
                cafeteriaSystem.refreshMenuFromDatabase();
                loadMenuItems(); // Reload after refresh
                
                // Test again after refresh
                long newDrinkCount = cafeteriaSystem.getMenuService().getAllMenuItems().stream()
                    .filter(item -> item.getCategory() == MenuItem.MenuCategory.DRINK)
                    .count();
                System.out.println("🥤 After refresh: " + newDrinkCount + " drinks available");
                
            } catch (Exception e) {
                System.err.println("❌ Failed to force refresh menu: " + e.getMessage());
            }
        }
    }
    

    
    private MenuItem findMenuItemByName(String name) {
        System.out.println("🔍 Looking for menu item: '" + name + "'");
        System.out.println("📋 Current menuItems list has " + menuItems.size() + " items");
        
        // Handle free water from loyalty redemption
        if (name.contains("Water") && name.contains("Free") && name.contains("Loyalty")) {
            // Find the regular water item to use as base
            List<MenuItem> allItems = cafeteriaSystem.getMenuService().getAllMenuItems();
            for (MenuItem item : allItems) {
                if (item.getName().contains("💧") && item.getName().contains("Water")) {
                    System.out.println("Found water item for loyalty redemption: '" + item.getName() + "'");
                    return item;
                }
            }
        }
        
        // First try to find exact match in loaded menu items
        for (MenuItem item : menuItems) {
            if (item.getName().equals(name)) {
                System.out.println("✅ Found exact match in menuItems: " + item.getName());
                return item;
            }
        }
        
        // If not found, search all menu items
        List<MenuItem> allItems = cafeteriaSystem.getMenuService().getAllMenuItems();
        System.out.println("📋 MenuService has " + allItems.size() + " items");
        
        for (MenuItem item : allItems) {
            if (item.getName().equals(name)) {
                System.out.println("✅ Found exact match in MenuService: " + item.getName());
                return item;
            }
        }
        
        // If still not found, try to find by partial match (remove emojis and compare)
        String cleanName = name.replaceAll("[🍔🍗🍟🥤]\\s*", "").trim();
        for (MenuItem item : allItems) {
            String cleanItemName = item.getName().replaceAll("[🍔🍗🍟🥤]\\s*", "").trim();
            if (cleanItemName.equalsIgnoreCase(cleanName)) {
                System.out.println("Found partial match: '" + name + "' -> '" + item.getName() + "'");
                return item;
            }
        }
        
        // Try alternative mappings for common mismatches
        if (name.contains("Classic Burger")) {
            for (MenuItem item : allItems) {
                if (item.getName().contains("Beef Burger") || item.getName().contains("Classic")) {
                    System.out.println("Found alternative match: '" + name + "' -> '" + item.getName() + "'");
                    return item;
                }
            }
        }
        
        if (name.contains("Cheese Burger")) {
            for (MenuItem item : allItems) {
                if (item.getName().contains("Cheese") && item.getName().contains("Burger")) {
                    System.out.println("Found alternative match: '" + name + "' -> '" + item.getName() + "'");
                    return item;
                }
            }
        }
        
        if (name.contains("Deluxe Burger")) {
            for (MenuItem item : allItems) {
                if (item.getName().contains("Deluxe") || item.getName().contains("Grilled Chicken")) {
                    System.out.println("Found alternative match: '" + name + "' -> '" + item.getName() + "'");
                    return item;
                }
            }
        }
        
        if (name.contains("Chicken Wings")) {
            for (MenuItem item : allItems) {
                if (item.getName().contains("Chicken Wings") || item.getName().contains("Wings")) {
                    System.out.println("Found alternative match: '" + name + "' -> '" + item.getName() + "'");
                    return item;
                }
            }
        }
        
        if (name.contains("French Fries")) {
            for (MenuItem item : allItems) {
                if (item.getName().contains("French Fries") || item.getName().contains("Fries")) {
                    System.out.println("Found alternative match: '" + name + "' -> '" + item.getName() + "'");
                    return item;
                }
            }
        }
        
        if (name.contains("Cola")) {
            for (MenuItem item : allItems) {
                if (item.getName().contains("Cola") || item.getName().contains("Drink")) {
                    System.out.println("Found alternative match: '" + name + "' -> '" + item.getName() + "'");
                    return item;
                }
            }
        }
        
        if (name.contains("Sprite")) {
            for (MenuItem item : allItems) {
                if (item.getName().contains("Sprite") || item.getName().contains("Lemonade")) {
                    System.out.println("Found alternative match: '" + name + "' -> '" + item.getName() + "'");
                    return item;
                }
            }
        }
        
        System.out.println("ERROR: Could not find any match for menu item: '" + name + "'");
        System.out.println("Available items in menu:");
        for (MenuItem item : allItems) {
            System.out.println("  - " + item.getName());
        }
        
        // Also check what's in the menuItems list
        System.out.println("Items in menuItems list:");
        for (MenuItem item : menuItems) {
            System.out.println("  - " + item.getName());
        }
        return null;
    }
    
    // Inner class for cart items (kept for compatibility)
    public static class CartItem {
        private String itemId;
        private String name;
        private BigDecimal price;
        private int quantity;
        
        public CartItem(String itemId, String name, BigDecimal price, int quantity) {
            this.itemId = itemId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
        
        public String getItemId() { return itemId; }
        public String getName() { return name; }
        public BigDecimal getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
