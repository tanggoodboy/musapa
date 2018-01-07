package wns.musapa.flink.sink;

import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wns.musapa.flink.bot.Reporter;
import wns.musapa.flink.command.Command;
import wns.musapa.flink.command.impl.NowCommand;
import wns.musapa.flink.model.CoinCandle;
import wns.musapa.flink.model.CoinCode;
import wns.musapa.flink.model.code.UpbitCoinCode;
import wns.musapa.flink.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NowDashboardSink extends RichSinkFunction<CoinCandle> implements Command.Handler {
    private static final Logger LOGGER = LoggerFactory.getLogger(NowDashboardSink.class);
    private static Map<CoinCode, CoinCandle> lastPrices = new ConcurrentHashMap<>();

    private final long windowSize;

    public NowDashboardSink(long windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public void invoke(CoinCandle value, Context context) {
        synchronized (lastPrices) {
            lastPrices.put(value.getCode(), value);
        }
        LOGGER.info("Updated: " + value.toString());
    }

    @Override
    public void handle(Command command, String[] tokens, User user, Reporter reporter) {
        if (!(command instanceof NowCommand)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Last " + this.windowSize / 60000 + " minutes...\n");
        List<CoinCandle> candles;
        synchronized (lastPrices) {
            candles = new ArrayList<>(lastPrices.values());
        }
        candles.sort((CoinCandle o1, CoinCandle o2) -> {
            if (o1.getRateOfChange() > o2.getRateOfChange()) {
                return -1;
            } else if (o1.getRateOfChange() < o2.getRateOfChange()) {
                return 1;
            } else {
                return 0;
            }
        });
        // Print top 5
        for (int i = 0; i < 5 && i < candles.size(); i++) {
            CoinCandle coinCandle = candles.get(i);
            UpbitCoinCode upbitCoinCode = (UpbitCoinCode) coinCandle.getCode();
            sb.append(String.format("%s(%s): %,d (%.3f%%) %s\n",
                    upbitCoinCode.getKorean(), upbitCoinCode.name(), (long) coinCandle.getClose().getPrice(),
                    coinCandle.getRateOfChange(), CoinCandle.printTimestamp(coinCandle.getClose().getTimestamp())));
        }
        sb.append("...\n");
        // Print bottom 5
        for (int i = candles.size() - Math.min(4, candles.size() - 1); i < candles.size(); i++) {
            CoinCandle coinCandle = candles.get(i);
            UpbitCoinCode upbitCoinCode = (UpbitCoinCode) coinCandle.getCode();
            sb.append(String.format("%s(%s): %,d (%.3f%%) %s\n",
                    upbitCoinCode.getKorean(), upbitCoinCode.name(), (long) coinCandle.getClose().getPrice(),
                    coinCandle.getRateOfChange(), CoinCandle.printTimestamp(coinCandle.getClose().getTimestamp())));
        }
        reporter.send(user, sb.toString());
    }
}
