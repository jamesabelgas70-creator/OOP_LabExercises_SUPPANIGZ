package com.aidsync.ui;

import com.aidsync.model.Beneficiary;
import com.aidsync.model.User;
import com.aidsync.service.BeneficiaryService;
import com.aidsync.service.DistributionService;
import com.aidsync.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Distribution Management Screen
 * 
 * Features:
 * - Shows all beneficiaries with distribution statistics
 * - Double-click to view distribution history and add new distributions
 * - Clear, user-friendly workflow
 */
public class DistributionFrame extends JFrame {
    // ==================== UI Components ====================
    private JTable beneficiaryTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton searchButton;
    private JButton refreshButton;
    
    // ==================== Services & Data ====================
    private User currentUser;
    private BeneficiaryService beneficiaryService;
    private DistributionService distributionService;
    private InventoryService inventoryService;
    
    // ==================== Color Constants ====================
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color PRIMARY_DARK = new Color(0, 82, 164);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(68, 68, 68);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color HOVER_COLOR = new Color(245, 245, 245);
    private static final Color TABLE_HEADER_COLOR = new Color(240, 240, 240);
    
    // ==================== Font Constants ====================
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BUTTON_FONT_SECONDARY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    
    // ==================== Spacing Constants ====================
    private static final int FRAME_WIDTH = 1200;
    private static final int FRAME_HEIGHT = 700;
    private static final int PADDING_LARGE = 20;
    private static final int PADDING_MEDIUM = 15;
    private static final int PADDING_SMALL = 10;
    private static final int BUTTON_HEIGHT = 38;
    private static final int BUTTON_WIDTH = 110;
    private static final int SEARCH_FIELD_WIDTH = 300;
    
    // ==================== Constructor ====================
    public DistributionFrame(User user) {
        this.currentUser = user;
        this.beneficiaryService = new BeneficiaryService();
        this.distributionService = new DistributionService();
        this.inventoryService = new InventoryService();
        initializeUI();
        loadBeneficiaries();
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
        setTitle("AidSync - Distribution Management");
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
     * Create the header panel with search and refresh button
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(PADDING_MEDIUM, 0));
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM, 0));
        
        headerPanel.add(createSearchPanel(), BorderLayout.WEST);
        headerPanel.add(createRefreshButtonPanel(), BorderLayout.EAST);
        
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
     * Create refresh button panel
     */
    private JPanel createRefreshButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING_SMALL, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        refreshButton = createActionButton("Refresh", BACKGROUND_COLOR, LABEL_COLOR, e -> loadBeneficiaries());
        buttonPanel.add(refreshButton);
        
        JButton quickDistributeButton = createPrimaryButton("Quick Distribute", e -> openQuickDistributeDialog());
        buttonPanel.add(quickDistributeButton);
        
        return buttonPanel;
    }
    
    /**
     * Create the search button
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
        addButtonHoverEffect(button, PRIMARY_COLOR, PRIMARY_DARK);
        return button;
    }
    
    /**
     * Create a primary action button
     */
    private JButton createPrimaryButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT_SECONDARY);
        button.setPreferredSize(new Dimension(140, BUTTON_HEIGHT));
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
    
    // ==================== Table Section ====================
    
    /**
     * Create the table panel
     */
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(BACKGROUND_COLOR);
        
        // Instruction label
        JLabel instructionLabel = new JLabel("Double-click on a beneficiary to view distribution history and add new distributions");
        instructionLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        instructionLabel.setForeground(new Color(100, 100, 100));
        instructionLabel.setBorder(new EmptyBorder(0, 0, PADDING_SMALL, 0));
        tablePanel.add(instructionLabel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = createTableScrollPane();
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    /**
     * Create the table with scroll pane
     */
    private JScrollPane createTableScrollPane() {
        String[] columnNames = {"ID", "Name", "Barangay", "Purok", "Family Size", "Distributions", "Last Distribution", "Total Items"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 4 || column == 5 || column == 7) return Integer.class; // Family Size, Distributions, Total Items
                return String.class;
            }
        };
        
        beneficiaryTable = new JTable(tableModel);
        beneficiaryTable.setFont(TABLE_FONT);
        beneficiaryTable.setRowHeight(30);
        beneficiaryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        beneficiaryTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        beneficiaryTable.setGridColor(BORDER_COLOR);
        beneficiaryTable.setShowGrid(true);
        beneficiaryTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Style table header
        beneficiaryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        beneficiaryTable.getTableHeader().setBackground(TABLE_HEADER_COLOR);
        beneficiaryTable.getTableHeader().setForeground(LABEL_COLOR);
        beneficiaryTable.getTableHeader().setReorderingAllowed(false);
        
        // Custom renderer for Integer columns (to left-align numbers)
        beneficiaryTable.setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Set center-left alignment (left horizontally, center vertically)
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setVerticalAlignment(SwingConstants.CENTER);
                
                return label;
            }
        });
        
        // Custom renderer for all other cells (center-left alignment)
        beneficiaryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Set center-left alignment (left horizontally, center vertically)
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setVerticalAlignment(SwingConstants.CENTER);
                
                return label;
            }
        });
        
        // Enable row selection on click
        beneficiaryTable.setRowSelectionAllowed(true);
        beneficiaryTable.setColumnSelectionAllowed(false);
        
        // Add double-click to open distribution details
        beneficiaryTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    openBeneficiaryDistributionDialog();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(beneficiaryTable);
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
     * Load all beneficiaries with distribution statistics
     */
    private void loadBeneficiaries() {
        tableModel.setRowCount(0);
        List<Beneficiary> beneficiaries = beneficiaryService.getAllBeneficiaries();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Beneficiary b : beneficiaries) {
            // Get distribution statistics
            com.aidsync.dao.DistributionDAO.DistributionStats stats = 
                distributionService.getDistributionStats(b.getId());
            
            Object[] row = {
                b.getBeneficiaryId(),
                b.getFullName(),
                b.getBarangay(),
                b.getPurok(),
                b.getFamilySize(),
                stats.getDistributionCount(),
                stats.getLastDistributionDate() != null ? 
                    stats.getLastDistributionDate().format(formatter) : "Never",
                stats.getTotalItemsReceived()
            };
            tableModel.addRow(row);
        }
        
        // Clear search field
        searchField.setText("");
    }
    
    /**
     * Perform search operation
     */
    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        tableModel.setRowCount(0);
        
        List<Beneficiary> beneficiaries;
        if (searchTerm.isEmpty()) {
            beneficiaries = beneficiaryService.getAllBeneficiaries();
        } else {
            beneficiaries = beneficiaryService.searchBeneficiaries(searchTerm);
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Beneficiary b : beneficiaries) {
            // Get distribution statistics
            com.aidsync.dao.DistributionDAO.DistributionStats stats = 
                distributionService.getDistributionStats(b.getId());
            
            Object[] row = {
                b.getBeneficiaryId(),
                b.getFullName(),
                b.getBarangay(),
                b.getPurok(),
                b.getFamilySize(),
                stats.getDistributionCount(),
                stats.getLastDistributionDate() != null ? 
                    stats.getLastDistributionDate().format(formatter) : "Never",
                stats.getTotalItemsReceived()
            };
            tableModel.addRow(row);
        }
    }
    
    // ==================== Dialog Operations ====================
    
    /**
     * Open beneficiary distribution dialog
     */
    private void openBeneficiaryDistributionDialog() {
        int selectedRow = beneficiaryTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("No Selection", "Please select a beneficiary.");
            return;
        }
        
        String beneficiaryId = (String) tableModel.getValueAt(selectedRow, 0);
        Beneficiary beneficiary = beneficiaryService.getBeneficiaryByBeneficiaryId(beneficiaryId);
        if (beneficiary != null) {
            SwingUtilities.invokeLater(() -> {
                BeneficiaryDistributionDialog dialog = new BeneficiaryDistributionDialog(
                    this,
                    beneficiary,
                    beneficiaryService,
                    distributionService,
                    inventoryService,
                    currentUser,
                    () -> loadBeneficiaries()
                );
                dialog.setVisible(true);
            });
        } else {
            showErrorDialog("Error", "Beneficiary not found.");
        }
    }
    
    /**
     * Open quick distribute dialog for selected beneficiary
     */
    private void openQuickDistributeDialog() {
        int selectedRow = beneficiaryTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("No Selection", "Please select a beneficiary first.");
            return;
        }
        
        String beneficiaryId = (String) tableModel.getValueAt(selectedRow, 0);
        Beneficiary beneficiary = beneficiaryService.getBeneficiaryByBeneficiaryId(beneficiaryId);
        
        if (beneficiary != null) {
            SwingUtilities.invokeLater(() -> {
                // Create a temporary dialog parent for DistributionItemDialog
                JDialog tempDialog = new JDialog(this, true);
                tempDialog.setVisible(false);
                
                DistributionItemDialog dialog = new DistributionItemDialog(
                    tempDialog,
                    beneficiary,
                    distributionService,
                    inventoryService,
                    currentUser,
                    () -> loadBeneficiaries()
                );
                dialog.setVisible(true);
                tempDialog.dispose();
            });
        } else {
            showWarningDialog("Error", "Beneficiary not found.");
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
