package com.savora.service;

import com.savora.model.MarketNews;
import com.savora.model.StockSymbol;
import com.savora.dao.SymbolDAO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service to generate realistic market news events for simulation
 */
public class MarketNewsGenerator {
    
    private final SymbolDAO symbolDAO;
    private final List<MarketNews> recentNews;
    private final Random random;
    
    // News templates for different categories
    private final Map<String, List<String[]>> newsTemplates;
    
    public MarketNewsGenerator() {
        this.symbolDAO = new SymbolDAO();
        this.recentNews = new ArrayList<>();
        this.random = new Random();
        this.newsTemplates = new HashMap<>();
        initializeNewsTemplates();
        generateInitialNews();
    }
    
    private void initializeNewsTemplates() {
        // Format: [headline_template, content_template, sentiment, category, min_impact, max_impact]
        newsTemplates.put("EARNINGS", Arrays.asList(
            new String[]{"[COMPANY] Reports Strong Q3 Earnings, Beats Expectations", 
                        "[COMPANY] announced quarterly earnings that exceeded analyst expectations, showing strong revenue growth and improved margins.",
                        "POSITIVE", "EARNINGS", "2.0", "8.0"},
            new String[]{"[COMPANY] Disappoints with Lower Than Expected Quarterly Results", 
                        "[COMPANY] reported quarterly earnings below analyst forecasts, citing challenging market conditions and increased competition.",
                        "NEGATIVE", "EARNINGS", "-6.0", "-2.0"},
            new String[]{"[COMPANY] Meets Earnings Expectations in Steady Quarter", 
                        "[COMPANY] delivered quarterly results in line with analyst expectations, maintaining steady performance amid market volatility.",
                        "NEUTRAL", "EARNINGS", "-1.0", "1.0"}
        ));
        
        newsTemplates.put("PRODUCT_LAUNCH", Arrays.asList(
            new String[]{"[COMPANY] Unveils Revolutionary New Product Line", 
                        "[COMPANY] announced the launch of innovative products that could disrupt the market and drive significant growth.",
                        "POSITIVE", "PRODUCT_LAUNCH", "3.0", "12.0"},
            new String[]{"[COMPANY] Delays Major Product Launch Due to Technical Issues", 
                        "[COMPANY] announced delays in their highly anticipated product launch, citing technical challenges and quality concerns.",
                        "NEGATIVE", "PRODUCT_LAUNCH", "-5.0", "-2.0"}
        ));
        
        newsTemplates.put("MERGER", Arrays.asList(
            new String[]{"[COMPANY] Announces Strategic Acquisition to Expand Market Reach", 
                        "[COMPANY] announced plans to acquire a key competitor, expected to strengthen market position and create synergies.",
                        "POSITIVE", "MERGER", "5.0", "15.0"},
            new String[]{"[COMPANY] Acquisition Deal Falls Through Due to Regulatory Concerns", 
                        "[COMPANY] announced that their planned acquisition has been terminated due to regulatory hurdles and valuation disagreements.",
                        "NEGATIVE", "MERGER", "-8.0", "-3.0"}
        ));
        
        newsTemplates.put("REGULATORY", Arrays.asList(
            new String[]{"Regulators Approve [COMPANY]'s New Market Expansion Plans", 
                        "Regulatory authorities have given approval for [COMPANY] to expand operations, opening new growth opportunities.",
                        "POSITIVE", "REGULATORY", "2.0", "6.0"},
            new String[]{"[COMPANY] Faces Increased Regulatory Scrutiny Over Business Practices", 
                        "Regulatory bodies announced investigations into [COMPANY]'s business practices, potentially leading to fines or restrictions.",
                        "NEGATIVE", "REGULATORY", "-7.0", "-2.0"}
        ));
        
        newsTemplates.put("ECONOMIC", Arrays.asList(
            new String[]{"Economic Data Boosts Market Confidence, Tech Stocks Rally", 
                        "Strong economic indicators have boosted investor confidence, with technology stocks leading the market rally.",
                        "POSITIVE", "ECONOMIC", "1.0", "4.0"},
            new String[]{"Market Volatility Increases Amid Economic Uncertainty", 
                        "Economic uncertainty and global tensions have increased market volatility, with investors seeking safe haven assets.",
                        "NEGATIVE", "ECONOMIC", "-4.0", "-1.0"}
        ));
    }
    
    private void generateInitialNews() {
        // Generate some initial news items for demonstration
        List<StockSymbol> symbols = symbolDAO.findAll();
        if (symbols.isEmpty()) return;
        
        // Generate 5-10 recent news items
        int newsCount = ThreadLocalRandom.current().nextInt(5, 11);
        for (int i = 0; i < newsCount; i++) {
            generateRandomNews(symbols);
        }
    }
    
    public MarketNews generateRandomNews(List<StockSymbol> symbols) {
        if (symbols.isEmpty()) return null;
        
        // Pick a random stock symbol
        StockSymbol symbol = symbols.get(random.nextInt(symbols.size()));
        
        // Pick a random news category
        String[] categories = {"EARNINGS", "PRODUCT_LAUNCH", "MERGER", "REGULATORY", "ECONOMIC"};
        String category = categories[random.nextInt(categories.length)];
        
        // Get templates for this category
        List<String[]> templates = newsTemplates.get(category);
        String[] template = templates.get(random.nextInt(templates.size()));
        
        // Generate the news
        String headline = template[0].replace("[COMPANY]", symbol.getName());
        String content = template[1].replace("[COMPANY]", symbol.getName());
        String sentiment = template[2];
        String newsCategory = template[3];
        
        // Generate random impact within range
        double minImpact = Double.parseDouble(template[4]);
        double maxImpact = Double.parseDouble(template[5]);
        double impact = minImpact + (maxImpact - minImpact) * random.nextDouble();
        impact = Math.round(impact * 100.0) / 100.0; // Round to 2 decimal places
        
        MarketNews news = new MarketNews(headline, content, symbol.getSymbol(), 
                                       sentiment, impact, newsCategory);
        
        // Set published time to sometime in the last 24 hours
        LocalDateTime publishTime = LocalDateTime.now()
            .minusHours(random.nextInt(24))
            .minusMinutes(random.nextInt(60));
        news.setPublishedAt(publishTime);
        
        recentNews.add(0, news); // Add to beginning of list
        
        // Keep only last 20 news items
        if (recentNews.size() > 20) {
            recentNews.remove(recentNews.size() - 1);
        }
        
        return news;
    }
    
    public List<MarketNews> getRecentNews() {
        return new ArrayList<>(recentNews);
    }
    
    public List<MarketNews> getNewsForSymbol(String symbol) {
        return recentNews.stream()
            .filter(news -> symbol.equals(news.getAffectedSymbol()))
            .limit(5) // Return last 5 news items for this symbol
            .toList();
    }
    
    public void generateDailyNews() {
        // Generate 2-5 news items per day
        List<StockSymbol> symbols = symbolDAO.findAll();
        if (symbols.isEmpty()) return;
        
        int dailyNewsCount = ThreadLocalRandom.current().nextInt(2, 6);
        for (int i = 0; i < dailyNewsCount; i++) {
            generateRandomNews(symbols);
        }
    }
    
    public MarketNews generateSpecificNews(String symbol, String category, boolean positive) {
        List<StockSymbol> symbols = symbolDAO.findAll();
        StockSymbol targetSymbol = symbols.stream()
            .filter(s -> s.getSymbol().equals(symbol))
            .findFirst()
            .orElse(null);
            
        if (targetSymbol == null) return null;
        
        List<String[]> templates = newsTemplates.get(category);
        if (templates == null) return null;
        
        // Filter templates by sentiment
        String targetSentiment = positive ? "POSITIVE" : "NEGATIVE";
        String[] template = templates.stream()
            .filter(t -> t[2].equals(targetSentiment))
            .findFirst()
            .orElse(templates.get(0));
        
        String headline = template[0].replace("[COMPANY]", targetSymbol.getName());
        String content = template[1].replace("[COMPANY]", targetSymbol.getName());
        String sentiment = template[2];
        String newsCategory = template[3];
        
        double minImpact = Double.parseDouble(template[4]);
        double maxImpact = Double.parseDouble(template[5]);
        double impact = minImpact + (maxImpact - minImpact) * random.nextDouble();
        impact = Math.round(impact * 100.0) / 100.0;
        
        MarketNews news = new MarketNews(headline, content, targetSymbol.getSymbol(), 
                                       sentiment, impact, newsCategory);
        
        recentNews.add(0, news);
        
        if (recentNews.size() > 20) {
            recentNews.remove(recentNews.size() - 1);
        }
        
        return news;
    }
}