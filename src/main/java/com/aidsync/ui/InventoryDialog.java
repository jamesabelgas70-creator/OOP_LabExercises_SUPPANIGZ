package com.aidsync.ui;

import com.aidsync.model.InventoryItem;
import com.aidsync.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Dialog for adding/editing inventory items
 * 
 * Features:
 * - Modern, clean UI matching application design
 * - Input validation and error handling
 * - Keyboard navigation support
 * - Responsive button interactions
 * - Consistent styling with other dialogs
 */
public class InventoryDialog extends JDialog {
    // ==================== UI Components ====================
    private JTextField itemNameField;
    private JTextField categoryField;
    private JSpinner quantitySpinner;
    private JTextField unitField;
    private JSpinner thresholdSpinner;
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel currentQuantityLabel;
    private JRadioButton setQuantityRadio;
    private JRadioButton restockRadio;
    private ButtonGroup quantityModeGroup;
    
    // ==================== Services & Data ====================
    private InventoryService inventoryService;
    private InventoryItem inventoryItem;
    private Runnable onSaveCallback;
    private com.aidsync.model.User currentUser;
    
    // ==================== Color Constants ====================
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color PRIMARY_DARK = new Color(0, 82, 164);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(68, 68, 68);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color HOVER_COLOR = new Color(245, 245, 245);
    
    // ==================== Font Constants ====================
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BUTTON_FONT_PRIMARY = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font BUTTON_FONT_SECONDARY = new Font("Segoe UI", Font.PLAIN, 13);
    
    // ==================== Spacing Constants ====================
    private static final int DIALOG_WIDTH = 500;
    private static final int DIALOG_HEIGHT_ADD = 450;
    private static final int DIALOG_HEIGHT_EDIT = 550;
    private static final int PADDING_LARGE = 25;
    private static final int PADDING_MEDIUM = 20;
    private static final int PADDING_SMALL = 10;
    private static final int LABEL_FIELD_SPACING = 5;
    private static final int BUTTON_HEIGHT = 40;
    private static final int BUTTON_WIDTH = 120;
    private static final int INPUT_HEIGHT = 38;
    
    // ==================== Constructor ====================
    public InventoryDialog(JFrame parent, InventoryService service, InventoryItem item, Runnable onSave, com.aidsync.model.User user) {
        super(parent, item == null ? "Add Inventory Item" : "Edit Inventory Item", true);
        this.inventoryService = service;
        this.inventoryItem = item;
        this.onSaveCallback = onSave;
        this.currentUser = user;
        initializeUI();
        if (inventoryItem != null) {
            loadInventoryItemData();
        }
    }
    
    // ==================== UI Initialization ====================
    
    /**
     * Initialize the main UI components
     */
    private void initializeUI() {
        configureDialog();
        JPanel mainPanel = createMainPanel();
        add(mainPanel);
        setupKeyboardNavigation();
    }
    
    /**
     * Configure dialog properties
     */
    private void configureDialog() {
        int height = inventoryItem == null ? DIALOG_HEIGHT_ADD : DIALOG_HEIGHT_EDIT;
        setSize(DIALOG_WIDTH, height);
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
    
    // ==================== Header Section ====================
    
    /**
     * Create the header section with title
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM, 0));
        
        JLabel titleLabel = new JLabel(inventoryItem == null ? "Add Inventory Item" : "Edit Inventory Item");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel);
        
        return headerPanel;
    }
    
    // ==================== Form Section ====================
    
    /**
     * Create the form panel with input fields
     */
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Item Name
        addFormField(formPanel, gbc, row, "Item Name *", createItemNameField());
        row += 2;
        
        // Category
        addFormField(formPanel, gbc, row, "Category", createCategoryField());
        row += 2;
        
        // Quantity section - different for add vs edit
        if (inventoryItem == null) {
            // Add mode: just quantity
            addFormField(formPanel, gbc, row, "Quantity *", createQuantitySpinner());
            row += 2;
        } else {
            // Edit mode: show current quantity and mode selection
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0;
            gbc.insets = new Insets(0, 0, LABEL_FIELD_SPACING, PADDING_MEDIUM);
            JLabel currentQtyLabel = new JLabel("Current Quantity:");
            currentQtyLabel.setFont(LABEL_FONT);
            currentQtyLabel.setForeground(LABEL_COLOR);
            formPanel.add(currentQtyLabel, gbc);
            
            gbc.gridx = 1;
            gbc.gridy = row;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(0, 0, LABEL_FIELD_SPACING, 0);
            currentQuantityLabel = new JLabel(String.valueOf(inventoryItem.getQuantity()) + 
                (inventoryItem.getUnit() != null && !inventoryItem.getUnit().isEmpty() ? " " + inventoryItem.getUnit() : ""));
            currentQuantityLabel.setFont(INPUT_FONT);
            currentQuantityLabel.setForeground(PRIMARY_COLOR);
            formPanel.add(currentQuantityLabel, gbc);
            row += 2;
            
            // Mode selection
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0;
            gbc.insets = new Insets(0, 0, LABEL_FIELD_SPACING, PADDING_MEDIUM);
            JLabel modeLabel = new JLabel("Update Mode:");
            modeLabel.setFont(LABEL_FONT);
            modeLabel.setForeground(LABEL_COLOR);
            formPanel.add(modeLabel, gbc);
            
            gbc.gridx = 1;
            gbc.gridy = row;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(0, 0, LABEL_FIELD_SPACING, 0);
            JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            modePanel.setBackground(BACKGROUND_COLOR);
            quantityModeGroup = new ButtonGroup();
            setQuantityRadio = new JRadioButton("Set Quantity", true);
            setQuantityRadio.setFont(LABEL_FONT);
            setQuantityRadio.setBackground(BACKGROUND_COLOR);
            setQuantityRadio.addActionListener(e -> updateQuantityMode());
            restockRadio = new JRadioButton("Restock (Add)", false);
            restockRadio.setFont(LABEL_FONT);
            restockRadio.setBackground(BACKGROUND_COLOR);
            restockRadio.addActionListener(e -> updateQuantityMode());
            quantityModeGroup.add(setQuantityRadio);
            quantityModeGroup.add(restockRadio);
            modePanel.add(setQuantityRadio);
            modePanel.add(restockRadio);
            formPanel.add(modePanel, gbc);
            row += 2;
            
            // Quantity field (changes based on mode)
            addFormField(formPanel, gbc, row, "Quantity *", createQuantitySpinner());
            row += 2;
        }
        
        // Unit
        addFormField(formPanel, gbc, row, "Unit", createUnitField());
        row += 2;
        
        // Low Stock Threshold
        addFormField(formPanel, gbc, row, "Low Stock Threshold", createThresholdSpinner());
        
        return formPanel;
    }
    
    /**
     * Add a form field to the panel
     */
    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        // Label
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, LABEL_FIELD_SPACING, PADDING_MEDIUM);
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        label.setForeground(LABEL_COLOR);
        panel.add(label, gbc);
        
        // Field
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, LABEL_FIELD_SPACING, 0);
        panel.add(field, gbc);
    }
    
    /**
     * Create item name field
     */
    private JTextField createItemNameField() {
        itemNameField = new JTextField();
        itemNameField.setFont(INPUT_FONT);
        itemNameField.setBorder(createInputBorder());
        itemNameField.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        return itemNameField;
    }
    
    /**
     * Create category field
     */
    private JTextField createCategoryField() {
        categoryField = new JTextField();
        categoryField.setFont(INPUT_FONT);
        categoryField.setBorder(createInputBorder());
        categoryField.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        return categoryField;
    }
    
    /**
     * Create quantity spinner
     */
    private JSpinner createQuantitySpinner() {
        if (inventoryItem == null) {
            // Add mode: start at 0
            quantitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        } else {
            // Edit mode: start at current quantity for "Set Quantity", or 0 for "Restock"
            quantitySpinner = new JSpinner(new SpinnerNumberModel(inventoryItem.getQuantity(), 0, Integer.MAX_VALUE, 1));
        }
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) quantitySpinner.getEditor();
        editor.getTextField().setFont(INPUT_FONT);
        editor.getTextField().setBorder(createInputBorder());
        editor.getTextField().setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        return quantitySpinner;
    }
    
    /**
     * Update quantity mode (Set Quantity vs Restock)
     */
    private void updateQuantityMode() {
        if (inventoryItem != null && quantitySpinner != null) {
            if (setQuantityRadio.isSelected()) {
                // Set Quantity mode: show current quantity
                quantitySpinner.setValue(inventoryItem.getQuantity());
            } else {
                // Restock mode: start at 0 (amount to add)
                quantitySpinner.setValue(0);
            }
        }
    }
    
    /**
     * Create unit field
     */
    private JTextField createUnitField() {
        unitField = new JTextField();
        unitField.setFont(INPUT_FONT);
        unitField.setBorder(createInputBorder());
        unitField.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        return unitField;
    }
    
    /**
     * Create threshold spinner
     */
    private JSpinner createThresholdSpinner() {
        thresholdSpinner = new JSpinner(new SpinnerNumberModel(10, 0, Integer.MAX_VALUE, 1));
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) thresholdSpinner.getEditor();
        editor.getTextField().setFont(INPUT_FONT);
        editor.getTextField().setBorder(createInputBorder());
        editor.getTextField().setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        return thresholdSpinner;
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
        
        saveButton = createPrimaryButton("Save", e -> saveInventoryItem());
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
    
    // ==================== Keyboard Navigation ====================
    
    /**
     * Setup keyboard navigation
     */
    private void setupKeyboardNavigation() {
        // ESC to close
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog"
        );
        getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });
        
        // Enter key on save button
        getRootPane().setDefaultButton(saveButton);
    }
    
    // ==================== Data Operations ====================
    
    /**
     * Load inventory item data into form fields
     */
    private void loadInventoryItemData() {
        if (inventoryItem == null) return;
        
        itemNameField.setText(inventoryItem.getItemName() != null ? inventoryItem.getItemName() : "");
        categoryField.setText(inventoryItem.getCategory() != null ? inventoryItem.getCategory() : "");
        quantitySpinner.setValue(inventoryItem.getQuantity());
        unitField.setText(inventoryItem.getUnit() != null ? inventoryItem.getUnit() : "");
        thresholdSpinner.setValue(inventoryItem.getLowStockThreshold());
    }
    
    /**
     * Save inventory item
     */
    private void saveInventoryItem() {
        // Validate required fields
        String itemName = itemNameField.getText().trim();
        if (itemName.isEmpty()) {
            showErrorDialog("Validation Error", "Item name is required.");
            itemNameField.requestFocus();
            return;
        }
        
        try {
            if (inventoryItem == null) {
                // Create new item
                InventoryItem item = new InventoryItem();
                item.setItemName(itemName);
                item.setCategory(categoryField.getText().trim());
                item.setQuantity((Integer) quantitySpinner.getValue());
                item.setUnit(unitField.getText().trim());
                item.setLowStockThreshold((Integer) thresholdSpinner.getValue());
                
                boolean success = inventoryService.createInventoryItem(item);
                if (success) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Inventory item created successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                    dispose();
                } else {
                    showErrorDialog("Error", "Failed to create inventory item. Please try again.");
                }
            } else {
                // Update existing item
                Integer userId = currentUser != null ? currentUser.getId() : null;
                
                if (restockRadio != null && restockRadio.isSelected()) {
                    // Restock mode: add quantity
                    int restockAmount = (Integer) quantitySpinner.getValue();
                    if (restockAmount <= 0) {
                        showErrorDialog("Validation Error", "Restock quantity must be greater than 0.");
                        quantitySpinner.requestFocus();
                        return;
                    }
                    
                    // Update other fields first
                    inventoryItem.setItemName(itemName);
                    inventoryItem.setCategory(categoryField.getText().trim());
                    inventoryItem.setUnit(unitField.getText().trim());
                    inventoryItem.setLowStockThreshold((Integer) thresholdSpinner.getValue());
                    inventoryService.updateInventoryItem(inventoryItem);
                    
                    // Then restock
                    boolean success = inventoryService.restockInventoryItem(
                        inventoryItem.getId(), 
                        restockAmount, 
                        userId,
                        "Restocked via Inventory Management"
                    );
                    
                    if (success) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Inventory item restocked successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        if (onSaveCallback != null) {
                            onSaveCallback.run();
                        }
                        dispose();
                    } else {
                        showErrorDialog("Error", "Failed to restock inventory item. Please try again.");
                    }
                } else {
                    // Set Quantity mode: set directly
                    int oldQuantity = inventoryItem.getQuantity();
                    int newQuantity = (Integer) quantitySpinner.getValue();
                    
                    // Update all fields including quantity
                    inventoryItem.setItemName(itemName);
                    inventoryItem.setCategory(categoryField.getText().trim());
                    inventoryItem.setUnit(unitField.getText().trim());
                    inventoryItem.setLowStockThreshold((Integer) thresholdSpinner.getValue());
                    inventoryItem.setQuantity(newQuantity);
                    
                    // Save all changes
                    boolean success = inventoryService.updateInventoryItem(inventoryItem);
                    
                    if (success) {
                        // Log transaction if quantity changed
                        if (oldQuantity != newQuantity) {
                            inventoryService.logTransaction(
                                inventoryItem.getId(),
                                "Set Quantity",
                                newQuantity - oldQuantity,
                                oldQuantity,
                                newQuantity,
                                userId,
                                "Quantity and details updated via Inventory Management",
                                null,
                                null
                            );
                        }
                        
                        JOptionPane.showMessageDialog(
                            this,
                            "Inventory item updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        if (onSaveCallback != null) {
                            onSaveCallback.run();
                        }
                        dispose();
                    } else {
                        showErrorDialog("Error", "Failed to update inventory item. Please try again.");
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            showErrorDialog("Validation Error", ex.getMessage());
        } catch (Exception ex) {
            showErrorDialog("Error", "An error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // ==================== Dialog Helpers ====================
    
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
