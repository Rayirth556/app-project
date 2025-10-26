package com.savora.service;

import com.savora.dao.*;
import com.savora.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Service to execute stock orders against market data
 * Handles MARKET and LIMIT orders with realistic execution rules
 */
public class OrderExecutor {
    private final OrderDAO orderDAO;
    private final TradeDAO tradeDAO;
    private final PositionDAO positionDAO;
    private final AccountDAO accountDAO;
    private final MarketDataDAO marketDataDAO;
    
    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.001"); // 0.1% commission
    private static final BigDecimal MIN_COMMISSION = new BigDecimal("1.00"); // $1 minimum
    private static final BigDecimal SLIPPAGE_RATE = new BigDecimal("0.0005"); // 0.05% slippage
    
    public OrderExecutor() {
        this.orderDAO = new OrderDAO();
        this.tradeDAO = new TradeDAO();
        this.positionDAO = new PositionDAO();
        this.accountDAO = new AccountDAO();
        this.marketDataDAO = new MarketDataDAO();
    }
    
    /**
     * Place a new order
     */
    public boolean placeOrder(Order order) {
        // Validate order
        if (order.getQuantity() <= 0) {
            System.err.println("Invalid quantity: " + order.getQuantity());
            return false;
        }
        
        // Check if we have enough cash for BUY orders (rough estimate)
        if (order.getSide() == Order.Side.BUY) {
            SimulatedAccount account = accountDAO.findById(order.getAccountId());
            if (account == null) {
                System.err.println("Account not found: " + order.getAccountId());
                return false;
            }
            
            // Get latest price for estimate
            MarketData latestData = marketDataDAO.findLatestBySymbol(order.getSymbolId());
            if (latestData == null) {
                System.err.println("No market data available for symbol ID: " + order.getSymbolId());
                return false;
            }
            
            BigDecimal estimatedPrice = order.getOrderType() == Order.OrderType.LIMIT ? 
                                        order.getLimitPrice() : latestData.getClosePrice();
            BigDecimal estimatedCost = estimatedPrice.multiply(new BigDecimal(order.getQuantity()));
            BigDecimal estimatedCommission = calculateCommission(estimatedCost);
            BigDecimal totalCost = estimatedCost.add(estimatedCommission);
            
            if (account.getCurrentCash().compareTo(totalCost) < 0) {
                System.err.println("Insufficient cash. Need: $" + totalCost + ", Have: $" + account.getCurrentCash());
                return false;
            }
        }
        
        // Check if we have enough shares for SELL orders
        if (order.getSide() == Order.Side.SELL) {
            Position position = positionDAO.findByAccountAndSymbol(order.getAccountId(), order.getSymbolId());
            if (position == null || position.getQuantity() < order.getQuantity()) {
                int available = position != null ? position.getQuantity() : 0;
                System.err.println("Insufficient shares. Need: " + order.getQuantity() + ", Have: " + available);
                return false;
            }
        }
        
        // Save order
        return orderDAO.create(order);
    }
    
    /**
     * Execute pending orders for a given date
     */
    public void executePendingOrders(int accountId, LocalDate date) {
        List<Order> pendingOrders = orderDAO.findPendingByDate(accountId, date);
        
        for (Order order : pendingOrders) {
            executeOrder(order, date);
        }
    }
    
    /**
     * Execute a single order
     */
    public boolean executeOrder(Order order, LocalDate executionDate) {
        // Get market data for the execution date
        MarketData marketData = marketDataDAO.findBySymbolAndDate(order.getSymbolId(), executionDate);
        
        if (marketData == null) {
            // No trading on this date (weekend/holiday)
            return false;
        }
        
        BigDecimal executionPrice = null;
        
        // Determine execution price based on order type
        if (order.getOrderType() == Order.OrderType.MARKET) {
            // Market orders execute at open price with slippage
            executionPrice = marketData.getOpenPrice();
            
            // Apply slippage
            BigDecimal slippage = executionPrice.multiply(SLIPPAGE_RATE);
            if (order.getSide() == Order.Side.BUY) {
                executionPrice = executionPrice.add(slippage);
            } else {
                executionPrice = executionPrice.subtract(slippage);
            }
            
        } else if (order.getOrderType() == Order.OrderType.LIMIT) {
            // Limit orders execute only if price reaches the limit
            BigDecimal limitPrice = order.getLimitPrice();
            
            if (order.getSide() == Order.Side.BUY) {
                // Buy limit: execute if day's low <= limit price
                if (marketData.getLowPrice().compareTo(limitPrice) <= 0) {
                    executionPrice = limitPrice;
                }
            } else {
                // Sell limit: execute if day's high >= limit price
                if (marketData.getHighPrice().compareTo(limitPrice) >= 0) {
                    executionPrice = limitPrice;
                }
            }
        }
        
        // If no execution price determined, order doesn't fill
        if (executionPrice == null) {
            return false;
        }
        
        // Round execution price
        executionPrice = executionPrice.setScale(2, RoundingMode.HALF_UP);
        
        // Calculate amounts
        BigDecimal subtotal = executionPrice.multiply(new BigDecimal(order.getQuantity()));
        BigDecimal commission = calculateCommission(subtotal);
        BigDecimal totalAmount;
        
        if (order.getSide() == Order.Side.BUY) {
            totalAmount = subtotal.add(commission);
        } else {
            totalAmount = subtotal.subtract(commission);
        }
        
        // Update account cash
        SimulatedAccount account = accountDAO.findById(order.getAccountId());
        BigDecimal newCash;
        
        if (order.getSide() == Order.Side.BUY) {
            newCash = account.getCurrentCash().subtract(totalAmount);
            if (newCash.compareTo(BigDecimal.ZERO) < 0) {
                System.err.println("Order would result in negative cash. Rejecting.");
                orderDAO.updateOrderFilled(order.getOrderId(), Order.Status.REJECTED, 0, null, executionDate);
                return false;
            }
        } else {
            newCash = account.getCurrentCash().add(totalAmount);
        }
        
        accountDAO.updateCash(order.getAccountId(), newCash);
        
        // Update position
        updatePosition(order.getAccountId(), order.getSymbolId(), order.getSide(), 
                      order.getQuantity(), executionPrice);
        
        // Create trade record
        Trade trade = new Trade(
            order.getOrderId(),
            order.getAccountId(),
            order.getSymbolId(),
            order.getSide(),
            order.getQuantity(),
            executionPrice,
            commission,
            executionDate
        );
        tradeDAO.create(trade);
        
        // Update order status
        orderDAO.updateOrderFilled(order.getOrderId(), Order.Status.FILLED, 
                                   order.getQuantity(), executionPrice, executionDate);
        
        System.out.println("✓ Executed " + order.getSide() + " " + order.getQuantity() + 
                          " shares at $" + executionPrice + " (Commission: $" + commission + ")");
        
        return true;
    }
    
    /**
     * Update position after trade execution
     */
    private void updatePosition(int accountId, int symbolId, Order.Side side, 
                                int quantity, BigDecimal price) {
        Position position = positionDAO.findByAccountAndSymbol(accountId, symbolId);
        
        if (position == null) {
            // Create new position for BUY
            if (side == Order.Side.BUY) {
                position = new Position(accountId, symbolId, quantity, price);
                position.setCurrentValue(price.multiply(new BigDecimal(quantity)));
                position.setUnrealizedPnl(BigDecimal.ZERO);
                positionDAO.upsert(position);
            }
        } else {
            // Update existing position
            int newQuantity;
            BigDecimal newAvgCost;
            
            if (side == Order.Side.BUY) {
                // Add to position - calculate new average cost
                BigDecimal oldCost = position.getAvgCost().multiply(new BigDecimal(position.getQuantity()));
                BigDecimal newCost = price.multiply(new BigDecimal(quantity));
                newQuantity = position.getQuantity() + quantity;
                newAvgCost = oldCost.add(newCost).divide(new BigDecimal(newQuantity), 2, RoundingMode.HALF_UP);
            } else {
                // Reduce position - keep same average cost
                newQuantity = position.getQuantity() - quantity;
                newAvgCost = position.getAvgCost();
            }
            
            position.setQuantity(newQuantity);
            position.setAvgCost(newAvgCost);
            position.setCurrentValue(price.multiply(new BigDecimal(newQuantity)));
            
            // Calculate unrealized P&L
            BigDecimal costBasis = newAvgCost.multiply(new BigDecimal(newQuantity));
            BigDecimal currentValue = price.multiply(new BigDecimal(newQuantity));
            position.setUnrealizedPnl(currentValue.subtract(costBasis));
            
            positionDAO.upsert(position);
            
            // Delete position if quantity is zero
            if (newQuantity == 0) {
                positionDAO.deleteIfEmpty(accountId, symbolId);
            }
        }
    }
    
    /**
     * Calculate commission for a trade
     */
    private BigDecimal calculateCommission(BigDecimal amount) {
        BigDecimal commission = amount.multiply(COMMISSION_RATE);
        if (commission.compareTo(MIN_COMMISSION) < 0) {
            commission = MIN_COMMISSION;
        }
        return commission.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Update all position values based on latest market prices
     */
    public void updatePositionValues(int accountId) {
        List<Position> positions = positionDAO.findByAccount(accountId);
        
        for (Position position : positions) {
            MarketData latestData = marketDataDAO.findLatestBySymbol(position.getSymbolId());
            if (latestData != null) {
                BigDecimal currentPrice = latestData.getClosePrice();
                BigDecimal currentValue = currentPrice.multiply(new BigDecimal(position.getQuantity()));
                BigDecimal costBasis = position.getAvgCost().multiply(new BigDecimal(position.getQuantity()));
                BigDecimal unrealizedPnl = currentValue.subtract(costBasis);
                
                positionDAO.updateValues(position.getPositionId(), currentValue, unrealizedPnl);
            }
        }
    }
}
