package wns.musapa.upbit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wns.musapa.UpbitMain;
import wns.musapa.model.CoinAnalysis;
import wns.musapa.model.CoinTick;
import wns.musapa.model.TradePrice;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class UpbitPipeline implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitPipeline.class);
    private Queue<CoinTick> coinTicks = new LinkedBlockingQueue<>();

    private final String coinCode;
    private final long interval;
    private final long windowSize;
    private CoinAnalysis coinAnalysis;

    private UpbitTelegramReporter reporter = null;

    public UpbitPipeline(String coinCode) {
        this(coinCode, UpbitMain.DEFAULT_INTERVAL, UpbitMain.DEFAULT_WINDOW_SIZE);
    }

    public UpbitPipeline(String coinCode, long interval, long windowSize) {
        this.coinCode = coinCode;
        this.interval = interval;
        this.windowSize = windowSize;
        this.coinAnalysis = new CoinAnalysis(coinCode);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (this.coinTicks) {
                long minTimestamp = System.currentTimeMillis() - this.windowSize;
                while (this.coinTicks != null
                        && !this.coinTicks.isEmpty()
                        && this.coinTicks.peek().getTimestamp() < minTimestamp) {
                    this.coinTicks.poll();
                }
            }

            if (this.coinTicks.isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
                continue;
            }

            synchronized (this.coinTicks) {
                Iterator<CoinTick> itr = this.coinTicks.iterator();
                CoinTick open = null;
                CoinTick close = null;
                CoinTick low = null;
                CoinTick high = null;
                while (itr.hasNext()) { // Not very efficient?
                    if (open == null) {
                        open = itr.next();
                        close = low = high = open;
                        continue;
                    }
                    CoinTick current = itr.next();
                    if (low.getTradePrice() > current.getTradePrice()) {
                        low = current;
                    }
                    if (high.getTradePrice() < current.getTradePrice()) {
                        high = current;
                    }
                    close = current;
                }
                coinAnalysis.setOpen(new TradePrice(open.getTimestamp(), open.getTradePrice()));
                coinAnalysis.setClose(new TradePrice(close.getTimestamp(), close.getTradePrice()));
                coinAnalysis.setLow(new TradePrice(low.getTimestamp(), low.getTradePrice()));
                coinAnalysis.setHigh(new TradePrice(high.getTimestamp(), high.getTradePrice()));
                coinAnalysis.setCount(coinTicks.size());
            }

            double rate = (coinAnalysis.getClose().getPrice() - coinAnalysis.getOpen().getPrice())
                    / coinAnalysis.getOpen().getPrice();
            coinAnalysis.setRateOfChange(rate);

            if (reporter != null) {
                reporter.report(coinAnalysis);
            }
            try {
                Thread.sleep(this.interval);
            } catch (InterruptedException e) {
            }
        }
    }

    public void addCoinTick(CoinTick coinTick) {
        synchronized (this.coinTicks) {
            this.coinTicks.add(coinTick);
        }
    }

    public void setReporter(UpbitTelegramReporter reporter) {
        this.reporter = reporter;
    }

    public String getCoinCode() {
        return coinCode;
    }
}
