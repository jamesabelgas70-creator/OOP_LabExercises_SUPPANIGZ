package com.aidsync.ui;

import com.aidsync.model.*;
import com.aidsync.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.SpinnerDateModel;

/**
 * Reports Frame for viewing system statistics and reports
 * 
 * Features:
 * - Summary Dashboard with overall statistics
 * - Distribution Reports with filtering
 * - Inventory Reports
 * - Beneficiary Reports
 */
public class ReportsFrame extends JFrame {
    // ==================== UI Components ====================
    private JTabbedPane tabbedPane;
    
    // Summary Dashboard
    private JLabel totalBeneficiariesLabel;
    private JLabel totalDistributionsLabel;
    private JLabel totalItemsDistributedLabel;
    private JLabel totalInventoryItemsLabel;
    private JLabel lowStockCountLabel;
    private JTable topItemsTable;
    private JTable topCalamitiesTable;
    
    // Distribution Reports
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JComboBox<String> calamityFilterComboBox;
    private JComboBox<String> barangayFilterComboBox;
    private JTable distributionTable;
    private DefaultTableModel distributionTableModel;
    
    // Inventory Reports
    private JTable inventoryTable;
    private DefaultTableModel inventoryTableModel;
    private JCheckBox lowStockOnlyCheckBox;
    
    // Beneficiary Reports
    private JTable beneficiaryTable;
    private DefaultTableModel beneficiaryTableModel;
    private JComboBox<String> beneficiaryFilterComboBox;
    
    // ==================== Services ====================
    private BeneficiaryService beneficiaryService;
    private DistributionService distributionService;
    private InventoryService inventoryService;
    private CalamityService calamityService;
    private UserService userService;
    
    // ==================== Color Constants ====================
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(68, 68, 68);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color TABLE_HEADER_COLOR = new Color(240, 240, 240);
    private static final Color STAT_CARD_COLOR = new Color(240, 248, 255);
    
    // ==================== Font Constants ====================
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font STAT_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font STAT_LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    
    // ==================== Spacing Constants ====================
    private static final int FRAME_WIDTH = 1200;
    private static final int FRAME_HEIGHT = 800;
    private static final int PADDING_LARGE = 25;
    private static final int PADDING_MEDIUM = 15;
    private static final int PADDING_SMALL = 10;
    
    // ==================== Constructor ====================
    public ReportsFrame(User user) {
        this.beneficiaryService = new BeneficiaryService();
        this.distributionService = new DistributionService();
        this.inventoryService = new InventoryService();
        this.calamityService = new CalamityService();
        this.userService = new UserService();
        initializeUI();
        loadAllReports();
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
        setTitle("AidSync - Reports");
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
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Tabbed pane with different report types
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(LABEL_FONT);
        tabbedPane.addTab("Summary Dashboard", createSummaryDashboardPanel());
        tabbedPane.addTab("Distribution Reports", createDistributionReportsPanel());
        tabbedPane.addTab("Inventory Reports", createInventoryReportsPanel());
        tabbedPane.addTab("Inventory Transactions", createInventoryTransactionsPanel());
        tabbedPane.addTab("Beneficiary Reports", createBeneficiaryReportsPanel());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    /**
     * Create header panel
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM, 0));
        
        JLabel titleLabel = new JLabel("Reports & Statistics");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(LABEL_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING_SMALL, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        JButton backButton = createActionButton("Back", BACKGROUND_COLOR, LABEL_COLOR, e -> dispose());
        buttonPanel.add(backButton);
        
        JButton refreshButton = createActionButton("Refresh", PRIMARY_COLOR, Color.WHITE, e -> loadAllReports());
        buttonPanel.add(refreshButton);
        
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    // ==================== Summary Dashboard ====================
    
    /**
     * Create summary dashboard panel
     */
    private JPanel createSummaryDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM));
        
        // Statistics cards
        JPanel statsPanel = createStatisticsCardsPanel();
        panel.add(statsPanel, BorderLayout.NORTH);
        
        // Tables panel
        JPanel tablesPanel = createSummaryTablesPanel();
        panel.add(tablesPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create statistics cards panel
     */
    private JPanel createStatisticsCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, PADDING_MEDIUM, PADDING_MEDIUM));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM, 0));
        
        totalBeneficiariesLabel = createStatCard("Total Beneficiaries", "0");
        totalDistributionsLabel = createStatCard("Total Distributions", "0");
        totalItemsDistributedLabel = createStatCard("Total Items Distributed", "0");
        totalInventoryItemsLabel = createStatCard("Inventory Items", "0");
        lowStockCountLabel = createStatCard("Low Stock Items", "0");
        
        panel.add(totalBeneficiariesLabel);
        panel.add(totalDistributionsLabel);
        panel.add(totalItemsDistributedLabel);
        panel.add(totalInventoryItemsLabel);
        panel.add(lowStockCountLabel);
        
        // Empty cell for layout
        panel.add(new JPanel());
        
        return panel;
    }
    
    /**
     * Create a statistics card
     */
    private JLabel createStatCard(String label, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(STAT_CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM)
        ));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(STAT_FONT);
        valueLabel.setForeground(PRIMARY_COLOR);
        card.add(valueLabel, BorderLayout.CENTER);
        
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(STAT_LABEL_FONT);
        labelComponent.setForeground(LABEL_COLOR);
        card.add(labelComponent, BorderLayout.SOUTH);
        
        // Wrap in a label for easy value updates
        JLabel wrapper = new JLabel();
        wrapper.setLayout(new BorderLayout());
        wrapper.add(card, BorderLayout.CENTER);
        wrapper.setName(value); // Store value in name
        return wrapper;
    }
    
    /**
     * Create summary tables panel
     */
    private JPanel createSummaryTablesPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, PADDING_MEDIUM, 0));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Top Items Table
        JPanel itemsPanel = createTablePanel("Most Distributed Items", createTopItemsTable());
        panel.add(itemsPanel);
        
        // Top Calamities Table
        JPanel calamitiesPanel = createTablePanel("Most Active Calamities", createTopCalamitiesTable());
        panel.add(calamitiesPanel);
        
        return panel;
    }
    
    /**
     * Create top items table
     */
    private JTable createTopItemsTable() {
        String[] columnNames = {"Item Name", "Category", "Total Distributed", "Unit"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        topItemsTable = new JTable(model);
        styleTable(topItemsTable);
        return topItemsTable;
    }
    
    /**
     * Create top calamities table
     */
    private JTable createTopCalamitiesTable() {
        String[] columnNames = {"Calamity/Event", "Distributions", "Total Items"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        topCalamitiesTable = new JTable(model);
        styleTable(topCalamitiesTable);
        return topCalamitiesTable;
    }
    
    /**
     * Create a table panel with title
     */
    private JPanel createTablePanel(String title, JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            title,
            0, 0,
            LABEL_FONT,
            LABEL_COLOR
        ));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ==================== Distribution Reports ====================
    
    /**
     * Create distribution reports panel
     */
    private JPanel createDistributionReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM));
        
        // Filter panel
        JPanel filterPanel = createDistributionFilterPanel();
        panel.add(filterPanel, BorderLayout.NORTH);
        
        // Table
        JPanel tablePanel = createDistributionTablePanel();
        panel.add(tablePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create distribution filter panel
     */
    private JPanel createDistributionFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING_SMALL, PADDING_SMALL));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            "Filters",
            0, 0,
            LABEL_FONT,
            LABEL_COLOR
        ));
        
        // Date range
        JLabel startDateLabel = new JLabel("Start Date:");
        startDateLabel.setFont(LABEL_FONT);
        panel.add(startDateLabel);
        
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(startEditor);
        startDateSpinner.setPreferredSize(new Dimension(150, 30));
        panel.add(startDateSpinner);
        
        JLabel endDateLabel = new JLabel("End Date:");
        endDateLabel.setFont(LABEL_FONT);
        panel.add(endDateLabel);
        
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        endDateSpinner.setEditor(endEditor);
        endDateSpinner.setPreferredSize(new Dimension(150, 30));
        panel.add(endDateSpinner);
        
        // Calamity filter
        JLabel calamityLabel = new JLabel("Calamity:");
        calamityLabel.setFont(LABEL_FONT);
        panel.add(calamityLabel);
        
        calamityFilterComboBox = new JComboBox<>();
        calamityFilterComboBox.setFont(LABEL_FONT);
        calamityFilterComboBox.setPreferredSize(new Dimension(200, 30));
        calamityFilterComboBox.addItem("All Calamities");
        panel.add(calamityFilterComboBox);
        
        // Barangay filter
        JLabel barangayLabel = new JLabel("Barangay:");
        barangayLabel.setFont(LABEL_FONT);
        panel.add(barangayLabel);
        
        barangayFilterComboBox = new JComboBox<>();
        barangayFilterComboBox.setFont(LABEL_FONT);
        barangayFilterComboBox.setPreferredSize(new Dimension(200, 30));
        barangayFilterComboBox.addItem("All Barangays");
        panel.add(barangayFilterComboBox);
        
        // Apply filter button
        JButton applyButton = createActionButton("Apply Filters", PRIMARY_COLOR, Color.WHITE, e -> loadDistributionReports());
        panel.add(applyButton);
        
        return panel;
    }
    
    /**
     * Create distribution table panel
     */
    private JPanel createDistributionTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        
        String[] columnNames = {"Date", "Beneficiary", "Barangay", "Purok", "Calamity/Event", "Items", "Total Qty", "Distributed By", "Notes"};
        distributionTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        distributionTable = new JTable(distributionTableModel);
        styleTable(distributionTable);
        
        // Set custom renderer for Items column to display HTML
        distributionTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setVerticalAlignment(SwingConstants.TOP);
                label.setVerticalTextPosition(SwingConstants.TOP);
                return label;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(distributionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ==================== Inventory Reports ====================
    
    /**
     * Create inventory reports panel
     */
    private JPanel createInventoryReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM));
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING_SMALL, PADDING_SMALL));
        filterPanel.setBackground(BACKGROUND_COLOR);
        filterPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            "Filters",
            0, 0,
            LABEL_FONT,
            LABEL_COLOR
        ));
        
        lowStockOnlyCheckBox = new JCheckBox("Show Low Stock Only");
        lowStockOnlyCheckBox.setFont(LABEL_FONT);
        lowStockOnlyCheckBox.addActionListener(e -> loadInventoryReports());
        filterPanel.add(lowStockOnlyCheckBox);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        
        // Table
        JPanel tablePanel = createInventoryTablePanel();
        panel.add(tablePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create inventory table panel
     */
    private JPanel createInventoryTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        
        String[] columnNames = {"Item Name", "Category", "Quantity", "Unit", "Low Stock Threshold", "Status"};
        inventoryTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        inventoryTable = new JTable(inventoryTableModel);
        styleTable(inventoryTable);
        
        // Custom renderer for low stock highlighting
        inventoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String status = (String) inventoryTableModel.getValueAt(row, 5);
                    if ("Low Stock".equals(status)) {
                        label.setBackground(new Color(255, 240, 240));
                        label.setForeground(new Color(200, 0, 0));
                    } else {
                        label.setBackground(BACKGROUND_COLOR);
                        label.setForeground(LABEL_COLOR);
                    }
                }
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setVerticalAlignment(SwingConstants.CENTER);
                return label;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ==================== Inventory Transactions ====================
    
    /**
     * Create inventory transactions panel
     */
    private JPanel createInventoryTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM));
        
        JLabel infoLabel = new JLabel("<html><b>Inventory Transaction History</b><br><br>" +
            "Click the button below to open the full transaction history viewer with advanced filtering options.</html>");
        infoLabel.setFont(LABEL_FONT);
        infoLabel.setForeground(LABEL_COLOR);
        infoLabel.setBorder(new EmptyBorder(PADDING_MEDIUM, 0, PADDING_MEDIUM, 0));
        
        JButton openButton = new JButton("Open Transaction History");
        openButton.setFont(LABEL_FONT);
        openButton.setBackground(PRIMARY_COLOR);
        openButton.setForeground(Color.WHITE);
        openButton.setBorderPainted(false);
        openButton.setFocusPainted(false);
        openButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        openButton.setPreferredSize(new Dimension(250, 40));
        openButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                new InventoryTransactionFrame(null).setVisible(true);
            });
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(openButton);
        
        panel.add(infoLabel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ==================== Beneficiary Reports ====================
    
    /**
     * Create beneficiary reports panel
     */
    private JPanel createBeneficiaryReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM));
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING_SMALL, PADDING_SMALL));
        filterPanel.setBackground(BACKGROUND_COLOR);
        filterPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            "Filters",
            0, 0,
            LABEL_FONT,
            LABEL_COLOR
        ));
        
        JLabel barangayLabel = new JLabel("Barangay:");
        barangayLabel.setFont(LABEL_FONT);
        filterPanel.add(barangayLabel);
        
        beneficiaryFilterComboBox = new JComboBox<>();
        beneficiaryFilterComboBox.setFont(LABEL_FONT);
        beneficiaryFilterComboBox.setPreferredSize(new Dimension(200, 30));
        beneficiaryFilterComboBox.addItem("All Barangays");
        beneficiaryFilterComboBox.addActionListener(e -> loadBeneficiaryReports());
        filterPanel.add(beneficiaryFilterComboBox);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        
        // Table
        JPanel tablePanel = createBeneficiaryTablePanel();
        panel.add(tablePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create beneficiary table panel
     */
    private JPanel createBeneficiaryTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        
        String[] columnNames = {"ID", "Name", "Barangay", "Purok", "Family Size", "Distributions", "Last Distribution", "Total Items"};
        beneficiaryTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        beneficiaryTable = new JTable(beneficiaryTableModel);
        styleTable(beneficiaryTable);
        
        JScrollPane scrollPane = new JScrollPane(beneficiaryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Style a table
     */
    private void styleTable(JTable table) {
        table.setFont(TABLE_FONT);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setGridColor(BORDER_COLOR);
        table.setShowGrid(true);
        
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(TABLE_HEADER_COLOR);
        table.getTableHeader().setForeground(LABEL_COLOR);
        table.getTableHeader().setReorderingAllowed(false);
        
        // Enhanced renderer with improved selection colors
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setVerticalAlignment(SwingConstants.CENTER);
                
                // Enhanced selection colors
                if (isSelected) {
                    label.setBackground(new Color(0, 102, 204, 40)); // Semi-transparent blue
                    label.setForeground(PRIMARY_COLOR);
                } else {
                    label.setBackground(BACKGROUND_COLOR);
                    label.setForeground(LABEL_COLOR);
                }
                
                return label;
            }
        };
        
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(Integer.class, renderer);
        
        // Add deselect on empty space functionality
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.rowAtPoint(evt.getPoint());
                if (row == -1) {
                    // Clicked on empty space - clear selection
                    table.clearSelection();
                }
            }
        });
    }
    
    /**
     * Create an action button
     */
    private JButton createActionButton(String text, Color bgColor, Color fgColor, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(LABEL_FONT);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(listener);
        button.setPreferredSize(new Dimension(120, 35));
        return button;
    }
    
    // ==================== Data Loading ====================
    
    /**
     * Load all reports
     */
    private void loadAllReports() {
        loadSummaryDashboard();
        loadDistributionReports();
        loadInventoryReports();
        loadBeneficiaryReports();
    }
    
    /**
     * Load summary dashboard data
     */
    private void loadSummaryDashboard() {
        // Total beneficiaries
        List<Beneficiary> beneficiaries = beneficiaryService.getAllBeneficiaries();
        updateStatCard(totalBeneficiariesLabel, String.valueOf(beneficiaries.size()));
        
        // Total distributions
        List<Distribution> distributions = distributionService.getAllDistributions();
        updateStatCard(totalDistributionsLabel, String.valueOf(distributions.size()));
        
        // Total items distributed
        int totalItems = 0;
        for (Distribution dist : distributions) {
            for (DistributionItem item : dist.getItems()) {
                totalItems += item.getQuantity();
            }
        }
        updateStatCard(totalItemsDistributedLabel, String.valueOf(totalItems));
        
        // Inventory items
        List<InventoryItem> inventoryItems = inventoryService.getAllInventoryItems();
        updateStatCard(totalInventoryItemsLabel, String.valueOf(inventoryItems.size()));
        
        // Low stock items
        List<InventoryItem> lowStockItems = inventoryService.getLowStockItems();
        updateStatCard(lowStockCountLabel, String.valueOf(lowStockItems.size()));
        
        // Top items
        loadTopItems(distributions);
        
        // Top calamities
        loadTopCalamities(distributions);
    }
    
    /**
     * Update stat card value
     */
    private void updateStatCard(JLabel card, String value) {
        Component[] components = ((JPanel) card.getComponent(0)).getComponents();
        if (components.length > 0 && components[0] instanceof JLabel) {
            ((JLabel) components[0]).setText(value);
        }
        card.setName(value);
    }
    
    /**
     * Load top items
     */
    private void loadTopItems(List<Distribution> distributions) {
        DefaultTableModel model = (DefaultTableModel) topItemsTable.getModel();
        model.setRowCount(0);
        
        Map<Integer, Integer> itemCounts = new HashMap<>();
        Map<Integer, InventoryItem> itemMap = new HashMap<>();
        
        for (Distribution dist : distributions) {
            for (DistributionItem di : dist.getItems()) {
                itemCounts.put(di.getInventoryId(), 
                    itemCounts.getOrDefault(di.getInventoryId(), 0) + di.getQuantity());
                if (!itemMap.containsKey(di.getInventoryId())) {
                    itemMap.put(di.getInventoryId(), inventoryService.getInventoryItemById(di.getInventoryId()));
                }
            }
        }
        
        List<Map.Entry<Integer, Integer>> sorted = itemCounts.entrySet().stream()
            .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toList());
        
        for (Map.Entry<Integer, Integer> entry : sorted) {
            InventoryItem item = itemMap.get(entry.getKey());
            if (item != null) {
                model.addRow(new Object[]{
                    item.getItemName(),
                    item.getCategory(),
                    entry.getValue(),
                    item.getUnit()
                });
            }
        }
    }
    
    /**
     * Load top calamities
     */
    private void loadTopCalamities(List<Distribution> distributions) {
        DefaultTableModel model = (DefaultTableModel) topCalamitiesTable.getModel();
        model.setRowCount(0);
        
        Map<Integer, Integer> calamityCounts = new HashMap<>();
        Map<Integer, Integer> calamityItemCounts = new HashMap<>();
        Map<Integer, Calamity> calamityMap = new HashMap<>();
        
        for (Distribution dist : distributions) {
            if (dist.getCalamityId() != null) {
                calamityCounts.put(dist.getCalamityId(), 
                    calamityCounts.getOrDefault(dist.getCalamityId(), 0) + 1);
                
                int itemCount = dist.getItems().stream()
                    .mapToInt(DistributionItem::getQuantity)
                    .sum();
                calamityItemCounts.put(dist.getCalamityId(),
                    calamityItemCounts.getOrDefault(dist.getCalamityId(), 0) + itemCount);
                
                if (!calamityMap.containsKey(dist.getCalamityId())) {
                    calamityMap.put(dist.getCalamityId(), calamityService.getCalamityById(dist.getCalamityId()));
                }
            }
        }
        
        List<Map.Entry<Integer, Integer>> sorted = calamityCounts.entrySet().stream()
            .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toList());
        
        for (Map.Entry<Integer, Integer> entry : sorted) {
            Calamity calamity = calamityMap.get(entry.getKey());
            if (calamity != null) {
                model.addRow(new Object[]{
                    calamity.getName(),
                    entry.getValue(),
                    calamityItemCounts.getOrDefault(entry.getKey(), 0)
                });
            }
        }
    }
    
    /**
     * Load distribution reports
     */
    private void loadDistributionReports() {
        distributionTableModel.setRowCount(0);
        
        List<Distribution> distributions = distributionService.getAllDistributions();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        // Apply filters
        Date startDate = startDateSpinner != null ? ((SpinnerDateModel) startDateSpinner.getModel()).getDate() : null;
        Date endDate = endDateSpinner != null ? ((SpinnerDateModel) endDateSpinner.getModel()).getDate() : null;
        String selectedCalamity = (String) (calamityFilterComboBox != null ? calamityFilterComboBox.getSelectedItem() : null);
        String selectedBarangay = (String) (barangayFilterComboBox != null ? barangayFilterComboBox.getSelectedItem() : null);
        
        for (Distribution dist : distributions) {
            // Date filter
            if (startDate != null) {
                LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (dist.getDistributionDate().toLocalDate().isBefore(startLocalDate)) {
                    continue;
                }
            }
            if (endDate != null) {
                LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (dist.getDistributionDate().toLocalDate().isAfter(endLocalDate)) {
                    continue;
                }
            }
            
            // Calamity filter
            if (selectedCalamity != null && !selectedCalamity.equals("All Calamities")) {
                Calamity calamity = dist.getCalamityId() != null ? calamityService.getCalamityById(dist.getCalamityId()) : null;
                if (calamity == null || !calamity.getName().equals(selectedCalamity)) {
                    continue;
                }
            }
            
            // Get beneficiary info
            Beneficiary beneficiary = beneficiaryService.getBeneficiaryById(dist.getBeneficiaryId());
            if (beneficiary == null) continue;
            
            // Barangay filter
            if (selectedBarangay != null && !selectedBarangay.equals("All Barangays")) {
                if (!beneficiary.getBarangay().equals(selectedBarangay)) {
                    continue;
                }
            }
            
            // Build items string
            StringBuilder itemsStr = new StringBuilder("<html>");
            int totalQuantity = 0;
            int itemCount = 0;
            for (DistributionItem item : dist.getItems()) {
                InventoryItem invItem = inventoryService.getInventoryItemById(item.getInventoryId());
                if (invItem != null) {
                    if (itemCount > 0) itemsStr.append("<br>");
                    itemsStr.append("• ").append(invItem.getItemName())
                             .append(" (").append(item.getQuantity())
                             .append(" ").append(invItem.getUnit()).append(")");
                    totalQuantity += item.getQuantity();
                    itemCount++;
                }
            }
            itemsStr.append("</html>");
            String itemsDisplay = itemCount > 0 ? itemsStr.toString() : "<html>No items</html>";
            
            // Get user name
            String distributedByName = getUserDisplayName(dist.getDistributedBy());
            
            // Get calamity name
            String calamityName = "-";
            if (dist.getCalamityId() != null) {
                Calamity calamity = calamityService.getCalamityById(dist.getCalamityId());
                if (calamity != null) {
                    calamityName = calamity.getName();
                }
            }
            
            String notes = dist.getNotes() != null && !dist.getNotes().trim().isEmpty() ? dist.getNotes() : "-";
            
            distributionTableModel.addRow(new Object[]{
                dist.getDistributionDate().format(formatter),
                beneficiary.getFullName(),
                beneficiary.getBarangay(),
                beneficiary.getPurok(),
                calamityName,
                itemsDisplay,
                totalQuantity,
                distributedByName,
                notes
            });
            
            // Adjust row height for items
            int rowIndex = distributionTableModel.getRowCount() - 1;
            int baseHeight = 30;
            int itemHeight = 20;
            int calculatedHeight = baseHeight + (itemCount * itemHeight);
            distributionTable.setRowHeight(rowIndex, Math.max(calculatedHeight, baseHeight));
        }
    }
    
    /**
     * Load inventory reports
     */
    private void loadInventoryReports() {
        inventoryTableModel.setRowCount(0);
        
        List<InventoryItem> items = inventoryService.getAllInventoryItems();
        boolean lowStockOnly = lowStockOnlyCheckBox != null && lowStockOnlyCheckBox.isSelected();
        
        for (InventoryItem item : items) {
            if (lowStockOnly && item.getQuantity() >= item.getLowStockThreshold()) {
                continue;
            }
            
            String status = item.getQuantity() <= item.getLowStockThreshold() ? "Low Stock" : "In Stock";
            
            inventoryTableModel.addRow(new Object[]{
                item.getItemName(),
                item.getCategory(),
                item.getQuantity(),
                item.getUnit(),
                item.getLowStockThreshold(),
                status
            });
        }
    }
    
    /**
     * Load beneficiary reports
     */
    private void loadBeneficiaryReports() {
        beneficiaryTableModel.setRowCount(0);
        
        List<Beneficiary> beneficiaries = beneficiaryService.getAllBeneficiaries();
        String selectedBarangay = (String) (beneficiaryFilterComboBox != null ? beneficiaryFilterComboBox.getSelectedItem() : null);
        
        for (Beneficiary beneficiary : beneficiaries) {
            if (selectedBarangay != null && !selectedBarangay.equals("All Barangays")) {
                if (!beneficiary.getBarangay().equals(selectedBarangay)) {
                    continue;
                }
            }
            
            com.aidsync.dao.DistributionDAO.DistributionStats stats = distributionService.getDistributionStats(beneficiary.getId());
            String lastDist = stats.getLastDistributionDate() != null ? 
                stats.getLastDistributionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "Never";
            
            beneficiaryTableModel.addRow(new Object[]{
                beneficiary.getBeneficiaryId(),
                beneficiary.getFullName(),
                beneficiary.getBarangay(),
                beneficiary.getPurok(),
                beneficiary.getFamilySize(),
                stats.getDistributionCount(),
                lastDist,
                stats.getTotalItemsReceived()
            });
        }
    }
    
    /**
     * Get user display name
     */
    private String getUserDisplayName(int userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            if (user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
                return user.getFullName();
            }
            return user.getUsername();
        }
        return "User ID: " + userId;
    }
    
    /**
     * Initialize filter dropdowns
     */
    private void initializeFilters() {
        // Load calamities
        if (calamityFilterComboBox != null) {
            List<Calamity> calamities = calamityService.getAllCalamities();
            for (Calamity calamity : calamities) {
                if (calamity.getStatus().equals("Active")) {
                    calamityFilterComboBox.addItem(calamity.getName());
                }
            }
        }
        
        // Load barangays
        if (barangayFilterComboBox != null || beneficiaryFilterComboBox != null) {
            List<String> barangays = com.aidsync.util.BarangayData.getAllBarangays();
            if (barangayFilterComboBox != null) {
                for (String barangay : barangays) {
                    barangayFilterComboBox.addItem(barangay);
                }
            }
            if (beneficiaryFilterComboBox != null) {
                for (String barangay : barangays) {
                    beneficiaryFilterComboBox.addItem(barangay);
                }
            }
        }
    }
    
    // Override setVisible to initialize filters when frame is shown
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            SwingUtilities.invokeLater(() -> {
                initializeFilters();
                loadAllReports();
            });
        }
    }
}

