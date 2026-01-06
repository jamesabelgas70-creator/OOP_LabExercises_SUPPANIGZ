package com.aidsync.ui;

import com.aidsync.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Main dashboard frame
 * 
 * Features:
 * - Modern, clean UI matching application design
 * - Quick action buttons for common tasks
 * - Menu bar with organized navigation
 * - Role-based menu items (Admin vs Staff)
 * - Responsive button interactions
 */
public class DashboardFrame extends JFrame {
    // ==================== UI Components ====================
    private JButton beneficiariesButton;
    private JButton distributionButton;
    private JButton inventoryButton;
    private JButton reportsButton;
    
    // ==================== Data ====================
    private User currentUser;
    
    // ==================== Color Constants ====================
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color PRIMARY_LIGHT = new Color(240, 248, 255);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(68, 68, 68);
    private static final Color SUBTITLE_COLOR = new Color(100, 100, 100);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color CARD_BACKGROUND = new Color(250, 250, 250);
    
    // ==================== Font Constants ====================
    private static final Font WELCOME_FONT = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font MENU_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    
    // ==================== Spacing Constants ====================
    private static final int FRAME_WIDTH = 1000;
    private static final int FRAME_HEIGHT = 700;
    private static final int PADDING_LARGE = 30;
    private static final int PADDING_MEDIUM = 20;
    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 120;
    private static final int BUTTON_SPACING = 15;
    
    // ==================== Constructor ====================
    public DashboardFrame(User user) {
        this.currentUser = user;
        initializeUI();
    }
    
    // ==================== UI Initialization ====================
    
    /**
     * Initialize the main UI components
     */
    private void initializeUI() {
        configureFrame();
        setJMenuBar(createMenuBar());
        JPanel mainPanel = createMainPanel();
        add(mainPanel);
    }
    
    /**
     * Configure frame properties
     */
    private void configureFrame() {
        setTitle("AidSync - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setBackground(BACKGROUND_COLOR);
    }
    
    /**
     * Create the main container panel
     */
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(PADDING_LARGE, PADDING_LARGE, PADDING_LARGE, PADDING_LARGE));
        
        mainPanel.add(createWelcomePanel(), BorderLayout.NORTH);
        mainPanel.add(createQuickActionsPanel(), BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    // ==================== Menu Bar ====================
    
    /**
     * Create the menu bar
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setFont(MENU_FONT);
        
        menuBar.add(createFileMenu());
        menuBar.add(createMainMenu());
        
        if (currentUser.isAdmin()) {
            menuBar.add(createAdminMenu());
        }
        
        return menuBar;
    }
    
    /**
     * Create the File menu
     */
    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(MENU_FONT);
        
        JMenuItem logoutMenuItem = new JMenuItem("Logout");
        logoutMenuItem.addActionListener(e -> handleLogout());
        
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(logoutMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        
        return fileMenu;
    }
    
    /**
     * Create the Main menu
     */
    private JMenu createMainMenu() {
        JMenu mainMenu = new JMenu("Main");
        mainMenu.setFont(MENU_FONT);
        
        JMenuItem beneficiariesMenuItem = new JMenuItem("Beneficiaries");
        beneficiariesMenuItem.addActionListener(e -> openBeneficiariesScreen());
        
        JMenuItem distributionMenuItem = new JMenuItem("Distributions");
        distributionMenuItem.addActionListener(e -> openDistributionScreen());
        
        JMenuItem inventoryMenuItem = new JMenuItem("Inventory");
        inventoryMenuItem.addActionListener(e -> openInventoryScreen());
        
        mainMenu.add(beneficiariesMenuItem);
        mainMenu.add(distributionMenuItem);
        mainMenu.add(inventoryMenuItem);
        
        return mainMenu;
    }
    
    /**
     * Create the Admin menu (Admin only)
     */
    private JMenu createAdminMenu() {
        JMenu adminMenu = new JMenu("Admin");
        adminMenu.setFont(MENU_FONT);
        
        JMenuItem userManagementMenuItem = new JMenuItem("User Management");
        userManagementMenuItem.addActionListener(e -> openUserManagementScreen());
        
        JMenuItem calamityManagementMenuItem = new JMenuItem("Calamity Management");
        calamityManagementMenuItem.addActionListener(e -> openCalamityManagementScreen());
        
        JMenuItem createAccountMenuItem = new JMenuItem("Create Account");
        createAccountMenuItem.addActionListener(e -> openCreateAccountDialog());
        
        adminMenu.add(userManagementMenuItem);
        adminMenu.add(calamityManagementMenuItem);
        adminMenu.addSeparator();
        adminMenu.add(createAccountMenuItem);
        
        return adminMenu;
    }
    
    // ==================== Welcome Section ====================
    
    /**
     * Create the welcome panel
     */
    private JPanel createWelcomePanel() {
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomePanel.setBackground(BACKGROUND_COLOR);
        welcomePanel.setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM, 0));
        
        // Get display name: first word of full name, or username if no full name
        String displayName = getDisplayName();
        JLabel welcomeLabel = new JLabel("Welcome, " + displayName);
        welcomeLabel.setFont(WELCOME_FONT);
        welcomeLabel.setForeground(PRIMARY_COLOR);
        
        JLabel roleLabel = new JLabel("(" + currentUser.getRole() + ")");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        roleLabel.setForeground(SUBTITLE_COLOR);
        roleLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
        
        welcomePanel.add(welcomeLabel);
        welcomePanel.add(roleLabel);
        
        return welcomePanel;
    }
    
    /**
     * Get display name: first word of full name, or username if no full name
     */
    private String getDisplayName() {
        String fullName = currentUser.getFullName();
        if (fullName != null && !fullName.trim().isEmpty()) {
            // Extract first word from full name
            String trimmed = fullName.trim();
            int spaceIndex = trimmed.indexOf(' ');
            if (spaceIndex > 0) {
                return trimmed.substring(0, spaceIndex);
            } else {
                return trimmed; // Single word name
            }
        } else {
            // Fall back to username if no full name
            return currentUser.getUsername();
        }
    }
    
    // ==================== Quick Actions Section ====================
    
    /**
     * Create the quick actions panel
     */
    private JPanel createQuickActionsPanel() {
        JPanel quickActionsPanel = new JPanel(new GridLayout(2, 2, BUTTON_SPACING, BUTTON_SPACING));
        quickActionsPanel.setBackground(BACKGROUND_COLOR);
        quickActionsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                "Quick Actions",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 16)
            ),
            new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM)
        ));
        
        beneficiariesButton = createQuickActionButton(
            "Manage Beneficiaries",
            "Register and manage relief recipients",
            e -> openBeneficiariesScreen()
        );
        
        distributionButton = createQuickActionButton(
            "Record Distribution",
            "Record relief package distribution",
            e -> openDistributionScreen()
        );
        
        inventoryButton = createQuickActionButton(
            "Inventory Management",
            "Manage relief items and stock",
            e -> openInventoryScreen()
        );
        
        reportsButton = createQuickActionButton(
            "Reports",
            "View system reports and statistics",
            e -> openReportsScreen()
        );
        
        quickActionsPanel.add(beneficiariesButton);
        quickActionsPanel.add(distributionButton);
        quickActionsPanel.add(inventoryButton);
        quickActionsPanel.add(reportsButton);
        
        return quickActionsPanel;
    }
    
    /**
     * Create a styled quick action button
     */
    private JButton createQuickActionButton(String title, String tooltip, ActionListener listener) {
        JButton button = new JButton("<html><center>" + title + "</center></html>");
        button.setToolTipText(tooltip);
        button.setFont(BUTTON_FONT);
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setBackground(CARD_BACKGROUND);
        button.setForeground(LABEL_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(listener);
        
        // Add hover effect
        addButtonHoverEffect(button, CARD_BACKGROUND, PRIMARY_LIGHT);
        
        return button;
    }
    
    /**
     * Add hover effect to a button
     */
    private void addButtonHoverEffect(JButton button, Color normalColor, Color hoverColor) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                    new EmptyBorder(15, 15, 15, 15)
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(normalColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    new EmptyBorder(15, 15, 15, 15)
                ));
            }
        });
    }
    
    // ==================== Event Handlers ====================
    
    /**
     * Handle logout action
     */
    private void handleLogout() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
            });
        }
    }
    
    /**
     * Open create account dialog
     */
    private void openCreateAccountDialog() {
        SwingUtilities.invokeLater(() -> {
            CreateAccountDialog dialog = new CreateAccountDialog(
                this,
                new com.aidsync.service.UserService()
            );
            dialog.setVisible(true);
        });
    }
    
    // ==================== Screen Navigation ====================
    
    /**
     * Open beneficiaries management screen
     */
    private void openBeneficiariesScreen() {
        SwingUtilities.invokeLater(() -> {
            new BeneficiaryManagementFrame(currentUser).setVisible(true);
        });
    }
    
    /**
     * Open distribution screen
     */
    private void openDistributionScreen() {
        SwingUtilities.invokeLater(() -> {
            new DistributionFrame(currentUser).setVisible(true);
        });
    }
    
    /**
     * Open inventory screen
     */
    private void openInventoryScreen() {
        SwingUtilities.invokeLater(() -> {
            new InventoryFrame(currentUser).setVisible(true);
        });
    }
    
    /**
     * Open reports screen
     */
    private void openReportsScreen() {
        SwingUtilities.invokeLater(() -> {
            new ReportsFrame(currentUser).setVisible(true);
        });
    }
    
    /**
     * Open user management screen
     */
    private void openUserManagementScreen() {
        SwingUtilities.invokeLater(() -> {
            new UserManagementFrame(currentUser).setVisible(true);
        });
    }
    
    /**
     * Open calamity management screen
     */
    private void openCalamityManagementScreen() {
        SwingUtilities.invokeLater(() -> {
            new CalamityManagementFrame(currentUser).setVisible(true);
        });
    }
}
