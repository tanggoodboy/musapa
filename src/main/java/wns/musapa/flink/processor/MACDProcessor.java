package wns.musapa.flink.processor;

import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.util.Collector;
import wns.musapa.flink.model.CoinMACD;
import wns.musapa.flink.model.CoinTick;

public class MACDProcessor implements AllWindowFunction<CoinTick, CoinMACD, GlobalWindow> {

    @Override
    public void apply(GlobalWindow globalWindow, Iterable<CoinTick> iterable, Collector<CoinMACD> collector) throws Exception {
        CoinMACD macd = null;
        for (CoinTick coinTick : iterable) {
            if (macd == null) {
                macd = new CoinMACD(coinTick.getCode());
            }
            macd.add(coinTick);
        }
        if (macd != null) {
            collector.collect(macd);
        }
    }
}
