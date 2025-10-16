package com.savora.view;

import com.savora.dao.BudgetDAO;
import com.savora.dao.CategoryDAO;
import com.savora.dao.TransactionDAO;
import com.savora.model.Budget;
import com.savora.model.Category;
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
 * Panel for managing budgets with progress tracking
 */
public class BudgetPanel extends JPanel {
    private final MainFrame mainFrame;
    private final BudgetDAO budgetDAO;
    private final CategoryDAO categoryDAO;
    private final TransactionDAO transactionDAO;
    
    private JTable budgetTable;
    private DefaultTableModel tableModel;
    
    private JTextField amountField;
    private JComboBox<Category> categoryCombo;
    private JComboBox<String> periodCombo;
    
    // Colors for modern UI
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color WARNING_COLOR = new Color(245, 158, 11);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color BACKGROUND_COLOR = new Color(248, 250, 252);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    
    public BudgetPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.budgetDAO = new BudgetDAO();
        this.categoryDAO = new CategoryDAO();
        this.transactionDAO = new TransactionDAO();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadBudgets();
    }
    
    private void initializeComponents() {
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        
        // Initialize table
        String[] columnNames = {"ID", "Category", "Budget Amount", "Spent Amount", "Remaining", "Progress", "Period"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) { // Progress column
                    return JProgressBar.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };
        
        budgetTable = new JTable(tableModel);
        budgetTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        budgetTable.setRowHeight(60);
        budgetTable.setShowGrid(false);
        budgetTable.setIntercellSpacing(new Dimension(0, 0));
        budgetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        budgetTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        budgetTable.getTableHeader().setBackground(new Color(249, 250, 251));
        budgetTable.getTableHeader().setForeground(TEXT_PRIMARY);
        
        // Hide ID column
        budgetTable.getColumnModel().getColumn(0).setMinWidth(0);
        budgetTable.getColumnModel().getColumn(0).setMaxWidth(0);
        budgetTable.getColumnModel().getColumn(0).setWidth(0);
        
        // Customize progress column
        budgetTable.getColumnModel().getColumn(5).setCellRenderer(new ProgressBarRenderer());
        
        // Initialize form fields
        initializeFormFields();
    }
    
    private void initializeFormFields() {
        amountField = new JTextField();
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        amountField.setBorder(BorderFactory.createCompoundBorder(
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
        
        loadCategories();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel with title and add button
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel with budget table
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with budget form
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 16, 0));
        
        // Title
        JLabel titleLabel = new JLabel("Budget Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        // Summary panel
        JPanel summaryPanel = createBudgetSummaryPanel();
        
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(summaryPanel, BorderLayout.EAST);
        
        return topPanel;
    }
    
    private JPanel createBudgetSummaryPanel() {
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        summaryPanel.setOpaque(false);
        
        // Total budget card
        JPanel totalBudgetCard = createSummaryCard("Total Budget", "0.00", PRIMARY_COLOR);
        summaryPanel.add(totalBudgetCard);
        
        // Total spent card
        JPanel totalSpentCard = createSummaryCard("Total Spent", "0.00", WARNING_COLOR);
        summaryPanel.add(totalSpentCard);
        
        // Remaining card
        JPanel remainingCard = createSummaryCard("Remaining", "0.00", SUCCESS_COLOR);
        summaryPanel.add(remainingCard);
        
        return summaryPanel;
    }
    
    private JPanel createSummaryCard(String title, String amount, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235)),
            new EmptyBorder(12, 16, 12, 16)
        ));
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_SECONDARY);
        
        // Amount
        JLabel amountLabel = new JLabel("$" + amount);
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        amountLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(amountLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        
        // Budget table
        JScrollPane scrollPane = new JScrollPane(budgetTable);
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
        JLabel formTitleLabel = new JLabel("Add New Budget");
        formTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitleLabel.setForeground(TEXT_PRIMARY);
        formTitleLabel.setBorder(new EmptyBorder(0, 0, 16, 0));
        
        // Form fields panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Category field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(categoryCombo, gbc);
        
        // Amount field
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Budget Amount ($):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(amountField, gbc);
        
        // Period field
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Period:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(periodCombo, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);
        
        // Add button
        JButton addButton = new JButton("Add Budget");
        addButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addButton.setBackground(PRIMARY_COLOR);
        addButton.setForeground(Color.WHITE);
        addButton.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> addBudget());
        
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
        editMenuItem.addActionListener(e -> editSelectedBudget());
        contextMenu.add(editMenuItem);
        
        JMenuItem deleteMenuItem = new JMenuItem("Delete");
        deleteMenuItem.addActionListener(e -> deleteSelectedBudget());
        contextMenu.add(deleteMenuItem);
        
        budgetTable.setComponentPopupMenu(contextMenu);
    }
    
    private void loadBudgets() {
        tableModel.setRowCount(0);
        List<Budget> budgets = budgetDAO.findActiveBudgets();
        
        for (Budget budget : budgets) {
            Category category = categoryDAO.findById(budget.getCategoryId());
            String categoryName = category != null ? category.getName() : "Unknown";
            
            // Calculate spent amount for current period
            LocalDate startDate = getPeriodStartDate(budget.getStartDate(), budget.getPeriod());
            LocalDate endDate = getPeriodEndDate(startDate, budget.getPeriod());
            BigDecimal spentAmount = budgetDAO.getSpentAmountForCategory(budget.getCategoryId(), startDate, endDate);
            
            BigDecimal remaining = budget.getAmount().subtract(spentAmount);
            double progressPercentage = spentAmount.divide(budget.getAmount(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100)).doubleValue();
            
            Object[] row = {
                budget.getBudgetId(),
                categoryName,
                budget.getAmount(),
                spentAmount,
                remaining,
                new ProgressBarData(progressPercentage, spentAmount, budget.getAmount()),
                budget.getPeriod()
            };
            tableModel.addRow(row);
        }
    }
    
    private LocalDate getPeriodStartDate(LocalDate startDate, String period) {
        LocalDate now = LocalDate.now();
        switch (period.toLowerCase()) {
            case "monthly":
                return now.withDayOfMonth(1);
            case "weekly":
                return now.minusDays(now.getDayOfWeek().getValue() - 1);
            case "yearly":
                return now.withDayOfYear(1);
            default:
                return startDate;
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
    
    private void addBudget() {
        try {
            // Validate form
            if (amountField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a budget amount.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Budget amount must be greater than zero.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Category selectedCategory = (Category) categoryCombo.getSelectedItem();
            if (selectedCategory == null) {
                JOptionPane.showMessageDialog(this, "Please select a category.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String period = (String) periodCombo.getSelectedItem();
            LocalDate startDate = LocalDate.now();
            
            // Create budget
            Budget budget = new Budget(selectedCategory.getCategoryId(), amount, period, startDate);
            
            if (budgetDAO.create(budget)) {
                JOptionPane.showMessageDialog(this, "Budget added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadBudgets();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add budget.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid budget amount.", "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding budget: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearForm() {
        amountField.setText("");
        categoryCombo.setSelectedIndex(0);
        periodCombo.setSelectedIndex(0);
    }
    
    private void editSelectedBudget() {
        int selectedRow = budgetTable.getSelectedRow();
        if (selectedRow >= 0) {
            int budgetId = (Integer) tableModel.getValueAt(selectedRow, 0);
            // Open edit dialog
            // Implementation for editing budget
        }
    }
    
    private void deleteSelectedBudget() {
        int selectedRow = budgetTable.getSelectedRow();
        if (selectedRow >= 0) {
            int budgetId = (Integer) tableModel.getValueAt(selectedRow, 0);
            
            int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this budget?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (option == JOptionPane.YES_OPTION) {
                if (budgetDAO.delete(budgetId)) {
                    JOptionPane.showMessageDialog(this, "Budget deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadBudgets();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete budget.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    public void refreshData() {
        loadBudgets();
        loadCategories();
    }
    
    // Progress bar data class
    private static class ProgressBarData {
        private final double percentage;
        private final BigDecimal spent;
        private final BigDecimal budget;
        
        public ProgressBarData(double percentage, BigDecimal spent, BigDecimal budget) {
            this.percentage = percentage;
            this.spent = spent;
            this.budget = budget;
        }
        
        public double getPercentage() { return percentage; }
        public BigDecimal getSpent() { return spent; }
        public BigDecimal getBudget() { return budget; }
    }
    
    // Custom progress bar renderer
    private class ProgressBarRenderer extends JProgressBar implements javax.swing.table.TableCellRenderer {
        public ProgressBarRenderer() {
            super(0, 100);
            setStringPainted(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 10));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof ProgressBarData) {
                ProgressBarData data = (ProgressBarData) value;
                double percentage = data.getPercentage();
                
                setValue((int) percentage);
                
                // Set color based on percentage
                if (percentage >= 100) {
                    setForeground(DANGER_COLOR);
                    setString(String.format("%.1f%% (Over Budget)", percentage));
                } else if (percentage >= 80) {
                    setForeground(WARNING_COLOR);
                    setString(String.format("%.1f%% (Warning)", percentage));
                } else {
                    setForeground(SUCCESS_COLOR);
                    setString(String.format("%.1f%%", percentage));
                }
                
                // Set background color
                if (isSelected) {
                    setBackground(table.getSelectionBackground());
                } else {
                    setBackground(table.getBackground());
                }
            }
            
            return this;
        }
    }
}
