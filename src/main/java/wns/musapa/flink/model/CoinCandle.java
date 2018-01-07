package wns.musapa.flink.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import wns.musapa.flink.model.code.UpbitCoinCode;

import java.util.Date;

public class CoinCandle extends Coin {
    private TradePrice open;
    private TradePrice close;
    private TradePrice high;
    private TradePrice low;

    public CoinCandle(CoinCode coinCode) {
        super(coinCode);
    }

    public TradePrice getOpen() {
        return open;
    }

    public void setOpen(TradePrice open) {
        this.open = open;
    }

    public TradePrice getClose() {
        return close;
    }

    public void setClose(TradePrice close) {
        this.close = close;
    }

    public TradePrice getHigh() {
        return high;
    }

    public void setHigh(TradePrice high) {
        this.high = high;
    }

    public TradePrice getLow() {
        return low;
    }

    public void setLow(TradePrice low) {
        this.low = low;
    }

    @JsonProperty("rateOfChange")
    public double getRateOfChange() {
        if (this.getOpen() == null || this.getHigh() == null) {
            return 0;
        }

        return (getClose().getPrice() - getOpen().getPrice())
                / getOpen().getPrice() * 100.0;
    }

    @JsonIgnore
    public String toAlertString() {
        StringBuilder sb = new StringBuilder();
        UpbitCoinCode upbitCoinCode = (UpbitCoinCode) getCode();
        sb.append(upbitCoinCode.getKorean() + " (" + upbitCoinCode.name() + ")\n");
        sb.append(String.format("Rate: %.3f%%\n", getRateOfChange()));
        sb.append(String.format("Close: %,d (%s)\n", (long) getClose().getPrice(), sdf.format(new Date(getClose().getTimestamp()))));
        sb.append(String.format("Open: %,d (%s)\n", (long) getOpen().getPrice(), sdf.format(new Date(getOpen().getTimestamp()))));
        sb.append(String.format("Low: %,d (%s)\n", (long) getLow().getPrice(), sdf.format(new Date(getLow().getTimestamp()))));
        sb.append(String.format("High: %,d (%s)\n", (long) getHigh().getPrice(), sdf.format(new Date(getHigh().getTimestamp()))));
        return sb.toString();
    }

    public static String printTimestamp(long timestamp) {
        return sdf.format(new Date(timestamp));
    }
}
