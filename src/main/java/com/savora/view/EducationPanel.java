package com.savora.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Educational panel for teaching finance and stock market concepts
 */
public class EducationPanel extends JPanel {
    
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color WARNING_COLOR = new Color(245, 158, 11);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    
    private JTabbedPane mainTabs;
    private Map<String, String> glossaryTerms;
    private Map<String, String> tutorials;
    
    public EducationPanel() {
        initializeData();
        initializeComponents();
        setupLayout();
    }
    
    private void initializeData() {
        // Initialize glossary terms
        glossaryTerms = new HashMap<>();
        glossaryTerms.put("📊 Stock", "A share in the ownership of a company. When you buy stock, you become a shareholder and own a piece of that company.");
        glossaryTerms.put("💰 Dividend", "A payment made by companies to their shareholders, usually quarterly, representing a share of the company's profits.");
        glossaryTerms.put("📈 Bull Market", "A period of rising stock prices, typically characterized by investor optimism and confidence in the economy.");
        glossaryTerms.put("📉 Bear Market", "A period of declining stock prices, usually defined as a 20% or more decline from recent highs.");
        glossaryTerms.put("🔢 P/E Ratio", "Price-to-Earnings ratio. A valuation metric calculated by dividing the stock price by earnings per share. Higher P/E suggests investors expect higher growth.");
        glossaryTerms.put("🏢 Market Cap", "Market Capitalization. The total value of a company's shares, calculated as stock price × number of shares outstanding.");
        glossaryTerms.put("📋 Order Types", "Different ways to buy/sell stocks: Market orders execute immediately, Limit orders execute at a specific price or better.");
        glossaryTerms.put("⚖️ Volatility", "A measure of how much a stock's price fluctuates. High volatility means larger price swings, indicating higher risk.");
        glossaryTerms.put("📊 Volume", "The number of shares traded in a given period. High volume often indicates strong investor interest.");
        glossaryTerms.put("🎯 Diversification", "Spreading investments across different assets to reduce risk. Don't put all your eggs in one basket!");
        glossaryTerms.put("🔍 Technical Analysis", "Analyzing stock price charts and patterns to predict future price movements based on historical data.");
        glossaryTerms.put("📚 Fundamental Analysis", "Evaluating a company's financial health, earnings, revenue, and business model to determine its intrinsic value.");
        glossaryTerms.put("🛡️ Stop Loss", "An order to sell a stock when it reaches a certain price, designed to limit losses on a position.");
        glossaryTerms.put("📈 Support Level", "A price level where a stock tends to find buying interest and bounce back up from.");
        glossaryTerms.put("📉 Resistance Level", "A price level where a stock tends to face selling pressure and struggle to break above.");
        
        // Initialize tutorials
        tutorials = new HashMap<>();
        tutorials.put("🚀 Getting Started", 
            "1. Research companies you understand\n" +
            "2. Start with small amounts\n" +
            "3. Diversify your investments\n" +
            "4. Set realistic expectations\n" +
            "5. Keep learning and stay informed");
            
        tutorials.put("📋 How to Place Orders", 
            "MARKET ORDER:\n" +
            "• Executes immediately at current price\n" +
            "• Use when you want to buy/sell right now\n\n" +
            "LIMIT ORDER:\n" +
            "• Executes only at your specified price\n" +
            "• Use when you want to control the price\n" +
            "• May not execute if price doesn't reach your limit");
            
        tutorials.put("📊 Reading Stock Charts", 
            "PRICE MOVEMENT:\n" +
            "• Green/Red lines show price over time\n" +
            "• Higher peaks = higher prices\n" +
            "• Look for trends: up, down, or sideways\n\n" +
            "VOLUME:\n" +
            "• Shows how many shares were traded\n" +
            "• High volume = strong interest\n" +
            "• Confirms price movements");
            
        tutorials.put("⚖️ Risk Management", 
            "DIVERSIFICATION:\n" +
            "• Spread money across different stocks/sectors\n" +
            "• Don't invest more than 5-10% in one stock\n\n" +
            "POSITION SIZING:\n" +
            "• Only invest what you can afford to lose\n" +
            "• Start small and learn\n\n" +
            "STOP LOSSES:\n" +
            "• Set automatic sell orders to limit losses\n" +
            "• Consider 10-20% below purchase price");
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        mainTabs = new JTabbedPane(JTabbedPane.TOP);
        mainTabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }
    
    private void setupLayout() {
        // Create main sections
        mainTabs.addTab("📚 Finance Glossary", createGlossaryPanel());
        mainTabs.addTab("🎓 Trading Tutorials", createTutorialsPanel());
        mainTabs.addTab("📈 Market Concepts", createConceptsPanel());
        mainTabs.addTab("🎮 Practice Quiz", createQuizPanel());
        
        add(mainTabs, BorderLayout.CENTER);
    }
    
    private JPanel createGlossaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(249, 250, 251));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("📚 Financial Terms Glossary");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Create glossary grid
        JPanel glossaryGrid = new JPanel(new GridLayout(0, 1, 0, 15));
        glossaryGrid.setBackground(new Color(249, 250, 251));
        
        for (Map.Entry<String, String> entry : glossaryTerms.entrySet()) {
            JPanel termCard = createTermCard(entry.getKey(), entry.getValue());
            glossaryGrid.add(termCard);
        }
        
        JScrollPane scrollPane = new JScrollPane(glossaryGrid);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createTermCard(String term, String definition) {
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(20, 25, 20, 25)
        ));
        
        JLabel termLabel = new JLabel(term);
        termLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        termLabel.setForeground(PRIMARY_COLOR);
        
        JTextArea definitionArea = new JTextArea(definition);
        definitionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        definitionArea.setForeground(TEXT_PRIMARY);
        definitionArea.setBackground(CARD_COLOR);
        definitionArea.setEditable(false);
        definitionArea.setWrapStyleWord(true);
        definitionArea.setLineWrap(true);
        definitionArea.setBorder(new EmptyBorder(5, 0, 0, 0));
        
        card.add(termLabel, BorderLayout.NORTH);
        card.add(definitionArea, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createTutorialsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(249, 250, 251));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("🎓 Step-by-Step Trading Tutorials");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JPanel tutorialGrid = new JPanel(new GridLayout(0, 1, 0, 20));
        tutorialGrid.setBackground(new Color(249, 250, 251));
        
        for (Map.Entry<String, String> entry : tutorials.entrySet()) {
            JPanel tutorialCard = createTutorialCard(entry.getKey(), entry.getValue());
            tutorialGrid.add(tutorialCard);
        }
        
        JScrollPane scrollPane = new JScrollPane(tutorialGrid);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createTutorialCard(String title, String content) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(25, 30, 25, 30)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentArea.setForeground(TEXT_PRIMARY);
        contentArea.setBackground(CARD_COLOR);
        contentArea.setEditable(false);
        contentArea.setWrapStyleWord(true);
        contentArea.setLineWrap(true);
        contentArea.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(contentArea, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createConceptsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(249, 250, 251));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("📈 Advanced Market Concepts");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Create concept sections
        JPanel conceptsGrid = new JPanel(new GridLayout(0, 1, 0, 20));
        conceptsGrid.setBackground(new Color(249, 250, 251));
        
        // Market Analysis Section
        JPanel analysisCard = createConceptCard(
            "🔍 Market Analysis Methods",
            "TECHNICAL ANALYSIS:\n" +
            "• Chart patterns and price movements\n" +
            "• Moving averages and trends\n" +
            "• Support and resistance levels\n\n" +
            "FUNDAMENTAL ANALYSIS:\n" +
            "• Company financial statements\n" +
            "• Revenue and profit growth\n" +
            "• Industry and economic factors"
        );
        
        JPanel riskCard = createConceptCard(
            "⚖️ Risk and Reward",
            "RISK FACTORS:\n" +
            "• Market volatility\n" +
            "• Company-specific risks\n" +
            "• Economic conditions\n\n" +
            "RISK MANAGEMENT:\n" +
            "• Diversification across sectors\n" +
            "• Position sizing (don't bet everything)\n" +
            "• Stop-loss orders\n" +
            "• Regular portfolio review"
        );
        
        JPanel psychologyCard = createConceptCard(
            "🧠 Trading Psychology",
            "COMMON EMOTIONS:\n" +
            "• Fear of missing out (FOMO)\n" +
            "• Panic selling during drops\n" +
            "• Overconfidence after wins\n\n" +
            "STAYING DISCIPLINED:\n" +
            "• Stick to your plan\n" +
            "• Don't let emotions drive decisions\n" +
            "• Learn from both wins and losses\n" +
            "• Take breaks when stressed"
        );
        
        conceptsGrid.add(analysisCard);
        conceptsGrid.add(riskCard);
        conceptsGrid.add(psychologyCard);
        
        JScrollPane scrollPane = new JScrollPane(conceptsGrid);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createConceptCard(String title, String content) {
        return createTutorialCard(title, content); // Same styling as tutorial cards
    }
    
    private JPanel createQuizPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(new Color(249, 250, 251));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        JLabel titleLabel = new JLabel("🎮 Test Your Knowledge");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        JPanel quizCard = new JPanel(new BorderLayout(20, 20));
        quizCard.setBackground(CARD_COLOR);
        quizCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 2),
            new EmptyBorder(40, 40, 40, 40)
        ));
        
        JLabel questionLabel = new JLabel("<html><div style='text-align: center;'>" +
            "📊 What does P/E ratio measure?</div></html>");
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        questionLabel.setForeground(TEXT_PRIMARY);
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 0, 15));
        optionsPanel.setBackground(CARD_COLOR);
        optionsPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        ButtonGroup group = new ButtonGroup();
        JRadioButton option1 = new JRadioButton("A) Company's total debt");
        JRadioButton option2 = new JRadioButton("B) Price relative to earnings per share");
        JRadioButton option3 = new JRadioButton("C) Number of employees");
        JRadioButton option4 = new JRadioButton("D) Company's age");
        
        Font optionFont = new Font("Segoe UI", Font.PLAIN, 14);
        option1.setFont(optionFont);
        option2.setFont(optionFont);
        option3.setFont(optionFont);
        option4.setFont(optionFont);
        
        option1.setBackground(CARD_COLOR);
        option2.setBackground(CARD_COLOR);
        option3.setBackground(CARD_COLOR);
        option4.setBackground(CARD_COLOR);
        
        group.add(option1);
        group.add(option2);
        group.add(option3);
        group.add(option4);
        
        optionsPanel.add(option1);
        optionsPanel.add(option2);
        optionsPanel.add(option3);
        optionsPanel.add(option4);
        
        JButton submitButton = new JButton("📝 Submit Answer");
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        submitButton.setBackground(PRIMARY_COLOR);
        submitButton.setForeground(Color.WHITE);
        submitButton.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        submitButton.setFocusPainted(false);
        
        JLabel resultLabel = new JLabel("");
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        submitButton.addActionListener(e -> {
            if (option2.isSelected()) {
                resultLabel.setText("✅ Correct! P/E ratio compares price to earnings per share.");
                resultLabel.setForeground(SUCCESS_COLOR);
            } else {
                resultLabel.setText("❌ Incorrect. P/E ratio measures price relative to earnings per share.");
                resultLabel.setForeground(DANGER_COLOR);
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(CARD_COLOR);
        buttonPanel.add(submitButton);
        
        quizCard.add(questionLabel, BorderLayout.NORTH);
        quizCard.add(optionsPanel, BorderLayout.CENTER);
        quizCard.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(quizCard, BorderLayout.CENTER);
        panel.add(resultLabel, BorderLayout.SOUTH);
        
        return panel;
    }
}