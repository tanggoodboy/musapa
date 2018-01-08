package wns.musapa.flink.model;

import wns.musapa.flink.model.code.UpbitCoinCode;

public class CoinMACD extends Coin {
    private EMA ema26;
    private EMA ema12;
    private EMA ema9;

    public CoinMACD(CoinCode code) {
        super(code);

        this.ema9 = new EMA(9);
        this.ema12 = new EMA(12);
        this.ema26 = new EMA(26);
    }

    public void add(CoinTick coinTick) {
        this.ema9.add(coinTick.getTradePrice().getPrice());
        this.ema12.add(coinTick.getTradePrice().getPrice());
        this.ema26.add(coinTick.getTradePrice().getPrice());
    }

    public double getMACD() {
        return this.ema12.value - this.ema26.value;
    }

    public double getSignal() {
        return this.ema9.value;
    }

    public double getHeight() {
        return getMACD() - getSignal();
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

        public String toAlertString() {
            StringBuilder sb = new StringBuilder();
            UpbitCoinCode upbitCoinCode = (UpbitCoinCode) this.previous.getCode();
            if (this.previous.getHeight() < this.current.getHeight()) {
                sb.append(upbitCoinCode.getKorean() + ": BUY").append("\n");
            } else if (this.previous.getHeight() > this.current.getHeight()) {
                sb.append(upbitCoinCode.getKorean() + ": SELL").append("\n");
            } else {
                sb.append(upbitCoinCode.getKorean() + ": STAY").append("\n");
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

    static class EMA {
        private final int size;
        private final double alpha;

        private int count = 0;
        private double value;

        public EMA(int size) {
            this(size, (2.0 / (size + 1)));
        }

        public EMA(int size, double alpha) {
            this.size = size;
            this.alpha = alpha;

            this.count = 0;
            this.value = 0;
        }

        public Double add(double price) {
            if (count + 1 < size) {
                value = value + price; // total
                count++;
                return null;
            } else if (count + 1 == size) {
                value = value / size;
                count++;
                return value;
            } else {
                value = (price - value) * alpha + value;
                return value;
            }
        }

    }
}
