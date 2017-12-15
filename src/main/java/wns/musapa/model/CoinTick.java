package wns.musapa.model;

public class CoinTick extends Coin {
    private TradePrice tradePrice;

    public CoinTick(CoinCode code, long timestamp, double tradePrice) {
        super(code);
        this.tradePrice = new TradePrice(timestamp, tradePrice);
    }

    public double getTradePrice() {
        return this.tradePrice.getPrice();
    }

    public long getTimestamp() {
        return this.tradePrice.getTimestamp();
    }
}
