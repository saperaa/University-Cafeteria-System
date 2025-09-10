package com.university.cafeteria.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.university.cafeteria.service.CafeteriaSystem;
import com.university.cafeteria.gui.controllers.LoginController;

/**
 * Main JavaFX Application class for the Cafeteria System GUI
 * Replaces the console interface with a modern graphical interface
 */
public class CafeteriaGUI extends Application {
    
    private CafeteriaSystem cafeteriaSystem;
    
    @Override
    public void init() {
        // Initialize the cafeteria system
        cafeteriaSystem = new CafeteriaSystem();
    }
    
    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.setTitle("University Cafeteria Order & Loyalty System");
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            
            // Show login screen
            showLoginScreen(primaryStage);
            
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showLoginScreen(Stage primaryStage) {
        LoginController loginController = new LoginController(cafeteriaSystem, primaryStage);
        Scene loginScene = loginController.createLoginScene();
        primaryStage.setScene(loginScene);
    }
    
    public static void main(String[] args) {
        System.out.println("=== University Cafeteria Order & Loyalty System - GUI Mode ===");
        System.out.println("Launching JavaFX GUI...\n");
        launch(args);
    }
}