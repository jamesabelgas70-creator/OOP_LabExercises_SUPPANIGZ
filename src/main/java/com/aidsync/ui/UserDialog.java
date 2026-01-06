package com.aidsync.ui;

import com.aidsync.model.User;
import com.aidsync.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.regex.Pattern;

/**
 * Dialog for adding/editing users
 * 
 * Features:
 * - Modern, clean UI matching application design
 * - Input validation (phone format: 09XXXXXXXXX, email format)
 * - Keyboard navigation support
 * - Responsive button interactions
 */
public class UserDialog extends JDialog {
    // ==================== UI Components ====================
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<String> roleComboBox;
    private JButton saveButton;
    private JButton cancelButton;
    
    // ==================== Services & Data ====================
    private UserService userService;
    private User user;
    private Runnable onSaveCallback;
    
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
    private static final int DIALOG_HEIGHT = 550;
    private static final int PADDING_LARGE = 25;
    private static final int PADDING_MEDIUM = 15;
    private static final int PADDING_SMALL = 10;
    private static final int FIELD_SPACING = 12;
    private static final int BUTTON_HEIGHT = 40;
    private static final int BUTTON_WIDTH = 120;
    private static final int INPUT_HEIGHT = 38;
    
    // ==================== Validation Patterns ====================
    private static final Pattern PHONE_PATTERN = Pattern.compile("^09\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    // ==================== Constructor ====================
    public UserDialog(JFrame parent, UserService service, User user, Runnable onSave) {
        super(parent, user == null ? "Add New User" : "Edit User", true);
        this.userService = service;
        this.user = user;
        this.onSaveCallback = onSave;
        initializeUI();
        if (user != null) {
            loadUserData();
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
        
        JLabel titleLabel = new JLabel(user == null ? "Add New User" : "Edit User");
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
        
        // Username
        formPanel.add(createFieldPanel("Username *", usernameField = createTextField()));
        formPanel.add(Box.createVerticalStrut(FIELD_SPACING));
        
        // Password fields (only for new users)
        if (user == null) {
            formPanel.add(createFieldPanel("Password *", passwordField = createPasswordField()));
            formPanel.add(Box.createVerticalStrut(FIELD_SPACING));
            formPanel.add(createFieldPanel("Confirm Password *", confirmPasswordField = createPasswordField()));
            formPanel.add(Box.createVerticalStrut(FIELD_SPACING));
        }
        
        // Full Name
        formPanel.add(createFieldPanel("Full Name", fullNameField = createTextField()));
        formPanel.add(Box.createVerticalStrut(FIELD_SPACING));
        
        // Email
        formPanel.add(createFieldPanel("Email", emailField = createTextField()));
        formPanel.add(Box.createVerticalStrut(FIELD_SPACING));
        
        // Phone
        formPanel.add(createFieldPanel("Phone (09XXXXXXXXX)", phoneField = createTextField()));
        formPanel.add(Box.createVerticalStrut(FIELD_SPACING));
        
        // Role
        formPanel.add(createFieldPanel("Role *", roleComboBox = createRoleComboBox()));
        
        // Add flexible space
        formPanel.add(Box.createVerticalGlue());
        
        // Wrap in scroll pane for smaller screens
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Adjust scroll sensitivity
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);
        verticalScrollBar.setBlockIncrement(64);
        
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(BACKGROUND_COLOR);
        wrapperPanel.add(scrollPane, BorderLayout.CENTER);
        
        return wrapperPanel;
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
     * Create a styled password field
     */
    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(INPUT_FONT);
        field.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, INPUT_HEIGHT));
        field.setBorder(createInputBorder());
        field.setBackground(BACKGROUND_COLOR);
        return field;
    }
    
    /**
     * Create role combo box
     */
    private JComboBox<String> createRoleComboBox() {
        JComboBox<String> comboBox = new JComboBox<>(new String[]{"Admin", "Staff"});
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
        saveButton = createPrimaryButton("Save", e -> saveUser());
        
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
     * Setup keyboard navigation
     */
    private void setupKeyboardNavigation() {
        // Enter key on save button
        getRootPane().setDefaultButton(saveButton);
        
        // Escape key to close
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
     * Load user data into form fields
     */
    private void loadUserData() {
        if (user == null) return;
        
        usernameField.setText(user.getUsername() != null ? user.getUsername() : "");
        fullNameField.setText(user.getFullName() != null ? user.getFullName() : "");
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        phoneField.setText(user.getPhone() != null ? user.getPhone() : "");
        roleComboBox.setSelectedItem(user.getRole() != null ? user.getRole() : "Staff");
    }
    
    /**
     * Save user (create or update)
     */
    private void saveUser() {
        try {
            // Validate inputs
            String username = usernameField.getText().trim();
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String role = (String) roleComboBox.getSelectedItem();
            
            // Validate username
            if (username.isEmpty()) {
                showError("Username is required.");
                usernameField.requestFocus();
                return;
            }
            
            // Validate password for new users
            if (user == null) {
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());
                
                if (password.isEmpty()) {
                    showError("Password is required.");
                    passwordField.requestFocus();
                    return;
                }
                
                if (!password.equals(confirmPassword)) {
                    showError("Passwords do not match.");
                    confirmPasswordField.requestFocus();
                    return;
                }
            }
            
            // Validate email format if provided
            if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
                showError("Invalid email format.");
                emailField.requestFocus();
                return;
            }
            
            // Validate phone format if provided
            if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
                showError("Invalid phone format. Must be 09XXXXXXXXX (11 digits starting with 09).");
                phoneField.requestFocus();
                return;
            }
            
            // Validate role
            if (role == null || role.isEmpty()) {
                showError("Role is required.");
                roleComboBox.requestFocus();
                return;
            }
            
            // Create or update user
            User u = user != null ? user : new User();
            u.setUsername(username);
            u.setFullName(fullName.isEmpty() ? null : fullName);
            u.setEmail(email.isEmpty() ? null : email);
            u.setPhone(phone.isEmpty() ? null : phone);
            u.setRole(role);
            
            if (user == null) {
                // New user - set password
                u.setPassword(new String(passwordField.getPassword()));
            }
            
            boolean success;
            if (user == null) {
                success = userService.createUser(u);
            } else {
                success = userService.updateUser(u);
            }
            
            if (success) {
                showSuccess(user == null ? "User created successfully!" : "User updated successfully!");
                if (onSaveCallback != null) {
                    onSaveCallback.run();
                }
                dispose();
            } else {
                showError("Failed to save user. Username may already exist.");
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
