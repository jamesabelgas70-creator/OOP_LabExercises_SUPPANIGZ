package com.aidsync.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.aidsync.model.Calamity;
import com.aidsync.model.CalamityItem;
import com.aidsync.model.InventoryItem;
import com.aidsync.service.CalamityService;
import com.aidsync.service.InventoryService;

/**
 * Dialog for adding/editing calamities with item assignment - Fixed version
 */
public class CalamityDialog_fix extends JDialog {
    // UI Components
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
    
    // Services & Data
    private final CalamityService calamityService;
    private final InventoryService inventoryService;
    private final Calamity calamity;
    private final Runnable onSaveCallback;
    private final Map<Integer, Integer> assignedItems;
    private List<InventoryItem> availableItems;
    
    // Constructor
    public CalamityDialog_fix(JFrame parent, CalamityService service, Calamity calamity, Runnable onSave) {
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
    }
    
    private void initializeUI() {
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Form fields
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Name field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);
        
        // Description field
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 0.3;
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(descriptionArea), gbc);
        
        // Status field
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        statusComboBox = new JComboBox<>(new String[]{"Active", "Inactive"});
        formPanel.add(statusComboBox, gbc);
        
        mainPanel.add(formPanel, BorderLayout.NORTH);
        
        // Items section
        JPanel itemsPanel = createItemsPanel();
        mainPanel.add(itemsPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveCalamity());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Assigned Items"));
        
        // Table
        String[] columns = {"Item Name", "Unit", "Standard Quantity"};
        itemsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemsTable = new JTable(itemsTableModel);
        panel.add(new JScrollPane(itemsTable), BorderLayout.CENTER);
        
        // Add item controls
        JPanel addPanel = new JPanel(new FlowLayout());
        itemComboBox = new JComboBox<>();
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        addItemButton = new JButton("Add");
        addItemButton.addActionListener(e -> addItem());
        removeItemButton = new JButton("Remove");
        removeItemButton.addActionListener(e -> removeSelectedItem());
        
        addPanel.add(new JLabel("Item:"));
        addPanel.add(itemComboBox);
        addPanel.add(new JLabel("Quantity:"));
        addPanel.add(quantitySpinner);
        addPanel.add(addItemButton);
        addPanel.add(removeItemButton);
        
        panel.add(addPanel, BorderLayout.SOUTH);
        return panel;
    }
    
    private void loadInventoryItems() {
        availableItems = inventoryService.getAllInventoryItems();
        DefaultComboBoxModel<InventoryItem> model = new DefaultComboBoxModel<>();
        for (InventoryItem item : availableItems) {
            model.addElement(item);
        }
        itemComboBox.setModel(model);
    }
    
    private void loadCalamityData() {
        if (calamity == null) return;
        
        nameField.setText(calamity.getName() != null ? calamity.getName() : "");
        descriptionArea.setText(calamity.getDescription() != null ? calamity.getDescription() : "");
        statusComboBox.setSelectedItem(calamity.getStatus() != null ? calamity.getStatus() : "Active");
        
        if (calamity.getItems() != null) {
            for (CalamityItem ci : calamity.getItems()) {
                assignedItems.put(ci.getInventoryId(), ci.getStandardQuantity());
            }
            refreshItemsTable();
        }
    }
    
    private void refreshItemsTable() {
        itemsTableModel.setRowCount(0);
        for (Map.Entry<Integer, Integer> entry : assignedItems.entrySet()) {
            InventoryItem item = inventoryService.getInventoryItemById(entry.getKey());
            if (item != null) {
                Object[] row = {
                    item.getItemName(),
                    item.getUnit(),
                    entry.getValue()
                };
                itemsTableModel.addRow(row);
            }
        }
    }
    
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
        
        int quantity = (Integer) quantitySpinner.getValue();
        assignedItems.put(selectedItem.getId(), quantity);
        refreshItemsTable();
    }
    
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
    
    private InventoryItem findItemByName(String name) {
        for (InventoryItem item : availableItems) {
            if (item.getItemName().equals(name)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Save calamity - Fixed version with proper callback execution
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
                
                // Close dialog first
                dispose();
                
                // Then execute callback on EDT to refresh the list
                SwingUtilities.invokeLater(() -> {
                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                });
            } else {
                showError("Failed to save calamity.");
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}