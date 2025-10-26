package com.savora.view;

import com.savora.dao.TransactionDAO;
import com.savora.dao.CategoryDAO;
import javax.swing.table.DefaultTableModel;
import com.savora.model.Transaction;
import com.savora.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main application window for Savora Finance Tracker
 */
public class MainFrame extends JFrame {
    private TransactionDAO transactionDAO;
    private JLabel balanceLabel;
    private JLabel incomeLabel;
    private JLabel expenseLabel;
    private JTabbedPane tabbedPane;
    private TransactionPanel transactionPanel;
    private BudgetPanel budgetPanel;
    private AnalyticsPanel analyticsPanel;
    private SpendingLimitPanel spendingLimitPanel;
    private StockMarketPanel stockMarketPanel;
    private EducationPanel educationPanel;
    // Recent transactions table on dashboard
    private JTable recentTable;
    private DefaultTableModel recentTableModel;
    private CategoryDAO categoryDAO;
    
    // Colors for modern UI
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color BACKGROUND_COLOR = new Color(248, 250, 252);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    
    public MainFrame() {
        this.transactionDAO = new TransactionDAO();
        this.categoryDAO = new CategoryDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadDashboardData();
    }
    
    private void initializeComponents() {
        setTitle("Savora Finance Tracker");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Set application icon
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon.png")));
        } catch (Exception e) {
            // Icon not found, continue without it
        }
        
        // Initialize panels
        transactionPanel = new TransactionPanel(this);
        budgetPanel = new BudgetPanel(this);
        analyticsPanel = new AnalyticsPanel(this);
        spendingLimitPanel = new SpendingLimitPanel();
        stockMarketPanel = new StockMarketPanel(this);
        educationPanel = new EducationPanel();
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BACKGROUND_COLOR);
        tabbedPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        // Add tabs with icons
        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Transactions", transactionPanel);
        tabbedPane.addTab("Budgets", budgetPanel);
        tabbedPane.addTab("Spending Limits", spendingLimitPanel);
        tabbedPane.addTab("Analytics", analyticsPanel);
        tabbedPane.addTab("Stock Market", stockMarketPanel);
        tabbedPane.addTab("📚 Education", educationPanel);
        
        // Set modern tab appearance
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setBackgroundAt(i, BACKGROUND_COLOR);
        }
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        
        // Create header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Add main content
        add(tabbedPane, BorderLayout.CENTER);
        
        // Create status bar
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
            new EmptyBorder(16, 24, 16, 24)
        ));
        
        // App title
        JLabel titleLabel = new JLabel("Savora Finance Tracker");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        // Current date
        JLabel dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(TEXT_SECONDARY);
        
        // Header layout
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(titleLabel, BorderLayout.CENTER);
        leftPanel.add(dateLabel, BorderLayout.SOUTH);
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshButton.setBackground(PRIMARY_COLOR);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> refreshAllData());
        
        headerPanel.add(leftPanel, BorderLayout.CENTER);
        headerPanel.add(refreshButton, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(BACKGROUND_COLOR);
        dashboardPanel.setBorder(new EmptyBorder(24, 24, 24, 24));
        
        // Summary cards
        JPanel summaryPanel = createSummaryPanel();
        dashboardPanel.add(summaryPanel, BorderLayout.NORTH);
        
        // Recent transactions
        JPanel recentTransactionsPanel = createRecentTransactionsPanel();
        dashboardPanel.add(recentTransactionsPanel, BorderLayout.CENTER);
        
        return dashboardPanel;
    }
    
    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 16, 16));
        summaryPanel.setOpaque(false);
        
        // Balance card
        JPanel balanceCard = createSummaryCard("Total Balance", "0.00", PRIMARY_COLOR, "balance");
        summaryPanel.add(balanceCard);
        
        // Income card
        JPanel incomeCard = createSummaryCard("Total Income", "0.00", SUCCESS_COLOR, "income");
        summaryPanel.add(incomeCard);
        
        // Expense card
        JPanel expenseCard = createSummaryCard("Total Expenses", "0.00", DANGER_COLOR, "expense");
        summaryPanel.add(expenseCard);
        
        return summaryPanel;
    }
    
    private JPanel createSummaryCard(String title, String amount, Color color, String type) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(TEXT_SECONDARY);
        
        // Amount
        JLabel amountLabel = new JLabel("$" + amount);
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        amountLabel.setForeground(color);
        
        // Store reference for updating
        if ("balance".equals(type)) {
            balanceLabel = amountLabel;
        } else if ("income".equals(type)) {
            incomeLabel = amountLabel;
        } else if ("expense".equals(type)) {
            expenseLabel = amountLabel;
        }
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(amountLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createRecentTransactionsPanel() {
        JPanel recentPanel = new JPanel(new BorderLayout());
        recentPanel.setOpaque(false);
        
        // Title
        JLabel titleLabel = new JLabel("Recent Transactions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, 16, 0));
        
        // Table for recent transactions
        String[] columnNames = {"Date", "Category", "Description", "Amount", "Type"};
        recentTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recentTable = new JTable(recentTableModel);
        recentTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        recentTable.setRowHeight(32);
        recentTable.setShowGrid(false);
        recentTable.setIntercellSpacing(new Dimension(0, 0));
        recentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        recentTable.getTableHeader().setBackground(new Color(249, 250, 251));
        recentTable.getTableHeader().setForeground(TEXT_PRIMARY);
        
        JScrollPane scrollPane = new JScrollPane(recentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        scrollPane.setBackground(CARD_COLOR);
        
        recentPanel.add(titleLabel, BorderLayout.NORTH);
        recentPanel.add(scrollPane, BorderLayout.CENTER);
        
        return recentPanel;
    }
    
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(249, 250, 251));
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)),
            new EmptyBorder(8, 16, 8, 16)
        ));
        
        JLabel statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);
        
        JLabel connectionLabel = new JLabel("Database: Connected");
        connectionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        connectionLabel.setForeground(SUCCESS_COLOR);
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(connectionLabel, BorderLayout.EAST);
        
        return statusPanel;
    }
    
    private void setupEventHandlers() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "Are you sure you want to exit?",
                    "Exit Application",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (option == JOptionPane.YES_OPTION) {
                    DatabaseConnection.getInstance().closeConnection();
                    System.exit(0);
                }
            }
        });
    }
    
    public void loadDashboardData() {
        try {
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
            
            BigDecimal totalIncome = transactionDAO.getTotalIncome(startOfMonth, endOfMonth);
            BigDecimal totalExpenses = transactionDAO.getTotalExpenses(startOfMonth, endOfMonth);
            BigDecimal balance = totalIncome.subtract(totalExpenses);
            
            // Update summary labels
            balanceLabel.setText("$" + balance.toString());
            incomeLabel.setText("$" + totalIncome.toString());
            expenseLabel.setText("$" + totalExpenses.toString());
            
            // Set colors based on balance
            if (balance.compareTo(BigDecimal.ZERO) >= 0) {
                balanceLabel.setForeground(SUCCESS_COLOR);
            } else {
                balanceLabel.setForeground(DANGER_COLOR);
            }
            // Populate recent transactions table (show latest 10)
            if (recentTableModel != null) {
                recentTableModel.setRowCount(0);
                List<Transaction> all = transactionDAO.findAll();
                int limit = Math.min(all.size(), 10);
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                for (int i = 0; i < limit; i++) {
                    Transaction t = all.get(i);
                    String categoryName = "Unknown";
                    try {
                        com.savora.model.Category cat = categoryDAO.findById(t.getCategoryId());
                        if (cat != null) categoryName = cat.getName();
                    } catch (Exception ex) {
                        // ignore
                    }
                    Object[] row = new Object[] {
                        t.getDate().format(fmt),
                        categoryName,
                        t.getDescription() != null ? t.getDescription() : "",
                        "$" + t.getAmount().toString(),
                        t.getType().toString()
                    };
                    recentTableModel.addRow(row);
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Failed to load dashboard data: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void refreshAllData() {
        loadDashboardData();
        transactionPanel.refreshData();
        budgetPanel.refreshData();
        spendingLimitPanel.refreshData();
        analyticsPanel.refreshData();
    }
    
    // Getters for panels
    public TransactionPanel getTransactionPanel() {
        return transactionPanel;
    }
    
    public BudgetPanel getBudgetPanel() {
        return budgetPanel;
    }
    
    public AnalyticsPanel getAnalyticsPanel() {
        return analyticsPanel;
    }
}
