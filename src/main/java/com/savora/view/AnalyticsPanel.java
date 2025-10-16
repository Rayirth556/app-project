package com.savora.view;

import com.savora.dao.CategoryDAO;
import com.savora.dao.TransactionDAO;
import com.savora.model.Category;
import com.savora.model.Transaction;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel for displaying analytics and reports with charts
 */
public class AnalyticsPanel extends JPanel {
    private final MainFrame mainFrame;
    private final TransactionDAO transactionDAO;
    private final CategoryDAO categoryDAO;
    
    private JComboBox<String> timeRangeCombo;
    private JPanel chartPanel;
    private JFreeChart pieChart;
    private JFreeChart barChart;
    private JFreeChart lineChart;
    
    // Colors for modern UI
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color WARNING_COLOR = new Color(245, 158, 11);
    private static final Color BACKGROUND_COLOR = new Color(248, 250, 252);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    
    // Chart colors
    private static final Color[] CHART_COLORS = {
        new Color(59, 130, 246),   // Blue
        new Color(34, 197, 94),    // Green
        new Color(239, 68, 68),    // Red
        new Color(245, 158, 11),   // Yellow
        new Color(168, 85, 247),   // Purple
        new Color(236, 72, 153),   // Pink
        new Color(14, 165, 233),   // Sky Blue
        new Color(251, 146, 60),   // Orange
        new Color(34, 211, 238),   // Cyan
        new Color(132, 204, 22)    // Lime
    };
    
    public AnalyticsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.transactionDAO = new TransactionDAO();
        this.categoryDAO = new CategoryDAO();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        generateCharts();
    }
    
    private void initializeComponents() {
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        
        // Time range combo box
        timeRangeCombo = new JComboBox<>(new String[]{
            "Last 7 Days", "Last 30 Days", "Last 3 Months", "Last 6 Months", "This Year", "All Time"
        });
        timeRangeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timeRangeCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        // Chart panel
        chartPanel = new JPanel(new GridLayout(2, 2, 16, 16));
        chartPanel.setOpaque(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel with controls
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel with charts
        add(chartPanel, BorderLayout.CENTER);
        
        // Bottom panel with export options
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 16, 0));
        
        // Title
        JLabel titleLabel = new JLabel("Analytics & Reports");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        // Controls panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controlsPanel.setOpaque(false);
        
        // Time range label
        JLabel timeRangeLabel = new JLabel("Time Range:");
        timeRangeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeRangeLabel.setForeground(TEXT_SECONDARY);
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh Charts");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshButton.setBackground(PRIMARY_COLOR);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> generateCharts());
        
        controlsPanel.add(timeRangeLabel);
        controlsPanel.add(timeRangeCombo);
        controlsPanel.add(refreshButton);
        
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(controlsPanel, BorderLayout.EAST);
        
        return topPanel;
    }
    
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(16, 0, 0, 0));
        
        // Export CSV button
        JButton exportButton = new JButton("Export CSV Report");
        exportButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        exportButton.setBackground(SUCCESS_COLOR);
        exportButton.setForeground(Color.WHITE);
        exportButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        exportButton.setFocusPainted(false);
        exportButton.addActionListener(e -> exportCSVReport());
        
        bottomPanel.add(exportButton);
        
        return bottomPanel;
    }
    
    private void setupEventHandlers() {
        timeRangeCombo.addActionListener(e -> generateCharts());
    }
    
    private void generateCharts() {
        chartPanel.removeAll();
        
        // Get date range
        LocalDate[] dateRange = getDateRange();
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];
        
        // Generate pie chart for expense distribution
        pieChart = createExpensePieChart(startDate, endDate);
        ChartPanel pieChartPanel = new ChartPanel(pieChart);
        pieChartPanel.setPreferredSize(new Dimension(400, 300));
        pieChartPanel.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        chartPanel.add(pieChartPanel);
        
        // Generate bar chart for income vs expenses
        barChart = createIncomeExpenseBarChart(startDate, endDate);
        ChartPanel barChartPanel = new ChartPanel(barChart);
        barChartPanel.setPreferredSize(new Dimension(400, 300));
        barChartPanel.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        chartPanel.add(barChartPanel);
        
        // Generate line chart for spending trends
        lineChart = createSpendingTrendLineChart(startDate, endDate);
        ChartPanel lineChartPanel = new ChartPanel(lineChart);
        lineChartPanel.setPreferredSize(new Dimension(400, 300));
        lineChartPanel.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        chartPanel.add(lineChartPanel);
        
        // Generate summary report
        JPanel summaryPanel = createSummaryReportPanel(startDate, endDate);
        chartPanel.add(summaryPanel);
        
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    private LocalDate[] getDateRange() {
        LocalDate now = LocalDate.now();
        String selectedRange = (String) timeRangeCombo.getSelectedItem();
        
        switch (selectedRange) {
            case "Last 7 Days":
                return new LocalDate[]{now.minusDays(7), now};
            case "Last 30 Days":
                return new LocalDate[]{now.minusDays(30), now};
            case "Last 3 Months":
                return new LocalDate[]{now.minusMonths(3), now};
            case "Last 6 Months":
                return new LocalDate[]{now.minusMonths(6), now};
            case "This Year":
                return new LocalDate[]{now.withDayOfYear(1), now};
            case "All Time":
                return new LocalDate[]{LocalDate.of(2020, 1, 1), now};
            default:
                return new LocalDate[]{now.minusDays(30), now};
        }
    }
    
    private JFreeChart createExpensePieChart(LocalDate startDate, LocalDate endDate) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        // Get expense transactions by category
        List<Transaction> expenses = transactionDAO.findByType(Transaction.TransactionType.EXPENSE);
        Map<Integer, BigDecimal> categoryExpenses = new HashMap<>();
        
        for (Transaction transaction : expenses) {
            if (transaction.getDate().isAfter(startDate.minusDays(1)) && 
                transaction.getDate().isBefore(endDate.plusDays(1))) {
                categoryExpenses.merge(transaction.getCategoryId(), transaction.getAmount(), BigDecimal::add);
            }
        }
        
        // Add categories to dataset
        for (Map.Entry<Integer, BigDecimal> entry : categoryExpenses.entrySet()) {
            Category category = categoryDAO.findById(entry.getKey());
            String categoryName = category != null ? category.getName() : "Unknown";
            dataset.setValue(categoryName, entry.getValue().doubleValue());
        }
        
        JFreeChart chart = ChartFactory.createPieChart(
            "Expense Distribution by Category",
            dataset,
            true, true, false
        );
        
        // Customize chart appearance
        PiePlot plot = (PiePlot) chart.getPlot();
        for (int i = 0; i < CHART_COLORS.length && i < dataset.getItemCount(); i++) {
            plot.setSectionPaint(dataset.getKey(i), CHART_COLORS[i]);
        }
        
        chart.setBackgroundPaint(CARD_COLOR);
        plot.setBackgroundPaint(CARD_COLOR);
        plot.setOutlineVisible(false);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        
        return chart;
    }
    
    private JFreeChart createIncomeExpenseBarChart(LocalDate startDate, LocalDate endDate) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Get income and expense totals
        BigDecimal totalIncome = transactionDAO.getTotalIncome(startDate, endDate);
        BigDecimal totalExpenses = transactionDAO.getTotalExpenses(startDate, endDate);
        
        dataset.addValue(totalIncome.doubleValue(), "Income", "Total");
        dataset.addValue(totalExpenses.doubleValue(), "Expenses", "Total");
        
        JFreeChart chart = ChartFactory.createBarChart(
            "Income vs Expenses",
            "Category",
            "Amount ($)",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false
        );
        
        // Customize chart appearance
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(CARD_COLOR);
        plot.getRenderer().setSeriesPaint(0, SUCCESS_COLOR);
        plot.getRenderer().setSeriesPaint(1, DANGER_COLOR);
        
        chart.setBackgroundPaint(CARD_COLOR);
        
        return chart;
    }
    
    private JFreeChart createSpendingTrendLineChart(LocalDate startDate, LocalDate endDate) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        
        // Create time series for expenses
        TimeSeries expenseSeries = new TimeSeries("Daily Expenses");
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            List<Transaction> dayExpenses = transactionDAO.findByDateRange(current, current);
            BigDecimal dayTotal = dayExpenses.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Date date = Date.from(current.atStartOfDay(ZoneId.systemDefault()).toInstant());
            expenseSeries.add(new Day(date), dayTotal.doubleValue());
            
            current = current.plusDays(1);
        }
        
        dataset.addSeries(expenseSeries);
        
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Daily Spending Trend",
            "Date",
            "Amount ($)",
            dataset,
            true, true, false
        );
        
        // Customize chart appearance
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(CARD_COLOR);
        plot.getRenderer().setSeriesPaint(0, PRIMARY_COLOR);
        
        DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
        dateAxis.setDateFormatOverride(new java.text.SimpleDateFormat("MMM dd"));
        
        NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();
        numberAxis.setNumberFormatOverride(java.text.NumberFormat.getCurrencyInstance());
        
        chart.setBackgroundPaint(CARD_COLOR);
        
        return chart;
    }
    
    private JPanel createSummaryReportPanel(LocalDate startDate, LocalDate endDate) {
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBackground(CARD_COLOR);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235)),
            new EmptyBorder(16, 16, 16, 16)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("Summary Report");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, 12, 0));
        
        // Calculate summary data
        BigDecimal totalIncome = transactionDAO.getTotalIncome(startDate, endDate);
        BigDecimal totalExpenses = transactionDAO.getTotalExpenses(startDate, endDate);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);
        
        // Summary data panel
        JPanel dataPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        dataPanel.setOpaque(false);
        
        // Period
        dataPanel.add(createSummaryItem("Period", startDate + " to " + endDate, TEXT_SECONDARY));
        dataPanel.add(new JLabel()); // Empty cell
        
        // Total Income
        dataPanel.add(createSummaryItem("Total Income", "$" + totalIncome.toString(), SUCCESS_COLOR));
        dataPanel.add(new JLabel()); // Empty cell
        
        // Total Expenses
        dataPanel.add(createSummaryItem("Total Expenses", "$" + totalExpenses.toString(), DANGER_COLOR));
        dataPanel.add(new JLabel()); // Empty cell
        
        // Net Balance
        Color balanceColor = netBalance.compareTo(BigDecimal.ZERO) >= 0 ? SUCCESS_COLOR : DANGER_COLOR;
        dataPanel.add(createSummaryItem("Net Balance", "$" + netBalance.toString(), balanceColor));
        dataPanel.add(new JLabel()); // Empty cell
        
        summaryPanel.add(titleLabel, BorderLayout.NORTH);
        summaryPanel.add(dataPanel, BorderLayout.CENTER);
        
        return summaryPanel;
    }
    
    private JLabel createSummaryItem(String label, String value, Color valueColor) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setOpaque(false);
        
        JLabel labelLabel = new JLabel(label + ":");
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelLabel.setForeground(TEXT_SECONDARY);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(valueColor);
        
        itemPanel.add(labelLabel, BorderLayout.WEST);
        itemPanel.add(valueLabel, BorderLayout.EAST);
        
        return new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Custom painting logic if needed
            }
        };
    }
    
    private void exportCSVReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("savora_report.csv"));
        
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            try (FileWriter writer = new FileWriter(file)) {
                LocalDate[] dateRange = getDateRange();
                LocalDate startDate = dateRange[0];
                LocalDate endDate = dateRange[1];
                
                // Write CSV header
                writer.append("Date,Category,Description,Amount,Type\n");
                
                // Write transaction data
                List<Transaction> transactions = transactionDAO.findByDateRange(startDate, endDate);
                for (Transaction transaction : transactions) {
                    Category category = categoryDAO.findById(transaction.getCategoryId());
                    String categoryName = category != null ? category.getName() : "Unknown";
                    
                    writer.append(String.format("%s,%s,%s,%.2f,%s\n",
                        transaction.getDate().toString(),
                        categoryName,
                        transaction.getDescription() != null ? transaction.getDescription() : "",
                        transaction.getAmount().doubleValue(),
                        transaction.getType().getValue()
                    ));
                }
                
                JOptionPane.showMessageDialog(this,
                    "Report exported successfully to: " + file.getName(),
                    "Export Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Failed to export report: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void refreshData() {
        generateCharts();
    }
}
