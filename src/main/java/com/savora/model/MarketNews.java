package com.savora.model;

import java.time.LocalDateTime;

/**
 * Model for market news events that can affect stock prices
 */
public class MarketNews {
    private Long id;
    private String headline;
    private String content;
    private String affectedSymbol;
    private String sentiment; // POSITIVE, NEGATIVE, NEUTRAL
    private double priceImpact; // Percentage impact on stock price
    private LocalDateTime publishedAt;
    private String category; // EARNINGS, MERGER, PRODUCT_LAUNCH, ECONOMIC, etc.
    
    public MarketNews() {}
    
    public MarketNews(String headline, String content, String affectedSymbol, 
                     String sentiment, double priceImpact, String category) {
        this.headline = headline;
        this.content = content;
        this.affectedSymbol = affectedSymbol;
        this.sentiment = sentiment;
        this.priceImpact = priceImpact;
        this.category = category;
        this.publishedAt = LocalDateTime.now();
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getAffectedSymbol() { return affectedSymbol; }
    public void setAffectedSymbol(String affectedSymbol) { this.affectedSymbol = affectedSymbol; }
    
    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    
    public double getPriceImpact() { return priceImpact; }
    public void setPriceImpact(double priceImpact) { this.priceImpact = priceImpact; }
    
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getSentimentEmoji() {
        switch (sentiment) {
            case "POSITIVE": return "📈";
            case "NEGATIVE": return "📉";
            default: return "📊";
        }
    }
    
    public String getCategoryEmoji() {
        switch (category) {
            case "EARNINGS": return "💰";
            case "MERGER": return "🤝";
            case "PRODUCT_LAUNCH": return "🚀";
            case "ECONOMIC": return "🏛️";
            case "REGULATORY": return "⚖️";
            default: return "📰";
        }
    }
}