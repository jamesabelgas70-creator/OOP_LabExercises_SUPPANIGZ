package com.aidsync.ui;

import com.aidsync.model.Calamity;
import com.aidsync.model.CalamityItem;
import com.aidsync.model.InventoryItem;
import com.aidsync.service.CalamityService;
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
 * Dialog for adding/editing calamities with item assignment
 * 
 * Features:
 * - Modern, clean UI matching application design
 * - Assign items with standard quantities
 * - Add/remove items from calamity
 * - Input validation
 * - Keyboard navigation support
 */
public class CalamityDialog extends JDialog {
    // ==================== UI Components ====================
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JComboBox<String> statusComboBox;
    private JTable itemsTable;
    private DefaultTableModel itemsTableModel;
    private JComboBox<InventoryItem> itemComboBox;
    private JSpinner quantitySpinner;
    private JButton addItemButton;
    private JButton removeItemButton;
    private JButton saveButton;
    private JButton cancelButton;
    
    // ==================== Services & Data ====================
    private CalamityService calamityService;
    private InventoryService inventoryService;
    private Calamity calamity;
    private Runnable onSaveCallback;
    private Map<Integer, Integer> assignedItems; // inventoryId -> standardQuantity
    private List<InventoryItem> availableItems;
    
    // ==================== Color Constants ====================
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color PRIMARY_DARK = new Color(0, 82, 164);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(68, 68, 68);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color HOVER_COLOR = new Color(245, 245, 245);
    private static final Color TABLE_HEADER_COLOR = new Color(240, 240, 240);
    
    // ==================== Font Constants ====================
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BUTTON_FONT_PRIMARY = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font BUTTON_FONT_SECONDARY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    
    // ==================== Spacing Constants ====================
    private static final int DIALOG_WIDTH = 1000;
    private static final int DIALOG_HEIGHT = 650;
    private static final int PADDING_LARGE = 25;
    private static final int PADDING_MEDIUM = 15;
    private static final int PADDING_SMALL = 10;
    private static final int FIELD_SPACING = 12;
    private static final int BUTTON_HEIGHT = 40;
    private static final int BUTTON_WIDTH = 120;
    private static final int INPUT_HEIGHT = 38;
    
    // ==================== Constructor ====================
    public CalamityDialog(JFrame parent, CalamityService service, Calamity calamity, Runnable onSave) {
        super(parent, calamity == null ? "Add New Calamity" : "Edit Calamity", true);
        this.calamityService = service;
        this.inventoryService = new InventoryService();
        this.calamity = calamity;
        this.onSaveCallback = onSave;
        this.assignedItems = new HashMap<>();
        initializeUI();
        loadInventoryItems();
        if (calamity != null) {
            loadCalamityData();
        }
        setupKeyboardNavigation();
    }
    
    // ==================== UI Initialization ====================
    
    /**
     * Initialize the main UI components
     */
    private void initializeUI() {
        configureDialog();
        JPanel mainPanel = createMainPanel();
        add(mainPanel);
    }
    
    /**
     * Configure dialog properties
     */
    private void configureDialog() {
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(getParent());
        setResizable(false);
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
        mainPanel.add(createFormPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    /**
     * Create the header panel
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM, 0));
        
        JLabel titleLabel = new JLabel(calamity == null ? "Add New Calamity" : "Edit Calamity");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(LABEL_COLOR);
        headerPanel.add(titleLabel);
        
        return headerPanel;
    }
    
    /**
     * Create the form panel with scrollable content
     */
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(BACKGROUND_COLOR);
        
        // Basic info fields
        formPanel.add(createFieldPanel("Name *", nameField = createTextField()));
        formPanel.add(Box.createVerticalStrut(FIELD_SPACING));
        
        formPanel.add(createFieldPanel("Description", descriptionArea = createTextArea()));
        formPanel.add(Box.createVerticalStrut(FIELD_SPACING));
        
        formPanel.add(createFieldPanel("Status *", statusComboBox = createStatusComboBox()));
        formPanel.add(Box.createVerticalStrut(FIELD_SPACING * 2));
        
        // Items assignment section
        formPanel.add(createItemsSection());
        
        // Add flexible space
        formPanel.add(Box.createVerticalGlue());
        
        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);
        verticalScrollBar.setBlockIncrement(64);
        
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(BACKGROUND_COLOR);
        wrapperPanel.add(scrollPane, BorderLayout.CENTER);
        
        return wrapperPanel;
    }
    
    /**
     * Create items assignment section
     */
    private JPanel createItemsSection() {
        JPanel sectionPanel = new JPanel(new BorderLayout(PADDING_SMALL, PADDING_SMALL));
        sectionPanel.setBackground(BACKGROUND_COLOR);
        sectionPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            "Assigned Items (Standard Quantities)",
            0, 0,
            LABEL_FONT,
            LABEL_COLOR
        ));
        
        // Items table
        JScrollPane tableScrollPane = createItemsTable();
        sectionPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Add item panel
        JPanel addItemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING_SMALL, PADDING_SMALL));
        addItemPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel itemLabel = new JLabel("Item:");
        itemLabel.setFont(LABEL_FONT);
        itemLabel.setForeground(LABEL_COLOR);
        addItemPanel.add(itemLabel);
        
        itemComboBox = new JComboBox<>();
        itemComboBox.setFont(INPUT_FONT);
        itemComboBox.setPreferredSize(new Dimension(250, INPUT_HEIGHT));
        itemComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof InventoryItem) {
                    InventoryItem item = (InventoryItem) value;
                    setText(item.getItemName() + " (" + item.getUnit() + ") - Available: " + item.getQuantity());
                }
                return this;
            }
        });
        // Update spinner max when item selection changes
        itemComboBox.addActionListener(e -> {
            InventoryItem selected = (InventoryItem) itemComboBox.getSelectedItem();
            if (selected != null) {
                SpinnerNumberModel model = (SpinnerNumberModel) quantitySpinner.getModel();
                model.setMaximum(selected.getQuantity());
                if ((Integer) quantitySpinner.getValue() > selected.getQuantity()) {
                    quantitySpinner.setValue(selected.getQuantity());
                }
            }
        });
        addItemPanel.add(itemComboBox);
        
        JLabel quantityLabel = new JLabel("Standard Quantity:");
        quantityLabel.setFont(LABEL_FONT);
        quantityLabel.setForeground(LABEL_COLOR);
        addItemPanel.add(quantityLabel);
        
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        quantitySpinner.setFont(INPUT_FONT);
        quantitySpinner.setPreferredSize(new Dimension(80, INPUT_HEIGHT));
        addItemPanel.add(quantitySpinner);
        
        addItemButton = createActionButton("Add", PRIMARY_COLOR, Color.WHITE, e -> addItem());
        addItemPanel.add(addItemButton);
        
        removeItemButton = createActionButton("Remove", new Color(220, 53, 69), Color.WHITE, e -> removeSelectedItem());
        addItemPanel.add(removeItemButton);
        
        sectionPanel.add(addItemPanel, BorderLayout.SOUTH);
        
        return sectionPanel;
    }
    
    /**
     * Create items table
     */
    private JScrollPane createItemsTable() {
        String[] columnNames = {"Item Name", "Unit", "Standard Quantity"};
        itemsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        itemsTable = new JTable(itemsTableModel);
        itemsTable.setFont(TABLE_FONT);
        itemsTable.setRowHeight(30);
        itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        itemsTable.setGridColor(BORDER_COLOR);
        itemsTable.setShowGrid(true);
        
        itemsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        itemsTable.getTableHeader().setBackground(TABLE_HEADER_COLOR);
        itemsTable.getTableHeader().setForeground(LABEL_COLOR);
        itemsTable.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.setPreferredSize(new Dimension(0, 200));
        
        return scrollPane;
    }
    
    /**
     * Create a field panel with label and input
     */
    private JPanel createFieldPanel(String labelText, JComponent inputComponent) {
        JPanel fieldPanel = new JPanel(new BorderLayout(0, PADDING_SMALL));
        fieldPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        label.setForeground(LABEL_COLOR);
        fieldPanel.add(label, BorderLayout.NORTH);
        
        fieldPanel.add(inputComponent, BorderLayout.CENTER);
        
        return fieldPanel;
    }
    
    /**
     * Create a styled text field
     */
    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(INPUT_FONT);
        field.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, INPUT_HEIGHT));
        field.setBorder(createInputBorder());
        field.setBackground(BACKGROUND_COLOR);
        return field;
    }
    
    /**
     * Create a styled text area
     */
    private JTextArea createTextArea() {
        JTextArea area = new JTextArea(3, 20);
        area.setFont(INPUT_FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(createInputBorder());
        area.setBackground(BACKGROUND_COLOR);
        return area;
    }
    
    /**
     * Create status combo box
     */
    private JComboBox<String> createStatusComboBox() {
        JComboBox<String> comboBox = new JComboBox<>(new String[]{"Active", "Inactive"});
        comboBox.setFont(INPUT_FONT);
        comboBox.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, INPUT_HEIGHT));
        comboBox.setBorder(createInputBorder());
        comboBox.setBackground(BACKGROUND_COLOR);
        return comboBox;
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
     * Create the button panel
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING_SMALL, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(new EmptyBorder(PADDING_MEDIUM, 0, 0, 0));
        
        cancelButton = createActionButton("Cancel", BACKGROUND_COLOR, LABEL_COLOR, e -> dispose());
        saveButton = createPrimaryButton("Save", e -> saveCalamity());
        
        buttonPanel.add(cancelButton);
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
        if (!bgColor.equals(BACKGROUND_COLOR)) {
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
    
    /**
     * Setup keyboard navigation
     */
    private void setupKeyboardNavigation() {
        getRootPane().setDefaultButton(saveButton);
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "closeDialog");
        getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });
    }
    
    // ==================== Data Operations ====================
    
    /**
     * Load inventory items
     */
    private void loadInventoryItems() {
        availableItems = inventoryService.getAllInventoryItems();
        DefaultComboBoxModel<InventoryItem> model = new DefaultComboBoxModel<>();
        for (InventoryItem item : availableItems) {
            model.addElement(item);
        }
        itemComboBox.setModel(model);
    }
    
    /**
     * Load calamity data into form
     */
    private void loadCalamityData() {
        if (calamity == null) return;
        
        nameField.setText(calamity.getName() != null ? calamity.getName() : "");
        descriptionArea.setText(calamity.getDescription() != null ? calamity.getDescription() : "");
        statusComboBox.setSelectedItem(calamity.getStatus() != null ? calamity.getStatus() : "Active");
        
        // Load assigned items
        if (calamity.getItems() != null) {
            for (CalamityItem ci : calamity.getItems()) {
                assignedItems.put(ci.getInventoryId(), ci.getStandardQuantity());
            }
            refreshItemsTable();
        }
    }
    
    /**
     * Refresh items table
     */
    private void refreshItemsTable() {
        itemsTableModel.setRowCount(0);
        for (Map.Entry<Integer, Integer> entry : assignedItems.entrySet()) {
            InventoryItem item = inventoryService.getInventoryItemById(entry.getKey());
            if (item != null) {
                int standardQuantity = entry.getValue();
                int availableQuantity = item.getQuantity();
                
                // Check if quantity exceeds inventory
                boolean exceedsInventory = standardQuantity > availableQuantity;
                String quantityDisplay = standardQuantity + (exceedsInventory ? " ⚠️ (Max: " + availableQuantity + ")" : "");
                
                Object[] row = {
                    item.getItemName(),
                    item.getUnit(),
                    quantityDisplay
                };
                itemsTableModel.addRow(row);
            }
        }
    }
    
    /**
     * Add item to calamity
     */
    private void addItem() {
        InventoryItem selectedItem = (InventoryItem) itemComboBox.getSelectedItem();
        if (selectedItem == null) {
            showError("Please select an item.");
            return;
        }
        
        if (assignedItems.containsKey(selectedItem.getId())) {
            showError("This item is already assigned to this calamity.");
            return;
        }
        
        // Validate that quantity doesn't exceed inventory quantity
        // Refresh item to get current stock
        InventoryItem currentItem = inventoryService.getInventoryItemById(selectedItem.getId());
        if (currentItem == null) {
            showError("Item no longer exists in inventory.");
            loadInventoryItems();
            return;
        }
        
        // Update spinner maximum to current inventory quantity
        SpinnerNumberModel spinnerModel = (SpinnerNumberModel) quantitySpinner.getModel();
        spinnerModel.setMaximum(currentItem.getQuantity());
        
        int quantity = (Integer) quantitySpinner.getValue();
        
        if (quantity > currentItem.getQuantity()) {
            showError(String.format(
                "Standard quantity (%d) cannot exceed available inventory quantity (%d %s). " +
                "The quantity has been adjusted to the maximum available.",
                quantity, currentItem.getQuantity(), currentItem.getUnit()
            ));
            quantity = currentItem.getQuantity();
            quantitySpinner.setValue(quantity);
            quantitySpinner.requestFocus();
        }
        
        assignedItems.put(selectedItem.getId(), quantity);
        refreshItemsTable();
        
        // Reset selection
        itemComboBox.setSelectedIndex(0);
        quantitySpinner.setValue(1);
    }
    
    /**
     * Remove selected item
     */
    private void removeSelectedItem() {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an item to remove.");
            return;
        }
        
        String itemName = (String) itemsTableModel.getValueAt(selectedRow, 0);
        InventoryItem item = findItemByName(itemName);
        if (item != null) {
            assignedItems.remove(item.getId());
            refreshItemsTable();
        }
    }
    
    /**
     * Find inventory item by name
     */
    private InventoryItem findItemByName(String name) {
        for (InventoryItem item : availableItems) {
            if (item.getItemName().equals(name)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Save calamity
     */
    private void saveCalamity() {
        try {
            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();
            String status = (String) statusComboBox.getSelectedItem();
            
            if (name.isEmpty()) {
                showError("Calamity name is required.");
                nameField.requestFocus();
                return;
            }
            
            // Final validation: Check all assigned items don't exceed inventory quantities
            for (Map.Entry<Integer, Integer> entry : assignedItems.entrySet()) {
                InventoryItem item = inventoryService.getInventoryItemById(entry.getKey());
                if (item == null) {
                    showError("One or more assigned items no longer exist in inventory. Please refresh and try again.");
                    loadInventoryItems();
                    refreshItemsTable();
                    return;
                }
                if (entry.getValue() > item.getQuantity()) {
                    showError(String.format(
                        "Item '%s' has a standard quantity (%d) that exceeds available inventory (%d %s). " +
                        "Please adjust the quantity.",
                        item.getItemName(), entry.getValue(), item.getQuantity(), item.getUnit()
                    ));
                    return;
                }
            }
            
            Calamity c = calamity != null ? calamity : new Calamity();
            c.setName(name);
            c.setDescription(description.isEmpty() ? null : description);
            c.setStatus(status);
            
            // Convert assigned items to CalamityItem list
            List<CalamityItem> items = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : assignedItems.entrySet()) {
                CalamityItem ci = new CalamityItem();
                ci.setInventoryId(entry.getKey());
                ci.setStandardQuantity(entry.getValue());
                items.add(ci);
            }
            c.setItems(items);
            
            boolean success;
            if (calamity == null) {
                success = calamityService.createCalamity(c);
            } else {
                success = calamityService.updateCalamity(c);
            }
            
            if (success) {
                showSuccess(calamity == null ? "Calamity created successfully!" : "Calamity updated successfully!");
                if (onSaveCallback != null) {
                    onSaveCallback.run();
                }
                dispose();
            } else {
                showError("Failed to save calamity.");
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Show success message
     */
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Success",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}

