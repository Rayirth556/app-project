package com.savora.view;

import com.savora.dao.*;
import com.savora.model.*;
import com.savora.service.OrderExecutor;
import com.savora.service.MarketNewsGenerator;
import com.savora.model.MarketNews;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel for stock market simulation
 */
public class StockMarketPanel extends JPanel {
    private final MainFrame mainFrame;
    private final SymbolDAO symbolDAO;
    private final MarketDataDAO marketDataDAO;
    private final AccountDAO accountDAO;
    private final PositionDAO positionDAO;
    private final OrderDAO orderDAO;
    private final TradeDAO tradeDAO;
    private final OrderExecutor orderExecutor;
    private final MarketNewsGenerator newsGenerator;
    
    private JLabel cashLabel;
    private JLabel portfolioValueLabel;
    private JLabel totalValueLabel;
    private JComboBox<StockSymbol> symbolCombo;
    private JTextField quantityField;
    private JTextField limitPriceField;
    private JComboBox<Order.OrderType> orderTypeCombo;
    private JComboBox<Order.Side> sideCombo;
    
    private DefaultTableModel positionsTableModel;
    private JTable positionsTable;
    private DefaultTableModel ordersTableModel;
    private JTable ordersTable;
    private DefaultTableModel tradesTableModel;
    private JTable tradesTable;
    
    private ChartPanel chartPanel;
    
    private static final int ACCOUNT_ID = 1; // Default demo account
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color BACKGROUND_COLOR = new Color(248, 250, 252);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    
    public StockMarketPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.symbolDAO = new SymbolDAO();
        this.marketDataDAO = new MarketDataDAO();
        this.accountDAO = new AccountDAO();
        this.positionDAO = new PositionDAO();
        this.orderDAO = new OrderDAO();
        this.tradeDAO = new TradeDAO();
        this.orderExecutor = new OrderExecutor();
        this.newsGenerator = new MarketNewsGenerator();
        
        initializeComponents();
        setupLayout();
        loadData();
    }
    
    private void initializeComponents() {
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        
        // Account summary labels
        cashLabel = new JLabel("Cash: $0.00");
        cashLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        portfolioValueLabel = new JLabel("Portfolio: $0.00");
        portfolioValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        totalValueLabel = new JLabel("Total: $0.00");
        totalValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalValueLabel.setForeground(PRIMARY_COLOR);
        
        // Order form components
        symbolCombo = new JComboBox<>();
        symbolCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Render stock symbols properly (show placeholder when null)
        symbolCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof StockSymbol) {
                    StockSymbol symbol = (StockSymbol) value;
                    setText(symbol.getSymbol() + " - " + symbol.getName());
                } else if (value == null) {
                    setText("Select a stock...");
                }
                return this;
            }
        });
        symbolCombo.setEnabled(true);
        symbolCombo.setOpaque(true);
        symbolCombo.setBackground(Color.WHITE);
        
        quantityField = new JTextField(10);
        quantityField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    quantityField.setPreferredSize(new Dimension(200, 28));
        
        limitPriceField = new JTextField(10);
        limitPriceField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        orderTypeCombo = new JComboBox<>(Order.OrderType.values());
        orderTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        orderTypeCombo.addActionListener(e -> {
            boolean isLimit = orderTypeCombo.getSelectedItem() == Order.OrderType.LIMIT;
            limitPriceField.setEnabled(isLimit);
        });
        orderTypeCombo.setPreferredSize(new Dimension(200, 28));
        
        sideCombo = new JComboBox<>(Order.Side.values());
        sideCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    sideCombo.setPreferredSize(new Dimension(200, 28));
        
        // Positions table
        String[] positionsColumns = {"Symbol", "Quantity", "Avg Cost", "Current Price", "Value", "P&L", "P&L %"};
        positionsTableModel = new DefaultTableModel(positionsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        positionsTable = new JTable(positionsTableModel);
        positionsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        positionsTable.setRowHeight(32);
        
        // Orders table
        String[] ordersColumns = {"Date", "Symbol", "Type", "Side", "Qty", "Limit Price", "Status", "Filled Price"};
        ordersTableModel = new DefaultTableModel(ordersColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ordersTable = new JTable(ordersTableModel);
        ordersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ordersTable.setRowHeight(32);
        
        // Trades table
        String[] tradesColumns = {"Date", "Symbol", "Side", "Quantity", "Price", "Commission", "Total"};
        tradesTableModel = new DefaultTableModel(tradesColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tradesTable = new JTable(tradesTableModel);
        tradesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tradesTable.setRowHeight(32);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Top panel - Account summary
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center - Split between chart and order form (make this much larger)
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplit.setLeftComponent(createChartPanel());
        centerSplit.setRightComponent(createOrderPanel());
        centerSplit.setDividerLocation(650);
        centerSplit.setResizeWeight(0.65);
        add(centerSplit, BorderLayout.CENTER);
        
        // Bottom - Tabbed pane with positions/orders/trades (reduce height significantly)
        JTabbedPane bottomTabs = new JTabbedPane();
        bottomTabs.addTab("Positions", new JScrollPane(positionsTable));
        bottomTabs.addTab("Orders", new JScrollPane(ordersTable));
        bottomTabs.addTab("Trades", new JScrollPane(tradesTable));
        bottomTabs.addTab("📰 Market News", createNewsPanel());
        bottomTabs.setPreferredSize(new Dimension(0, 180)); // Reduced from 250 to 180
        add(bottomTabs, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        panel.setOpaque(false);
        
        panel.add(new JLabel("Cash:"));
        panel.add(cashLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(new JLabel("Portfolio:"));
        panel.add(portfolioValueLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(new JLabel("Total:"));
        panel.add(totalValueLabel);
        
        return panel;
    }
    
    private JPanel createChartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235)),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        // Create initial empty chart
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Stock Price History",
            "Date",
            "Price ($)",
            dataset,
            true,
            true,
            false
        );
        
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 500)); // Increased from 600x400 to 700x500
        panel.add(chartPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel("Place Order");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setBorder(new EmptyBorder(0, 0, 16, 0));
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Symbol selection
        JPanel symbolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        symbolPanel.setOpaque(false);
        JLabel symbolLabel = new JLabel("🔸 SELECT STOCK:");
        symbolLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        symbolLabel.setForeground(PRIMARY_COLOR);
        
        JButton selectStockButton = new JButton("📈 Choose Stock Symbol");
        selectStockButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        selectStockButton.setPreferredSize(new Dimension(320, 45)); // Increased from 280x40 to 320x45
        selectStockButton.setBackground(PRIMARY_COLOR);
        selectStockButton.setForeground(Color.WHITE);
        selectStockButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        selectStockButton.setFocusPainted(false);
        
        // Add a label to show currently selected stock
        JLabel selectedStockLabel = new JLabel("No stock selected");
        selectedStockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        selectedStockLabel.setForeground(new Color(107, 114, 128));
        
        selectStockButton.addActionListener(ev -> {
            List<StockSymbol> list = symbolDAO.findAll();
            DefaultListModel<StockSymbol> lm = new DefaultListModel<>();
            for (StockSymbol s : list) lm.addElement(s);
            JList<StockSymbol> jlist = new JList<>(lm);
            jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            jlist.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            jlist.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof StockSymbol) {
                        StockSymbol ss = (StockSymbol) value;
                        setText("📊 " + ss.getSymbol() + " - " + ss.getName());
                        setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    }
                    return this;
                }
            });
            JScrollPane scrollPane = new JScrollPane(jlist);
            scrollPane.setPreferredSize(new Dimension(500, 400));
            
            int option = JOptionPane.showConfirmDialog(
                this,
                scrollPane,
                "🔍 Select a Stock Symbol to Trade",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );
            if (option == JOptionPane.OK_OPTION && jlist.getSelectedValue() != null) {
                StockSymbol selected = jlist.getSelectedValue();
                symbolCombo.setSelectedItem(selected);
                selectedStockLabel.setText("✅ Selected: " + selected.getSymbol() + " - " + selected.getName());
                selectedStockLabel.setForeground(SUCCESS_COLOR);
                updateChart(selected);
            }
        });
        
        symbolPanel.add(symbolLabel);
        symbolPanel.add(selectStockButton);
        formPanel.add(symbolPanel);
        formPanel.add(Box.createVerticalStrut(5));
        
        JPanel selectedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        selectedPanel.setOpaque(false);
        selectedPanel.add(selectedStockLabel);
        formPanel.add(selectedPanel);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Side selection
        JPanel sidePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        sidePanel.setOpaque(false);
        JLabel sideLabel = new JLabel("💰 Side:");
        sideLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sideCombo.setPreferredSize(new Dimension(280, 40)); // Increased from 250x35 to 280x40
        sideCombo.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 2));
        sidePanel.add(sideLabel);
        sidePanel.add(sideCombo);
        formPanel.add(sidePanel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Order Type
        JPanel orderTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        orderTypePanel.setOpaque(false);
        JLabel orderTypeLabel = new JLabel("📋 Order Type:");
        orderTypeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        orderTypeCombo.setPreferredSize(new Dimension(280, 40)); // Increased from 250x35 to 280x40
        orderTypeCombo.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 2));
        orderTypePanel.add(orderTypeLabel);
        orderTypePanel.add(orderTypeCombo);
        formPanel.add(orderTypePanel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Quantity
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        quantityPanel.setOpaque(false);
        JLabel quantityLabel = new JLabel("🔢 Quantity:");
        quantityLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        quantityField.setPreferredSize(new Dimension(280, 40)); // Increased from 250x35 to 280x40
        quantityField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 2),
            new EmptyBorder(8, 12, 8, 12)
        ));
        quantityPanel.add(quantityLabel);
        quantityPanel.add(quantityField);
        formPanel.add(quantityPanel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Limit Price
        JPanel limitPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        limitPanel.setOpaque(false);
        JLabel limitLabel = new JLabel("💵 Limit Price:");
        limitLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        limitPriceField.setPreferredSize(new Dimension(280, 40)); // Increased from 250x35 to 280x40
        limitPriceField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 2),
            new EmptyBorder(8, 12, 8, 12)
        ));
        limitPanel.add(limitLabel);
        limitPanel.add(limitPriceField);
        formPanel.add(limitPanel);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Wrap the form in a scroll pane
        JScrollPane formScrollPane = new JScrollPane(formPanel);
        formScrollPane.setBorder(BorderFactory.createEmptyBorder());
        formScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        formScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        formScrollPane.setPreferredSize(new Dimension(450, 500)); // Increased from 400x350 to 450x500
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttonPanel.setOpaque(false);
        
        JButton placeOrderButton = new JButton("Place Order");
        placeOrderButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        placeOrderButton.setBackground(PRIMARY_COLOR);
        placeOrderButton.setForeground(Color.WHITE);
        placeOrderButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        placeOrderButton.setFocusPainted(false);
        placeOrderButton.addActionListener(e -> placeOrder());
        
        JButton executeButton = new JButton("Execute Today's Orders");
        executeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        executeButton.setBackground(SUCCESS_COLOR);
        executeButton.setForeground(Color.WHITE);
        executeButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        executeButton.setFocusPainted(false);
        executeButton.addActionListener(e -> executeTodaysOrders());
        
        buttonPanel.add(placeOrderButton);
        buttonPanel.add(executeButton);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadData() {
        // Load symbols
        symbolCombo.removeAllItems();
        List<StockSymbol> symbols = symbolDAO.findAll();
        // If there aren't many symbols yet, inject a larger sample set for the demo
        if (symbols.size() < 20) {
            String[][] demo = new String[][]{
                {"AAPL","Apple Inc."},
                {"AMZN","Amazon.com Inc."},
                {"GOOGL","Alphabet Inc."},
                {"MSFT","Microsoft Corporation"},
                {"TSLA","Tesla Inc."},
                {"NFLX","Netflix, Inc."},
                {"NVDA","NVIDIA Corporation"},
                {"INTC","Intel Corporation"},
                {"AMD","Advanced Micro Devices"},
                {"BABA","Alibaba Group"},
                {"JPM","JPMorgan Chase & Co."},
                {"V","Visa Inc."},
                {"MA","Mastercard Inc."},
                {"ORCL","Oracle Corporation"},
                {"KO","Coca-Cola Co."},
                {"PFE","Pfizer Inc."},
                {"JNJ","Johnson & Johnson"},
                {"BAC","Bank of America"},
                {"XOM","Exxon Mobil"},
                {"WMT","Walmart Inc."},
                {"PG","Procter & Gamble"},
                {"DIS","Disney"},
                {"NKE","Nike, Inc."},
                {"HD","Home Depot"},
                {"MCD","McDonald's Corp."}
            };
            for (String[] pair : demo) {
                try {
                    if (symbolDAO.findBySymbol(pair[0]) == null) {
                        StockSymbol s = new StockSymbol();
                        s.setSymbol(pair[0]);
                        s.setName(pair[1]);
                        boolean created = symbolDAO.create(s);
                        System.out.println("Inserted demo symbol " + pair[0] + " => " + created);
                    }
                } catch (Exception ex) {
                    System.err.println("Failed to insert demo symbol " + pair[0] + ": " + ex.getMessage());
                }
            }
            symbols = symbolDAO.findAll();
        }
        System.out.println("Loading " + symbols.size() + " stock symbols...");
        for (StockSymbol symbol : symbols) {
            System.out.println("Adding symbol: " + symbol.getSymbol() + " - " + symbol.getName());
            symbolCombo.addItem(symbol);
        }
        // Select first symbol by default so the combo shows a value
        if (symbolCombo.getItemCount() > 0) {
            try {
                symbolCombo.setSelectedIndex(0);
            } catch (Exception ex) {
                // ignore
            }
        }
        System.out.println("Symbol combo now has " + symbolCombo.getItemCount() + " items");
        
        // Load account data
        updateAccountSummary();
        loadPositions();
        loadOrders();
        loadTrades();
        
        // Load chart for first symbol
        if (symbolCombo.getItemCount() > 0) {
            updateChart((StockSymbol) symbolCombo.getSelectedItem());
        }
        
        // Add listener to update chart on symbol change
        symbolCombo.addActionListener(e -> {
            StockSymbol selected = (StockSymbol) symbolCombo.getSelectedItem();
            if (selected != null) {
                updateChart(selected);
            }
        });
    }
    
    private void updateAccountSummary() {
        SimulatedAccount account = accountDAO.findById(ACCOUNT_ID);
        if (account != null) {
            cashLabel.setText("Cash: $" + String.format("%,.2f", account.getCurrentCash()));
            
            // Calculate portfolio value
            List<Position> positions = positionDAO.findByAccount(ACCOUNT_ID);
            BigDecimal portfolioValue = BigDecimal.ZERO;
            for (Position pos : positions) {
                if (pos.getCurrentValue() != null) {
                    portfolioValue = portfolioValue.add(pos.getCurrentValue());
                }
            }
            
            portfolioValueLabel.setText("Portfolio: $" + String.format("%,.2f", portfolioValue));
            
            BigDecimal total = account.getCurrentCash().add(portfolioValue);
            totalValueLabel.setText("Total: $" + String.format("%,.2f", total));
        }
    }
    
    private void loadPositions() {
        positionsTableModel.setRowCount(0);
        
        // Update position values first
        orderExecutor.updatePositionValues(ACCOUNT_ID);
        
        List<Position> positions = positionDAO.findByAccount(ACCOUNT_ID);
        for (Position pos : positions) {
            MarketData latest = marketDataDAO.findLatestBySymbol(pos.getSymbolId());
            BigDecimal currentPrice = latest != null ? latest.getClosePrice() : pos.getAvgCost();
            
            BigDecimal pnl = pos.getUnrealizedPnl() != null ? pos.getUnrealizedPnl() : BigDecimal.ZERO;
            BigDecimal costBasis = pos.getAvgCost().multiply(new BigDecimal(pos.getQuantity()));
            double pnlPercent = costBasis.doubleValue() > 0 ? 
                (pnl.doubleValue() / costBasis.doubleValue()) * 100 : 0;
            
            Object[] row = {
                pos.getSymbol(),
                pos.getQuantity(),
                String.format("$%.2f", pos.getAvgCost()),
                String.format("$%.2f", currentPrice),
                String.format("$%.2f", pos.getCurrentValue()),
                String.format("$%.2f", pnl),
                String.format("%.2f%%", pnlPercent)
            };
            positionsTableModel.addRow(row);
        }
    }
    
    private void loadOrders() {
        ordersTableModel.setRowCount(0);
        List<Order> orders = orderDAO.findByAccount(ACCOUNT_ID);
        
        for (Order order : orders) {
            Object[] row = {
                order.getOrderDate(),
                order.getSymbol(),
                order.getOrderType(),
                order.getSide(),
                order.getQuantity(),
                order.getLimitPrice() != null ? String.format("$%.2f", order.getLimitPrice()) : "-",
                order.getStatus(),
                order.getFilledPrice() != null ? String.format("$%.2f", order.getFilledPrice()) : "-"
            };
            ordersTableModel.addRow(row);
        }
    }
    
    private void loadTrades() {
        tradesTableModel.setRowCount(0);
        List<Trade> trades = tradeDAO.findByAccount(ACCOUNT_ID);
        
        for (Trade trade : trades) {
            Object[] row = {
                trade.getTradeDate(),
                trade.getSymbol(),
                trade.getSide(),
                trade.getQuantity(),
                String.format("$%.2f", trade.getPrice()),
                String.format("$%.2f", trade.getCommission()),
                String.format("$%.2f", trade.getTotalAmount())
            };
            tradesTableModel.addRow(row);
        }
    }
    
    private void updateChart(StockSymbol symbol) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(6); // Show 6 months
        
        List<MarketData> data = marketDataDAO.findBySymbolAndDateRange(
            symbol.getSymbolId(), startDate, endDate
        );
        
        TimeSeries series = new TimeSeries(symbol.getSymbol());
        for (MarketData md : data) {
            series.add(
                new Day(md.getTradeDate().getDayOfMonth(), 
                       md.getTradeDate().getMonthValue(), 
                       md.getTradeDate().getYear()),
                md.getClosePrice().doubleValue()
            );
        }
        
        TimeSeriesCollection dataset = new TimeSeriesCollection(series);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            symbol.getSymbol() + " - Price History (6 Months)",
            "Date",
            "Price ($)",
            dataset,
            true,
            true,
            false
        );
        
        chartPanel.setChart(chart);
    }
    
    private void placeOrder() {
        try {
            StockSymbol symbol = (StockSymbol) symbolCombo.getSelectedItem();
            if (symbol == null) {
                JOptionPane.showMessageDialog(this, "Please select a symbol", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Order.OrderType orderType = (Order.OrderType) orderTypeCombo.getSelectedItem();
            Order.Side side = (Order.Side) sideCombo.getSelectedItem();
            
            BigDecimal limitPrice = null;
            if (orderType == Order.OrderType.LIMIT) {
                limitPrice = new BigDecimal(limitPriceField.getText().trim());
            }
            
            Order order = new Order(
                ACCOUNT_ID,
                symbol.getSymbolId(),
                orderType,
                side,
                quantity,
                limitPrice,
                LocalDate.now()
            );
            
            if (orderExecutor.placeOrder(order)) {
                JOptionPane.showMessageDialog(this, "Order placed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                quantityField.setText("");
                limitPriceField.setText("");
                loadOrders();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to place order", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void executeTodaysOrders() {
        orderExecutor.executePendingOrders(ACCOUNT_ID, LocalDate.now());
        JOptionPane.showMessageDialog(this, "Orders executed!", "Success", JOptionPane.INFORMATION_MESSAGE);
        
        updateAccountSummary();
        loadPositions();
        loadOrders();
        loadTrades();
    }
    
    public void refreshData() {
        loadData();
    }
    
    private JPanel createNewsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Title
        JLabel titleLabel = new JLabel("📰 Latest Market News");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // News container
        JPanel newsContainer = new JPanel();
        newsContainer.setLayout(new BoxLayout(newsContainer, BoxLayout.Y_AXIS));
        newsContainer.setBackground(CARD_COLOR);
        
        // Get recent news from generator
        List<MarketNews> recentNews = newsGenerator.getRecentNews();
        
        if (recentNews.isEmpty()) {
            JLabel noNewsLabel = new JLabel("No recent market news available");
            noNewsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            noNewsLabel.setForeground(new Color(107, 114, 128));
            noNewsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            newsContainer.add(noNewsLabel);
        } else {
            // Display up to 5 most recent news items
            for (int i = 0; i < Math.min(5, recentNews.size()); i++) {
                MarketNews news = recentNews.get(i);
                JPanel newsItem = createNewsItem(news);
                newsContainer.add(newsItem);
                
                if (i < Math.min(4, recentNews.size() - 1)) {
                    newsContainer.add(Box.createVerticalStrut(10));
                }
            }
        }
        
        // Generate new news button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_COLOR);
        
        JButton generateNewsButton = new JButton("🔄 Generate News");
        generateNewsButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        generateNewsButton.setBackground(PRIMARY_COLOR);
        generateNewsButton.setForeground(Color.WHITE);
        generateNewsButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        generateNewsButton.setFocusPainted(false);
        
        generateNewsButton.addActionListener(e -> {
            List<StockSymbol> symbols = symbolDAO.findAll();
            if (!symbols.isEmpty()) {
                newsGenerator.generateRandomNews(symbols);
                // Refresh the news panel
                panel.removeAll();
                panel.add(createNewsPanel());
                panel.revalidate();
                panel.repaint();
            }
        });
        
        buttonPanel.add(generateNewsButton);
        
        JScrollPane scrollPane = new JScrollPane(newsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createNewsItem(MarketNews news) {
        JPanel item = new JPanel(new BorderLayout(10, 5));
        item.setBackground(new Color(249, 250, 251));
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(12, 15, 12, 15)
        ));
        
        // Header with emoji, symbol, and time
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(249, 250, 251));
        
        JLabel symbolLabel = new JLabel(news.getSentimentEmoji() + " " + news.getAffectedSymbol());
        symbolLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        symbolLabel.setForeground(PRIMARY_COLOR);
        
        String timeAgo = getTimeAgo(news.getPublishedAt());
        JLabel timeLabel = new JLabel(timeAgo);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(new Color(107, 114, 128));
        
        headerPanel.add(symbolLabel, BorderLayout.WEST);
        headerPanel.add(timeLabel, BorderLayout.EAST);
        
        // Headline
        JLabel headlineLabel = new JLabel("<html>" + news.getHeadline() + "</html>");
        headlineLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        headlineLabel.setForeground(new Color(17, 24, 39));
        
        // Impact indicator
        JLabel impactLabel = new JLabel(String.format("Impact: %+.1f%%", news.getPriceImpact()));
        impactLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        Color impactColor = news.getPriceImpact() > 0 ? SUCCESS_COLOR : 
                           news.getPriceImpact() < 0 ? new Color(239, 68, 68) : 
                           new Color(107, 114, 128);
        impactLabel.setForeground(impactColor);
        
        item.add(headerPanel, BorderLayout.NORTH);
        item.add(headlineLabel, BorderLayout.CENTER);
        item.add(impactLabel, BorderLayout.SOUTH);
        
        return item;
    }
    
    private String getTimeAgo(java.time.LocalDateTime publishedAt) {
        java.time.Duration duration = java.time.Duration.between(publishedAt, java.time.LocalDateTime.now());
        long hours = duration.toHours();
        long minutes = duration.toMinutes();
        
        if (hours > 0) {
            return hours + "h ago";
        } else if (minutes > 0) {
            return minutes + "m ago";
        } else {
            return "Just now";
        }
    }
}
