package com.aidsync.ui;

import com.aidsync.model.Beneficiary;
import com.aidsync.model.Calamity;
import com.aidsync.model.CalamityItem;
import com.aidsync.model.Distribution;
import com.aidsync.model.DistributionItem;
import com.aidsync.model.InventoryItem;
import com.aidsync.model.User;
import com.aidsync.service.CalamityService;
import com.aidsync.service.DistributionService;
import com.aidsync.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog for selecting items and quantities for a new distribution
 * 
 * Features:
 * - Shows beneficiary information for context
 * - Clear item selection with stock availability
 * - Selected items table with editable quantities
 * - Real-time stock validation
 * - Distribution summary
 * - Notes field
 * - Keyboard navigation support
 * - Clean, user-friendly interface
 */
public class DistributionItemDialog extends JDialog {
    // ==================== UI Components ====================
    private JTable selectedItemsTable;
    private DefaultTableModel selectedItemsTableModel;
    private JTextField quantityField;
    private JTextArea notesArea;
    private JButton addItemButton;
    private JButton removeItemButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JComboBox<InventoryItem> itemComboBox;
    private JComboBox<Calamity> calamityComboBox;
    private JButton loadCalamityItemsButton;
    private JLabel totalItemsLabel;
    private JLabel beneficiaryInfoLabel;
    
    // ==================== Services & Data ====================
    private Beneficiary beneficiary;
    private DistributionService distributionService;
    private InventoryService inventoryService;
    private CalamityService calamityService;
    private User currentUser;
    private Runnable onSaveCallback;
    private List<InventoryItem> availableItems;
    private Map<Integer, Integer> selectedItems; // inventoryId -> quantity
    
    // ==================== Color Constants ====================
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color PRIMARY_DARK = new Color(0, 82, 164);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(68, 68, 68);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color HOVER_COLOR = new Color(245, 245, 245);
    private static final Color TABLE_HEADER_COLOR = new Color(240, 240, 240);
    private static final Color INFO_BG_COLOR = new Color(248, 249, 250);
    private static final Color LOW_STOCK_COLOR = new Color(255, 193, 7);
    
    // ==================== Font Constants ====================
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BUTTON_FONT_PRIMARY = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font BUTTON_FONT_SECONDARY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font INFO_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    
    // ==================== Spacing Constants ====================
    private static final int DIALOG_WIDTH = 1200;
    private static final int DIALOG_HEIGHT = 800;
    private static final int PADDING_LARGE = 25;
    private static final int PADDING_MEDIUM = 15;
    private static final int PADDING_SMALL = 10;
    private static final int BUTTON_HEIGHT = 40;
    private static final int BUTTON_WIDTH = 130;
    private static final int INPUT_HEIGHT = 38;
    
    // ==================== Constructor ====================
    public DistributionItemDialog(
            JDialog parent,
            Beneficiary beneficiary,
            DistributionService distributionService,
            InventoryService inventoryService,
            User currentUser,
            Runnable onSave) {
        super(parent, "Add New Distribution - " + beneficiary.getFullName(), true);
        this.beneficiary = beneficiary;
        this.distributionService = distributionService;
        this.inventoryService = inventoryService;
        this.calamityService = new CalamityService();
        this.currentUser = currentUser;
        this.onSaveCallback = onSave;
        this.selectedItems = new HashMap<>();
        initializeUI();
        loadInventoryItems();
        loadCalamities();
        setupKeyboardNavigation();
    }
    
    // ==================== UI Initialization ====================
    
    /**
     * Initialize the main UI components
     */
    private void initializeUI() {
        JPanel mainPanel = createMainPanel();
        add(mainPanel);
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(getParent());
    }
    
    /**
     * Create the main container panel
     */
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(PADDING_LARGE, PADDING_LARGE, PADDING_LARGE, PADDING_LARGE));
        
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createContentPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    // ==================== Header Section ====================
    
    /**
     * Create the header panel with title and beneficiary info
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(PADDING_MEDIUM, PADDING_SMALL));
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM, 0));
        
        JLabel titleLabel = new JLabel("Select Items for Distribution");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(LABEL_COLOR);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Beneficiary info panel
        JPanel infoPanel = new JPanel(new BorderLayout(PADDING_SMALL, PADDING_SMALL));
        infoPanel.setBackground(INFO_BG_COLOR);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(PADDING_SMALL, PADDING_MEDIUM, PADDING_SMALL, PADDING_MEDIUM)
        ));
        
        beneficiaryInfoLabel = new JLabel(String.format(
            "<html><b>Beneficiary:</b> %s (ID: %s) | <b>Barangay:</b> %s | <b>Purok:</b> %s | <b>Family Size:</b> %d</html>",
            beneficiary.getFullName(),
            beneficiary.getBeneficiaryId(),
            beneficiary.getBarangay(),
            beneficiary.getPurok(),
            beneficiary.getFamilySize()
        ));
        beneficiaryInfoLabel.setFont(INFO_FONT);
        beneficiaryInfoLabel.setForeground(LABEL_COLOR);
        infoPanel.add(beneficiaryInfoLabel, BorderLayout.NORTH);
        
        // Calamity selection panel
        JPanel calamityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING_SMALL, 0));
        calamityPanel.setBackground(INFO_BG_COLOR);
        
        JLabel calamityLabel = new JLabel("Calamity/Event (Optional):");
        calamityLabel.setFont(LABEL_FONT);
        calamityLabel.setForeground(LABEL_COLOR);
        calamityPanel.add(calamityLabel);
        
        calamityComboBox = new JComboBox<>();
        calamityComboBox.setFont(INPUT_FONT);
        calamityComboBox.setPreferredSize(new Dimension(250, INPUT_HEIGHT - 5));
        calamityComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Calamity) {
                    Calamity c = (Calamity) value;
                    setText(c.getName() + (c.getStatus().equals("Active") ? "" : " (Inactive)"));
                } else if (value == null) {
                    setText("-- Select Calamity --");
                }
                return this;
            }
        });
        calamityPanel.add(calamityComboBox);
        
        loadCalamityItemsButton = new JButton("Load Items");
        loadCalamityItemsButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loadCalamityItemsButton.setPreferredSize(new Dimension(100, INPUT_HEIGHT - 5));
        loadCalamityItemsButton.setBackground(PRIMARY_COLOR);
        loadCalamityItemsButton.setForeground(Color.WHITE);
        loadCalamityItemsButton.setBorderPainted(false);
        loadCalamityItemsButton.setFocusPainted(false);
        loadCalamityItemsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loadCalamityItemsButton.addActionListener(e -> loadCalamityItems());
        loadCalamityItemsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loadCalamityItemsButton.setBackground(PRIMARY_DARK);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loadCalamityItemsButton.setBackground(PRIMARY_COLOR);
            }
        });
        calamityPanel.add(loadCalamityItemsButton);
        
        infoPanel.add(calamityPanel, BorderLayout.SOUTH);
        
        headerPanel.add(infoPanel, BorderLayout.SOUTH);
        
        return headerPanel;
    }
    
    // ==================== Content Section ====================
    
    /**
     * Create the content panel with item selection and selected items
     */
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout(PADDING_MEDIUM, PADDING_MEDIUM));
        contentPanel.setBackground(BACKGROUND_COLOR);
        
        // Top: Item selection panel
        JPanel selectionPanel = createSelectionPanel();
        contentPanel.add(selectionPanel, BorderLayout.NORTH);
        
        // Center: Selected items table
        JPanel selectedPanel = createSelectedItemsPanel();
        contentPanel.add(selectedPanel, BorderLayout.CENTER);
        
        // Bottom: Notes and summary
        JPanel bottomPanel = createBottomPanel();
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        return contentPanel;
    }
    
    /**
     * Create the item selection panel
     */
    private JPanel createSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(PADDING_SMALL, PADDING_SMALL));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            "Add Item to Distribution",
            0, 0,
            LABEL_FONT,
            LABEL_COLOR
        ));
        
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING_SMALL, PADDING_SMALL));
        inputPanel.setBackground(BACKGROUND_COLOR);
        
        // Item label and combo box
        JLabel itemLabel = new JLabel("Item:");
        itemLabel.setFont(LABEL_FONT);
        itemLabel.setForeground(LABEL_COLOR);
        inputPanel.add(itemLabel);
        
        itemComboBox = new JComboBox<>();
        itemComboBox.setFont(INPUT_FONT);
        itemComboBox.setPreferredSize(new Dimension(300, INPUT_HEIGHT));
        itemComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof InventoryItem) {
                    InventoryItem item = (InventoryItem) value;
                    String text = item.getItemName() + " - " + item.getQuantity() + " " + item.getUnit() + " available";
                    setText(text);
                    // Highlight low stock items
                    if (item.isLowStock()) {
                        setForeground(LOW_STOCK_COLOR);
                    }
                }
                return this;
            }
        });
        inputPanel.add(itemComboBox);
        
        // Quantity label and field
        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setFont(LABEL_FONT);
        quantityLabel.setForeground(LABEL_COLOR);
        inputPanel.add(quantityLabel);
        
        quantityField = new JTextField();
        quantityField.setFont(INPUT_FONT);
        quantityField.setBorder(createInputBorder());
        quantityField.setPreferredSize(new Dimension(100, INPUT_HEIGHT));
        quantityField.addActionListener(e -> addSelectedItem()); // Enter key support
        inputPanel.add(quantityField);
        
        // Add button
        addItemButton = createPrimaryButton("Add Item", e -> addSelectedItem());
        inputPanel.add(addItemButton);
        
        panel.add(inputPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create the selected items panel
     */
    private JPanel createSelectedItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            "Selected Items",
            0, 0,
            LABEL_FONT,
            LABEL_COLOR
        ));
        
        String[] columnNames = {"Item", "Quantity", "Unit", "Remaining Stock"};
        selectedItemsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 1 || column == 3) return Integer.class; // Quantity, Remaining Stock
                return String.class;
            }
        };
        
        selectedItemsTable = new JTable(selectedItemsTableModel);
        selectedItemsTable.setFont(TABLE_FONT);
        selectedItemsTable.setRowHeight(30);
        selectedItemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectedItemsTable.setGridColor(BORDER_COLOR);
        selectedItemsTable.setShowGrid(true);
        selectedItemsTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Style table header
        selectedItemsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        selectedItemsTable.getTableHeader().setBackground(TABLE_HEADER_COLOR);
        selectedItemsTable.getTableHeader().setForeground(LABEL_COLOR);
        selectedItemsTable.getTableHeader().setReorderingAllowed(false);
        
        // Enable double-click to remove
        selectedItemsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    removeSelectedItem();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(selectedItemsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Adjust scroll sensitivity
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);
        verticalScrollBar.setBlockIncrement(64);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Remove button and summary
        JPanel buttonPanel = new JPanel(new BorderLayout(PADDING_SMALL, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(new EmptyBorder(PADDING_SMALL, 0, 0, 0));
        
        removeItemButton = createActionButton("Remove Selected", BACKGROUND_COLOR, LABEL_COLOR, e -> removeSelectedItem());
        buttonPanel.add(removeItemButton, BorderLayout.WEST);
        
        // Summary label
        totalItemsLabel = new JLabel("Total Items: 0");
        totalItemsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalItemsLabel.setForeground(PRIMARY_COLOR);
        buttonPanel.add(totalItemsLabel, BorderLayout.EAST);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create the bottom panel with notes
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(PADDING_SMALL, PADDING_SMALL));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(PADDING_MEDIUM, 0, 0, 0));
        
        JLabel notesLabel = new JLabel("Notes (optional):");
        notesLabel.setFont(LABEL_FONT);
        notesLabel.setForeground(LABEL_COLOR);
        panel.add(notesLabel, BorderLayout.NORTH);
        
        notesArea = new JTextArea(3, 30);
        notesArea.setFont(INPUT_FONT);
        notesArea.setBorder(createInputBorder());
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(createInputBorder());
        notesScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Adjust scroll sensitivity
        JScrollBar notesScrollBar = notesScroll.getVerticalScrollBar();
        notesScrollBar.setUnitIncrement(16);
        notesScrollBar.setBlockIncrement(64);
        
        panel.add(notesScroll, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ==================== Button Section ====================
    
    /**
     * Create the button panel
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING_SMALL, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(new EmptyBorder(PADDING_MEDIUM, 0, 0, 0));
        
        cancelButton = createActionButton("Cancel", BACKGROUND_COLOR, LABEL_COLOR, e -> dispose());
        buttonPanel.add(cancelButton);
        
        saveButton = createPrimaryButton("Save Distribution", e -> saveDistribution());
        saveButton.setPreferredSize(new Dimension(150, BUTTON_HEIGHT));
        buttonPanel.add(saveButton);
        
        return buttonPanel;
    }
    
    /**
     * Create a primary action button
     */
    private JButton createPrimaryButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT_PRIMARY);
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(listener);
        addButtonHoverEffect(button, PRIMARY_COLOR, PRIMARY_DARK);
        return button;
    }
    
    /**
     * Create a styled action button
     */
    private JButton createActionButton(String text, Color bgColor, Color fgColor, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT_SECONDARY);
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
        addButtonHoverEffect(button, BACKGROUND_COLOR, HOVER_COLOR);
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
    
    /**
     * Create a styled border for input fields
     */
    private javax.swing.border.Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        );
    }
    
    /**
     * Setup keyboard navigation
     */
    private void setupKeyboardNavigation() {
        // ESC to close
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke("ESCAPE"), "closeDialog"
        );
        getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });
    }
    
    // ==================== Data Operations ====================
    
    /**
     * Load available inventory items
     */
    private void loadInventoryItems() {
        availableItems = inventoryService.getAllInventoryItems();
        DefaultComboBoxModel<InventoryItem> model = new DefaultComboBoxModel<>();
        for (InventoryItem item : availableItems) {
            if (item.getQuantity() > 0) { // Only show items with stock
                model.addElement(item);
            }
        }
        itemComboBox.setModel(model);
        
        if (model.getSize() == 0) {
            showWarningDialog("No Items Available", 
                "There are no items available in inventory. Please add items to inventory first.");
        }
    }
    
    /**
     * Load active calamities
     */
    private void loadCalamities() {
        List<Calamity> calamities = calamityService.getActiveCalamities();
        DefaultComboBoxModel<Calamity> model = new DefaultComboBoxModel<>();
        model.addElement(null); // Allow no calamity selection
        for (Calamity calamity : calamities) {
            model.addElement(calamity);
        }
        calamityComboBox.setModel(model);
    }
    
    /**
     * Load items from selected calamity
     */
    private void loadCalamityItems() {
        Calamity selectedCalamity = (Calamity) calamityComboBox.getSelectedItem();
        if (selectedCalamity == null) {
            showWarningDialog("No Calamity Selected", "Please select a calamity first.");
            return;
        }
        
        // Get full calamity with items
        Calamity fullCalamity = calamityService.getCalamityById(selectedCalamity.getId());
        if (fullCalamity == null || fullCalamity.getItems() == null || fullCalamity.getItems().isEmpty()) {
            showWarningDialog("No Items Assigned", 
                "This calamity has no items assigned. Please assign items in Calamity Management first.");
            return;
        }
        
        // Clear current selection
        selectedItems.clear();
        
        // Add items from calamity with standard quantities
        for (CalamityItem ci : fullCalamity.getItems()) {
            InventoryItem item = inventoryService.getInventoryItemById(ci.getInventoryId());
            if (item != null && item.getQuantity() > 0) {
                // Use standard quantity, but don't exceed available stock
                int quantity = Math.min(ci.getStandardQuantity(), item.getQuantity());
                selectedItems.put(ci.getInventoryId(), quantity);
            }
        }
        
        updateSelectedItemsTable();
        
        if (selectedItems.isEmpty()) {
            showWarningDialog("No Items Available", 
                "The items assigned to this calamity are not available in stock.");
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Items from calamity '" + fullCalamity.getName() + "' have been loaded.\n" +
                "You can adjust quantities or add/remove items as needed.",
                "Items Loaded",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
    
    /**
     * Add selected item to the distribution
     */
    private void addSelectedItem() {
        InventoryItem selectedItem = (InventoryItem) itemComboBox.getSelectedItem();
        if (selectedItem == null) {
            showErrorDialog("No Item Selected", "Please select an item from the list.");
            quantityField.requestFocus();
            return;
        }
        
        String quantityText = quantityField.getText().trim();
        if (quantityText.isEmpty()) {
            showErrorDialog("Quantity Required", "Please enter a quantity.");
            quantityField.requestFocus();
            return;
        }
        
        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                showErrorDialog("Invalid Quantity", "Quantity must be greater than 0.");
                quantityField.requestFocus();
                quantityField.selectAll();
                return;
            }
        } catch (NumberFormatException e) {
            showErrorDialog("Invalid Quantity", "Please enter a valid number.");
            quantityField.requestFocus();
            quantityField.selectAll();
            return;
        }
        
        // Refresh item to get current stock
        InventoryItem currentItem = inventoryService.getInventoryItemById(selectedItem.getId());
        if (currentItem == null) {
            showErrorDialog("Item Not Found", "The selected item is no longer available.");
            loadInventoryItems();
            return;
        }
        
        // Check if already added
        int existingQty = selectedItems.getOrDefault(currentItem.getId(), 0);
        int totalRequested = quantity + existingQty;
        
        // Validate stock
        if (totalRequested > currentItem.getQuantity()) {
            showErrorDialog("Insufficient Stock", 
                String.format("Available: %d %s\nAlready selected: %d %s\nRequested: %d %s\n\nTotal requested exceeds available stock.",
                    currentItem.getQuantity(), currentItem.getUnit(),
                    existingQty, currentItem.getUnit(),
                    quantity, currentItem.getUnit()));
            quantityField.requestFocus();
            quantityField.selectAll();
            return;
        }
        
        // Add to selected items
        selectedItems.put(currentItem.getId(), totalRequested);
        updateSelectedItemsTable();
        
        // Clear and focus quantity field for next entry
        quantityField.setText("");
        quantityField.requestFocus();
        
        // Show confirmation for multiple items
        if (existingQty > 0) {
            JOptionPane.showMessageDialog(
                this,
                String.format("Updated quantity for %s to %d %s", 
                    currentItem.getItemName(), totalRequested, currentItem.getUnit()),
                "Item Updated",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
    
    /**
     * Remove selected item from the distribution
     */
    private void removeSelectedItem() {
        int selectedRow = selectedItemsTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("No Selection", "Please select an item to remove, or double-click on an item.");
            return;
        }
        
        String itemName = (String) selectedItemsTableModel.getValueAt(selectedRow, 0);
        InventoryItem itemToRemove = null;
        for (InventoryItem item : availableItems) {
            if (item.getItemName().equals(itemName)) {
                itemToRemove = item;
                break;
            }
        }
        
        if (itemToRemove != null) {
            selectedItems.remove(itemToRemove.getId());
            updateSelectedItemsTable();
        }
    }
    
    /**
     * Update the selected items table
     */
    private void updateSelectedItemsTable() {
        selectedItemsTableModel.setRowCount(0);
        int totalItems = 0;
        
        for (Map.Entry<Integer, Integer> entry : selectedItems.entrySet()) {
            int inventoryId = entry.getKey();
            int quantity = entry.getValue();
            totalItems += quantity;
            
            InventoryItem item = inventoryService.getInventoryItemById(inventoryId);
            if (item != null) {
                int remainingStock = item.getQuantity() - quantity;
                Object[] row = {
                    item.getItemName(),
                    quantity,
                    item.getUnit(),
                    remainingStock
                };
                selectedItemsTableModel.addRow(row);
            }
        }
        
        // Update summary
        totalItemsLabel.setText(String.format("Total Items: %d", totalItems));
        
        // Enable/disable save button
        saveButton.setEnabled(!selectedItems.isEmpty());
    }
    
    /**
     * Save the distribution
     */
    private void saveDistribution() {
        if (selectedItems.isEmpty()) {
            showErrorDialog("No Items Selected", "Please add at least one item to the distribution.");
            return;
        }
        
        // Final validation - refresh all items to ensure stock is still available
        for (Map.Entry<Integer, Integer> entry : selectedItems.entrySet()) {
            InventoryItem item = inventoryService.getInventoryItemById(entry.getKey());
            if (item == null) {
                showErrorDialog("Item Unavailable", 
                    "One or more items are no longer available. Please refresh and try again.");
                loadInventoryItems();
                return;
            }
            if (item.getQuantity() < entry.getValue()) {
                showErrorDialog("Insufficient Stock", 
                    String.format("Item '%s' no longer has sufficient stock. Available: %d, Requested: %d",
                        item.getItemName(), item.getQuantity(), entry.getValue()));
                loadInventoryItems();
                updateSelectedItemsTable();
                return;
            }
        }
        
        // Create distribution
        Distribution distribution = new Distribution();
        distribution.setBeneficiaryId(beneficiary.getId());
        distribution.setDistributedBy(currentUser.getId());
        distribution.setNotes(notesArea.getText().trim());
        
        // Set calamity if selected
        Calamity selectedCalamity = (Calamity) calamityComboBox.getSelectedItem();
        if (selectedCalamity != null) {
            distribution.setCalamityId(selectedCalamity.getId());
        }
        
        // Add distribution items
        List<DistributionItem> items = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : selectedItems.entrySet()) {
            DistributionItem item = new DistributionItem(entry.getKey(), entry.getValue());
            items.add(item);
        }
        distribution.setItems(items);
        
        // Validate and save
        try {
            boolean success = distributionService.createDistribution(distribution);
            if (success) {
                JOptionPane.showMessageDialog(
                    this,
                    "Distribution recorded successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
                if (onSaveCallback != null) {
                    onSaveCallback.run();
                }
                dispose();
            } else {
                showErrorDialog("Error", "Failed to save distribution. Please try again.");
            }
        } catch (IllegalArgumentException e) {
            showErrorDialog("Validation Error", e.getMessage());
        } catch (Exception e) {
            showErrorDialog("Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
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
}
