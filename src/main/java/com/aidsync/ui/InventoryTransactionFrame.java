package com.aidsync.ui;

import com.aidsync.model.InventoryItem;
import com.aidsync.model.InventoryTransaction;
import com.aidsync.model.User;
import com.aidsync.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.SpinnerDateModel;

/**
 * Frame for viewing inventory transaction history/audit trail
 * 
 * Features:
 * - View all inventory transactions
 * - Filter by item, date range, transaction type, user
 * - Shows complete audit trail with before/after quantities
 * - Modern, clean UI matching application design
 */
public class InventoryTransactionFrame extends JFrame {
    // ==================== UI Components ====================
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JComboBox<InventoryItem> itemFilterComboBox;
    private JComboBox<String> typeFilterComboBox;
    private JComboBox<String> userFilterComboBox;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JButton applyFilterButton;
    private JButton refreshButton;
    private JButton closeButton;
    
    // ==================== Services & Data ====================
    private InventoryService inventoryService;
    private com.aidsync.service.UserService userService;
    
    // ==================== Color Constants ====================
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color PRIMARY_DARK = new Color(0, 82, 164);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(68, 68, 68);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color HOVER_COLOR = new Color(245, 245, 245);
    private static final Color TABLE_HEADER_COLOR = new Color(240, 240, 240);
    private static final Color RESTOCK_COLOR = new Color(200, 255, 200);
    private static final Color DISTRIBUTION_COLOR = new Color(255, 200, 200);
    
    // ==================== Font Constants ====================
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    
    // ==================== Spacing Constants ====================
    private static final int FRAME_WIDTH = 1400;
    private static final int FRAME_HEIGHT = 800;
    private static final int PADDING_LARGE = 25;
    private static final int PADDING_MEDIUM = 15;
    private static final int PADDING_SMALL = 10;
    private static final int BUTTON_HEIGHT = 38;
    private static final int BUTTON_WIDTH = 120;
    
    // ==================== Constructor ====================
    public InventoryTransactionFrame(User user) {
        this.inventoryService = new InventoryService();
        this.userService = new com.aidsync.service.UserService();
        initializeUI();
        loadTransactions();
        initializeFilters();
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
        setTitle("AidSync - Inventory Transaction History");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
        
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createFilterPanel(), BorderLayout.CENTER);
        mainPanel.add(createTablePanel(), BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    /**
     * Create header panel
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM, 0));
        
        JLabel titleLabel = new JLabel("Inventory Transaction History");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(LABEL_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING_SMALL, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        JButton backButton = createActionButton("Back", BACKGROUND_COLOR, LABEL_COLOR, e -> dispose());
        buttonPanel.add(backButton);
        
        refreshButton = createActionButton("Refresh", BACKGROUND_COLOR, LABEL_COLOR, e -> loadTransactions());
        buttonPanel.add(refreshButton);
        
        closeButton = createActionButton("Close", BACKGROUND_COLOR, LABEL_COLOR, e -> dispose());
        buttonPanel.add(closeButton);
        
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Create filter panel
     */
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING_SMALL, PADDING_SMALL));
        filterPanel.setBackground(BACKGROUND_COLOR);
        filterPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            "Filters",
            0, 0,
            LABEL_FONT,
            LABEL_COLOR
        ));
        
        // Item filter
        JLabel itemLabel = new JLabel("Item:");
        itemLabel.setFont(LABEL_FONT);
        filterPanel.add(itemLabel);
        
        itemFilterComboBox = new JComboBox<>();
        itemFilterComboBox.setFont(LABEL_FONT);
        itemFilterComboBox.setPreferredSize(new Dimension(250, 30));
        itemFilterComboBox.addItem(null); // "All Items" option
        itemFilterComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("All Items");
                } else if (value instanceof InventoryItem) {
                    InventoryItem item = (InventoryItem) value;
                    setText(item.getItemName());
                }
                return this;
            }
        });
        filterPanel.add(itemFilterComboBox);
        
        // Transaction type filter
        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setFont(LABEL_FONT);
        filterPanel.add(typeLabel);
        
        typeFilterComboBox = new JComboBox<>();
        typeFilterComboBox.setFont(LABEL_FONT);
        typeFilterComboBox.setPreferredSize(new Dimension(150, 30));
        typeFilterComboBox.addItem("All Types");
        typeFilterComboBox.addItem("Restock");
        typeFilterComboBox.addItem("Set Quantity");
        typeFilterComboBox.addItem("Distribution");
        typeFilterComboBox.addItem("Void Distribution");
        filterPanel.add(typeFilterComboBox);
        
        // User filter
        JLabel userLabel = new JLabel("User:");
        userLabel.setFont(LABEL_FONT);
        filterPanel.add(userLabel);
        
        userFilterComboBox = new JComboBox<>();
        userFilterComboBox.setFont(LABEL_FONT);
        userFilterComboBox.setPreferredSize(new Dimension(150, 30));
        userFilterComboBox.addItem("All Users");
        filterPanel.add(userFilterComboBox);
        
        // Date range
        JLabel startDateLabel = new JLabel("Start Date:");
        startDateLabel.setFont(LABEL_FONT);
        filterPanel.add(startDateLabel);
        
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(startEditor);
        startDateSpinner.setPreferredSize(new Dimension(150, 30));
        filterPanel.add(startDateSpinner);
        
        JLabel endDateLabel = new JLabel("End Date:");
        endDateLabel.setFont(LABEL_FONT);
        filterPanel.add(endDateLabel);
        
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        endDateSpinner.setEditor(endEditor);
        endDateSpinner.setPreferredSize(new Dimension(150, 30));
        filterPanel.add(endDateSpinner);
        
        // Apply filter button
        applyFilterButton = createActionButton("Apply Filters", PRIMARY_COLOR, Color.WHITE, e -> loadTransactions());
        filterPanel.add(applyFilterButton);
        
        return filterPanel;
    }
    
    /**
     * Create table panel
     */
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(BACKGROUND_COLOR);
        tablePanel.setBorder(new EmptyBorder(PADDING_MEDIUM, 0, 0, 0));
        
        String[] columnNames = {"Date/Time", "Item", "Transaction Type", "User", "Quantity Change", 
                               "Quantity Before", "Quantity After", "Notes", "Reference"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        transactionTable = new JTable(tableModel);
        transactionTable.setFont(TABLE_FONT);
        transactionTable.setRowHeight(30);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        transactionTable.setGridColor(BORDER_COLOR);
        transactionTable.setShowGrid(true);
        
        // Custom renderer for transaction type coloring with improved selection
        transactionTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Enhanced selection colors take priority
                if (isSelected) {
                    label.setBackground(new Color(0, 102, 204, 40)); // Semi-transparent blue
                    label.setForeground(PRIMARY_COLOR);
                } else {
                    // Color coding based on transaction type
                    if (column == 2) { // Transaction Type column
                        String type = (String) value;
                        if ("Restock".equals(type)) {
                            label.setBackground(RESTOCK_COLOR);
                            label.setForeground(new Color(0, 100, 0));
                        } else if ("Distribution".equals(type) || "Void Distribution".equals(type)) {
                            label.setBackground(DISTRIBUTION_COLOR);
                            label.setForeground(new Color(150, 0, 0));
                        } else {
                            label.setBackground(BACKGROUND_COLOR);
                            label.setForeground(LABEL_COLOR);
                        }
                    } else {
                        label.setBackground(BACKGROUND_COLOR);
                        label.setForeground(LABEL_COLOR);
                    }
                }
                
                // Center-left alignment
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setVerticalAlignment(SwingConstants.CENTER);
                
                return label;
            }
        });
        
        // Custom renderer for quantity change (show + or -)
        transactionTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (value instanceof Integer) {
                    int change = (Integer) value;
                    if (change > 0) {
                        label.setText("+" + change);
                        label.setForeground(new Color(0, 150, 0));
                    } else if (change < 0) {
                        label.setText(String.valueOf(change));
                        label.setForeground(new Color(200, 0, 0));
                    } else {
                        label.setText("0");
                        label.setForeground(LABEL_COLOR);
                    }
                }
                
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setVerticalAlignment(SwingConstants.CENTER);
                
                return label;
            }
        });
        
        // Style table header
        transactionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        transactionTable.getTableHeader().setBackground(TABLE_HEADER_COLOR);
        transactionTable.getTableHeader().setForeground(LABEL_COLOR);
        transactionTable.getTableHeader().setReorderingAllowed(false);
        
        // Add deselect on empty space functionality
        transactionTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = transactionTable.rowAtPoint(evt.getPoint());
                if (row == -1) {
                    // Clicked on empty space - clear selection
                    transactionTable.clearSelection();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Adjust scroll sensitivity
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);
        verticalScrollBar.setBlockIncrement(64);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    /**
     * Create an action button
     */
    private JButton createActionButton(String text, Color bgColor, Color fgColor, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(LABEL_FONT);
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setBorderPainted(bgColor == BACKGROUND_COLOR);
        if (bgColor == BACKGROUND_COLOR) {
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(8, 15, 8, 15)
            ));
        } else {
            button.setBorderPainted(false);
        }
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(listener);
        if (bgColor != BACKGROUND_COLOR) {
            addButtonHoverEffect(button, bgColor, PRIMARY_DARK);
        } else {
            addButtonHoverEffect(button, BACKGROUND_COLOR, HOVER_COLOR);
        }
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
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(normalColor);
            }
        });
    }
    
    // ==================== Data Operations ====================
    
    /**
     * Initialize filter dropdowns
     */
    private void initializeFilters() {
        // Load inventory items
        List<InventoryItem> items = inventoryService.getAllInventoryItems();
        DefaultComboBoxModel<InventoryItem> itemModel = (DefaultComboBoxModel<InventoryItem>) itemFilterComboBox.getModel();
        for (InventoryItem item : items) {
            itemModel.addElement(item);
        }
        
        // Load users
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            String displayName = user.getFullName() != null && !user.getFullName().trim().isEmpty() 
                ? user.getFullName() 
                : user.getUsername();
            userFilterComboBox.addItem(displayName);
        }
    }
    
    /**
     * Load transactions with filters
     */
    private void loadTransactions() {
        tableModel.setRowCount(0);
        
        List<InventoryTransaction> transactions = inventoryService.getAllTransactions();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        // Apply filters
        InventoryItem selectedItem = (InventoryItem) itemFilterComboBox.getSelectedItem();
        String selectedType = (String) typeFilterComboBox.getSelectedItem();
        String selectedUser = (String) userFilterComboBox.getSelectedItem();
        java.util.Date startDate = startDateSpinner != null ? ((SpinnerDateModel) startDateSpinner.getModel()).getDate() : null;
        java.util.Date endDate = endDateSpinner != null ? ((SpinnerDateModel) endDateSpinner.getModel()).getDate() : null;
        
        for (InventoryTransaction transaction : transactions) {
            // Item filter
            if (selectedItem != null && transaction.getInventoryId() != selectedItem.getId()) {
                continue;
            }
            
            // Type filter
            if (selectedType != null && !selectedType.equals("All Types") && 
                !transaction.getTransactionType().equals(selectedType)) {
                continue;
            }
            
            // User filter
            if (selectedUser != null && !selectedUser.equals("All Users")) {
                String transactionUser = transaction.getUserName() != null ? transaction.getUserName() : "System";
                if (!transactionUser.equals(selectedUser)) {
                    continue;
                }
            }
            
            // Date filter
            if (transaction.getCreatedAt() != null) {
                if (startDate != null) {
                    java.time.LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    if (transaction.getCreatedAt().toLocalDate().isBefore(startLocalDate)) {
                        continue;
                    }
                }
                if (endDate != null) {
                    java.time.LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    if (transaction.getCreatedAt().toLocalDate().isAfter(endLocalDate)) {
                        continue;
                    }
                }
            }
            
            // Build reference string
            String reference = "-";
            if (transaction.getReferenceId() != null && transaction.getReferenceType() != null) {
                reference = transaction.getReferenceType() + " #" + transaction.getReferenceId();
            }
            
            String notes = transaction.getNotes() != null && !transaction.getNotes().trim().isEmpty() 
                ? transaction.getNotes() : "-";
            
            String userName = transaction.getUserName() != null && !transaction.getUserName().trim().isEmpty() 
                ? transaction.getUserName() 
                : (transaction.getUserId() != null ? "User ID: " + transaction.getUserId() : "System");
            String itemName = transaction.getInventoryItemName() != null ? transaction.getInventoryItemName() : "Item ID: " + transaction.getInventoryId();
            
            Object[] row = {
                transaction.getCreatedAt() != null ? transaction.getCreatedAt().format(formatter) : "-",
                itemName,
                transaction.getTransactionType(),
                userName,
                transaction.getQuantityChange(),
                transaction.getQuantityBefore(),
                transaction.getQuantityAfter(),
                notes,
                reference
            };
            tableModel.addRow(row);
        }
    }
}

