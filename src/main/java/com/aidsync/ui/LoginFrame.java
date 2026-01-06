package com.aidsync.ui;

import com.aidsync.model.User;
import com.aidsync.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Login screen for AidSync - Relief Distribution Management System
 * 
 * Features:
 * - Modern, clean UI with proper contrast
 * - Keyboard navigation support
 * - Input validation and error handling
 * - Responsive button interactions
 */
public class LoginFrame extends JFrame {
    // UI Components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox rememberMeCheckBox;
    private JButton loginButton;
    private JButton createAccountButton;
    
    // Services
    private UserService userService;
    
    // Color Constants - WCAG AA compliant contrast ratios
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color PRIMARY_DARK = new Color(0, 82, 164);
    private static final Color PRIMARY_LIGHT = new Color(240, 248, 255);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(68, 68, 68);
    private static final Color SUBTITLE_COLOR = new Color(100, 100, 100);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    
    // Font Constants
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 38);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BUTTON_FONT_PRIMARY = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font BUTTON_FONT_SECONDARY = new Font("Segoe UI", Font.PLAIN, 13);
    
    // Spacing Constants
    private static final int WINDOW_WIDTH = 480;
    private static final int WINDOW_HEIGHT = 520;
    private static final int PADDING_LARGE = 30;
    private static final int PADDING_MEDIUM = 20;
    private static final int PADDING_SMALL = 10;
    private static final int FIELD_SPACING = 15;
    private static final int BUTTON_HEIGHT = 42;
    private static final int BUTTON_WIDTH = 150;
    
    public LoginFrame() {
        userService = new UserService();
        initializeUI();
        setupKeyboardNavigation();
    }
    
    /**
     * Initialize the main UI components
     */
    private void initializeUI() {
        configureWindow();
        JPanel mainPanel = createMainPanel();
        add(mainPanel);
    }
    
    /**
     * Configure window properties
     */
    private void configureWindow() {
        setTitle("AidSync - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
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
        mainPanel.add(createFormCard(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    /**
     * Create the header section with title and subtitle
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM + 5, 0));
        
        JLabel titleLabel = createTitleLabel();
        JLabel subtitleLabel = createSubtitleLabel();
        
        headerPanel.add(Box.createVerticalGlue());
        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createVerticalGlue());
        
        return headerPanel;
    }
    
    /**
     * Create the main title label
     */
    private JLabel createTitleLabel() {
        JLabel titleLabel = new JLabel("AidSync", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return titleLabel;
    }
    
    /**
     * Create the subtitle label
     */
    private JLabel createSubtitleLabel() {
        JLabel subtitleLabel = new JLabel("Relief Distribution Management System", SwingConstants.CENTER);
        subtitleLabel.setFont(SUBTITLE_FONT);
        subtitleLabel.setForeground(SUBTITLE_COLOR);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        return subtitleLabel;
    }
    
    /**
     * Create the form card container
     */
    private JPanel createFormCard() {
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(BACKGROUND_COLOR);
        cardPanel.setBorder(createCardBorder());
        
        JPanel formPanel = createFormPanel();
        cardPanel.add(formPanel, BorderLayout.CENTER);
        
        return cardPanel;
    }
    
    /**
     * Create a styled border for the form card
     */
    private javax.swing.border.Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
            ),
            new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM)
        );
    }
    
    /**
     * Create the form panel with input fields
     */
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username field
        addFormField(formPanel, gbc, 0, "Username", createUsernameField());
        
        // Password field
        addFormField(formPanel, gbc, 2, "Password", createPasswordField());
        
        // Remember me checkbox
        addRememberMeCheckbox(formPanel, gbc, 4);
        
        return formPanel;
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
        gbc.insets = new Insets(0, 0, 5, 0);
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
        usernameField.setPreferredSize(new Dimension(0, 38));
        return usernameField;
    }
    
    /**
     * Create the password input field
     */
    private JPasswordField createPasswordField() {
        passwordField = new JPasswordField();
        passwordField.setFont(INPUT_FONT);
        passwordField.setBorder(createInputBorder());
        passwordField.setPreferredSize(new Dimension(0, 38));
        passwordField.addActionListener(new LoginActionListener());
        return passwordField;
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
    
    /**
     * Add the remember me checkbox
     */
    private void addRememberMeCheckbox(JPanel panel, GridBagConstraints gbc, int row) {
        rememberMeCheckBox = new JCheckBox("Remember me");
        rememberMeCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberMeCheckBox.setForeground(LABEL_COLOR);
        rememberMeCheckBox.setBackground(BACKGROUND_COLOR);
        rememberMeCheckBox.setOpaque(false);
        rememberMeCheckBox.setFocusPainted(false);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(5, 0, 0, 0);
        panel.add(rememberMeCheckBox, gbc);
    }
    
    /**
     * Create the button panel
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, PADDING_SMALL, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(new EmptyBorder(PADDING_MEDIUM, 0, 0, 0));
        
        loginButton = createLoginButton();
        createAccountButton = createCreateAccountButton();
        
        buttonPanel.add(loginButton);
        buttonPanel.add(createAccountButton);
        
        return buttonPanel;
    }
    
    /**
     * Create the primary login button
     */
    private JButton createLoginButton() {
        JButton button = new JButton("Login");
        button.setFont(BUTTON_FONT_PRIMARY);
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(new LoginActionListener());
        addButtonHoverEffect(button, PRIMARY_COLOR, PRIMARY_DARK);
        return button;
    }
    
    /**
     * Create the secondary create account button
     */
    private JButton createCreateAccountButton() {
        JButton button = new JButton("Create Account");
        button.setFont(BUTTON_FONT_SECONDARY);
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setBackground(BACKGROUND_COLOR);
        button.setForeground(PRIMARY_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            new EmptyBorder(10, 15, 10, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> openCreateAccountDialog());
        addButtonHoverEffect(button, BACKGROUND_COLOR, PRIMARY_LIGHT);
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
     * Setup keyboard navigation (Tab, Enter keys)
     */
    private void setupKeyboardNavigation() {
        // Tab navigation is handled automatically by Swing
        // Enter key on username field focuses password field
        usernameField.addActionListener(e -> passwordField.requestFocus());
    }
    
    /**
     * Open the create account dialog
     */
    private void openCreateAccountDialog() {
        SwingUtilities.invokeLater(() -> {
            CreateAccountDialog dialog = new CreateAccountDialog(this, userService);
            dialog.setVisible(true);
        });
    }
    
    /**
     * Handle login action
     */
    private class LoginActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            performLogin();
        }
    }
    
    /**
     * Perform the login operation
     */
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (!validateInput(username, password)) {
            return;
        }
        
        try {
            User user = userService.login(username, password);
            if (user != null) {
                handleSuccessfulLogin(user);
            } else {
                handleFailedLogin();
            }
        } catch (Exception ex) {
            showErrorDialog("Login Error", "Error: " + ex.getMessage());
        }
    }
    
    /**
     * Validate user input
     */
    private boolean validateInput(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showErrorDialog("Login Error", "Please enter both username and password.");
            return false;
        }
        return true;
    }
    
    /**
     * Handle successful login
     */
    private void handleSuccessfulLogin(User user) {
        dispose();
        SwingUtilities.invokeLater(() -> {
            new DashboardFrame(user).setVisible(true);
        });
    }
    
    /**
     * Handle failed login
     */
    private void handleFailedLogin() {
        showErrorDialog("Login Failed", "Invalid username or password.");
        passwordField.setText("");
        passwordField.requestFocus();
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
