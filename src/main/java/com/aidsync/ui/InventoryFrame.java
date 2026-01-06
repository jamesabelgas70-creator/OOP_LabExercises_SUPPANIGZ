package com.aidsync.ui;

import com.aidsync.model.InventoryItem;
import com.aidsync.model.User;
import com.aidsync.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Inventory Management Screen
 * 
 * Features:
 * - Modern, clean UI matching application design
 * - Search functionality
 * - CRUD operations for inventory items
 * - Low stock highlighting
 * - Double-click to edit
 * - Responsive button interactions
 */
public class InventoryFrame extends JFrame {
    // ==================== UI Components ====================
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton;
    private JButton editButton;
    private JButton refreshButton;
    private JButton searchButton;
    
    // ==================== Services & Data ====================
    private InventoryService inventoryService;
    private User currentUser;
    
    // ==================== Color Constants ====================
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color PRIMARY_HOVER = new Color(0, 122, 255); // Brighter for better contrast
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(68, 68, 68);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color HOVER_COLOR = new Color(230, 230, 230); // Darker for better contrast
    private static final Color HOVER_BORDER_COLOR = new Color(180, 180, 180); // Darker border on hover
    private static final Color TABLE_HEADER_COLOR = new Color(240, 240, 240);
    private static final Color LOW_STOCK_COLOR = new Color(255, 243, 205); // Light yellow
    private static final Color LOW_STOCK_TEXT_COLOR = new Color(184, 134, 11); // Dark yellow
    
    // ==================== Font Constants ====================
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BUTTON_FONT_SECONDARY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    
    // ==================== Spacing Constants ====================
    private static final int FRAME_WIDTH = 1400;
    private static final int FRAME_HEIGHT = 700;
    private static final int PADDING_LARGE = 20;
    private static final int PADDING_MEDIUM = 15;
    private static final int PADDING_SMALL = 10;
    private static final int BUTTON_HEIGHT = 38;
    private static final int BUTTON_WIDTH = 110;
    private static final int SEARCH_FIELD_WIDTH = 300;
    
    // ==================== Constructor ====================
    public InventoryFrame(User user) {
        this.currentUser = user;
        this.inventoryService = new InventoryService();
        initializeUI();
        loadInventory();
        checkLowStockQuietly(); // Check but don't show popup immediately
    }
    
    // ==================== UI Initialization ====================
    
    /**
     * Initialize the main UI components
     */
    private void initializeUI() {
        configureFrame();
        JPanel mainPanel = createMainPanel();
        add(mainPanel);
    }
    
    /**
     * Configure frame properties
     */
    private void configureFrame() {
        setTitle("AidSync - Inventory Management");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBackground(BACKGROUND_COLOR);
    }
    
    /**
     * Create the main container panel
     */
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(PADDING_LARGE, PADDING_LARGE, PADDING_LARGE, PADDING_LARGE));
        
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    // ==================== Header Section ====================
    
    /**
     * Create the header panel with search and action buttons
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(PADDING_MEDIUM, 0));
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM, 0));
        
        headerPanel.add(createSearchPanel(), BorderLayout.WEST);
        headerPanel.add(createActionButtonPanel(), BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Create the search panel
     */
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING_SMALL, 0));
        searchPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(LABEL_FONT);
        searchLabel.setForeground(LABEL_COLOR);
        searchPanel.add(searchLabel);
        
        searchField = new JTextField(SEARCH_FIELD_WIDTH / 10);
        searchField.setFont(INPUT_FONT);
        searchField.setBorder(createInputBorder());
        searchField.setPreferredSize(new Dimension(SEARCH_FIELD_WIDTH, BUTTON_HEIGHT));
        searchField.addActionListener(e -> performSearch());
        searchPanel.add(searchField);
        
        searchButton = createSearchButton();
        searchPanel.add(searchButton);
        
        return searchPanel;
    }
    
    /**
     * Create the action buttons panel
     * Organized by function: View/Refresh, Edit, Add
     */
    private JPanel createActionButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING_SMALL, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        // Back button (navigation)
        JButton backButton = createSecondaryButton("Back", e -> dispose());
        buttonPanel.add(backButton);
        
        // View/Refresh buttons (secondary actions)
        refreshButton = createSecondaryButton("Refresh", e -> {
            loadInventory();
            checkLowStockQuietly();
        });
        buttonPanel.add(refreshButton);
        
        JButton transactionHistoryButton = createSecondaryButton("Transaction History", e -> openTransactionHistory());
        buttonPanel.add(transactionHistoryButton);
        
        // Edit button (secondary action)
        editButton = createSecondaryButton("Edit", e -> editSelectedItem());
        buttonPanel.add(editButton);
        
        // Primary action button (most important)
        addButton = createPrimaryButton("Add Item", e -> openAddEditDialog(null));
        buttonPanel.add(addButton);
        
        return buttonPanel;
    }
    
    // ==================== Button Creation Methods ====================
    
    /**
     * Create the search button (primary style for search action)
     */
    private JButton createSearchButton() {
        JButton button = new JButton("Search");
        button.setFont(BUTTON_FONT_SECONDARY);
        button.setPreferredSize(new Dimension(90, BUTTON_HEIGHT));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> performSearch());
        addPrimaryButtonHoverEffect(button);
        return button;
    }
    
    /**
     * Create a primary action button (most important actions like Add)
     */
    private JButton createPrimaryButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT_SECONDARY);
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(listener);
        addPrimaryButtonHoverEffect(button);
        return button;
    }
    
    /**
     * Create a secondary action button (secondary actions like Edit, Refresh, Transaction History)
     */
    private JButton createSecondaryButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT_SECONDARY);
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setBackground(BACKGROUND_COLOR);
        button.setForeground(LABEL_COLOR);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 15, 8, 15)
        ));
        button.addActionListener(listener);
        addSecondaryButtonHoverEffect(button);
        return button;
    }
    
    // ==================== Button Hover Effects ====================
    
    /**
     * Add hover effect to primary buttons (blue buttons)
     * Uses brighter blue for better contrast
     */
    private void addPrimaryButtonHoverEffect(JButton button) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_HOVER);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
    }
    
    /**
     * Add hover effect to secondary buttons (white buttons with border)
     * Uses darker background and darker border for better contrast
     */
    private void addSecondaryButtonHoverEffect(JButton button) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(HOVER_COLOR);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(HOVER_BORDER_COLOR, 1),
                    new EmptyBorder(8, 15, 8, 15)
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BACKGROUND_COLOR);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    new EmptyBorder(8, 15, 8, 15)
                ));
            }
        });
    }
    
    /**
     * Create a styled border for input fields
     */
    private javax.swing.border.Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        );
    }
    
    // ==================== Table Section ====================
    
    /**
     * Create the table panel
     */
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(BACKGROUND_COLOR);
        
        JScrollPane scrollPane = createTableScrollPane();
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    /**
     * Create the table with scroll pane
     */
    private JScrollPane createTableScrollPane() {
        String[] columnNames = {"ID", "Item Name", "Category", "Quantity", "Unit", "Low Stock Threshold", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0 || column == 3 || column == 5) return Integer.class; // ID, Quantity, Threshold
                return String.class;
            }
        };
        
        inventoryTable = new JTable(tableModel);
        inventoryTable.setFont(TABLE_FONT);
        inventoryTable.setRowHeight(30);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        inventoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        inventoryTable.setGridColor(BORDER_COLOR);
        inventoryTable.setShowGrid(true);
        inventoryTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Style table header
        inventoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        inventoryTable.getTableHeader().setBackground(TABLE_HEADER_COLOR);
        inventoryTable.getTableHeader().setForeground(LABEL_COLOR);
        inventoryTable.getTableHeader().setReorderingAllowed(false);
        
        // Custom renderer for Integer columns with improved selection and low stock highlighting
        inventoryTable.setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Set center-left alignment (left horizontally, center vertically)
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setVerticalAlignment(SwingConstants.CENTER);
                
                if (isSelected) {
                    label.setBackground(new Color(0, 102, 204, 40)); // Semi-transparent blue
                    label.setForeground(PRIMARY_COLOR);
                } else {
                    String status = (String) tableModel.getValueAt(row, 6);
                    if ("Low Stock".equals(status)) {
                        label.setBackground(LOW_STOCK_COLOR);
                        label.setForeground(LOW_STOCK_TEXT_COLOR);
                    } else {
                        label.setBackground(BACKGROUND_COLOR);
                        label.setForeground(LABEL_COLOR);
                    }
                }
                
                return label;
            }
        });
        
        // Custom renderer for all other cells with improved selection and low stock highlighting
        inventoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Set center-left alignment (left horizontally, center vertically)
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setVerticalAlignment(SwingConstants.CENTER);
                
                if (isSelected) {
                    label.setBackground(new Color(0, 102, 204, 40)); // Semi-transparent blue
                    label.setForeground(PRIMARY_COLOR);
                } else {
                    String status = (String) tableModel.getValueAt(row, 6);
                    if ("Low Stock".equals(status)) {
                        label.setBackground(LOW_STOCK_COLOR);
                        label.setForeground(LOW_STOCK_TEXT_COLOR);
                    } else {
                        label.setBackground(BACKGROUND_COLOR);
                        label.setForeground(LABEL_COLOR);
                    }
                }
                
                return label;
            }
        });
        
        // Enable double-click to edit and deselect on empty space
        inventoryTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = inventoryTable.rowAtPoint(evt.getPoint());
                if (row == -1) {
                    // Clicked on empty space - clear selection
                    inventoryTable.clearSelection();
                } else if (evt.getClickCount() == 2) {
                    editSelectedItem();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Adjust scroll sensitivity
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);
        verticalScrollBar.setBlockIncrement(64);
        
        return scrollPane;
    }
    
    // ==================== Data Operations ====================
    
    /**
     * Load all inventory items
     */
    private void loadInventory() {
        tableModel.setRowCount(0);
        List<InventoryItem> items = inventoryService.getAllInventoryItems();
        
        for (InventoryItem item : items) {
            String status = item.isLowStock() ? "Low Stock" : "OK";
            Object[] row = {
                item.getId(),
                item.getItemName(),
                item.getCategory() != null ? item.getCategory() : "-",
                item.getQuantity(),
                item.getUnit() != null ? item.getUnit() : "-",
                item.getLowStockThreshold(),
                status
            };
            tableModel.addRow(row);
        }
    }
    
    /**
     * Perform search operation
     */
    private void performSearch() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        
        List<InventoryItem> items = inventoryService.getAllInventoryItems();
        
        for (InventoryItem item : items) {
            // Search in item name, category, and unit
            boolean matches = searchTerm.isEmpty() ||
                (item.getItemName() != null && item.getItemName().toLowerCase().contains(searchTerm)) ||
                (item.getCategory() != null && item.getCategory().toLowerCase().contains(searchTerm)) ||
                (item.getUnit() != null && item.getUnit().toLowerCase().contains(searchTerm));
            
            if (matches) {
                String status = item.isLowStock() ? "Low Stock" : "OK";
                Object[] row = {
                    item.getId(),
                    item.getItemName(),
                    item.getCategory() != null ? item.getCategory() : "-",
                    item.getQuantity(),
                    item.getUnit() != null ? item.getUnit() : "-",
                    item.getLowStockThreshold(),
                    status
                };
                tableModel.addRow(row);
            }
        }
    }
    
    /**
     * Check for low stock items (quietly, without popup)
     */
    private void checkLowStockQuietly() {
        // Low stock items are already highlighted in the table
        // No need for intrusive popup
    }
    
    /**
     * Show low stock alert (can be called manually if needed)
     * This method is available for future use (e.g., menu item or button)
     */
    @SuppressWarnings("unused")
    private void showLowStockAlert() {
        List<InventoryItem> lowStockItems = inventoryService.getLowStockItems();
        if (!lowStockItems.isEmpty()) {
            StringBuilder message = new StringBuilder("Low stock alert for the following items:\n\n");
            for (InventoryItem item : lowStockItems) {
                message.append(String.format("• %s: %d %s (Threshold: %d)\n",
                    item.getItemName(), item.getQuantity(), 
                    item.getUnit() != null ? item.getUnit() : "units", 
                    item.getLowStockThreshold()));
            }
            JOptionPane.showMessageDialog(this,
                message.toString(),
                "Low Stock Alert",
                JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "All items are above the low stock threshold.",
                "Stock Status",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    // ==================== Dialog Operations ====================
    
    /**
     * Open add/edit dialog
     */
    private void openAddEditDialog(InventoryItem item) {
        SwingUtilities.invokeLater(() -> {
            InventoryDialog dialog = new InventoryDialog(
                this,
                inventoryService,
                item,
                () -> {
                    loadInventory();
                    checkLowStockQuietly();
                },
                currentUser
            );
            dialog.setVisible(true);
        });
    }
    
    /**
     * Edit selected item
     */
    private void editSelectedItem() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("No Selection", "Please select an item to edit, or double-click on an item.");
            return;
        }
        
        int itemId = (Integer) tableModel.getValueAt(selectedRow, 0);
        InventoryItem item = inventoryService.getInventoryItemById(itemId);
        if (item != null) {
            openAddEditDialog(item);
        } else {
            showErrorDialog("Error", "Item not found.");
        }
    }
    
    // ==================== Dialog Helpers ====================
    
    /**
     * Show a warning dialog
     */
    private void showWarningDialog(String title, String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            title,
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    /**
     * Show an error dialog
     */
    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            title,
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Open transaction history frame
     */
    private void openTransactionHistory() {
        SwingUtilities.invokeLater(() -> {
            new InventoryTransactionFrame(currentUser).setVisible(true);
        });
    }
}

