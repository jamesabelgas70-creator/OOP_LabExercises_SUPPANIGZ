package com.aidsync.ui;

import com.aidsync.model.Beneficiary;
import com.aidsync.model.FilterCriteria;
import com.aidsync.model.User;
import com.aidsync.service.BeneficiaryService;
import com.aidsync.util.BarangayData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Beneficiary Management Screen
 * 
 * Features:
 * - Modern, clean UI matching application design
 * - Search functionality
 * - CRUD operations for beneficiaries
 * - Table view with sorting
 * - Responsive button interactions
 */
public class BeneficiaryManagementFrame extends JFrame {
    // ==================== UI Components ====================
    private JTable beneficiaryTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton searchButton;
    private JButton toggleFilterButton;
    private JPanel filterPanel;
    private boolean filterPanelVisible = false;
    
    // Filter components
    private JComboBox<String> barangayFilter;
    private JComboBox<String> statusFilter;
    private JComboBox<String> genderFilter;
    private JCheckBox pwdFilter;
    private JCheckBox seniorFilter;
    private JCheckBox pregnantFilter;
    private JCheckBox soloParentFilter;
    private JSpinner minFamilySizeFilter;
    private JSpinner maxFamilySizeFilter;
    
    // ==================== Services & Data ====================
    private BeneficiaryService beneficiaryService;
    private User currentUser;
    
    // ==================== Color Constants ====================
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color PRIMARY_DARK = new Color(0, 82, 164);
    private static final Color DELETE_COLOR = new Color(220, 53, 69);
    private static final Color DELETE_DARK = new Color(200, 33, 49);
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
    public BeneficiaryManagementFrame(User user) {
        this.beneficiaryService = new BeneficiaryService();
        this.currentUser = user;
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
        setTitle("AidSync - Beneficiary Management");
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
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BACKGROUND_COLOR);
        topPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        
        filterPanel = createFilterPanel();
        filterPanel.setVisible(false);
        topPanel.add(filterPanel, BorderLayout.CENTER);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
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
     */
    private JPanel createActionButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING_SMALL, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        toggleFilterButton = createActionButton("Filters", BACKGROUND_COLOR, LABEL_COLOR, e -> toggleFilterPanel());
        addButton = createActionButton("Add New", PRIMARY_COLOR, Color.WHITE, e -> openAddEditDialog(null));
        editButton = createActionButton("Edit", PRIMARY_COLOR, Color.WHITE, e -> editSelectedBeneficiary());
        deleteButton = createActionButton("Delete", DELETE_COLOR, Color.WHITE, e -> deleteSelectedBeneficiary());
        refreshButton = createActionButton("Refresh", BACKGROUND_COLOR, LABEL_COLOR, e -> loadBeneficiaries());
        
        buttonPanel.add(toggleFilterButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        
        return buttonPanel;
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
        
        // Add hover effect
        if (bgColor.equals(PRIMARY_COLOR)) {
            addButtonHoverEffect(button, PRIMARY_COLOR, PRIMARY_DARK);
        } else if (bgColor.equals(DELETE_COLOR)) {
            addButtonHoverEffect(button, DELETE_COLOR, DELETE_DARK);
        } else {
            addButtonHoverEffect(button, BACKGROUND_COLOR, HOVER_COLOR);
        }
        
        return button;
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
    
    // ==================== Filter Panel ====================
    
    /**
     * Create the filter panel
     */
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM)
        ));
        
        JPanel filtersGrid = new JPanel(new GridLayout(2, 4, PADDING_SMALL, PADDING_SMALL));
        filtersGrid.setBackground(BACKGROUND_COLOR);
        
        // Row 1
        filtersGrid.add(createBarangayFilterPanel());
        filtersGrid.add(createStatusFilterPanel());
        filtersGrid.add(createGenderFilterPanel());
        filtersGrid.add(createFamilySizeFilterPanel());
        
        // Row 2
        filtersGrid.add(createSpecialConditionsPanel());
        filtersGrid.add(new JPanel()); // Empty
        filtersGrid.add(new JPanel()); // Empty
        filtersGrid.add(createFilterButtonsPanel());
        
        panel.add(filtersGrid, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createBarangayFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(BACKGROUND_COLOR);
        
        JLabel label = new JLabel("Barangay:");
        label.setFont(LABEL_FONT);
        label.setForeground(LABEL_COLOR);
        
        // For staff users, only show their assigned barangay
        if (currentUser.isStaff() && currentUser.getBarangay() != null) {
            barangayFilter = new JComboBox<>(new String[]{currentUser.getBarangay()});
            barangayFilter.setEnabled(false);
        } else {
            String[] barangays = new String[BarangayData.getAllBarangays().size() + 1];
            barangays[0] = "All";
            System.arraycopy(BarangayData.getAllBarangays().toArray(), 0, barangays, 1, BarangayData.getAllBarangays().size());
            barangayFilter = new JComboBox<>(barangays);
        }
        
        barangayFilter.setFont(INPUT_FONT);
        
        panel.add(label, BorderLayout.WEST);
        panel.add(barangayFilter, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createStatusFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(BACKGROUND_COLOR);
        
        JLabel label = new JLabel("Status:");
        label.setFont(LABEL_FONT);
        label.setForeground(LABEL_COLOR);
        
        statusFilter = new JComboBox<>(new String[]{"All", "Active", "Inactive"});
        statusFilter.setFont(INPUT_FONT);
        
        panel.add(label, BorderLayout.WEST);
        panel.add(statusFilter, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createGenderFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(BACKGROUND_COLOR);
        
        JLabel label = new JLabel("Gender:");
        label.setFont(LABEL_FONT);
        label.setForeground(LABEL_COLOR);
        
        genderFilter = new JComboBox<>(new String[]{"All", "Male", "Female", "Prefer not to say"});
        genderFilter.setFont(INPUT_FONT);
        
        panel.add(label, BorderLayout.WEST);
        panel.add(genderFilter, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createFamilySizeFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(BACKGROUND_COLOR);
        
        JLabel label = new JLabel("Family Size:");
        label.setFont(LABEL_FONT);
        label.setForeground(LABEL_COLOR);
        
        JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        sizePanel.setBackground(BACKGROUND_COLOR);
        
        minFamilySizeFilter = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        maxFamilySizeFilter = new JSpinner(new SpinnerNumberModel(20, 1, 20, 1));
        
        minFamilySizeFilter.setPreferredSize(new Dimension(50, 25));
        maxFamilySizeFilter.setPreferredSize(new Dimension(50, 25));
        
        sizePanel.add(minFamilySizeFilter);
        sizePanel.add(new JLabel(" - "));
        sizePanel.add(maxFamilySizeFilter);
        
        panel.add(label, BorderLayout.WEST);
        panel.add(sizePanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createSpecialConditionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        
        JLabel label = new JLabel("Special:");
        label.setFont(LABEL_FONT);
        label.setForeground(LABEL_COLOR);
        
        JPanel checkPanel = new JPanel(new GridLayout(2, 2, 2, 2));
        checkPanel.setBackground(BACKGROUND_COLOR);
        
        pwdFilter = new JCheckBox("PWD");
        seniorFilter = new JCheckBox("Senior");
        pregnantFilter = new JCheckBox("Pregnant");
        soloParentFilter = new JCheckBox("Solo Parent");
        
        pwdFilter.setBackground(BACKGROUND_COLOR);
        seniorFilter.setBackground(BACKGROUND_COLOR);
        pregnantFilter.setBackground(BACKGROUND_COLOR);
        soloParentFilter.setBackground(BACKGROUND_COLOR);
        
        checkPanel.add(pwdFilter);
        checkPanel.add(seniorFilter);
        checkPanel.add(pregnantFilter);
        checkPanel.add(soloParentFilter);
        
        panel.add(label, BorderLayout.WEST);
        panel.add(checkPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createFilterButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panel.setBackground(BACKGROUND_COLOR);
        
        JButton applyButton = new JButton("Apply");
        applyButton.setFont(BUTTON_FONT_SECONDARY);
        applyButton.setBackground(PRIMARY_COLOR);
        applyButton.setForeground(Color.WHITE);
        applyButton.setBorderPainted(false);
        applyButton.setFocusPainted(false);
        applyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        applyButton.addActionListener(e -> applyFilters());
        
        JButton clearButton = new JButton("Clear");
        clearButton.setFont(BUTTON_FONT_SECONDARY);
        clearButton.setBackground(BACKGROUND_COLOR);
        clearButton.setForeground(LABEL_COLOR);
        clearButton.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        clearButton.setFocusPainted(false);
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.addActionListener(e -> clearFilters());
        
        panel.add(clearButton);
        panel.add(applyButton);
        return panel;
    }
    
    private void toggleFilterPanel() {
        filterPanelVisible = !filterPanelVisible;
        filterPanel.setVisible(filterPanelVisible);
        toggleFilterButton.setText(filterPanelVisible ? "Hide Filters" : "Filters");
        revalidate();
        repaint();
    }
    
    private void applyFilters() {
        FilterCriteria criteria = new FilterCriteria();
        
        if (!"All".equals(barangayFilter.getSelectedItem())) {
            criteria.setBarangay((String) barangayFilter.getSelectedItem());
        }
        if (!"All".equals(statusFilter.getSelectedItem())) {
            criteria.setStatus((String) statusFilter.getSelectedItem());
        }
        if (!"All".equals(genderFilter.getSelectedItem())) {
            criteria.setGender((String) genderFilter.getSelectedItem());
        }
        
        if (pwdFilter.isSelected()) criteria.setIsPwd(true);
        if (seniorFilter.isSelected()) criteria.setIsSeniorCitizen(true);
        if (pregnantFilter.isSelected()) criteria.setIsPregnant(true);
        if (soloParentFilter.isSelected()) criteria.setIsSoloParent(true);
        
        int minSize = (Integer) minFamilySizeFilter.getValue();
        int maxSize = (Integer) maxFamilySizeFilter.getValue();
        if (minSize > 1) criteria.setMinFamilySize(minSize);
        if (maxSize < 20) criteria.setMaxFamilySize(maxSize);
        
        loadBeneficiariesWithFilter(criteria);
    }
    
    private void clearFilters() {
        barangayFilter.setSelectedIndex(0);
        statusFilter.setSelectedIndex(0);
        genderFilter.setSelectedIndex(0);
        pwdFilter.setSelected(false);
        seniorFilter.setSelected(false);
        pregnantFilter.setSelected(false);
        soloParentFilter.setSelected(false);
        minFamilySizeFilter.setValue(1);
        maxFamilySizeFilter.setValue(20);
        loadBeneficiaries();
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
        String[] columnNames = {"ID", "Name", "Barangay", "Purok", "Family Size", "Status", "Date Registered"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 4) return Integer.class; // Family Size
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
        
        // Add double-click to edit
        beneficiaryTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editSelectedBeneficiary();
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
     * Load all beneficiaries into the table
     */
    private void loadBeneficiaries() {
        // For staff users, automatically filter by their barangay
        if (currentUser.isStaff() && currentUser.getBarangay() != null) {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setBarangay(currentUser.getBarangay());
            loadBeneficiariesWithFilter(criteria, null);
        } else {
            tableModel.setRowCount(0);
            List<Beneficiary> beneficiaries = beneficiaryService.getAllBeneficiaries();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            for (Beneficiary b : beneficiaries) {
                Object[] row = {
                    b.getBeneficiaryId(),
                    b.getFullName(),
                    b.getBarangay(),
                    b.getPurok(),
                    b.getFamilySize(),
                    b.getStatus(),
                    b.getDateRegistered() != null ? b.getDateRegistered().format(formatter) : ""
                };
                tableModel.addRow(row);
            }
            
            // Clear search field
            searchField.setText("");
        }
    }
    
    /**
     * Perform search operation
     */
    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        loadBeneficiariesWithFilter(null, searchTerm);
    }
    
    private void loadBeneficiariesWithFilter(FilterCriteria criteria) {
        loadBeneficiariesWithFilter(criteria, searchField.getText().trim());
    }
    
    private void loadBeneficiariesWithFilter(FilterCriteria criteria, String searchTerm) {
        tableModel.setRowCount(0);
        
        List<Beneficiary> beneficiaries = beneficiaryService.filterBeneficiaries(criteria, 
            searchTerm.isEmpty() ? null : searchTerm);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Beneficiary b : beneficiaries) {
            Object[] row = {
                b.getBeneficiaryId(),
                b.getFullName(),
                b.getBarangay(),
                b.getPurok(),
                b.getFamilySize(),
                b.getStatus(),
                b.getDateRegistered() != null ? b.getDateRegistered().format(formatter) : ""
            };
            tableModel.addRow(row);
        }
    }
    
    // ==================== Dialog Operations ====================
    
    /**
     * Open add/edit dialog
     */
    private void openAddEditDialog(Beneficiary beneficiary) {
        SwingUtilities.invokeLater(() -> {
            BeneficiaryDialog dialog = new BeneficiaryDialog(this, beneficiaryService, beneficiary, currentUser, () -> {
                loadBeneficiaries();
            });
            dialog.setVisible(true);
        });
    }
    
    /**
     * Edit selected beneficiary
     */
    private void editSelectedBeneficiary() {
        int selectedRow = beneficiaryTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("No Selection", "Please select a beneficiary to edit.");
            return;
        }
        
        String beneficiaryId = (String) tableModel.getValueAt(selectedRow, 0);
        Beneficiary beneficiary = beneficiaryService.getBeneficiaryByBeneficiaryId(beneficiaryId);
        if (beneficiary != null) {
            openAddEditDialog(beneficiary);
        } else {
            showErrorDialog("Error", "Beneficiary not found.");
        }
    }
    
    /**
     * Delete selected beneficiary
     */
    private void deleteSelectedBeneficiary() {
        int selectedRow = beneficiaryTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("No Selection", "Please select a beneficiary to delete.");
            return;
        }
        
        String beneficiaryId = (String) tableModel.getValueAt(selectedRow, 0);
        String beneficiaryName = (String) tableModel.getValueAt(selectedRow, 1);
        
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this beneficiary?\n\n" +
            "Beneficiary: " + beneficiaryName + "\n" +
            "ID: " + beneficiaryId + "\n\n" +
            "The record will be moved to recycle bin for 30 days.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            Beneficiary beneficiary = beneficiaryService.getBeneficiaryByBeneficiaryId(beneficiaryId);
            if (beneficiary != null) {
                if (beneficiaryService.deleteBeneficiary(beneficiary.getId(), currentUser.getUsername())) {
                    showSuccessDialog("Success", "Beneficiary deleted successfully.");
                    loadBeneficiaries();
                } else {
                    showErrorDialog("Error", "Failed to delete beneficiary.");
                }
            } else {
                showErrorDialog("Error", "Beneficiary not found.");
            }
        }
    }
    
    // ==================== Dialog Helpers ====================
    
    /**
     * Show a success dialog
     */
    private void showSuccessDialog(String title, String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            title,
            JOptionPane.INFORMATION_MESSAGE
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
}
