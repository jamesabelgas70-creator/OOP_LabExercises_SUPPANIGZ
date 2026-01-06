package com.aidsync.ui;

import com.aidsync.model.Beneficiary;
import com.aidsync.model.InventoryItem;
import com.aidsync.model.User;
import com.aidsync.service.DistributionService;
import com.aidsync.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Dialog for batch distribution to multiple beneficiaries
 */
public class BatchDistributeDialog extends JDialog {
    private final List<Beneficiary> beneficiaries;
    private final DistributionService distributionService;
    private final InventoryService inventoryService;
    private final User currentUser;
    private final Runnable onComplete;
    
    private JList<InventoryItem> itemList;
    private JSpinner quantitySpinner;
    private JTextArea notesArea;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    
    public BatchDistributeDialog(JFrame parent, List<Beneficiary> beneficiaries, 
                               DistributionService distributionService, 
                               InventoryService inventoryService,
                               User currentUser, Runnable onComplete) {
        super(parent, "Batch Distribute to " + beneficiaries.size() + " Beneficiaries", true);
        this.beneficiaries = beneficiaries;
        this.distributionService = distributionService;
        this.inventoryService = inventoryService;
        this.currentUser = currentUser;
        this.onComplete = onComplete;
        
        initializeUI();
        loadInventoryItems();
    }
    
    private void initializeUI() {
        setSize(600, 500);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Header
        JLabel headerLabel = new JLabel("Distribute to " + beneficiaries.size() + " selected beneficiaries");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerLabel.setForeground(PRIMARY_COLOR);
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        
        // Center panel
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        // Item selection
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBorder(BorderFactory.createTitledBorder("Select Items"));
        itemPanel.setBackground(BACKGROUND_COLOR);
        
        itemList = new JList<>();
        itemList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane itemScroll = new JScrollPane(itemList);
        itemScroll.setPreferredSize(new Dimension(0, 120));
        itemPanel.add(itemScroll, BorderLayout.CENTER);
        
        // Quantity
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quantityPanel.setBackground(BACKGROUND_COLOR);
        quantityPanel.add(new JLabel("Quantity per beneficiary:"));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        quantityPanel.add(quantitySpinner);
        itemPanel.add(quantityPanel, BorderLayout.SOUTH);
        
        centerPanel.add(itemPanel);
        
        // Notes
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setBorder(BorderFactory.createTitledBorder("Notes"));
        notesPanel.setBackground(BACKGROUND_COLOR);
        
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesPanel.add(notesScroll, BorderLayout.CENTER);
        
        centerPanel.add(notesPanel);
        
        // Progress
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBackground(BACKGROUND_COLOR);
        
        statusLabel = new JLabel("Ready to distribute");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        progressPanel.add(statusLabel, BorderLayout.NORTH);
        
        progressBar = new JProgressBar(0, beneficiaries.size());
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        
        centerPanel.add(progressPanel);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        JButton distributeButton = new JButton("Start Distribution");
        distributeButton.setBackground(PRIMARY_COLOR);
        distributeButton.setForeground(Color.WHITE);
        distributeButton.addActionListener(e -> startBatchDistribution());
        buttonPanel.add(distributeButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }
    
    private void loadInventoryItems() {
        List<InventoryItem> items = inventoryService.getAllInventoryItems();
        DefaultListModel<InventoryItem> model = new DefaultListModel<>();
        for (InventoryItem item : items) {
            if (item.getQuantity() > 0) {
                model.addElement(item);
            }
        }
        itemList.setModel(model);
        
        itemList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof InventoryItem) {
                    InventoryItem item = (InventoryItem) value;
                    setText(item.getItemName() + " (Available: " + item.getQuantity() + " " + item.getUnit() + ")");
                }
                return this;
            }
        });
    }
    
    private void startBatchDistribution() {
        List<InventoryItem> selectedItems = itemList.getSelectedValuesList();
        if (selectedItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one item.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int quantity = (Integer) quantitySpinner.getValue();
        String notes = notesArea.getText().trim();
        
        // Validate inventory
        for (InventoryItem item : selectedItems) {
            int totalNeeded = quantity * beneficiaries.size();
            if (item.getQuantity() < totalNeeded) {
                JOptionPane.showMessageDialog(this, 
                    "Insufficient inventory for " + item.getItemName() + 
                    ". Need: " + totalNeeded + ", Available: " + item.getQuantity(),
                    "Insufficient Inventory", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // Start background distribution
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setVisible(true);
                    progressBar.setValue(0);
                    statusLabel.setText("Starting batch distribution...");
                });
                
                int completed = 0;
                for (Beneficiary beneficiary : beneficiaries) {
                    try {
                        // Create distribution using existing service method
                        com.aidsync.model.Distribution distribution = new com.aidsync.model.Distribution();
                        distribution.setBeneficiaryId(beneficiary.getId());
                        distribution.setDistributedBy(currentUser.getId());
                        distribution.setNotes(notes.isEmpty() ? null : notes);
                        
                        // Create distribution items
                        java.util.List<com.aidsync.model.DistributionItem> items = new java.util.ArrayList<>();
                        for (InventoryItem item : selectedItems) {
                            com.aidsync.model.DistributionItem distItem = new com.aidsync.model.DistributionItem();
                            distItem.setInventoryId(item.getId());
                            distItem.setQuantity(quantity);
                            items.add(distItem);
                        }
                        distribution.setItems(items);
                        
                        boolean success = distributionService.createDistribution(distribution);
                        
                        if (success) {
                            completed++;
                        }
                        
                        publish(completed);
                        
                    } catch (Exception e) {
                        System.err.println("Error distributing to beneficiary " + beneficiary.getBeneficiaryId() + ": " + e.getMessage());
                    }
                }
                
                return null;
            }
            
            @Override
            protected void process(List<Integer> chunks) {
                int latest = chunks.get(chunks.size() - 1);
                progressBar.setValue(latest);
                statusLabel.setText("Distributed to " + latest + " of " + beneficiaries.size() + " beneficiaries");
            }
            
            @Override
            protected void done() {
                progressBar.setVisible(false);
                statusLabel.setText("Batch distribution completed!");
                
                JOptionPane.showMessageDialog(BatchDistributeDialog.this,
                    "Successfully distributed to " + progressBar.getValue() + " beneficiaries.",
                    "Distribution Complete", JOptionPane.INFORMATION_MESSAGE);
                
                if (onComplete != null) {
                    onComplete.run();
                }
                dispose();
            }
        };
        
        worker.execute();
    }
}