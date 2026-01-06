package com.aidsync.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.aidsync.model.Beneficiary;
import com.aidsync.model.User;
import com.aidsync.service.BeneficiaryService;
import com.aidsync.util.BarangayData;

/**
 * Dialog for adding/editing beneficiaries
 * 
 * Features:
 * - Modern, clean UI matching application design
 * - Input validation and error handling
 * - Dynamic barangay/purok selection
 * - Keyboard navigation support
 * - Responsive button interactions
 */
public class BeneficiaryDialog extends JDialog {
    // ==================== UI Components ====================
    private JTextField nameField;
    private JTextField birthDateField;
    private JComboBox<String> genderComboBox;
    private JTextField contactField;
    private JComboBox<String> barangayComboBox;
    private JComboBox<String> purokComboBox;
    private JTextField streetAddressField;
    private JSpinner familySizeSpinner;
    private JCheckBox householdHeadCheckBox;
    private JCheckBox pwdCheckBox;
    private JCheckBox seniorCitizenCheckBox;
    private JCheckBox pregnantCheckBox;
    private JCheckBox soloParentCheckBox;
    private JComboBox<String> statusComboBox;
    private JButton saveButton;
    private JButton cancelButton;
    
    // ==================== Services & Data ====================
    private BeneficiaryService beneficiaryService;
    private Beneficiary beneficiary;
    private User currentUser;
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
    private static final int DIALOG_WIDTH = 650;
    private static final int DIALOG_HEIGHT = 720;
    private static final int PADDING_LARGE = 25;
    private static final int PADDING_MEDIUM = 20;
    private static final int PADDING_SMALL = 10;
    private static final int FIELD_SPACING = 12;
    private static final int LABEL_FIELD_SPACING = 5;
    private static final int BUTTON_HEIGHT = 40;
    private static final int BUTTON_WIDTH = 120;
    private static final int INPUT_HEIGHT = 38;
    
    // ==================== Constructor ====================
    public BeneficiaryDialog(JFrame parent, BeneficiaryService service, Beneficiary beneficiary, User currentUser, Runnable onSave) {
        super(parent, beneficiary == null ? "Add New Beneficiary" : "Edit Beneficiary", true);
        this.beneficiaryService = service;
        this.beneficiary = beneficiary;
        this.currentUser = currentUser;
        this.onSaveCallback = onSave;
        initializeUI();
        if (beneficiary != null) {
            loadBeneficiaryData();
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
        mainPanel.add(createScrollableFormPanel(), BorderLayout.CENTER);
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
        
        JLabel titleLabel = new JLabel(beneficiary == null ? "Add New Beneficiary" : "Edit Beneficiary");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel);
        
        return headerPanel;
    }
    
    // ==================== Form Section ====================
    
    /**
     * Create scrollable form panel
     */
    private JScrollPane createScrollableFormPanel() {
        JPanel formPanel = createFormPanel();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Adjust scroll sensitivity
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16); // Fine scrolling (smaller = more sensitive)
        verticalScrollBar.setBlockIncrement(64); // Page scrolling (larger = bigger jumps)
        
        return scrollPane;
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
        
        int row = 0;
        
        // Full Name (row 0-1)
        addFormField(formPanel, gbc, row, "Full Name *", createNameField());
        row += 2;
        
        // Birth Date (row 2-3)
        addFormField(formPanel, gbc, row, "Birth Date", createBirthDateField());
        row += 2;
        
        // Gender (row 4-5)
        addFormField(formPanel, gbc, row, "Gender", createGenderComboBox());
        row += 2;
        
        // Contact Number (row 6-7)
        addFormField(formPanel, gbc, row, "Contact Number", createContactField());
        row += 2;
        
        // Barangay (row 8-9)
        addFormField(formPanel, gbc, row, "Barangay *", createBarangayComboBox());
        row += 2;
        
        // Purok (row 10-11)
        addFormField(formPanel, gbc, row, "Purok *", createPurokComboBox());
        row += 2;
        
        // Street Address (row 12-13)
        addFormField(formPanel, gbc, row, "Street Address", createStreetAddressField());
        row += 2;
        
        // Family Size (row 14-15)
        addFormField(formPanel, gbc, row, "Family Size *", createFamilySizeSpinner());
        row += 2;
        
        // Special Conditions (row 16)
        addSpecialConditionsPanel(formPanel, gbc, row);
        row += 1;
        
        // Status (row 17-18)
        addFormField(formPanel, gbc, row, "Status", createStatusComboBox());
        
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
     * Create the name input field
     */
    private JTextField createNameField() {
        nameField = new JTextField();
        nameField.setFont(INPUT_FONT);
        nameField.setBorder(createInputBorder());
        nameField.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        return nameField;
    }
    
    /**
     * Create the birth date input field
     */
    private JTextField createBirthDateField() {
        birthDateField = new JTextField();
        birthDateField.setFont(INPUT_FONT);
        birthDateField.setBorder(createInputBorder());
        birthDateField.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        birthDateField.setToolTipText("Format: YYYY-MM-DD");
        return birthDateField;
    }
    
    /**
     * Create the gender combo box
     */
    private JComboBox<String> createGenderComboBox() {
        genderComboBox = new JComboBox<>(new String[]{"", "Male", "Female", "Prefer not to say"});
        genderComboBox.setFont(INPUT_FONT);
        genderComboBox.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        genderComboBox.setBorder(createInputBorder());
        return genderComboBox;
    }
    
    /**
     * Create the contact number input field
     */
    private JTextField createContactField() {
        contactField = new JTextField();
        contactField.setFont(INPUT_FONT);
        contactField.setBorder(createInputBorder());
        contactField.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        return contactField;
    }
    
    /**
     * Create the barangay combo box
     */
    private JComboBox<String> createBarangayComboBox() {
        List<String> barangays = BarangayData.getAllBarangays();
        
        // For staff users, restrict to their assigned barangay
        if (currentUser.isStaff() && currentUser.getBarangay() != null) {
            barangayComboBox = new JComboBox<>(new String[]{currentUser.getBarangay()});
            barangayComboBox.setEnabled(false);
        } else {
            barangayComboBox = new JComboBox<>(barangays.toArray(new String[0]));
        }
        
        barangayComboBox.setFont(INPUT_FONT);
        barangayComboBox.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        barangayComboBox.setBorder(createInputBorder());
        barangayComboBox.addActionListener(e -> updatePurokList());
        return barangayComboBox;
    }
    
    /**
     * Create the purok combo box
     */
    private JComboBox<String> createPurokComboBox() {
        purokComboBox = new JComboBox<>();
        purokComboBox.setFont(INPUT_FONT);
        purokComboBox.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        purokComboBox.setBorder(createInputBorder());
        purokComboBox.setEnabled(false);
        return purokComboBox;
    }
    
    /**
     * Update purok list based on selected barangay
     */
    private void updatePurokList() {
        String selectedBarangay = (String) barangayComboBox.getSelectedItem();
        if (selectedBarangay != null && !selectedBarangay.isEmpty()) {
            List<String> puroks = BarangayData.getPuroksForBarangay(selectedBarangay);
            purokComboBox.setModel(new DefaultComboBoxModel<>(puroks.toArray(new String[0])));
            purokComboBox.setEnabled(true);
        } else {
            purokComboBox.setModel(new DefaultComboBoxModel<>());
            purokComboBox.setEnabled(false);
        }
    }
    
    /**
     * Create the street address input field
     */
    private JTextField createStreetAddressField() {
        streetAddressField = new JTextField();
        streetAddressField.setFont(INPUT_FONT);
        streetAddressField.setBorder(createInputBorder());
        streetAddressField.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        return streetAddressField;
    }
    
    /**
     * Create the family size spinner
     */
    private JSpinner createFamilySizeSpinner() {
        familySizeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        familySizeSpinner.setFont(INPUT_FONT);
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) familySizeSpinner.getEditor();
        editor.getTextField().setBorder(createInputBorder());
        editor.getTextField().setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        return familySizeSpinner;
    }
    
    /**
     * Add special conditions checkboxes panel
     */
    private void addSpecialConditionsPanel(JPanel panel, GridBagConstraints gbc, int row) {
        // Label
        JLabel label = new JLabel("Special Conditions");
        label.setFont(LABEL_FONT);
        label.setForeground(LABEL_COLOR);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, LABEL_FIELD_SPACING, 0);
        panel.add(label, gbc);
        
        // Checkboxes panel
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        checkboxPanel.setBackground(BACKGROUND_COLOR);
        
        householdHeadCheckBox = createCheckBox("Household Head");
        pwdCheckBox = createCheckBox("PWD");
        seniorCitizenCheckBox = createCheckBox("Senior Citizen");
        pregnantCheckBox = createCheckBox("Pregnant");
        soloParentCheckBox = createCheckBox("Solo Parent");
        
        checkboxPanel.add(householdHeadCheckBox);
        checkboxPanel.add(pwdCheckBox);
        checkboxPanel.add(seniorCitizenCheckBox);
        checkboxPanel.add(pregnantCheckBox);
        checkboxPanel.add(soloParentCheckBox);
        
        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, FIELD_SPACING, 0);
        gbc.gridwidth = 2;
        panel.add(checkboxPanel, gbc);
        gbc.gridwidth = 1;
    }
    
    /**
     * Create a styled checkbox
     */
    private JCheckBox createCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        checkBox.setForeground(LABEL_COLOR);
        checkBox.setBackground(BACKGROUND_COLOR);
        checkBox.setOpaque(false);
        checkBox.setFocusPainted(false);
        return checkBox;
    }
    
    /**
     * Create the status combo box
     */
    private JComboBox<String> createStatusComboBox() {
        statusComboBox = new JComboBox<>(new String[]{"Active", "Inactive"});
        statusComboBox.setFont(INPUT_FONT);
        statusComboBox.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        statusComboBox.setBorder(createInputBorder());
        return statusComboBox;
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
        
        saveButton = createSaveButton();
        cancelButton = createCancelButton();
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        return buttonPanel;
    }
    
    /**
     * Create the primary save button
     */
    private JButton createSaveButton() {
        JButton button = new JButton("Save");
        button.setFont(BUTTON_FONT_PRIMARY);
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(new SaveActionListener());
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
        // Focus starts on name field
        SwingUtilities.invokeLater(() -> nameField.requestFocus());
    }
    
    // ==================== Data Loading ====================
    
    /**
     * Load beneficiary data into form fields
     */
    private void loadBeneficiaryData() {
        if (beneficiary == null) return;
        
        nameField.setText(beneficiary.getFullName());
        
        if (beneficiary.getBirthDate() != null) {
            birthDateField.setText(beneficiary.getBirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        if (beneficiary.getGender() != null) {
            genderComboBox.setSelectedItem(beneficiary.getGender());
        }
        
        contactField.setText(beneficiary.getContactNumber());
        barangayComboBox.setSelectedItem(beneficiary.getBarangay());
        updatePurokList();
        purokComboBox.setSelectedItem(beneficiary.getPurok());
        streetAddressField.setText(beneficiary.getStreetAddress());
        familySizeSpinner.setValue(beneficiary.getFamilySize());
        householdHeadCheckBox.setSelected(beneficiary.isHouseholdHead());
        pwdCheckBox.setSelected(beneficiary.isPwd());
        seniorCitizenCheckBox.setSelected(beneficiary.isSeniorCitizen());
        pregnantCheckBox.setSelected(beneficiary.isPregnant());
        soloParentCheckBox.setSelected(beneficiary.isSoloParent());
        statusComboBox.setSelectedItem(beneficiary.getStatus());
    }
    
    // ==================== Event Handlers ====================
    
    /**
     * Handle save action
     */
    private class SaveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            performSave();
        }
    }
    
    /**
     * Perform the save operation
     */
    private void performSave() {
        try {
            Beneficiary b = beneficiary != null ? beneficiary : new Beneficiary();
            
            // Set basic information
            b.setFullName(nameField.getText().trim());
            
            // Parse birth date
            String birthDateStr = birthDateField.getText().trim();
            if (!birthDateStr.isEmpty()) {
                try {
                    b.setBirthDate(LocalDate.parse(birthDateStr));
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Invalid birth date format. Use YYYY-MM-DD");
                }
            }
            
            // Set gender
            String gender = (String) genderComboBox.getSelectedItem();
            b.setGender(gender != null && !gender.isEmpty() ? gender : null);
            
            // Set contact
            b.setContactNumber(contactField.getText().trim());
            
            // Validate and set barangay
            String barangay = (String) barangayComboBox.getSelectedItem();
            if (barangay == null || barangay.isEmpty()) {
                throw new IllegalArgumentException("Barangay is required");
            }
            b.setBarangay(barangay);
            
            // Validate and set purok
            String purok = (String) purokComboBox.getSelectedItem();
            if (purok == null || purok.isEmpty()) {
                throw new IllegalArgumentException("Purok is required");
            }
            b.setPurok(purok);
            
            // Set address and family size
            b.setStreetAddress(streetAddressField.getText().trim());
            b.setFamilySize((Integer) familySizeSpinner.getValue());
            
            // Set special conditions
            b.setHouseholdHead(householdHeadCheckBox.isSelected());
            b.setPwd(pwdCheckBox.isSelected());
            b.setSeniorCitizen(seniorCitizenCheckBox.isSelected());
            b.setPregnant(pregnantCheckBox.isSelected());
            b.setSoloParent(soloParentCheckBox.isSelected());
            
            // Set status
            b.setStatus((String) statusComboBox.getSelectedItem());
            
            // Save beneficiary
            boolean success;
            if (beneficiary == null) {
                success = beneficiaryService.createBeneficiary(b, currentUser.getUsername());
            } else {
                success = beneficiaryService.updateBeneficiary(b, currentUser.getUsername());
            }
            
            if (success) {
                handleSuccessfulSave();
            } else {
                showErrorDialog("Error", "Failed to save beneficiary.");
            }
        } catch (IllegalArgumentException ex) {
            showErrorDialog("Validation Error", ex.getMessage());
        } catch (Exception ex) {
            showErrorDialog("Error", "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // ==================== Dialog Helpers ====================
    
    /**
     * Handle successful save
     */
    private void handleSuccessfulSave() {
        JOptionPane.showMessageDialog(
            this,
            beneficiary == null ? "Beneficiary created successfully!" : "Beneficiary updated successfully!",
            "Success",
            JOptionPane.INFORMATION_MESSAGE
        );
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        
        // For new beneficiaries, clear form and stay open
        if (beneficiary == null) {
            clearForm();
            nameField.requestFocus();
        } else {
            // For edits, close the dialog
            dispose();
        }
    }
    
    /**
     * Clear all form fields for new entry
     */
    private void clearForm() {
        nameField.setText("");
        birthDateField.setText("");
        genderComboBox.setSelectedIndex(0);
        contactField.setText("");
        
        // For staff users, keep their barangay selected
        if (currentUser.isAdmin()) {
            barangayComboBox.setSelectedIndex(0);
            purokComboBox.setModel(new DefaultComboBoxModel<>());
            purokComboBox.setEnabled(false);
        } else {
            // Staff user - barangay is already set and disabled, just update purok
            updatePurokList();
        }
        
        streetAddressField.setText("");
        familySizeSpinner.setValue(1);
        householdHeadCheckBox.setSelected(false);
        pwdCheckBox.setSelected(false);
        seniorCitizenCheckBox.setSelected(false);
        pregnantCheckBox.setSelected(false);
        soloParentCheckBox.setSelected(false);
        statusComboBox.setSelectedIndex(0);
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
