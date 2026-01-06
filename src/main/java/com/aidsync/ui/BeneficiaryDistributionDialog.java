package com.aidsync.ui;

import com.aidsync.model.Beneficiary;
import com.aidsync.model.Distribution;
import com.aidsync.model.DistributionItem;
import com.aidsync.model.User;
import com.aidsync.service.BeneficiaryService;
import com.aidsync.service.DistributionService;
import com.aidsync.service.InventoryService;
import com.aidsync.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dialog showing beneficiary distribution history and allowing new distributions
 * 
 * Features:
 * - Shows beneficiary details
 * - Displays distribution history in a table
 * - "Add New Distribution" button to create new distributions
 * - Clean, organized layout
 */
public class BeneficiaryDistributionDialog extends JDialog {
    // ==================== UI Components ====================
    private JTable distributionTable;
    private DefaultTableModel tableModel;
    private JButton addDistributionButton;
    private JButton voidDistributionButton;
    private JButton closeButton;
    private JButton refreshButton;
    
    // ==================== Services & Data ====================
    private Beneficiary beneficiary;
    private DistributionService distributionService;
    private InventoryService inventoryService;
    private UserService userService;
    private User currentUser;
    private Runnable onDistributionAdded;
    
    // ==================== Color Constants ====================
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color PRIMARY_DARK = new Color(0, 82, 164);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(68, 68, 68);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color HOVER_COLOR = new Color(245, 245, 245);
    private static final Color TABLE_HEADER_COLOR = new Color(240, 240, 240);
    private static final Color DELETE_COLOR = new Color(220, 53, 69);
    private static final Color DELETE_DARK = new Color(200, 35, 51);
    
    // ==================== Font Constants ====================
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font BUTTON_FONT_PRIMARY = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font BUTTON_FONT_SECONDARY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    
    // ==================== Spacing Constants ====================
    private static final int DIALOG_WIDTH = 1250;
    private static final int DIALOG_HEIGHT = 700;
    private static final int PADDING_LARGE = 25;
    private static final int PADDING_MEDIUM = 15;
    private static final int PADDING_SMALL = 10;
    private static final int BUTTON_HEIGHT = 40;
    private static final int BUTTON_WIDTH = 150;
    
    // ==================== Constructor ====================
    public BeneficiaryDistributionDialog(
            JFrame parent,
            Beneficiary beneficiary,
            BeneficiaryService beneficiaryService,
            DistributionService distributionService,
            InventoryService inventoryService,
            User currentUser,
            Runnable onDistributionAdded) {
        super(parent, "Distribution History - " + beneficiary.getFullName(), true);
        this.beneficiary = beneficiary;
        this.distributionService = distributionService;
        this.inventoryService = inventoryService;
        this.userService = new UserService();
        this.currentUser = currentUser;
        this.onDistributionAdded = onDistributionAdded;
        initializeUI();
        loadDistributions();
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
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    // ==================== Header Section ====================
    
    /**
     * Create the header panel with beneficiary info
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM, 0));
        
        JLabel titleLabel = new JLabel("Distribution History");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(LABEL_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel infoPanel = createBeneficiaryInfoPanel();
        headerPanel.add(infoPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Create beneficiary info panel
     */
    private JPanel createBeneficiaryInfoPanel() {
        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        infoPanel.setBackground(BACKGROUND_COLOR);
        
        addInfoLabel(infoPanel, "ID:", beneficiary.getBeneficiaryId());
        addInfoLabel(infoPanel, "Name:", beneficiary.getFullName());
        addInfoLabel(infoPanel, "Barangay:", beneficiary.getBarangay());
        addInfoLabel(infoPanel, "Purok:", beneficiary.getPurok());
        addInfoLabel(infoPanel, "Family Size:", String.valueOf(beneficiary.getFamilySize()));
        
        return infoPanel;
    }
    
    /**
     * Add an info label to the panel
     */
    private void addInfoLabel(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(LABEL_FONT);
        labelComponent.setForeground(new Color(100, 100, 100));
        panel.add(labelComponent);
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(LABEL_FONT);
        valueComponent.setForeground(LABEL_COLOR);
        panel.add(valueComponent);
    }
    
    // ==================== Table Section ====================
    
    /**
     * Create the table panel
     */
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(BACKGROUND_COLOR);
        tablePanel.setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM, 0));
        
        JScrollPane scrollPane = createTableScrollPane();
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    /**
     * Create the table with scroll pane
     */
    private JScrollPane createTableScrollPane() {
        String[] columnNames = {"Date", "Calamity/Event", "Items", "Total Quantity", "Distributed By", "Notes"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        distributionTable = new JTable(tableModel);
        distributionTable.setFont(TABLE_FONT);
        distributionTable.setRowHeight(30);
        distributionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        distributionTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        distributionTable.setGridColor(BORDER_COLOR);
        distributionTable.setShowGrid(true);
        distributionTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Set custom renderer for Items column (column index 2) to display HTML
        distributionTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null && value instanceof String) {
                    String text = (String) value;
                    // If it's HTML formatted, set it as HTML
                    if (text.startsWith("<html>")) {
                        label.setText(text);
                    } else {
                        label.setText(text);
                    }
                }
                label.setVerticalAlignment(SwingConstants.TOP);
                label.setVerticalTextPosition(SwingConstants.TOP);
                return label;
            }
        });
        
        // Style table header
        distributionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        distributionTable.getTableHeader().setBackground(TABLE_HEADER_COLOR);
        distributionTable.getTableHeader().setForeground(LABEL_COLOR);
        distributionTable.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(distributionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Adjust scroll sensitivity
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);
        verticalScrollBar.setBlockIncrement(64);
        
        return scrollPane;
    }
    
    // ==================== Button Section ====================
    
    /**
     * Create the button panel
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING_SMALL, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        refreshButton = createActionButton("Refresh", BACKGROUND_COLOR, LABEL_COLOR, e -> loadDistributions());
        buttonPanel.add(refreshButton);
        
        voidDistributionButton = createDeleteButton("Void Selected", e -> voidSelectedDistribution());
        buttonPanel.add(voidDistributionButton);
        
        addDistributionButton = createPrimaryButton("Add New Distribution", e -> openDistributionItemDialog());
        // Make this button wider than others
        addDistributionButton.setPreferredSize(new Dimension(200, BUTTON_HEIGHT));
        buttonPanel.add(addDistributionButton);
        
        closeButton = createActionButton("Close", BACKGROUND_COLOR, LABEL_COLOR, e -> dispose());
        buttonPanel.add(closeButton);
        
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
     * Create a delete/void button (red button)
     */
    private JButton createDeleteButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT_SECONDARY);
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setBackground(DELETE_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(listener);
        addButtonHoverEffect(button, DELETE_COLOR, DELETE_DARK);
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
     * Get user display name (full name or username)
     */
    private String getUserDisplayName(int userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            String fullName = user.getFullName();
            if (fullName != null && !fullName.trim().isEmpty()) {
                return fullName;
            } else {
                return user.getUsername();
            }
        }
        return "User ID: " + userId; // Fallback if user not found
    }
    
    /**
     * Load distributions for the beneficiary
     */
    private void loadDistributions() {
        tableModel.setRowCount(0);
        List<Distribution> distributions = distributionService.getDistributionsByBeneficiary(beneficiary.getId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (Distribution dist : distributions) {
            // Build items string with HTML formatting for vertical stacking
            StringBuilder itemsStr = new StringBuilder("<html>");
            int totalQuantity = 0;
            int itemCount = 0;
            
            for (DistributionItem item : dist.getItems()) {
                // Get inventory item name
                com.aidsync.model.InventoryItem invItem = inventoryService.getInventoryItemById(item.getInventoryId());
                if (invItem != null) {
                    if (itemCount > 0) {
                        itemsStr.append("<br>");
                    }
                    itemsStr.append("• ").append(invItem.getItemName())
                             .append(" (").append(item.getQuantity())
                             .append(" ").append(invItem.getUnit()).append(")");
                    totalQuantity += item.getQuantity();
                    itemCount++;
                }
            }
            
            itemsStr.append("</html>");
            String itemsDisplay = itemCount > 0 ? itemsStr.toString() : "<html>No items</html>";
            String notes = dist.getNotes() != null && !dist.getNotes().trim().isEmpty() ? dist.getNotes() : "-";
            
            // Get user name for "Distributed By" column
            String distributedByName = getUserDisplayName(dist.getDistributedBy());
            
            // Get calamity name
            String calamityName = "-";
            if (dist.getCalamityId() != null) {
                com.aidsync.service.CalamityService calamityService = new com.aidsync.service.CalamityService();
                com.aidsync.model.Calamity calamity = calamityService.getCalamityById(dist.getCalamityId());
                if (calamity != null) {
                    calamityName = calamity.getName();
                }
            }
            
            Object[] row = {
                dist.getDistributionDate().format(formatter),
                calamityName,
                itemsDisplay,
                totalQuantity,
                distributedByName,
                notes
            };
            tableModel.addRow(row);
            
            // Adjust row height based on number of items
            int rowIndex = tableModel.getRowCount() - 1;
            int baseHeight = 30;
            int itemHeight = 20; // Height per item line
            int calculatedHeight = baseHeight + (itemCount * itemHeight);
            distributionTable.setRowHeight(rowIndex, Math.max(calculatedHeight, baseHeight));
        }
    }
    
    // ==================== Dialog Operations ====================
    
    /**
     * Open distribution item dialog
     */
    private void openDistributionItemDialog() {
        SwingUtilities.invokeLater(() -> {
            DistributionItemDialog dialog = new DistributionItemDialog(
                this,
                beneficiary,
                distributionService,
                inventoryService,
                currentUser,
                () -> {
                    loadDistributions();
                    if (onDistributionAdded != null) {
                        onDistributionAdded.run();
                    }
                }
            );
            dialog.setVisible(true);
        });
    }
    
    /**
     * Void the selected distribution
     */
    private void voidSelectedDistribution() {
        int selectedRow = distributionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Please select a distribution to void.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        // Get distribution ID from the distribution list
        List<Distribution> distributions = distributionService.getDistributionsByBeneficiary(beneficiary.getId());
        if (selectedRow >= distributions.size()) {
            return;
        }
        
        Distribution selectedDistribution = distributions.get(selectedRow);
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to void this distribution?\n\n" +
            "This will restore all items to inventory and cannot be undone.\n\n" +
            "Date: " + selectedDistribution.getDistributionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n" +
            "Items will be restored to inventory.",
            "Confirm Void Distribution",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = distributionService.voidDistribution(selectedDistribution.getId());
                if (success) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Distribution voided successfully. Inventory has been restored.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    loadDistributions();
                    if (onDistributionAdded != null) {
                        onDistributionAdded.run();
                    }
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Failed to void distribution. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}

