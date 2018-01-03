package wns.musapa.flink.model;

import wns.musapa.flink.model.code.UpbitCoinCode;

public class CoinTick extends Coin {
    private TradePrice tradePrice;

    public CoinTick(CoinCode code, long timestamp, double tradePrice) {
        super(code);
        this.tradePrice = new TradePrice(timestamp, tradePrice);
    }

    public TradePrice getTradePrice() {
        return this.tradePrice;
    }

    public static void main(String[] args) {
        CoinTick coinTick = new CoinTick(UpbitCoinCode.ADA, System.currentTimeMillis(), 3.20);
        System.out.println(coinTick);
    }
}
