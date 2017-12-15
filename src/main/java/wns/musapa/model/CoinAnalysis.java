package wns.musapa.model;

public class CoinAnalysis extends Coin {

    private TradePrice open = null;
    private TradePrice close = null;
    private TradePrice low = null;
    private TradePrice high = null;
    private int count = 0;
    private double rateOfChange = 0;

    public CoinAnalysis(CoinCode coinCode) {
        super(coinCode);
        this.count = 0;
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

    public TradePrice getLow() {
        return low;
    }

    public void setLow(TradePrice low) {
        this.low = low;
    }

    public TradePrice getHigh() {
        return high;
    }

    public void setHigh(TradePrice high) {
        this.high = high;
    }

    public void addCount(){
        this.count++;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getRateOfChange() {
        return rateOfChange;
    }

    public void setRateOfChange(double rateOfChange) {
        this.rateOfChange = rateOfChange;
    }
}
