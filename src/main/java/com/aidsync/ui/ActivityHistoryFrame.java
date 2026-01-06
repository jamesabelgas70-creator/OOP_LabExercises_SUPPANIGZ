package com.aidsync.ui;

import com.aidsync.model.ActivityLog;
import com.aidsync.service.ActivityLogService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Activity History Frame for admin transparency
 */
public class ActivityHistoryFrame extends JFrame {
    private JTable activityTable;
    private DefaultTableModel tableModel;
    private ActivityLogService activityLogService;
    
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(68, 68, 68);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color TABLE_HEADER_COLOR = new Color(240, 240, 240);
    
    private static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    public ActivityHistoryFrame() {
        this.activityLogService = new ActivityLogService();
        initializeUI();
        loadActivityLogs();
    }

    private void initializeUI() {
        setTitle("AidSync - Activity History");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBackground(BACKGROUND_COLOR);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("System Activity History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(BUTTON_FONT);
        refreshButton.setBackground(PRIMARY_COLOR);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBorderPainted(false);
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> loadActivityLogs());
        headerPanel.add(refreshButton, BorderLayout.EAST);

        // Table
        String[] columnNames = {"Timestamp", "User", "Action", "Details"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        activityTable = new JTable(tableModel);
        activityTable.setFont(TABLE_FONT);
        activityTable.setRowHeight(25);
        activityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        activityTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        activityTable.setGridColor(BORDER_COLOR);
        activityTable.setShowGrid(true);

        // Style table header
        activityTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        activityTable.getTableHeader().setBackground(TABLE_HEADER_COLOR);
        activityTable.getTableHeader().setForeground(LABEL_COLOR);
        activityTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(activityTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void loadActivityLogs() {
        tableModel.setRowCount(0);
        List<ActivityLog> logs = activityLogService.getAllLogs();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (ActivityLog log : logs) {
            Object[] row = {
                log.getTimestamp().format(formatter),
                log.getUsername(),
                log.getAction(),
                log.getDetails()
            };
            tableModel.addRow(row);
        }
    }
}