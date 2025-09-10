package com.university.cafeteria.gui.styles;

/**
 * Centralized styling constants for the University Cafeteria System GUI
 * Provides consistent colors, fonts, and style properties across all interfaces
 */
public class StyleConstants {
    
    // ==================== COLOR PALETTE ====================
    
    // Primary Brand Colors
    public static final String PRIMARY_BLUE = "#1565C0";
    public static final String PRIMARY_BLUE_LIGHT = "#42A5F5";
    public static final String PRIMARY_BLUE_DARK = "#0D47A1";
    
    // Secondary Colors
    public static final String SECONDARY_ORANGE = "#FF8F00";
    public static final String SECONDARY_ORANGE_LIGHT = "#FFB74D";
    public static final String SECONDARY_ORANGE_DARK = "#E65100";
    
    // Success & Action Colors
    public static final String SUCCESS_GREEN = "#2E7D32";
    public static final String SUCCESS_GREEN_LIGHT = "#66BB6A";
    public static final String SUCCESS_GREEN_DARK = "#1B5E20";
    
    // Warning & Alert Colors
    public static final String WARNING_AMBER = "#F57C00";
    public static final String WARNING_AMBER_LIGHT = "#FFB74D";
    public static final String DANGER_RED = "#C62828";
    public static final String DANGER_RED_LIGHT = "#EF5350";
    
    // Neutral Colors
    public static final String BACKGROUND_LIGHT = "#FAFAFA";
    public static final String BACKGROUND_WHITE = "#FFFFFF";
    public static final String CARD_BACKGROUND = "#FFFFFF";
    public static final String BORDER_LIGHT = "#E0E0E0";
    public static final String TEXT_PRIMARY = "#212121";
    public static final String TEXT_SECONDARY = "#757575";
    public static final String TEXT_HINT = "#BDBDBD";
    
    // ==================== GRADIENTS ====================
    
    public static final String GRADIENT_PRIMARY = "linear-gradient(135deg, " + PRIMARY_BLUE + " 0%, " + PRIMARY_BLUE_LIGHT + " 100%)";
    public static final String GRADIENT_SECONDARY = "linear-gradient(135deg, " + SECONDARY_ORANGE + " 0%, " + SECONDARY_ORANGE_LIGHT + " 100%)";
    public static final String GRADIENT_SUCCESS = "linear-gradient(135deg, " + SUCCESS_GREEN + " 0%, " + SUCCESS_GREEN_LIGHT + " 100%)";
    public static final String GRADIENT_BACKGROUND = "linear-gradient(135deg, #F5F7FA 0%, #C3CFE2 100%)";
    
    // ==================== SHADOW EFFECTS ====================
    
    public static final String SHADOW_LIGHT = "dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2)";
    public static final String SHADOW_MEDIUM = "dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4)";
    public static final String SHADOW_HEAVY = "dropshadow(gaussian, rgba(0,0,0,0.25), 16, 0, 0, 6)";
    public static final String SHADOW_COLORED = "dropshadow(gaussian, rgba(21,101,192,0.3), 12, 0, 0, 4)";
    
    // ==================== BORDER RADIUS ====================
    
    public static final String RADIUS_SMALL = "6";
    public static final String RADIUS_MEDIUM = "12";
    public static final String RADIUS_LARGE = "20";
    public static final String RADIUS_PILL = "50";
    
    // ==================== FONT STYLES ====================
    
    public static final String FONT_FAMILY = "Segoe UI";
    public static final String FONT_SIZE_SMALL = "12px";
    public static final String FONT_SIZE_NORMAL = "14px";
    public static final String FONT_SIZE_MEDIUM = "16px";
    public static final String FONT_SIZE_LARGE = "18px";
    public static final String FONT_SIZE_XLARGE = "24px";
    public static final String FONT_SIZE_XXLARGE = "32px";
    
    // ==================== COMPONENT STYLES ====================
    
    // Button Styles
    public static final String BUTTON_PRIMARY = String.format(
        "-fx-background-color: %s; " +
        "-fx-text-fill: white; " +
        "-fx-font-family: '%s'; " +
        "-fx-font-size: %s; " +
        "-fx-font-weight: bold; " +
        "-fx-background-radius: %s; " +
        "-fx-cursor: hand; " +
        "-fx-effect: %s; " +
        "-fx-padding: 12 24;",
        GRADIENT_PRIMARY, FONT_FAMILY, FONT_SIZE_NORMAL, RADIUS_MEDIUM, SHADOW_LIGHT
    );
    
    public static final String BUTTON_PRIMARY_HOVER = String.format(
        "-fx-background-color: %s; " +
        "-fx-text-fill: white; " +
        "-fx-font-family: '%s'; " +
        "-fx-font-size: %s; " +
        "-fx-font-weight: bold; " +
        "-fx-background-radius: %s; " +
        "-fx-cursor: hand; " +
        "-fx-effect: %s; " +
        "-fx-padding: 12 24; " +
        "-fx-scale-x: 1.05; " +
        "-fx-scale-y: 1.05;",
        PRIMARY_BLUE_DARK, FONT_FAMILY, FONT_SIZE_NORMAL, RADIUS_MEDIUM, SHADOW_MEDIUM
    );
    
    public static final String BUTTON_SUCCESS = String.format(
        "-fx-background-color: %s; " +
        "-fx-text-fill: white; " +
        "-fx-font-family: '%s'; " +
        "-fx-font-size: %s; " +
        "-fx-font-weight: bold; " +
        "-fx-background-radius: %s; " +
        "-fx-cursor: hand; " +
        "-fx-effect: %s; " +
        "-fx-padding: 12 24;",
        GRADIENT_SUCCESS, FONT_FAMILY, FONT_SIZE_NORMAL, RADIUS_MEDIUM, SHADOW_LIGHT
    );
    
    public static final String BUTTON_SUCCESS_HOVER = String.format(
        "-fx-background-color: %s; " +
        "-fx-text-fill: white; " +
        "-fx-font-family: '%s'; " +
        "-fx-font-size: %s; " +
        "-fx-font-weight: bold; " +
        "-fx-background-radius: %s; " +
        "-fx-cursor: hand; " +
        "-fx-effect: %s; " +
        "-fx-padding: 12 24; " +
        "-fx-scale-x: 1.05; " +
        "-fx-scale-y: 1.05;",
        SUCCESS_GREEN_DARK, FONT_FAMILY, FONT_SIZE_NORMAL, RADIUS_MEDIUM, SHADOW_MEDIUM
    );
    
    public static final String BUTTON_SECONDARY = String.format(
        "-fx-background-color: %s; " +
        "-fx-text-fill: white; " +
        "-fx-font-family: '%s'; " +
        "-fx-font-size: %s; " +
        "-fx-font-weight: bold; " +
        "-fx-background-radius: %s; " +
        "-fx-cursor: hand; " +
        "-fx-effect: %s; " +
        "-fx-padding: 12 24;",
        GRADIENT_SECONDARY, FONT_FAMILY, FONT_SIZE_NORMAL, RADIUS_MEDIUM, SHADOW_LIGHT
    );
    
    public static final String BUTTON_DANGER = String.format(
        "-fx-background-color: %s; " +
        "-fx-text-fill: white; " +
        "-fx-font-family: '%s'; " +
        "-fx-font-size: %s; " +
        "-fx-font-weight: bold; " +
        "-fx-background-radius: %s; " +
        "-fx-cursor: hand; " +
        "-fx-effect: %s; " +
        "-fx-padding: 8 16;",
        DANGER_RED, FONT_FAMILY, FONT_SIZE_SMALL, RADIUS_MEDIUM, SHADOW_LIGHT
    );
    
    // Input Field Styles
    public static final String INPUT_FIELD = String.format(
        "-fx-background-color: %s; " +
        "-fx-border-color: %s; " +
        "-fx-border-width: 1; " +
        "-fx-border-radius: %s; " +
        "-fx-background-radius: %s; " +
        "-fx-font-family: '%s'; " +
        "-fx-font-size: %s; " +
        "-fx-padding: 12 16; " +
        "-fx-effect: %s;",
        BACKGROUND_WHITE, BORDER_LIGHT, RADIUS_MEDIUM, RADIUS_MEDIUM, FONT_FAMILY, FONT_SIZE_NORMAL, SHADOW_LIGHT
    );
    
    public static final String INPUT_FIELD_FOCUS = String.format(
        "-fx-background-color: %s; " +
        "-fx-border-color: %s; " +
        "-fx-border-width: 2; " +
        "-fx-border-radius: %s; " +
        "-fx-background-radius: %s; " +
        "-fx-font-family: '%s'; " +
        "-fx-font-size: %s; " +
        "-fx-padding: 12 16; " +
        "-fx-effect: %s;",
        BACKGROUND_WHITE, PRIMARY_BLUE, RADIUS_MEDIUM, RADIUS_MEDIUM, FONT_FAMILY, FONT_SIZE_NORMAL, SHADOW_COLORED
    );
    
    // Card Styles
    public static final String CARD_STYLE = String.format(
        "-fx-background-color: %s; " +
        "-fx-background-radius: %s; " +
        "-fx-effect: %s; " +
        "-fx-border-color: %s; " +
        "-fx-border-width: 1; " +
        "-fx-border-radius: %s;",
        CARD_BACKGROUND, RADIUS_LARGE, SHADOW_MEDIUM, BORDER_LIGHT, RADIUS_LARGE
    );
    
    public static final String CARD_HOVER = String.format(
        "-fx-background-color: %s; " +
        "-fx-background-radius: %s; " +
        "-fx-effect: %s; " +
        "-fx-border-color: %s; " +
        "-fx-border-width: 1; " +
        "-fx-border-radius: %s; " +
        "-fx-scale-x: 1.02; " +
        "-fx-scale-y: 1.02;",
        CARD_BACKGROUND, RADIUS_LARGE, SHADOW_HEAVY, PRIMARY_BLUE_LIGHT, RADIUS_LARGE
    );
    
    // Header Styles
    public static final String HEADER_STYLE = String.format(
        "-fx-background-color: %s; " +
        "-fx-padding: 20; " +
        "-fx-effect: %s;",
        GRADIENT_PRIMARY, SHADOW_MEDIUM
    );
    
    public static final String TITLE_STYLE = String.format(
        "-fx-font-family: '%s'; " +
        "-fx-font-size: %s; " +
        "-fx-font-weight: bold; " +
        "-fx-text-fill: %s;",
        FONT_FAMILY, FONT_SIZE_XXLARGE, PRIMARY_BLUE_DARK
    );
    
    public static final String SUBTITLE_STYLE = String.format(
        "-fx-font-family: '%s'; " +
        "-fx-font-size: %s; " +
        "-fx-text-fill: %s;",
        FONT_FAMILY, FONT_SIZE_MEDIUM, TEXT_SECONDARY
    );
    
    // Background Styles
    public static final String MAIN_BACKGROUND = String.format(
        "-fx-background-color: %s;",
        GRADIENT_BACKGROUND
    );
    
    public static final String CONTENT_BACKGROUND = String.format(
        "-fx-background-color: %s; " +
        "-fx-padding: 20;",
        BACKGROUND_LIGHT
    );
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Creates a custom button style with the specified background color
     */
    public static String createButtonStyle(String backgroundColor, String textColor, String hoverColor) {
        return String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-font-family: '%s'; " +
            "-fx-font-size: %s; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: %s; " +
            "-fx-cursor: hand; " +
            "-fx-effect: %s; " +
            "-fx-padding: 12 24;",
            backgroundColor, textColor, FONT_FAMILY, FONT_SIZE_NORMAL, RADIUS_MEDIUM, SHADOW_LIGHT
        );
    }
    
    /**
     * Creates a custom card style with optional hover effects
     */
    public static String createCardStyle(String backgroundColor, boolean withHover) {
        String baseStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: %s; " +
            "-fx-effect: %s; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: %s;",
            backgroundColor, RADIUS_LARGE, SHADOW_LIGHT, BORDER_LIGHT, RADIUS_LARGE
        );
        
        if (withHover) {
            baseStyle += " -fx-cursor: hand;";
        }
        
        return baseStyle;
    }
    
    /**
     * Creates a text style with the specified size and color
     */
    public static String createTextStyle(String fontSize, String color, boolean bold) {
        return String.format(
            "-fx-font-family: '%s'; " +
            "-fx-font-size: %s; " +
            "%s" +
            "-fx-text-fill: %s;",
            FONT_FAMILY, fontSize, bold ? "-fx-font-weight: bold; " : "", color
        );
    }
}

