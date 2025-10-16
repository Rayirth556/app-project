package com.savora.util;

import com.savora.dao.SpendingLimitDAO;
import com.savora.dao.TransactionDAO;
import com.savora.model.SpendingLimit;
import com.savora.model.Transaction;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Manages spending limit notifications and alerts
 */
public class NotificationManager {
    private final SpendingLimitDAO spendingLimitDAO;
    private final TransactionDAO transactionDAO;
    
    // Colors for notifications
    private static final Color WARNING_COLOR = new Color(245, 158, 11);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    
    public NotificationManager() {
        this.spendingLimitDAO = new SpendingLimitDAO();
        this.transactionDAO = new TransactionDAO();
    }
    
    /**
     * Check spending limits after a new transaction is added
     */
    public void checkSpendingLimits(Transaction transaction) {
        if (transaction.getType() != Transaction.TransactionType.EXPENSE) {
            return; // Only check limits for expenses
        }
        
        // Check category-specific limits
        checkCategoryLimit(transaction);
        
        // Check overall spending limit
        checkOverallLimit();
    }
    
    /**
     * Check spending limit for a specific category
     */
    private void checkCategoryLimit(Transaction transaction) {
        SpendingLimit limit = spendingLimitDAO.findByCategory(transaction.getCategoryId());
        if (limit == null || !limit.isActive()) {
            return;
        }
        
        LocalDate startOfPeriod = getPeriodStartDate(limit.getPeriod());
        LocalDate endOfPeriod = getPeriodEndDate(startOfPeriod, limit.getPeriod());
        
        BigDecimal spentAmount = transactionDAO.getTotalExpenses(startOfPeriod, endOfPeriod);
        double percentage = spentAmount.divide(limit.getLimitAmount(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100)).doubleValue();
        
        if (percentage >= 100) {
            showLimitExceededNotification(transaction.getCategoryId(), limit.getLimitAmount(), spentAmount, percentage);
        } else if (percentage >= 80) {
            showLimitWarningNotification(transaction.getCategoryId(), limit.getLimitAmount(), spentAmount, percentage);
        }
    }
    
    /**
     * Check overall spending limit
     */
    private void checkOverallLimit() {
        SpendingLimit overallLimit = spendingLimitDAO.findOverallLimit();
        if (overallLimit == null || !overallLimit.isActive()) {
            return;
        }
        
        LocalDate startOfPeriod = getPeriodStartDate(overallLimit.getPeriod());
        LocalDate endOfPeriod = getPeriodEndDate(startOfPeriod, overallLimit.getPeriod());
        
        BigDecimal totalSpent = transactionDAO.getTotalExpenses(startOfPeriod, endOfPeriod);
        double percentage = totalSpent.divide(overallLimit.getLimitAmount(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100)).doubleValue();
        
        if (percentage >= 100) {
            showOverallLimitExceededNotification(overallLimit.getLimitAmount(), totalSpent, percentage);
        } else if (percentage >= 80) {
            showOverallLimitWarningNotification(overallLimit.getLimitAmount(), totalSpent, percentage);
        }
    }
    
    /**
     * Show notification when category limit is exceeded
     */
    private void showLimitExceededNotification(int categoryId, BigDecimal limitAmount, BigDecimal spentAmount, double percentage) {
        String message = String.format(
            "⚠️ Category spending limit EXCEEDED!\n\n" +
            "Limit: $%.2f\n" +
            "Spent: $%.2f\n" +
            "Over by: $%.2f (%.1f%%)\n\n" +
            "Please review your spending for this category.",
            limitAmount.doubleValue(),
            spentAmount.doubleValue(),
            spentAmount.subtract(limitAmount).doubleValue(),
            percentage
        );
        
        showNotification("Spending Limit Exceeded", message, JOptionPane.ERROR_MESSAGE, DANGER_COLOR);
    }
    
    /**
     * Show warning when approaching category limit
     */
    private void showLimitWarningNotification(int categoryId, BigDecimal limitAmount, BigDecimal spentAmount, double percentage) {
        BigDecimal remaining = limitAmount.subtract(spentAmount);
        
        String message = String.format(
            "⚠️ Approaching spending limit!\n\n" +
            "Limit: $%.2f\n" +
            "Spent: $%.2f\n" +
            "Remaining: $%.2f (%.1f%% used)\n\n" +
            "You have used %.1f%% of your budget for this category.",
            limitAmount.doubleValue(),
            spentAmount.doubleValue(),
            remaining.doubleValue(),
            percentage,
            percentage
        );
        
        showNotification("Spending Limit Warning", message, JOptionPane.WARNING_MESSAGE, WARNING_COLOR);
    }
    
    /**
     * Show notification when overall limit is exceeded
     */
    private void showOverallLimitExceededNotification(BigDecimal limitAmount, BigDecimal totalSpent, double percentage) {
        String message = String.format(
            "🚨 Overall spending limit EXCEEDED!\n\n" +
            "Monthly Limit: $%.2f\n" +
            "Total Spent: $%.2f\n" +
            "Over by: $%.2f (%.1f%%)\n\n" +
            "Please review your overall spending and consider adjusting your budget.",
            limitAmount.doubleValue(),
            totalSpent.doubleValue(),
            totalSpent.subtract(limitAmount).doubleValue(),
            percentage
        );
        
        showNotification("Overall Spending Limit Exceeded", message, JOptionPane.ERROR_MESSAGE, DANGER_COLOR);
    }
    
    /**
     * Show warning when approaching overall limit
     */
    private void showOverallLimitWarningNotification(BigDecimal limitAmount, BigDecimal totalSpent, double percentage) {
        BigDecimal remaining = limitAmount.subtract(totalSpent);
        
        String message = String.format(
            "⚠️ Approaching overall spending limit!\n\n" +
            "Monthly Limit: $%.2f\n" +
            "Total Spent: $%.2f\n" +
            "Remaining: $%.2f (%.1f%% used)\n\n" +
            "You have used %.1f%% of your monthly spending limit.",
            limitAmount.doubleValue(),
            totalSpent.doubleValue(),
            remaining.doubleValue(),
            percentage,
            percentage
        );
        
        showNotification("Overall Spending Limit Warning", message, JOptionPane.WARNING_MESSAGE, WARNING_COLOR);
    }
    
    /**
     * Show a custom notification dialog
     */
    private void showNotification(String title, String message, int messageType, Color backgroundColor) {
        // Create custom dialog
        JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        // Create content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(backgroundColor);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Message label
        JLabel messageLabel = new JLabel("<html><body style='width: 400px;'>" + message.replace("\n", "<br>") + "</body></html>");
        messageLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        messageLabel.setForeground(java.awt.Color.WHITE);
        messageLabel.setVerticalAlignment(SwingConstants.TOP);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton okButton = new JButton("OK");
        okButton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        okButton.setBackground(java.awt.Color.WHITE);
        okButton.setForeground(backgroundColor);
        okButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        okButton.setFocusPainted(false);
        okButton.addActionListener(e -> dialog.dispose());
        
        // Disable future notifications button (for warnings)
        JButton disableButton = new JButton("Don't Show Again");
        disableButton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        disableButton.setBackground(new java.awt.Color(156, 163, 175));
        disableButton.setForeground(java.awt.Color.WHITE);
        disableButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        disableButton.setFocusPainted(false);
        disableButton.addActionListener(e -> {
            // Store user preference to disable notifications
            // This could be implemented with a preferences file or database setting
            dialog.dispose();
        });
        
        buttonPanel.add(disableButton);
        buttonPanel.add(okButton);
        
        contentPanel.add(messageLabel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setContentPane(contentPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        
        // Make dialog visible
        dialog.setVisible(true);
    }
    
    /**
     * Check all spending limits and show notifications
     */
    public void checkAllSpendingLimits() {
        // Check category limits
        List<SpendingLimit> categoryLimits = spendingLimitDAO.findActiveLimits();
        for (SpendingLimit limit : categoryLimits) {
            if (limit.getCategoryId() != null) { // Skip overall limits
                checkCategoryLimitForPeriod(limit);
            }
        }
        
        // Check overall limit
        checkOverallLimitForPeriod();
    }
    
    /**
     * Check category limit for current period
     */
    private void checkCategoryLimitForPeriod(SpendingLimit limit) {
        LocalDate startOfPeriod = getPeriodStartDate(limit.getPeriod());
        LocalDate endOfPeriod = getPeriodEndDate(startOfPeriod, limit.getPeriod());
        
        BigDecimal spentAmount = transactionDAO.getTotalExpenses(startOfPeriod, endOfPeriod);
        double percentage = spentAmount.divide(limit.getLimitAmount(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100)).doubleValue();
        
        if (percentage >= 100) {
            showLimitExceededNotification(limit.getCategoryId(), limit.getLimitAmount(), spentAmount, percentage);
        } else if (percentage >= 80) {
            showLimitWarningNotification(limit.getCategoryId(), limit.getLimitAmount(), spentAmount, percentage);
        }
    }
    
    /**
     * Check overall limit for current period
     */
    private void checkOverallLimitForPeriod() {
        SpendingLimit overallLimit = spendingLimitDAO.findOverallLimit();
        if (overallLimit == null || !overallLimit.isActive()) {
            return;
        }
        
        LocalDate startOfPeriod = getPeriodStartDate(overallLimit.getPeriod());
        LocalDate endOfPeriod = getPeriodEndDate(startOfPeriod, overallLimit.getPeriod());
        
        BigDecimal totalSpent = transactionDAO.getTotalExpenses(startOfPeriod, endOfPeriod);
        double percentage = totalSpent.divide(overallLimit.getLimitAmount(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100)).doubleValue();
        
        if (percentage >= 100) {
            showOverallLimitExceededNotification(overallLimit.getLimitAmount(), totalSpent, percentage);
        } else if (percentage >= 80) {
            showOverallLimitWarningNotification(overallLimit.getLimitAmount(), totalSpent, percentage);
        }
    }
    
    /**
     * Get start date for the specified period
     */
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
    
    /**
     * Get end date for the specified period
     */
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
}
