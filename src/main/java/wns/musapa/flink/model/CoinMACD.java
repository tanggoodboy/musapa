package wns.musapa.flink.model;

import wns.musapa.flink.model.code.UpbitCoinCode;

public class CoinMACD extends Coin {
    private double ema26;
    private double ema12;
    private double signal;

    private TradePrice tradePrice;

    public CoinMACD(CoinCode code) {
        super(code);
    }

    public void setEma26(double ema26) {
        this.ema26 = ema26;
    }

    public void setEma12(double ema12) {
        this.ema12 = ema12;
    }

    public TradePrice getTradePrice() {
        return tradePrice;
    }

    public void setTradePrice(TradePrice tradePrice) {
        this.tradePrice = tradePrice;
    }

    public double getMACD() {
        return this.ema12 - this.ema26;
    }

    public double getSignal() {
        return this.signal;
    }

    public double getHeight() {
        return getMACD() - getSignal();
    }

    public void setSignal(double signal) {
        this.signal = signal;
    }

    public static class Pair {
        private CoinMACD previous;
        private CoinMACD current;

        public Pair(CoinMACD previous, CoinMACD current) {
            this.previous = previous;
            this.current = current;
        }

        public CoinMACD getPrevious() {
            return previous;
        }

        public void setPrevious(CoinMACD previous) {
            this.previous = previous;
        }

        public CoinMACD getCurrent() {
            return current;
        }

        public void setCurrent(CoinMACD current) {
            this.current = current;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(this.current.getTradePrice().getTimestamp()).append(",");
            sb.append(this.previous.getTradePrice().getTimestamp()).append(",");
            sb.append(this.current.getTradePrice().getPrice()).append(",");
            sb.append(this.previous.getTradePrice().getPrice()).append(",");
            sb.append(this.current.getMACD()).append(",");
            sb.append(this.previous.getMACD()).append(",");
            sb.append(this.current.getSignal()).append(",");
            sb.append(this.previous.getSignal()).append(",");
            sb.append(this.current.getHeight()).append(",");
            sb.append(this.previous.getHeight()).append(",");
            sb.append(this.current.getHeight() - this.previous.getHeight());

            return sb.toString();
        }

        public String toAlertString() {
            StringBuilder sb = new StringBuilder();
            UpbitCoinCode upbitCoinCode = (UpbitCoinCode) this.previous.getCode();
            if (this.previous.getHeight() < this.current.getHeight()) {
                sb.append(upbitCoinCode.getKorean() + ": BUY").append("\n");
            } else if (this.previous.getHeight() > this.current.getHeight()) {
                sb.append(upbitCoinCode.getKorean() + ": SELL").append("\n");
            } else {
                sb.append(upbitCoinCode.getKorean() + ": HOLD").append("\n");
            }

            sb.append("Previous: \n");
            sb.append(String.format("  MACD   = %.3f", previous.getMACD())).append("\n");
            sb.append(String.format("  Signal = %.3f", previous.getSignal())).append("\n");
            sb.append(String.format("  Height = %.3f", previous.getHeight())).append("\n");
            sb.append("Current: \n");
            sb.append(String.format("  MACD   = %.3f", current.getMACD())).append("\n");
            sb.append(String.format("  Signal = %.3f", current.getSignal())).append("\n");
            sb.append(String.format("  Height = %.3f", current.getHeight())).append("\n");
            sb.append(String.format("Height diff = %.3f", this.current.getHeight() - this.previous.getHeight()));

            return sb.toString();
        }
    }
}
