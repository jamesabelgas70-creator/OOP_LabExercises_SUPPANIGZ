package com.aidsync;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.aidsync.ui.LoginFrame;
import com.aidsync.util.DatabaseManager;

/**
 * Main entry point for AidSync application
 */
public class Main {
    public static void main(String[] args) {
        // Initialize database
        DatabaseManager.initialize();
        
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException e) {
            System.err.println("Failed to set system look and feel: " + e.getMessage());
        }
        
        // Start application
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}

