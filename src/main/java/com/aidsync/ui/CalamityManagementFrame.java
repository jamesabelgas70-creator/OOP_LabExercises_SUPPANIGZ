package com.aidsync.ui;

import com.aidsync.model.Calamity;
import com.aidsync.model.User;
import com.aidsync.service.CalamityService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Calamity Management Screen for Admin
 * 
 * Features:
 * - Modern, clean UI matching application design
 * - Search functionality
 * - CRUD operations for calamities
 * - Double-click to edit
 * - Responsive button interactions
 */
public class CalamityManagementFrame extends JFrame {
    // ==================== UI Components ====================
    private JTable calamityTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton searchButton;
    
    // ==================== Services & Data ====================
    private CalamityService calamityService;
    
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
    private static final int FRAME_WIDTH = 1300;
    private static final int FRAME_HEIGHT = 700;
    private static final int PADDING_LARGE = 20;
    private static final int PADDING_MEDIUM = 15;
    private static final int PADDING_SMALL = 10;
    private static final int BUTTON_HEIGHT = 38;
    private static final int BUTTON_WIDTH = 110;
    private static final int SEARCH_FIELD_WIDTH = 300;
    
    // ==================== Constructor ====================
    public CalamityManagementFrame(User user) {
        this.calamityService = new CalamityService();
        initializeUI();
        loadCalamities();
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
        setTitle("AidSync - Calamity Management");
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
        
        refreshButton = createActionButton("Refresh", BACKGROUND_COLOR, LABEL_COLOR, e -> loadCalamities());
        editButton = createActionButton("Edit", PRIMARY_COLOR, Color.WHITE, e -> editSelectedCalamity());
        deleteButton = createActionButton("Delete", DELETE_COLOR, Color.WHITE, e -> deleteSelectedCalamity());
        addButton = createActionButton("Add New", PRIMARY_COLOR, Color.WHITE, e -> openAddEditDialog(null));
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(addButton);
        
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
        String[] columnNames = {"ID", "Name", "Description", "Status", "Items Count", "Created At"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0 || column == 4) return Integer.class; // ID and Items Count
                return String.class;
            }
        };
        
        calamityTable = new JTable(tableModel);
        calamityTable.setFont(TABLE_FONT);
        calamityTable.setRowHeight(30);
        calamityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        calamityTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        calamityTable.setGridColor(BORDER_COLOR);
        calamityTable.setShowGrid(true);
        calamityTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Style table header
        calamityTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        calamityTable.getTableHeader().setBackground(TABLE_HEADER_COLOR);
        calamityTable.getTableHeader().setForeground(LABEL_COLOR);
        calamityTable.getTableHeader().setReorderingAllowed(false);
        
        // Custom renderer for Integer columns (to left-align numbers)
        calamityTable.setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {
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
        calamityTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
        calamityTable.setRowSelectionAllowed(true);
        calamityTable.setColumnSelectionAllowed(false);
        
        // Add double-click to edit
        calamityTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editSelectedCalamity();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(calamityTable);
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
     * Load all calamities into the table
     */
    private void loadCalamities() {
        tableModel.setRowCount(0);
        List<Calamity> calamities = calamityService.getAllCalamities();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (Calamity c : calamities) {
            Object[] row = {
                c.getId(),
                c.getName(),
                c.getDescription() != null && !c.getDescription().trim().isEmpty() ? c.getDescription() : "-",
                c.getStatus(),
                c.getItems() != null ? c.getItems().size() : 0,
                c.getCreatedAt() != null ? c.getCreatedAt().format(formatter) : ""
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
        String searchTerm = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        
        List<Calamity> calamities = calamityService.getAllCalamities();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (Calamity c : calamities) {
            // Search in name, description, and status
            boolean matches = searchTerm.isEmpty() ||
                (c.getName() != null && c.getName().toLowerCase().contains(searchTerm)) ||
                (c.getDescription() != null && c.getDescription().toLowerCase().contains(searchTerm)) ||
                (c.getStatus() != null && c.getStatus().toLowerCase().contains(searchTerm));
            
            if (matches) {
                Object[] row = {
                    c.getId(),
                    c.getName(),
                    c.getDescription() != null && !c.getDescription().trim().isEmpty() ? c.getDescription() : "-",
                    c.getStatus(),
                    c.getItems() != null ? c.getItems().size() : 0,
                    c.getCreatedAt() != null ? c.getCreatedAt().format(formatter) : ""
                };
                tableModel.addRow(row);
            }
        }
    }
    
    // ==================== Dialog Operations ====================
    
    /**
     * Open add/edit dialog
     */
    private void openAddEditDialog(Calamity calamity) {
        SwingUtilities.invokeLater(() -> {
            CalamityDialog dialog = new CalamityDialog(this, calamityService, calamity, () -> {
                loadCalamities();
            });
            dialog.setVisible(true);
        });
    }
    
    /**
     * Edit selected calamity
     */
    private void editSelectedCalamity() {
        int selectedRow = calamityTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("No Selection", "Please select a calamity to edit, or double-click on a calamity.");
            return;
        }
        
        int calamityId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Calamity calamity = calamityService.getCalamityById(calamityId);
        if (calamity != null) {
            openAddEditDialog(calamity);
        } else {
            showErrorDialog("Error", "Calamity not found.");
        }
    }
    
    /**
     * Delete selected calamity
     */
    private void deleteSelectedCalamity() {
        int selectedRow = calamityTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("No Selection", "Please select a calamity to delete.");
            return;
        }
        
        int calamityId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String calamityName = (String) tableModel.getValueAt(selectedRow, 1);
        
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this calamity?\n\n" +
            "Name: " + calamityName + "\n" +
            "ID: " + calamityId + "\n\n" +
            "Note: Calamities that are used in distributions cannot be deleted.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            if (calamityService.deleteCalamity(calamityId)) {
                showSuccessDialog("Success", "Calamity deleted successfully.");
                loadCalamities();
            } else {
                showErrorDialog("Error", "Failed to delete calamity. It may be in use by existing distributions.");
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

