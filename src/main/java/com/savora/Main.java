package com.savora;

import com.formdev.flatlaf.FlatLightLaf;
import com.savora.view.MainFrame;
import com.savora.util.DatabaseConnection;

import javax.swing.*;
import java.awt.*;

/**
 * Main application class for Savora Finance Tracker
 */
public class Main {
    public static void main(String[] args) {
        // Set system look and feel for better appearance
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Failed to set FlatLaf look and feel: " + e.getMessage());
        }

        // Set custom UI properties for modern appearance
        setupUIManager();

        // Test database connection
        if (!DatabaseConnection.getInstance().testConnection()) {
            JOptionPane.showMessageDialog(null,
                "Failed to connect to database. Please ensure MySQL is running and the database is set up correctly.",
                "Database Connection Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Create and show the main application window
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Failed to start application: " + e.getMessage(),
                    "Application Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Setup UI Manager with custom properties for modern appearance
     */
    private static void setupUIManager() {
        // Set modern color scheme
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ProgressBar.arc", 8);
        
        // Set modern button style
        UIManager.put("Button.defaultButtonFollowsFocus", true);
        
        // Set modern table style
        UIManager.put("Table.selectionBackground", new Color(59, 130, 246));
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("Table.gridColor", new Color(229, 231, 235));
        
        // Set modern tabbed pane style
        UIManager.put("TabbedPane.tabSelectionHeight", 3);
        UIManager.put("TabbedPane.selectedTabBackground", Color.WHITE);
        
        // Set modern panel style
        UIManager.put("Panel.background", new Color(248, 250, 252));
        
        // Set modern text field style
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        // Set modern combo box style
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.border", BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        
        // Set modern scroll pane style
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("ScrollBar.thumb", new Color(156, 163, 175));
        UIManager.put("ScrollBar.track", new Color(243, 244, 246));
        
        // Set modern menu style
        UIManager.put("MenuBar.background", new Color(255, 255, 255));
        UIManager.put("Menu.background", new Color(255, 255, 255));
        UIManager.put("MenuItem.background", new Color(255, 255, 255));
        
        // Set modern tooltip style
        UIManager.put("ToolTip.background", new Color(31, 41, 55));
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("ToolTip.border", BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }
}
