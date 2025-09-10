package com.university.cafeteria.gui.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import com.university.cafeteria.service.CafeteriaSystem;
import com.university.cafeteria.model.User;
import com.university.cafeteria.model.Student;
import com.university.cafeteria.model.Staff;

/**
 * Controller for the food-themed login screen
 * Uses background image with overlay buttons for authentication
 */
public class LoginController {
    
    private CafeteriaSystem cafeteriaSystem;
    private Stage primaryStage;
    
    public LoginController(CafeteriaSystem cafeteriaSystem, Stage primaryStage) {
        this.cafeteriaSystem = cafeteriaSystem;
        this.primaryStage = primaryStage;
    }
    
    public Scene createLoginScene() {
        // Create main container with background image
        StackPane root = new StackPane();
        root.setPrefSize(1000, 750);
        
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #FF6B35, #F7931E, #FFD23F); " +
            "-fx-background-radius: 0;"
        );
        
        ImageView backgroundImage = createBackgroundImage();
        if (backgroundImage.getImage() != null) {
            root.getChildren().add(backgroundImage);
        }
        
        VBox overlay = createButtonOverlay();
        root.getChildren().add(overlay);
        
        Button backButton = createBackButton();
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(20));
        root.getChildren().add(backButton);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
        
        Scene scene = new Scene(root, 1000, 750);
        return scene;
    }
    
    private ImageView createBackgroundImage() {
        ImageView imageView = new ImageView();
        Image loginImage = null;
        
        try {
            loginImage = new Image(getClass().getResourceAsStream("/images/login.png"));
            System.out.println("Attempting to load from resources: /images/login.png");
            
            if (loginImage.isError()) {
                System.out.println("Resource loading failed, trying alternative paths...");
                loginImage = new Image("file:src/main/resources/images/login.png");
                
                if (loginImage.isError()) {
                    // Fallback 2: try src/images path
                    loginImage = new Image("file:src/images/login.png");
                    
                    if (loginImage.isError()) {
                        // Fallback 3: try absolute path
                        loginImage = new Image("file:C:/Users/Data/Desktop/filedwithdatabase 3/filedwithdatabase/filed44/filed/src/main/resources/images/login.png");
                    }
                }
            }
            
            if (loginImage != null && !loginImage.isError()) {
                imageView.setImage(loginImage);
                System.out.println("Successfully loaded login.png image!");
            } else {
                System.out.println("All image loading attempts failed, using fallback");
                // Create a fallback background with gradient colors
                imageView = createFallbackBackground();
                return imageView;
            }
            
            imageView.setFitWidth(1000);
            imageView.setFitHeight(750);
            imageView.setPreserveRatio(false); // Fill the entire scene
            imageView.setSmooth(true);
            
        } catch (Exception e) {
            System.out.println("Exception loading image: " + e.getMessage());
            imageView = createFallbackBackground();
            return imageView;
        }
        
        return imageView;
    }
    
    private ImageView createFallbackBackground() {
        // Create a food-themed gradient background as fallback
        ImageView fallback = new ImageView();
        fallback.setFitWidth(1000);
        fallback.setFitHeight(750);
        // Note: Since we can't create gradients programmatically in ImageView easily,
        // we'll let the StackPane handle the background styling
        return fallback;
    }
    
    private VBox createButtonOverlay() {
        VBox overlay = new VBox(20);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPadding(new Insets(50));
        
        // Position the buttons in the lower portion of the screen like in the design
        VBox buttonContainer = new VBox(15);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setMaxWidth(400);
        
        // Create LOGIN button (matching the golden/orange theme)
        Button loginButton = createStyledButton("LOG IN", true);
        loginButton.setOnAction(e -> showLoginDialog());
        
        // Create REGISTER button
        Button registerButton = createStyledButton("REGISTER", false);
        registerButton.setOnAction(e -> showRegistrationDialog());
        
        buttonContainer.getChildren().addAll(loginButton, registerButton);
        
        // Add some spacing to push buttons to lower part of screen
        Region topSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        
        overlay.getChildren().addAll(topSpacer, buttonContainer);
        
        return overlay;
    }
    
    private Button createStyledButton(String text, boolean isPrimary) {
        Button button = new Button(text);
        
        // Style matching the golden/orange theme from the image
        String baseStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-font-family: 'Arial Black', 'Arial', sans-serif; " +
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 25; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 300px; " +
            "-fx-pref-height: 60px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);",
            isPrimary ? "#FFB84D" : "#FF9933", // Golden orange colors
            "#2C1810" // Dark brown text
        );
        
        button.setStyle(baseStyle);
        
        // Add hover effects
        button.setOnMouseEntered(e -> {
            String hoverStyle = baseStyle.replace(
                isPrimary ? "#FFB84D" : "#FF9933", 
                isPrimary ? "#FFA726" : "#FF8800"
            ) + " -fx-scale-x: 1.05; -fx-scale-y: 1.05;";
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
    
    private Button createBackButton() {
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
        
        backButton.setOnAction(e -> handleBackAction());
        
        return backButton;
    }
    
    private void handleBackAction() {
        // Show a confirmation dialog before exiting
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Exit Application");
        confirmDialog.setHeaderText("Are you sure you want to exit?");
        confirmDialog.setContentText("This will close the cafeteria application.");
        
        // Style the confirmation dialog
        confirmDialog.getDialogPane().setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5); " +
            "-fx-padding: 20;"
        );
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                primaryStage.close();
            }
        });
    }
    
    private void showLoginDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("🍔 University Cafeteria - Login");
        dialog.setHeaderText("Sign in to your account");
        
        // Style the dialog
        dialog.getDialogPane().setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5); " +
            "-fx-padding: 30;"
        );
        
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.CENTER);
        
        // Username field
        VBox usernameBox = createInputField("Username", "Enter your username");
        TextField usernameField = (TextField) ((VBox) usernameBox.getChildren().get(1)).getChildren().get(0);
        
        // Password field
        VBox passwordBox = createInputField("Password", "Enter your password");
        PasswordField passwordField = (PasswordField) ((VBox) passwordBox.getChildren().get(1)).getChildren().get(0);
        
        // Status label
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px; -fx-font-weight: bold;");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(300);
        statusLabel.setAlignment(Pos.CENTER);
        
        container.getChildren().addAll(usernameBox, passwordBox, statusLabel);
        dialog.getDialogPane().setContent(container);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Style the dialog buttons
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Sign In");
        okButton.setStyle(
            "-fx-background-color: #FFB84D; " +
            "-fx-text-fill: #2C1810; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 10; " +
            "-fx-pref-width: 100px;"
        );
        
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("← Back");
        cancelButton.setStyle(
            "-fx-background-color: #D32F2F; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 10; " +
            "-fx-pref-width: 100px;"
        );
        
        // Handle Enter key
        passwordField.setOnAction(e -> okButton.fire());
        
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                handleLogin(usernameField.getText(), passwordField.getText(), statusLabel, dialog);
            }
        });
    }
    
    private VBox createInputField(String labelText, String promptText) {
        VBox fieldBox = new VBox(8);
        
        Label label = new Label(labelText);
        label.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2C1810;"
        );
        
        VBox inputContainer = new VBox();
        TextInputControl inputField;
        
        if (labelText.equals("Password")) {
            inputField = new PasswordField();
        } else {
            inputField = new TextField();
        }
        
        inputField.setPromptText(promptText);
        inputField.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #FFB84D; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-font-size: 14px; " +
            "-fx-pref-height: 40px; " +
            "-fx-pref-width: 300px; " +
            "-fx-padding: 8 12;"
        );
        
        // Add focus effects
        inputField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                inputField.setStyle(inputField.getStyle() + " -fx-border-color: #FF9933; -fx-effect: dropshadow(gaussian, rgba(255,153,51,0.4), 8, 0, 0, 0);");
            } else {
                inputField.setStyle(inputField.getStyle().replace(" -fx-border-color: #FF9933; -fx-effect: dropshadow(gaussian, rgba(255,153,51,0.4), 8, 0, 0, 0);", ""));
            }
        });
        
        inputContainer.getChildren().add(inputField);
        fieldBox.getChildren().addAll(label, inputContainer);
        
        return fieldBox;
    }
    
    private void handleLogin(String username, String password, Label statusLabel, Dialog<?> dialog) {
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("⚠️ Please enter both username and password");
            return;
        }
        
        java.util.Optional<User> userOpt = cafeteriaSystem.getAuthenticationService().login(username, password);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            statusLabel.setText("✅ Login successful!");
            statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12px; -fx-font-weight: bold;");
            
            // Close dialog and navigate
            dialog.close();
            
            if (user instanceof Student) {
                showStudentInterface((Student) user);
            } else if (user instanceof Staff) {
                showStaffInterface((Staff) user);
            }
        } else {
            statusLabel.setText("❌ Invalid username or password");
            statusLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px; -fx-font-weight: bold;");
        }
    }
    
    private void showRegistrationDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("🍔 University Cafeteria - Register");
        dialog.setHeaderText("Create your student account");
        
        dialog.getDialogPane().setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5); " +
            "-fx-padding: 30;"
        );
        
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.CENTER);
        
        // Input fields
        VBox studentIdBox = createInputField("Student ID", "Enter your student ID");
        TextField studentIdField = (TextField) ((VBox) studentIdBox.getChildren().get(1)).getChildren().get(0);
        
        VBox nameBox = createInputField("Full Name", "Enter your full name");
        TextField nameField = (TextField) ((VBox) nameBox.getChildren().get(1)).getChildren().get(0);
        
        VBox passwordBox = createInputField("Password", "Create a password");
        PasswordField passwordField = (PasswordField) ((VBox) passwordBox.getChildren().get(1)).getChildren().get(0);
        
        container.getChildren().addAll(studentIdBox, nameBox, passwordBox);
        dialog.getDialogPane().setContent(container);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Style the dialog buttons
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Register");
        okButton.setStyle(
            "-fx-background-color: #FFB84D; " +
            "-fx-text-fill: #2C1810; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 10; " +
            "-fx-pref-width: 100px;"
        );
        
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("← Back");
        cancelButton.setStyle(
            "-fx-background-color: #D32F2F; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 10; " +
            "-fx-pref-width: 100px;"
        );
        
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String studentId = studentIdField.getText();
                String name = nameField.getText();
                String password = passwordField.getText();
                
                if (!studentId.isEmpty() && !name.isEmpty() && !password.isEmpty()) {
                    try {
                        Student registeredStudent = cafeteriaSystem.getAuthenticationService().registerStudent(studentId, name, password, studentId);
                        if (registeredStudent != null) {
                            showStyledAlert("✅ Registration Successful", "Student registered successfully! You can now login.", Alert.AlertType.INFORMATION);
                        } else {
                            showStyledAlert("❌ Registration Failed", "Registration failed. Student ID might already exist.", Alert.AlertType.ERROR);
                        }
                    } catch (Exception e) {
                        showStyledAlert("❌ Registration Failed", "Registration failed: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            }
        });
    }
    
    private void showStudentInterface(Student student) {
        StudentInterfaceController studentController = new StudentInterfaceController(cafeteriaSystem, primaryStage, student);
        Scene studentScene = studentController.createStudentScene();
        primaryStage.setScene(studentScene);
    }
    
    private void showStaffInterface(Staff staff) {
        StaffInterfaceController staffController = new StaffInterfaceController(cafeteriaSystem, primaryStage, staff);
        Scene staffScene = staffController.createStaffScene();
        primaryStage.setScene(staffScene);
    }
    
    /**
     * Helper method to show styled alerts
     */
    private void showStyledAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
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
}