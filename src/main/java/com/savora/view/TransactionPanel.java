package com.savora.view;

import com.savora.dao.CategoryDAO;
import com.savora.dao.TransactionDAO;
import com.savora.model.Category;
import com.savora.model.Transaction;
import com.savora.util.NotificationManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for managing transactions with Google Pay-inspired design
 */
public class TransactionPanel extends JPanel {
    private final MainFrame mainFrame;
    private final TransactionDAO transactionDAO;
    private final CategoryDAO categoryDAO;
    private final NotificationManager notificationManager;
    
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    
    private JTextField amountField;
    private JTextField descriptionField;
    private JComboBox<Category> categoryCombo;
    private JComboBox<Transaction.TransactionType> typeCombo;
    private JFormattedTextField dateField;
    
    // Colors for modern UI
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color BACKGROUND_COLOR = new Color(248, 250, 252);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    
    public TransactionPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.transactionDAO = new TransactionDAO();
        this.categoryDAO = new CategoryDAO();
        this.notificationManager = new NotificationManager();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadTransactions();
    }
    
    private void initializeComponents() {
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        
        // Initialize table
        String[] columnNames = {"ID", "Date", "Category", "Description", "Amount", "Type"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        transactionTable = new JTable(tableModel);
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        transactionTable.setRowHeight(48);
        transactionTable.setShowGrid(false);
        transactionTable.setIntercellSpacing(new Dimension(0, 0));
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        transactionTable.getTableHeader().setBackground(new Color(249, 250, 251));
        transactionTable.getTableHeader().setForeground(TEXT_PRIMARY);
        
        // Hide ID column
        transactionTable.getColumnModel().getColumn(0).setMinWidth(0);
        transactionTable.getColumnModel().getColumn(0).setMaxWidth(0);
        transactionTable.getColumnModel().getColumn(0).setWidth(0);
        
        // Customize amount column
        transactionTable.getColumnModel().getColumn(4).setCellRenderer(new AmountCellRenderer());
        
        // Customize type column
        transactionTable.getColumnModel().getColumn(5).setCellRenderer(new TypeCellRenderer());
        
        tableSorter = new TableRowSorter<>(tableModel);
        transactionTable.setRowSorter(tableSorter);
        
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
        
        descriptionField = new JTextField();
        descriptionField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            new EmptyBorder(12, 16, 12, 16)
        ));
        
        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        categoryCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        // Render Category objects by name to avoid showing the full toString()
        categoryCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Category) {
                    setText(((Category) value).getName());
                }
                return this;
            }
        });
        
        typeCombo = new JComboBox<>(Transaction.TransactionType.values());
        typeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        dateField = new JFormattedTextField();
        dateField.setValue(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dateField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            new EmptyBorder(12, 16, 12, 16)
        ));
        
        loadCategories();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel with filters and add button
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel with transaction table
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with transaction form
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        // Make the top panel paint its background so the white card below doesn't show through
        topPanel.setOpaque(true);
        topPanel.setBackground(BACKGROUND_COLOR);
        topPanel.setBorder(new EmptyBorder(0, 0, 24, 0));
        
        // Title
        JLabel titleLabel = new JLabel("Transaction Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);

        // Wrap the title in a small white "card" to visually separate it from the table card below
        JPanel titleCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleCard.setBackground(CARD_COLOR);
        titleCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235)),
            new EmptyBorder(10, 12, 10, 12)
        ));
        titleCard.add(titleLabel);
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);
        
        // Type filter
        JLabel typeFilterLabel = new JLabel("Type:");
        typeFilterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        typeFilterLabel.setForeground(TEXT_SECONDARY);
        
        JComboBox<Transaction.TransactionType> typeFilter = new JComboBox<>();
        typeFilter.addItem(null); // All types
        for (Transaction.TransactionType type : Transaction.TransactionType.values()) {
            typeFilter.addItem(type);
        }
        typeFilter.addActionListener(e -> filterTransactions(typeFilter, null, null));
        
        // Category filter
        JLabel categoryFilterLabel = new JLabel("Category:");
        categoryFilterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryFilterLabel.setForeground(TEXT_SECONDARY);
        
        JComboBox<Category> categoryFilter = new JComboBox<>();
        categoryFilter.addItem(null); // All categories
        // Render categories by name (and show friendly label for null)
        categoryFilter.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Category) {
                    setText(((Category) value).getName());
                } else if (value == null) {
                    setText("All Categories");
                }
                return this;
            }
        });
        List<Category> categories = categoryDAO.findAll();
        for (Category category : categories) {
            categoryFilter.addItem(category);
        }
        categoryFilter.addActionListener(e -> filterTransactions(null, categoryFilter, null));
        
        // Date filter
        JLabel dateFilterLabel = new JLabel("Date:");
        dateFilterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateFilterLabel.setForeground(TEXT_SECONDARY);
        
        JFormattedTextField dateFilter = new JFormattedTextField();
        dateFilter.setColumns(10);
        dateFilter.addActionListener(e -> filterTransactions(null, null, dateFilter));
        
        // Clear filters button
        JButton clearFiltersButton = new JButton("Clear Filters");
        clearFiltersButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearFiltersButton.setBackground(new Color(156, 163, 175));
        clearFiltersButton.setForeground(Color.WHITE);
        clearFiltersButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        clearFiltersButton.setFocusPainted(false);
        clearFiltersButton.addActionListener(e -> {
            typeFilter.setSelectedItem(null);
            categoryFilter.setSelectedItem(null);
            dateFilter.setValue("");
            tableSorter.setRowFilter(null);
        });
        
        // Refresh button
        JButton refreshButton = new JButton("🔄 Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshButton.setBackground(PRIMARY_COLOR);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> {
            loadTransactions();
            JOptionPane.showMessageDialog(this, "Transactions refreshed!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        
        filterPanel.add(typeFilterLabel);
        filterPanel.add(typeFilter);
        filterPanel.add(categoryFilterLabel);
        filterPanel.add(categoryFilter);
        filterPanel.add(dateFilterLabel);
        filterPanel.add(dateFilter);
        filterPanel.add(clearFiltersButton);
        filterPanel.add(refreshButton);
        
        topPanel.add(titleCard, BorderLayout.WEST);
        topPanel.add(filterPanel, BorderLayout.EAST);
        
        return topPanel;
    }
    
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        // Transaction table inside a white card so sections are distinct
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(CARD_COLOR);
        scrollPane.getViewport().setBackground(CARD_COLOR);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(CARD_COLOR);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235)),
            new EmptyBorder(12, 12, 12, 12)
        ));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Add a small top gap so the card doesn't butt directly under the title
        centerPanel.setBorder(new EmptyBorder(8, 0, 8, 0));
        centerPanel.add(tableCard, BorderLayout.CENTER);

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
        JLabel formTitleLabel = new JLabel("Add New Transaction");
        formTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitleLabel.setForeground(TEXT_PRIMARY);
        formTitleLabel.setBorder(new EmptyBorder(0, 0, 16, 0));
        
        // Form fields panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Amount field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Amount ($):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(amountField, gbc);
        
        // Date field
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(dateField, gbc);
        
        // Category field
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(categoryCombo, gbc);
        
        // Type field
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(typeCombo, gbc);
        
        // Description field
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(descriptionField, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);
        
        // Add button
        JButton addButton = new JButton("Add Transaction");
        addButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addButton.setBackground(PRIMARY_COLOR);
        addButton.setForeground(Color.WHITE);
        addButton.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> addTransaction());
        
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
        // Double-click to edit transaction
        transactionTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedTransaction();
                }
            }
        });
        
        // Right-click context menu
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem editMenuItem = new JMenuItem("Edit");
        editMenuItem.addActionListener(e -> editSelectedTransaction());
        contextMenu.add(editMenuItem);
        
        JMenuItem deleteMenuItem = new JMenuItem("Delete");
        deleteMenuItem.addActionListener(e -> deleteSelectedTransaction());
        contextMenu.add(deleteMenuItem);
        
        transactionTable.setComponentPopupMenu(contextMenu);
    }
    
    private void filterTransactions(JComboBox<Transaction.TransactionType> typeFilter, 
                                   JComboBox<Category> categoryFilter, 
                                   JFormattedTextField dateFilter) {
        // Implementation for filtering transactions
        // This would set up row filters based on the selected criteria
    }
    
    private void loadTransactions() {
        SwingUtilities.invokeLater(() -> {
            // Clear any row filters before repopulating
            if (tableSorter != null) {
                tableSorter.setRowFilter(null);
            }
            tableModel.setRowCount(0);
            List<Transaction> transactions = transactionDAO.findAll();
            
            System.out.println("Loading " + transactions.size() + " transactions...");
            
            for (Transaction transaction : transactions) {
                Category category = categoryDAO.findById(transaction.getCategoryId());
                String categoryName = category != null ? category.getName() : "Unknown";
                
                Object[] row = {
                    transaction.getTransactionId(),
                    transaction.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    categoryName,
                    transaction.getDescription() != null ? transaction.getDescription() : "",
                    transaction.getAmount(),
                    transaction.getType()
                };
                tableModel.addRow(row);
            }
            
            tableModel.fireTableDataChanged();
            transactionTable.revalidate();
            transactionTable.repaint();
            System.out.println("Table updated with " + tableModel.getRowCount() + " rows");
        });
    }
    
    private void loadCategories() {
        categoryCombo.removeAllItems();
        List<Category> categories = categoryDAO.findAll();
        for (Category category : categories) {
            categoryCombo.addItem(category);
        }
    }
    
    private void addTransaction() {
        try {
            // Validate form
            if (amountField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an amount.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than zero.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            LocalDate date = LocalDate.now();
            try {
                String dateStr = dateField.getText().trim();
                if (!dateStr.isEmpty()) {
                    date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
            } catch (Exception ex) {
                // If parsing fails, use current date
                System.err.println("Failed to parse date, using current date: " + ex.getMessage());
            }
            
            Category selectedCategory = (Category) categoryCombo.getSelectedItem();
            if (selectedCategory == null) {
                JOptionPane.showMessageDialog(this, "Please select a category.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Transaction.TransactionType type = (Transaction.TransactionType) typeCombo.getSelectedItem();
            String description = descriptionField.getText().trim();
            
            // Create transaction
            Transaction transaction = new Transaction(amount, date, selectedCategory.getCategoryId(), description, type);
            
            if (transactionDAO.create(transaction)) {
                JOptionPane.showMessageDialog(this, "Transaction added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadTransactions();
                mainFrame.loadDashboardData(); // Refresh dashboard
                
                // Check spending limits and show notifications
                notificationManager.checkSpendingLimits(transaction);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add transaction.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.", "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding transaction: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearForm() {
        amountField.setText("");
        descriptionField.setText("");
        dateField.setValue(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        categoryCombo.setSelectedIndex(0);
        typeCombo.setSelectedIndex(0);
    }
    
    private void editSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow >= 0) {
            int transactionId = (Integer) tableModel.getValueAt(selectedRow, 0);
            // Open edit dialog
            // Implementation for editing transaction
        }
    }
    
    private void deleteSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow >= 0) {
            int transactionId = (Integer) tableModel.getValueAt(selectedRow, 0);
            
            int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this transaction?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (option == JOptionPane.YES_OPTION) {
                if (transactionDAO.delete(transactionId)) {
                    JOptionPane.showMessageDialog(this, "Transaction deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadTransactions();
                    mainFrame.loadDashboardData(); // Refresh dashboard
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete transaction.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    public void refreshData() {
        loadTransactions();
        loadCategories();
    }
    
    // Custom cell renderers
    private class AmountCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof BigDecimal) {
                BigDecimal amount = (BigDecimal) value;
                setText("$" + amount.toString());
                setHorizontalAlignment(SwingConstants.RIGHT);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
            }
            
            return this;
        }
    }
    
    private class TypeCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Transaction.TransactionType) {
                Transaction.TransactionType type = (Transaction.TransactionType) value;
                setText(type.toString());
                setHorizontalAlignment(SwingConstants.CENTER);
                
                if (type == Transaction.TransactionType.INCOME) {
                    setForeground(SUCCESS_COLOR);
                } else {
                    setForeground(DANGER_COLOR);
                }
            }
            
            return this;
        }
    }
}
