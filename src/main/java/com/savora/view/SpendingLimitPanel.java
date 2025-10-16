package com.savora.view;

import com.savora.dao.CategoryDAO;
import com.savora.dao.SpendingLimitDAO;
import com.savora.dao.TransactionDAO;
import com.savora.model.Category;
import com.savora.model.SpendingLimit;
import com.savora.model.Transaction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel for managing spending limits
 */
public class SpendingLimitPanel extends JPanel {
    private final SpendingLimitDAO spendingLimitDAO;
    private final CategoryDAO categoryDAO;
    private final TransactionDAO transactionDAO;
    
    private JTable limitTable;
    private DefaultTableModel tableModel;
    
    private JTextField limitAmountField;
    private JComboBox<Category> categoryCombo;
    private JComboBox<String> periodCombo;
    private JCheckBox overallLimitCheckBox;
    
    // Colors for modern UI
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color WARNING_COLOR = new Color(245, 158, 11);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color BACKGROUND_COLOR = new Color(248, 250, 252);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    
    public SpendingLimitPanel() {
        this.spendingLimitDAO = new SpendingLimitDAO();
        this.categoryDAO = new CategoryDAO();
        this.transactionDAO = new TransactionDAO();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadSpendingLimits();
    }
    
    private void initializeComponents() {
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        
        // Initialize table
        String[] columnNames = {"ID", "Category", "Limit Amount", "Current Spending", "Remaining", "Usage %", "Period"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        limitTable = new JTable(tableModel);
        limitTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        limitTable.setRowHeight(48);
        limitTable.setShowGrid(false);
        limitTable.setIntercellSpacing(new Dimension(0, 0));
        limitTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        limitTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        limitTable.getTableHeader().setBackground(new Color(249, 250, 251));
        limitTable.getTableHeader().setForeground(TEXT_PRIMARY);
        
        // Hide ID column
        limitTable.getColumnModel().getColumn(0).setMinWidth(0);
        limitTable.getColumnModel().getColumn(0).setMaxWidth(0);
        limitTable.getColumnModel().getColumn(0).setWidth(0);
        
        // Initialize form fields
        initializeFormFields();
    }
    
    private void initializeFormFields() {
        limitAmountField = new JTextField();
        limitAmountField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        limitAmountField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            new EmptyBorder(12, 16, 12, 16)
        ));
        
        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        categoryCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        periodCombo = new JComboBox<>(new String[]{"monthly", "weekly", "yearly"});
        periodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        periodCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        overallLimitCheckBox = new JCheckBox("Overall spending limit (applies to all categories)");
        overallLimitCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        overallLimitCheckBox.setForeground(TEXT_PRIMARY);
        overallLimitCheckBox.addActionListener(e -> {
            categoryCombo.setEnabled(!overallLimitCheckBox.isSelected());
        });
        
        loadCategories();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel with title
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel with spending limits table
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with spending limit form
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 16, 0));
        
        // Title
        JLabel titleLabel = new JLabel("Spending Limits Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        infoPanel.setOpaque(false);
        
        JLabel infoLabel = new JLabel("<html>Set spending limits to receive notifications when you approach or exceed your budget.<br>You'll get warnings at 80% and alerts at 100% of your limits.</html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(TEXT_SECONDARY);
        
        infoPanel.add(infoLabel);
        
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(infoPanel, BorderLayout.EAST);
        
        return topPanel;
    }
    
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        
        // Spending limits table
        JScrollPane scrollPane = new JScrollPane(limitTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        scrollPane.setBackground(CARD_COLOR);
        scrollPane.getViewport().setBackground(CARD_COLOR);
        
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        return centerPanel;
    }
    
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(CARD_COLOR);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Form title
        JLabel formTitleLabel = new JLabel("Add New Spending Limit");
        formTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitleLabel.setForeground(TEXT_PRIMARY);
        formTitleLabel.setBorder(new EmptyBorder(0, 0, 16, 0));
        
        // Form fields panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Overall limit checkbox
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(overallLimitCheckBox, gbc);
        
        // Category field (disabled when overall limit is selected)
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(categoryCombo, gbc);
        
        // Limit amount field
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Limit Amount ($):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(limitAmountField, gbc);
        
        // Period field
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Period:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(periodCombo, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);
        
        // Add button
        JButton addButton = new JButton("Add Limit");
        addButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addButton.setBackground(PRIMARY_COLOR);
        addButton.setForeground(Color.WHITE);
        addButton.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> addSpendingLimit());
        
        // Clear button
        JButton clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        clearButton.setBackground(new Color(156, 163, 175));
        clearButton.setForeground(Color.WHITE);
        clearButton.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(e -> clearForm());
        
        buttonPanel.add(clearButton);
        buttonPanel.add(addButton);
        
        bottomPanel.add(formTitleLabel, BorderLayout.NORTH);
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return bottomPanel;
    }
    
    private void setupEventHandlers() {
        // Right-click context menu
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem editMenuItem = new JMenuItem("Edit");
        editMenuItem.addActionListener(e -> editSelectedLimit());
        contextMenu.add(editMenuItem);
        
        JMenuItem deleteMenuItem = new JMenuItem("Delete");
        deleteMenuItem.addActionListener(e -> deleteSelectedLimit());
        contextMenu.add(deleteMenuItem);
        
        JMenuItem toggleMenuItem = new JMenuItem("Toggle Active");
        toggleMenuItem.addActionListener(e -> toggleLimitStatus());
        contextMenu.add(toggleMenuItem);
        
        limitTable.setComponentPopupMenu(contextMenu);
    }
    
    private void loadSpendingLimits() {
        tableModel.setRowCount(0);
        List<SpendingLimit> limits = spendingLimitDAO.findAll();
        
        for (SpendingLimit limit : limits) {
            String categoryName = "Overall Spending";
            if (limit.getCategoryId() != null) {
                Category category = categoryDAO.findById(limit.getCategoryId());
                categoryName = category != null ? category.getName() : "Unknown";
            }
            
            // Calculate current spending for the period
            BigDecimal currentSpending = getCurrentSpendingForPeriod(limit);
            BigDecimal remaining = limit.getLimitAmount().subtract(currentSpending);
            double usagePercentage = currentSpending.divide(limit.getLimitAmount(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100)).doubleValue();
            
            Object[] row = {
                limit.getLimitId(),
                categoryName,
                limit.getLimitAmount(),
                currentSpending,
                remaining,
                String.format("%.1f%%", usagePercentage),
                limit.getPeriod()
            };
            tableModel.addRow(row);
        }
    }
    
    private BigDecimal getCurrentSpendingForPeriod(SpendingLimit limit) {
        LocalDate startOfPeriod = getPeriodStartDate(limit.getPeriod());
        LocalDate endOfPeriod = getPeriodEndDate(startOfPeriod, limit.getPeriod());
        
        if (limit.getCategoryId() != null) {
            // Category-specific spending
            return transactionDAO.getTotalExpenses(startOfPeriod, endOfPeriod);
        } else {
            // Overall spending
            return transactionDAO.getTotalExpenses(startOfPeriod, endOfPeriod);
        }
    }
    
    private LocalDate getPeriodStartDate(String period) {
        LocalDate now = LocalDate.now();
        switch (period.toLowerCase()) {
            case "monthly":
                return now.withDayOfMonth(1);
            case "weekly":
                return now.minusDays(now.getDayOfWeek().getValue() - 1);
            case "yearly":
                return now.withDayOfYear(1);
            default:
                return now.withDayOfMonth(1);
        }
    }
    
    private LocalDate getPeriodEndDate(LocalDate startDate, String period) {
        switch (period.toLowerCase()) {
            case "monthly":
                return startDate.withDayOfMonth(startDate.lengthOfMonth());
            case "weekly":
                return startDate.plusDays(6);
            case "yearly":
                return startDate.withDayOfYear(startDate.lengthOfYear());
            default:
                return LocalDate.now();
        }
    }
    
    private void loadCategories() {
        categoryCombo.removeAllItems();
        List<Category> categories = categoryDAO.findAll();
        for (Category category : categories) {
            categoryCombo.addItem(category);
        }
    }
    
    private void addSpendingLimit() {
        try {
            // Validate form
            if (limitAmountField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a limit amount.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            BigDecimal limitAmount = new BigDecimal(limitAmountField.getText().trim());
            if (limitAmount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Limit amount must be greater than zero.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String period = (String) periodCombo.getSelectedItem();
            Integer categoryId = null;
            
            if (!overallLimitCheckBox.isSelected()) {
                Category selectedCategory = (Category) categoryCombo.getSelectedItem();
                if (selectedCategory == null) {
                    JOptionPane.showMessageDialog(this, "Please select a category or check 'Overall spending limit'.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                categoryId = selectedCategory.getCategoryId();
                
                // Check if limit already exists for this category
                if (spendingLimitDAO.existsForCategory(categoryId)) {
                    JOptionPane.showMessageDialog(this, "A spending limit already exists for this category.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                // Check if overall limit already exists
                if (spendingLimitDAO.existsOverallLimit()) {
                    JOptionPane.showMessageDialog(this, "An overall spending limit already exists.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Create spending limit
            SpendingLimit spendingLimit = new SpendingLimit(categoryId, limitAmount, period);
            
            if (spendingLimitDAO.create(spendingLimit)) {
                JOptionPane.showMessageDialog(this, "Spending limit added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadSpendingLimits();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add spending limit.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid limit amount.", "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding spending limit: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearForm() {
        limitAmountField.setText("");
        categoryCombo.setSelectedIndex(0);
        periodCombo.setSelectedIndex(0);
        overallLimitCheckBox.setSelected(false);
        categoryCombo.setEnabled(true);
    }
    
    private void editSelectedLimit() {
        int selectedRow = limitTable.getSelectedRow();
        if (selectedRow >= 0) {
            int limitId = (Integer) tableModel.getValueAt(selectedRow, 0);
            // Open edit dialog
            // Implementation for editing spending limit
        }
    }
    
    private void deleteSelectedLimit() {
        int selectedRow = limitTable.getSelectedRow();
        if (selectedRow >= 0) {
            int limitId = (Integer) tableModel.getValueAt(selectedRow, 0);
            
            int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this spending limit?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (option == JOptionPane.YES_OPTION) {
                if (spendingLimitDAO.delete(limitId)) {
                    JOptionPane.showMessageDialog(this, "Spending limit deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadSpendingLimits();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete spending limit.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void toggleLimitStatus() {
        int selectedRow = limitTable.getSelectedRow();
        if (selectedRow >= 0) {
            int limitId = (Integer) tableModel.getValueAt(selectedRow, 0);
            SpendingLimit limit = spendingLimitDAO.findById(limitId);
            
            if (limit != null) {
                limit.setActive(!limit.isActive());
                if (spendingLimitDAO.update(limit)) {
                    JOptionPane.showMessageDialog(this, "Spending limit status updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadSpendingLimits();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update spending limit status.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    public void refreshData() {
        loadSpendingLimits();
        loadCategories();
    }
}
