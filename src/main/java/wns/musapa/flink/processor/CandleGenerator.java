package wns.musapa.flink.processor;

import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import wns.musapa.flink.model.CoinCandle;
import wns.musapa.flink.model.CoinCode;
import wns.musapa.flink.model.CoinTick;
import wns.musapa.flink.rule.RuleManager;
import wns.musapa.flink.rule.impl.RateOfChangeRule;

public class CandleGenerator implements WindowFunction<CoinTick, CoinCandle, CoinCode, TimeWindow> {

    @Override
    public void apply(CoinCode coinCode, TimeWindow timeWindow, Iterable<CoinTick> iterable, Collector<CoinCandle> collector) throws Exception {
        CoinCandle coinCandle = new CoinCandle(coinCode);
        for (CoinTick coinTick : iterable) {
            if (coinCandle.getOpen() == null) {
                coinCandle.setOpen(coinTick.getTradePrice());
            }

            if (coinCandle.getLow() == null
                    || coinCandle.getLow().getPrice() > coinTick.getTradePrice().getPrice()) {
                coinCandle.setLow(coinTick.getTradePrice());
            }

            if (coinCandle.getHigh() == null
                    || coinCandle.getHigh().getPrice() < coinTick.getTradePrice().getPrice()) {
                coinCandle.setHigh(coinTick.getTradePrice());
            }

            coinCandle.setClose(coinTick.getTradePrice());
        }
        collector.collect(coinCandle);

        // Add to fact
        RuleManager.getInstance().addFact(RateOfChangeRule.getFactKey(coinCode), coinCandle);
        RuleManager.getInstance().fireRules();
    }
}
