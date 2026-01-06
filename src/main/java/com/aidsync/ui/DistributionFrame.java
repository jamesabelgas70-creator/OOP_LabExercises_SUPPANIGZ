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
    private JComboBox<String> barangayFilter;
    private JComboBox<String> statusFilter;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    // ==================== Pagination ====================
    private int currentPage = 0;
    private int pageSize = 100;
    private int totalRecords = 0;
    private JButton prevButton;
    private JButton nextButton;
    private JLabel pageLabel;
    
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
        initializeFilters();
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
        
        // Add filters
        JLabel barangayLabel = new JLabel("Barangay:");
        barangayLabel.setFont(LABEL_FONT);
        barangayLabel.setForeground(LABEL_COLOR);
        searchPanel.add(barangayLabel);
        
        barangayFilter = new JComboBox<>();
        barangayFilter.setFont(LABEL_FONT);
        barangayFilter.setPreferredSize(new Dimension(120, BUTTON_HEIGHT));
        barangayFilter.addItem("All");
        barangayFilter.addActionListener(e -> performSearch());
        searchPanel.add(barangayFilter);
        
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(LABEL_FONT);
        statusLabel.setForeground(LABEL_COLOR);
        searchPanel.add(statusLabel);
        
        statusFilter = new JComboBox<>();
        statusFilter.setFont(LABEL_FONT);
        statusFilter.setPreferredSize(new Dimension(100, BUTTON_HEIGHT));
        statusFilter.addItem("All");
        statusFilter.addItem("Never Distributed");
        statusFilter.addItem("Recently Distributed");
        statusFilter.addActionListener(e -> performSearch());
        searchPanel.add(statusFilter);
        
        return searchPanel;
    }
    
    /**
     * Create refresh button panel
     */
    private JPanel createRefreshButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING_SMALL, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        JButton backButton = createActionButton("Back", BACKGROUND_COLOR, LABEL_COLOR, e -> dispose());
        refreshButton = createActionButton("Refresh", BACKGROUND_COLOR, LABEL_COLOR, e -> loadBeneficiaries());
        buttonPanel.add(backButton);
        buttonPanel.add(refreshButton);
        
        JButton batchDistributeButton = createPrimaryButton("Batch Distribute", e -> openBatchDistributeDialog());
        JButton quickDistributeButton = createPrimaryButton("Quick Distribute", e -> openQuickDistributeDialog());
        buttonPanel.add(batchDistributeButton);
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
        
        // Top panel with instruction and progress
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel instructionLabel = new JLabel("Double-click to distribute | Ctrl+Click for multi-select | Right-click for batch actions");
        instructionLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        instructionLabel.setForeground(new Color(100, 100, 100));
        instructionLabel.setBorder(new EmptyBorder(0, 0, PADDING_SMALL, 0));
        topPanel.add(instructionLabel, BorderLayout.WEST);
        
        // Progress bar and status
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        progressPanel.setBackground(BACKGROUND_COLOR);
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(LABEL_FONT);
        statusLabel.setForeground(LABEL_COLOR);
        progressPanel.add(statusLabel);
        
        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(200, 20));
        progressBar.setVisible(false);
        progressPanel.add(progressBar);
        
        topPanel.add(progressPanel, BorderLayout.EAST);
        tablePanel.add(topPanel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = createTableScrollPane();
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Pagination panel
        JPanel paginationPanel = createPaginationPanel();
        tablePanel.add(paginationPanel, BorderLayout.SOUTH);
        
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
        beneficiaryTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        beneficiaryTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        beneficiaryTable.setGridColor(BORDER_COLOR);
        beneficiaryTable.setShowGrid(true);
        beneficiaryTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Style table header
        beneficiaryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        beneficiaryTable.getTableHeader().setBackground(TABLE_HEADER_COLOR);
        beneficiaryTable.getTableHeader().setForeground(LABEL_COLOR);
        beneficiaryTable.getTableHeader().setReorderingAllowed(false);
        
        // Custom renderer for Integer columns with improved selection
        beneficiaryTable.setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Set center-left alignment (left horizontally, center vertically)
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
        });
        
        // Custom renderer for all other cells with improved selection
        beneficiaryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Set center-left alignment (left horizontally, center vertically)
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
        });
        
        // Enable row selection on click
        beneficiaryTable.setRowSelectionAllowed(true);
        beneficiaryTable.setColumnSelectionAllowed(false);
        
        // Add double-click to open distribution details and right-click for batch actions
        beneficiaryTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = beneficiaryTable.rowAtPoint(evt.getPoint());
                if (row == -1) {
                    beneficiaryTable.clearSelection();
                } else if (evt.getClickCount() == 2) {
                    openBeneficiaryDistributionDialog();
                }
            }
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    showBatchContextMenu(evt);
                }
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    showBatchContextMenu(evt);
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
     * Load beneficiaries with pagination (keeps existing method for compatibility)
     */
    private void loadBeneficiaries() {
        loadBeneficiariesPaginated(currentPage, pageSize);
    }
    
    /**
     * Load beneficiaries with pagination support
     */
    private void loadBeneficiariesPaginated(int page, int size) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                    statusLabel.setText("Loading beneficiaries...");
                });
                
                List<Beneficiary> allBeneficiaries = beneficiaryService.getAllBeneficiaries();
                totalRecords = allBeneficiaries.size();
                
                // Apply filters
                allBeneficiaries = applyFilters(allBeneficiaries);
                
                // Pagination
                int start = page * size;
                int end = Math.min(start + size, allBeneficiaries.size());
                List<Beneficiary> pageBeneficiaries = allBeneficiaries.subList(start, end);
                
                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    
                    for (Beneficiary b : pageBeneficiaries) {
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
                    
                    updatePaginationControls();
                    progressBar.setVisible(false);
                    statusLabel.setText("Showing " + pageBeneficiaries.size() + " of " + totalRecords + " beneficiaries");
                });
                
                return null;
            }
        };
        worker.execute();
    }
    
    /**
     * Perform search operation with filters
     */
    private void performSearch() {
        currentPage = 0; // Reset to first page when searching
        loadBeneficiaries();
    }
    
    /**
     * Initialize filter dropdowns
     */
    private void initializeFilters() {
        // Load barangays
        java.util.Set<String> barangays = new java.util.HashSet<>();
        List<Beneficiary> allBeneficiaries = beneficiaryService.getAllBeneficiaries();
        for (Beneficiary b : allBeneficiaries) {
            if (b.getBarangay() != null) {
                barangays.add(b.getBarangay());
            }
        }
        
        for (String barangay : barangays) {
            barangayFilter.addItem(barangay);
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
    
    // ==================== New Efficiency Methods ====================
    
    /**
     * Create pagination panel
     */
    private JPanel createPaginationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(PADDING_SMALL, 0, 0, 0));
        
        prevButton = new JButton("Previous");
        prevButton.setFont(LABEL_FONT);
        prevButton.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadBeneficiaries();
            }
        });
        
        nextButton = new JButton("Next");
        nextButton.setFont(LABEL_FONT);
        nextButton.addActionListener(e -> {
            currentPage++;
            loadBeneficiaries();
        });
        
        pageLabel = new JLabel("Page 1");
        pageLabel.setFont(LABEL_FONT);
        pageLabel.setBorder(new EmptyBorder(0, 20, 0, 20));
        
        panel.add(prevButton);
        panel.add(pageLabel);
        panel.add(nextButton);
        
        return panel;
    }
    
    /**
     * Update pagination controls
     */
    private void updatePaginationControls() {
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        pageLabel.setText("Page " + (currentPage + 1) + " of " + Math.max(1, totalPages));
        prevButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < totalPages - 1);
    }
    
    /**
     * Apply filters to beneficiary list
     */
    private List<Beneficiary> applyFilters(List<Beneficiary> beneficiaries) {
        String selectedBarangay = (String) barangayFilter.getSelectedItem();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        String searchTerm = searchField.getText().trim().toLowerCase();
        
        return beneficiaries.stream()
            .filter(b -> selectedBarangay.equals("All") || b.getBarangay().equals(selectedBarangay))
            .filter(b -> {
                if (selectedStatus.equals("All")) return true;
                if (selectedStatus.equals("Never Distributed")) {
                    com.aidsync.dao.DistributionDAO.DistributionStats stats = distributionService.getDistributionStats(b.getId());
                    return stats.getDistributionCount() == 0;
                }
                return true;
            })
            .filter(b -> searchTerm.isEmpty() || 
                b.getFullName().toLowerCase().contains(searchTerm) ||
                b.getBeneficiaryId().toLowerCase().contains(searchTerm))
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Show batch context menu
     */
    private void showBatchContextMenu(java.awt.event.MouseEvent evt) {
        int[] selectedRows = beneficiaryTable.getSelectedRows();
        if (selectedRows.length == 0) return;
        
        JPopupMenu contextMenu = new JPopupMenu();
        
        JMenuItem batchDistribute = new JMenuItem("Batch Distribute (" + selectedRows.length + ")");
        batchDistribute.addActionListener(e -> openBatchDistributeDialog());
        contextMenu.add(batchDistribute);
        
        if (selectedRows.length == 1) {
            contextMenu.addSeparator();
            JMenuItem quickDistribute = new JMenuItem("Quick Distribute");
            quickDistribute.addActionListener(e -> openQuickDistributeDialog());
            contextMenu.add(quickDistribute);
            
            JMenuItem viewHistory = new JMenuItem("View History");
            viewHistory.addActionListener(e -> openBeneficiaryDistributionDialog());
            contextMenu.add(viewHistory);
        }
        
        contextMenu.show(beneficiaryTable, evt.getX(), evt.getY());
    }
    
    /**
     * Open batch distribute dialog
     */
    private void openBatchDistributeDialog() {
        int[] selectedRows = beneficiaryTable.getSelectedRows();
        if (selectedRows.length == 0) {
            showWarningDialog("No Selection", "Please select beneficiaries for batch distribution.");
            return;
        }
        
        java.util.List<Beneficiary> selectedBeneficiaries = new java.util.ArrayList<>();
        for (int row : selectedRows) {
            String beneficiaryId = (String) tableModel.getValueAt(row, 0);
            Beneficiary beneficiary = beneficiaryService.getBeneficiaryByBeneficiaryId(beneficiaryId);
            if (beneficiary != null) {
                selectedBeneficiaries.add(beneficiary);
            }
        }
        
        if (!selectedBeneficiaries.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                BatchDistributeDialog dialog = new BatchDistributeDialog(
                    this,
                    selectedBeneficiaries,
                    distributionService,
                    inventoryService,
                    currentUser,
                    () -> loadBeneficiaries()
                );
                dialog.setVisible(true);
            });
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
