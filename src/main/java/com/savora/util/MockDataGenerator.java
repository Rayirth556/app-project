package com.savora.util;

import com.savora.dao.MarketDataDAO;
import com.savora.dao.SymbolDAO;
import com.savora.model.MarketData;
import com.savora.model.StockSymbol;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility to generate realistic mock stock market data for simulation
 * Generates 10 years of daily data for popular stocks
 */
public class MockDataGenerator {
    private final SymbolDAO symbolDAO;
    private final MarketDataDAO marketDataDAO;
    private final Random random;
    
    // Stock configurations: symbol -> [starting price, volatility (0-1), drift (annual %)]
    private static final Object[][] STOCK_CONFIGS = {
        {"AAPL", 30.0, 0.25, 0.20},    // Apple: started around $30, 25% vol, 20% annual growth
        {"GOOGL", 300.0, 0.22, 0.18},  // Alphabet: started around $300, 22% vol, 18% annual growth
        {"MSFT", 25.0, 0.20, 0.25},    // Microsoft: started around $25, 20% vol, 25% annual growth
        {"AMZN", 150.0, 0.30, 0.22},   // Amazon: started around $150, 30% vol, 22% annual growth
        {"TSLA", 15.0, 0.45, 0.35},    // Tesla: started around $15, 45% vol, 35% annual growth
        {"NFLX", 20.0, 0.35, 0.28},    // Netflix: started around $20, 35% vol, 28% annual growth
        {"NVDA", 8.0, 0.40, 0.45},     // NVIDIA: started around $8, 40% vol, 45% annual growth
        {"INTC", 20.0, 0.18, 0.08},    // Intel: started around $20, 18% vol, 8% annual growth
        {"AMD", 5.0, 0.50, 0.40},      // AMD: started around $5, 50% vol, 40% annual growth
        {"BABA", 100.0, 0.35, 0.15},   // Alibaba: started around $100, 35% vol, 15% annual growth
        {"JPM", 80.0, 0.25, 0.12},     // JPMorgan: started around $80, 25% vol, 12% annual growth
        {"V", 60.0, 0.20, 0.18},       // Visa: started around $60, 20% vol, 18% annual growth
        {"MA", 50.0, 0.22, 0.20},      // Mastercard: started around $50, 22% vol, 20% annual growth
        {"ORCL", 15.0, 0.18, 0.10},    // Oracle: started around $15, 18% vol, 10% annual growth
        {"KO", 35.0, 0.15, 0.08},      // Coca-Cola: started around $35, 15% vol, 8% annual growth
        {"PFE", 25.0, 0.20, 0.06},     // Pfizer: started around $25, 20% vol, 6% annual growth
        {"JNJ", 90.0, 0.15, 0.07},     // Johnson & Johnson: started around $90, 15% vol, 7% annual growth
        {"BAC", 10.0, 0.30, 0.15},     // Bank of America: started around $10, 30% vol, 15% annual growth
        {"XOM", 60.0, 0.25, 0.05},     // Exxon Mobil: started around $60, 25% vol, 5% annual growth
        {"WMT", 50.0, 0.18, 0.10},     // Walmart: started around $50, 18% vol, 10% annual growth
        {"PG", 55.0, 0.16, 0.09},      // Procter & Gamble: started around $55, 16% vol, 9% annual growth
        {"DIS", 70.0, 0.22, 0.12},     // Disney: started around $70, 22% vol, 12% annual growth
        {"NKE", 45.0, 0.24, 0.16},     // Nike: started around $45, 24% vol, 16% annual growth
        {"HD", 40.0, 0.20, 0.14},      // Home Depot: started around $40, 20% vol, 14% annual growth
        {"MCD", 60.0, 0.16, 0.11}      // McDonald's: started around $60, 16% vol, 11% annual growth
    };
    
    public MockDataGenerator() {
        this.symbolDAO = new SymbolDAO();
        this.marketDataDAO = new MarketDataDAO();
        this.random = new Random(42); // Fixed seed for reproducibility
    }
    
    /**
     * Generate and insert 10 years of mock data for all configured stocks
     */
    public void generateAllData() {
        LocalDate endDate = LocalDate.now(); // Include today
        LocalDate startDate = endDate.minusYears(10);
        
        System.out.println("Starting mock data generation for 10 years (" + startDate + " to " + endDate + ")");
        
        for (Object[] config : STOCK_CONFIGS) {
            String symbol = (String) config[0];
            double startPrice = (double) config[1];
            double volatility = (double) config[2];
            double annualDrift = (double) config[3];
            
            System.out.println("\nGenerating data for " + symbol + "...");
            
            // Get or verify symbol exists
            StockSymbol stockSymbol = symbolDAO.findBySymbol(symbol);
            if (stockSymbol == null) {
                System.err.println("Symbol " + symbol + " not found in database. Skipping.");
                continue;
            }
            
            // Check if data already exists and delete it to regenerate with current dates
            int existingCount = marketDataDAO.getDataCountForSymbol(stockSymbol.getSymbolId());
            if (existingCount > 0) {
                System.out.println("  " + symbol + " has " + existingCount + " old data points. Deleting...");
                marketDataDAO.deleteBySymbolId(stockSymbol.getSymbolId());
            }
            
            // Generate data
            List<MarketData> dataPoints = generateStockData(
                stockSymbol.getSymbolId(),
                startDate,
                endDate,
                startPrice,
                volatility,
                annualDrift
            );
            
            // Batch insert
            System.out.println("  Inserting " + dataPoints.size() + " data points for " + symbol + "...");
            int inserted = marketDataDAO.batchInsert(dataPoints);
            System.out.println("  ✓ Inserted " + inserted + " records for " + symbol);
        }
        
        System.out.println("\n✓ Mock data generation complete!");
    }
    
    /**
     * Generate realistic stock price data using geometric Brownian motion
     */
    private List<MarketData> generateStockData(int symbolId, LocalDate startDate, LocalDate endDate,
                                                 double startPrice, double volatility, double annualDrift) {
        List<MarketData> dataPoints = new ArrayList<>();
        
        double currentPrice = startPrice;
        LocalDate currentDate = startDate;
        
        // Daily drift and volatility
        double dt = 1.0 / 252.0; // Trading days in a year
        double dailyDrift = annualDrift * dt;
        double dailyVol = volatility * Math.sqrt(dt);
        
        while (!currentDate.isAfter(endDate)) {
            // Skip weekends
            if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY || 
                currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                currentDate = currentDate.plusDays(1);
                continue;
            }
            
            // Generate daily return using geometric Brownian motion
            double randomShock = random.nextGaussian();
            double dailyReturn = dailyDrift + (dailyVol * randomShock);
            
            // Calculate new price
            double newPrice = currentPrice * Math.exp(dailyReturn);
            
            // Generate OHLC (Open, High, Low, Close) for the day
            double open = currentPrice;
            double close = newPrice;
            
            // High and low with some intraday volatility
            double intradayVol = 0.02; // 2% intraday movement
            double high = Math.max(open, close) * (1 + Math.abs(random.nextGaussian()) * intradayVol);
            double low = Math.min(open, close) * (1 - Math.abs(random.nextGaussian()) * intradayVol);
            
            // Ensure high >= open,close and low <= open,close
            high = Math.max(high, Math.max(open, close));
            low = Math.min(low, Math.min(open, close));
            
            // Generate volume (higher on big price moves)
            long baseVolume = 10_000_000L + random.nextInt(5_000_000);
            long volume = (long) (baseVolume * (1 + Math.abs(dailyReturn) * 10));
            
            // Create market data entry
            MarketData data = new MarketData(
                symbolId,
                currentDate,
                round(open),
                round(high),
                round(low),
                round(close),
                round(close), // adjusted_close = close (no splits/dividends in mock data)
                volume
            );
            
            dataPoints.add(data);
            
            // Update for next day
            currentPrice = newPrice;
            currentDate = currentDate.plusDays(1);
        }
        
        return dataPoints;
    }
    
    /**
     * Round to 2 decimal places
     */
    private BigDecimal round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Main method for standalone execution
     */
    public static void main(String[] args) {
        System.out.println("=== Savora Stock Market Data Generator ===\n");
        
        MockDataGenerator generator = new MockDataGenerator();
        generator.generateAllData();
        
        System.out.println("\nData generation complete. You can now run the application.");
    }
}
