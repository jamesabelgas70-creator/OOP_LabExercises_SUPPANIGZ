package com.aidsync.ui;

import com.aidsync.model.User;
import com.aidsync.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * User Management Screen for Admin
 * 
 * Features:
 * - Modern, clean UI matching application design
 * - Search functionality
 * - CRUD operations for users
 * - Password reset functionality
 * - Double-click to edit
 * - Responsive button interactions
 */
public class UserManagementFrame extends JFrame {
    // ==================== UI Components ====================
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton resetPasswordButton;
    private JButton refreshButton;
    private JButton searchButton;
    
    // ==================== Services & Data ====================
    private User currentUser;
    private UserService userService;
    
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
    public UserManagementFrame(User user) {
        this.currentUser = user;
        this.userService = new UserService();
        initializeUI();
        loadUsers();
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
        setTitle("AidSync - User Management");
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
        
        refreshButton = createActionButton("Refresh", BACKGROUND_COLOR, LABEL_COLOR, e -> loadUsers());
        resetPasswordButton = createActionButton("Reset Password", PRIMARY_COLOR, Color.WHITE, e -> resetPassword());
        editButton = createActionButton("Edit", PRIMARY_COLOR, Color.WHITE, e -> editSelectedUser());
        deleteButton = createActionButton("Delete", DELETE_COLOR, Color.WHITE, e -> deleteSelectedUser());
        addButton = createActionButton("Add New", PRIMARY_COLOR, Color.WHITE, e -> openAddEditDialog(null));
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(resetPasswordButton);
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
        String[] columnNames = {"ID", "Username", "Full Name", "Email", "Phone", "Role", "Created At", "Last Login"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Integer.class; // ID
                return String.class;
            }
        };
        
        userTable = new JTable(tableModel);
        userTable.setFont(TABLE_FONT);
        userTable.setRowHeight(30);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        userTable.setGridColor(BORDER_COLOR);
        userTable.setShowGrid(true);
        userTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Style table header
        userTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        userTable.getTableHeader().setBackground(TABLE_HEADER_COLOR);
        userTable.getTableHeader().setForeground(LABEL_COLOR);
        userTable.getTableHeader().setReorderingAllowed(false);
        
        // Custom renderer for Integer columns (to left-align numbers)
        userTable.setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {
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
        userTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
        userTable.setRowSelectionAllowed(true);
        userTable.setColumnSelectionAllowed(false);
        
        // Add double-click to edit
        userTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editSelectedUser();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(userTable);
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
     * Load all users into the table
     */
    private void loadUsers() {
        tableModel.setRowCount(0);
        List<User> users = userService.getAllUsers();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (User u : users) {
            Object[] row = {
                u.getId(),
                u.getUsername(),
                u.getFullName() != null ? u.getFullName() : "-",
                u.getEmail() != null ? u.getEmail() : "-",
                u.getPhone() != null ? u.getPhone() : "-",
                u.getRole(),
                u.getCreatedAt() != null ? u.getCreatedAt().format(formatter) : "",
                u.getLastLogin() != null ? u.getLastLogin().format(formatter) : "Never"
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
        
        List<User> users = userService.getAllUsers();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (User u : users) {
            // Search in username, full name, email, phone, and role
            boolean matches = searchTerm.isEmpty() ||
                (u.getUsername() != null && u.getUsername().toLowerCase().contains(searchTerm)) ||
                (u.getFullName() != null && u.getFullName().toLowerCase().contains(searchTerm)) ||
                (u.getEmail() != null && u.getEmail().toLowerCase().contains(searchTerm)) ||
                (u.getPhone() != null && u.getPhone().contains(searchTerm)) ||
                (u.getRole() != null && u.getRole().toLowerCase().contains(searchTerm));
            
            if (matches) {
                Object[] row = {
                    u.getId(),
                    u.getUsername(),
                    u.getFullName() != null ? u.getFullName() : "-",
                    u.getEmail() != null ? u.getEmail() : "-",
                    u.getPhone() != null ? u.getPhone() : "-",
                    u.getRole(),
                    u.getCreatedAt() != null ? u.getCreatedAt().format(formatter) : "",
                    u.getLastLogin() != null ? u.getLastLogin().format(formatter) : "Never"
                };
                tableModel.addRow(row);
            }
        }
    }
    
    // ==================== Dialog Operations ====================
    
    /**
     * Open add/edit dialog
     */
    private void openAddEditDialog(User user) {
        SwingUtilities.invokeLater(() -> {
            UserDialog dialog = new UserDialog(this, userService, user, () -> {
                loadUsers();
            });
            dialog.setVisible(true);
        });
    }
    
    /**
     * Edit selected user
     */
    private void editSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("No Selection", "Please select a user to edit, or double-click on a user.");
            return;
        }
        
        int userId = (Integer) tableModel.getValueAt(selectedRow, 0);
        User user = userService.getUserById(userId);
        if (user != null) {
            openAddEditDialog(user);
        } else {
            showErrorDialog("Error", "User not found.");
        }
    }
    
    /**
     * Delete selected user
     */
    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("No Selection", "Please select a user to delete.");
            return;
        }
        
        int userId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        
        // Prevent deleting yourself
        if (userId == currentUser.getId()) {
            showWarningDialog("Cannot Delete", "You cannot delete your own account.");
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this user?\n\n" +
            "Username: " + username + "\n" +
            "ID: " + userId + "\n\n" +
            "This action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            if (userService.deleteUser(userId)) {
                showSuccessDialog("Success", "User deleted successfully.");
                loadUsers();
            } else {
                showErrorDialog("Error", "Failed to delete user.");
            }
        }
    }
    
    /**
     * Reset password for selected user
     */
    private void resetPassword() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("No Selection", "Please select a user to reset password.");
            return;
        }
        
        int userId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        
        String newPassword = JOptionPane.showInputDialog(
            this,
            "Enter new password for user: " + username,
            "Reset Password",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            try {
                if (userService.updatePassword(userId, newPassword.trim())) {
                    showSuccessDialog("Success", "Password reset successfully.");
                } else {
                    showErrorDialog("Error", "Failed to reset password.");
                }
            } catch (IllegalArgumentException ex) {
                showErrorDialog("Validation Error", ex.getMessage());
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
