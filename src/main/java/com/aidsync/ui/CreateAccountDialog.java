package com.aidsync.ui;

import com.aidsync.model.User;
import com.aidsync.service.ActivityLogService;
import com.aidsync.service.UserService;
import com.aidsync.util.BarangayData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

/**
 * Dialog for creating new user accounts from login screen
 * 
 * Note: All accounts created from this dialog are automatically assigned "Staff" role.
 * For Admin role assignment, use User Management screen (Admin menu).
 * 
 * Features:
 * - Modern, clean UI matching LoginFrame design
 * - Input validation and error handling
 * - Keyboard navigation support
 * - Responsive button interactions
 * - Properly sized to fit all content
 */
public class CreateAccountDialog extends JDialog {
    // ==================== UI Components ====================
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<String> barangayComboBox;
    private JButton createButton;
    private JButton cancelButton;
    
    // ==================== Services ====================
    private UserService userService;
    private JFrame parent;
    
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
    private static final int DIALOG_WIDTH = 420;
    private static final int DIALOG_HEIGHT = 650; // Adjusted for additional fields
    private static final int PADDING_LARGE = 25;
    private static final int PADDING_MEDIUM = 20;
    private static final int PADDING_SMALL = 10;
    private static final int FIELD_SPACING = 15;
    private static final int LABEL_FIELD_SPACING = 5;
    private static final int BUTTON_HEIGHT = 40;
    private static final int BUTTON_WIDTH = 120;
    private static final int INPUT_HEIGHT = 38;
    
    // ==================== Validation Patterns ====================
    private static final Pattern PHONE_PATTERN = Pattern.compile("^09\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    // ==================== Constructor ====================
    public CreateAccountDialog(JFrame parent, UserService userService) {
        super(parent, "Create New Account", true);
        this.parent = parent;
        this.userService = userService;
        initializeUI();
        setupKeyboardNavigation();
    }
    
    // ==================== UI Initialization ====================
    
    /**
     * Initialize the main UI components
     */
    private void initializeUI() {
        configureDialog();
        JPanel mainPanel = createMainPanel();
        add(mainPanel);
    }
    
    /**
     * Configure dialog properties
     */
    private void configureDialog() {
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(parent);
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
        
        JLabel titleLabel = new JLabel("Create New Account");
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
        
        // Username field
        addFormField(formPanel, gbc, row, "Username *", createUsernameField());
        row += 2;
        
        // Password field
        addFormField(formPanel, gbc, row, "Password *", createPasswordField());
        row += 2;
        
        // Confirm Password field
        addFormField(formPanel, gbc, row, "Confirm Password *", createConfirmPasswordField());
        row += 2;
        
        // Full Name field
        addFormField(formPanel, gbc, row, "Full Name", createFullNameField());
        row += 2;
        
        // Email field
        addFormField(formPanel, gbc, row, "Email", createEmailField());
        row += 2;
        
        // Phone field
        addFormField(formPanel, gbc, row, "Phone (09XXXXXXXXX)", createPhoneField());
        row += 2;
        
        // Barangay field
        addFormField(formPanel, gbc, row, "Barangay *", createBarangayComboBox());
        
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
     * Helper method to add a form field with label
     */
    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        // Label
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        label.setForeground(LABEL_COLOR);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, LABEL_FIELD_SPACING, 0);
        panel.add(label, gbc);
        
        // Field
        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, FIELD_SPACING, 0);
        gbc.gridwidth = 2;
        panel.add(field, gbc);
        gbc.gridwidth = 1;
    }
    
    /**
     * Create the username input field
     */
    private JTextField createUsernameField() {
        usernameField = new JTextField();
        usernameField.setFont(INPUT_FONT);
        usernameField.setBorder(createInputBorder());
        usernameField.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        return usernameField;
    }
    
    /**
     * Create the password input field
     */
    private JPasswordField createPasswordField() {
        passwordField = new JPasswordField();
        passwordField.setFont(INPUT_FONT);
        passwordField.setBorder(createInputBorder());
        passwordField.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        // Enter key moves to confirm password field
        passwordField.addActionListener(e -> confirmPasswordField.requestFocus());
        return passwordField;
    }
    
    /**
     * Create the confirm password input field
     */
    private JPasswordField createConfirmPasswordField() {
        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(INPUT_FONT);
        confirmPasswordField.setBorder(createInputBorder());
        confirmPasswordField.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        // Enter key moves to full name field
        confirmPasswordField.addActionListener(e -> fullNameField.requestFocus());
        return confirmPasswordField;
    }
    
    /**
     * Create the full name input field
     */
    private JTextField createFullNameField() {
        fullNameField = new JTextField();
        fullNameField.setFont(INPUT_FONT);
        fullNameField.setBorder(createInputBorder());
        fullNameField.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        // Enter key moves to email field
        fullNameField.addActionListener(e -> emailField.requestFocus());
        return fullNameField;
    }
    
    /**
     * Create the email input field
     */
    private JTextField createEmailField() {
        emailField = new JTextField();
        emailField.setFont(INPUT_FONT);
        emailField.setBorder(createInputBorder());
        emailField.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        // Enter key moves to phone field
        emailField.addActionListener(e -> phoneField.requestFocus());
        return emailField;
    }
    
    /**
     * Create the phone input field
     */
    private JTextField createPhoneField() {
        phoneField = new JTextField();
        phoneField.setFont(INPUT_FONT);
        phoneField.setBorder(createInputBorder());
        phoneField.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        // Enter key moves to barangay field
        phoneField.addActionListener(e -> barangayComboBox.requestFocus());
        return phoneField;
    }
    
    /**
     * Create the barangay combo box
     */
    private JComboBox<String> createBarangayComboBox() {
        barangayComboBox = new JComboBox<>(BarangayData.getAllBarangays().toArray(new String[0]));
        barangayComboBox.setFont(INPUT_FONT);
        barangayComboBox.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        barangayComboBox.setBorder(createInputBorder());
        // Enter key triggers create action
        barangayComboBox.addActionListener(e -> {
            if (e.getActionCommand().equals("comboBoxChanged")) return;
            performCreateAccount();
        });
        return barangayComboBox;
    }
    
    /**
     * Create a styled border for input fields
     */
    private javax.swing.border.Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(10, 12, 10, 12)
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
        
        createButton = createCreateButton();
        cancelButton = createCancelButton();
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);
        
        return buttonPanel;
    }
    
    /**
     * Create the primary create button
     */
    private JButton createCreateButton() {
        JButton button = new JButton("Create");
        button.setFont(BUTTON_FONT_PRIMARY);
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(new CreateActionListener());
        addButtonHoverEffect(button, PRIMARY_COLOR, PRIMARY_DARK);
        return button;
    }
    
    /**
     * Create the secondary cancel button
     */
    private JButton createCancelButton() {
        JButton button = new JButton("Cancel");
        button.setFont(BUTTON_FONT_SECONDARY);
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setBackground(BACKGROUND_COLOR);
        button.setForeground(LABEL_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(10, 15, 10, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> dispose());
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
        // Tab navigation is handled automatically by Swing
        // Focus starts on username field
        SwingUtilities.invokeLater(() -> usernameField.requestFocus());
    }
    
    // ==================== Event Handlers ====================
    
    /**
     * Handle create account action
     */
    private class CreateActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            performCreateAccount();
        }
    }
    
    /**
     * Perform the create account operation
     * Note: All accounts created from this dialog are assigned "Staff" role
     */
    private void performCreateAccount() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String barangay = (String) barangayComboBox.getSelectedItem();
        
        if (!validateInput(username, password, confirmPassword, email, phone, barangay)) {
            return;
        }
        
        try {
            // All accounts created from login screen are Staff role
            User newUser = new User(username, password, "Staff");
            newUser.setFullName(fullName.isEmpty() ? null : fullName);
            newUser.setEmail(email.isEmpty() ? null : email);
            newUser.setPhone(phone.isEmpty() ? null : phone);
            newUser.setBarangay(barangay);
            
            if (userService.createUser(newUser, "LoginScreen")) {
                ActivityLogService.logActivity("LoginScreen", "ACCOUNT_CREATED", 
                    "New staff account created: " + username + " for barangay: " + barangay);
                handleSuccessfulCreation();
            } else {
                showErrorDialog("Error", "Failed to create account. Username may already exist.");
            }
        } catch (IllegalArgumentException ex) {
            showErrorDialog("Validation Error", ex.getMessage());
        } catch (Exception ex) {
            showErrorDialog("Error", "Error: " + ex.getMessage());
        }
    }
    
    // ==================== Validation ====================
    
    /**
     * Validate user input
     */
    private boolean validateInput(String username, String password, String confirmPassword, String email, String phone, String barangay) {
        // Validate required fields
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || barangay == null) {
            showErrorDialog("Validation Error", "Please fill in all required fields (marked with *).");
            return false;
        }
        
        // Validate password match
        if (!password.equals(confirmPassword)) {
            showErrorDialog("Validation Error", "Passwords do not match.");
            confirmPasswordField.setText("");
            confirmPasswordField.requestFocus();
            return false;
        }
        
        // Validate email format if provided
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            showErrorDialog("Validation Error", "Invalid email format.");
            emailField.requestFocus();
            return false;
        }
        
        // Validate phone format if provided
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            showErrorDialog("Validation Error", "Invalid phone format. Must be 09XXXXXXXXX (11 digits starting with 09).");
            phoneField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    // ==================== Dialog Helpers ====================
    
    /**
     * Handle successful account creation
     */
    private void handleSuccessfulCreation() {
        JOptionPane.showMessageDialog(
            this,
            "Account created successfully!",
            "Success",
            JOptionPane.INFORMATION_MESSAGE
        );
        dispose();
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
