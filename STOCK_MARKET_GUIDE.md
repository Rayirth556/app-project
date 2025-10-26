# Stock Market Simulation Feature

## Overview
The Stock Market Simulator allows you to practice trading stocks using 10 years of historical mock data without risking real money.

## Features

### 📊 **Market Data**
- 10 years of daily price data (2015-2025)
- 5 stocks available: AAPL, GOOGL, MSFT, AMZN, TSLA
- Realistic price movements using geometric Brownian motion
- OHLC (Open, High, Low, Close) data with volume

### 💼 **Trading Account**
- **Starting Cash**: $100,000
- **Commission**: 0.1% per trade (minimum $1)
- **Slippage**: 0.05% on market orders

### 📈 **Order Types**

#### Market Orders
- Execute immediately at the next available price
- Filled at the opening price of the trading day
- Small slippage applied for realism

#### Limit Orders
- Execute only when price reaches your limit
- **Buy Limit**: Executes if day's low price ≤ limit price
- **Sell Limit**: Executes if day's high price ≥ limit price

### 📊 **Portfolio Management**
- **Real-time position tracking**
- **Average cost calculation**
- **Unrealized P&L** (Profit & Loss)
- **P&L percentage** per position

## How to Use

### 1. **View Stock Charts**
- Select a stock from the dropdown
- View 6-month price history
- Chart updates automatically

### 2. **Place an Order**

**Steps:**
1. Select **Symbol** (e.g., AAPL)
2. Choose **Side**: BUY or SELL
3. Select **Order Type**: MARKET or LIMIT
4. Enter **Quantity** (number of shares)
5. If LIMIT order, enter **Limit Price**
6. Click **Place Order**

**Example - Buy 10 shares of Apple at market price:**
```
Symbol: AAPL
Side: BUY
Order Type: MARKET
Quantity: 10
```

**Example - Sell 5 shares of Google at $350:**
```
Symbol: GOOGL
Side: SELL
Order Type: LIMIT
Quantity: 5
Limit Price: 350.00
```

### 3. **Execute Orders**
- Orders are placed as **PENDING**
- Click **Execute Today's Orders** to fill them
- Orders execute against today's market data
- Failed orders remain pending (e.g., limit not reached)

### 4. **Monitor Portfolio**

**Positions Tab:**
- View all your holdings
- Current value and unrealized P&L
- P&L percentage for each position

**Orders Tab:**
- See all orders (pending, filled, cancelled)
- Check fill prices and execution dates

**Trades Tab:**
- Complete trade history
- Commission and total amounts

## Trading Rules

### ✅ **Allowed**
- Buy stocks with available cash
- Sell stocks you own
- Multiple positions in different stocks
- Both market and limit orders

### ❌ **Not Allowed**
- **No short selling** (can't sell stocks you don't own)
- **No margin trading** (can't spend more than your cash)
- **No weekend trading** (orders execute on trading days only)

## Account Summary

The top of the panel shows:
- **Cash**: Available buying power
- **Portfolio**: Total value of all positions
- **Total**: Cash + Portfolio (Net Asset Value)

## Tips for Success

1. **Start Small**: Practice with small positions first
2. **Use Limit Orders**: Control your entry/exit prices
3. **Monitor P&L**: Track which positions are profitable
4. **Diversify**: Don't put all money in one stock
5. **Check Charts**: Understand price trends before trading

## Data Generation

Mock data is automatically generated on first run. To regenerate:

```bash
java -cp target/savora-finance-1.0.0.jar com.savora.util.MockDataGenerator
```

This will populate 10 years of data for all 5 stocks (~13,000 data points).

## Technical Details

### Database Tables
- `stock_symbols` - Stock ticker and name
- `market_data` - Historical OHLC data
- `simulated_accounts` - Trading account balance
- `positions` - Current holdings
- `orders` - Order book
- `trades` - Executed trade history

### Order Execution Logic
```
MARKET BUY:  Price = Open + Slippage
MARKET SELL: Price = Open - Slippage

LIMIT BUY:   Price = Limit (if Low ≤ Limit)
LIMIT SELL:  Price = Limit (if High ≥ Limit)

Commission = max(Amount × 0.1%, $1.00)
```

### Position Calculation
```
Avg Cost = (Old Cost × Old Qty + New Price × New Qty) / Total Qty
Unrealized P&L = (Current Price - Avg Cost) × Quantity
P&L % = (Unrealized P&L / Cost Basis) × 100
```

## Troubleshooting

**Q: Orders not executing?**
- Check if limit price is reachable
- Ensure you have enough cash/shares
- Click "Execute Today's Orders" button

**Q: Can't sell a stock?**
- You must own shares first (check Positions tab)
- Can't sell more than you own

**Q: Commission seems high?**
- Minimum commission is $1
- 0.1% of trade value
- Applied to both buys and sells

**Q: No data showing?**
- Run MockDataGenerator to populate database
- Check database connection

## Demo Scenario

Try this demo to learn the system:

1. **View AAPL chart** - Check current price
2. **Buy 50 shares of AAPL** - Use market order
3. **Click Execute** - Fill the order
4. **Check Positions tab** - See your holding
5. **Buy 25 more shares** - Watch avg cost adjust
6. **Place SELL LIMIT** - Set price 5% higher
7. **Execute orders daily** - Wait for limit to hit
8. **Check Trades** - Review your history

## Performance Metrics

After trading, you can calculate:
- **Total Return**: (Total Value - $100,000) / $100,000 × 100%
- **Win Rate**: Profitable trades / Total trades × 100%
- **Average P&L**: Sum of P&L / Number of trades

## Notes

- All data is **simulated** - not real market data
- Prices generated using **realistic volatility models**
- Perfect for **learning and practice**
- **No financial risk** involved

---

**Happy Trading! 📈💰**
