package wns.musapa.flink.processor;

import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import wns.musapa.flink.model.CoinCode;
import wns.musapa.flink.model.CoinTick;

import java.util.Iterator;

public class GetLastCoinTickProcessor implements WindowFunction<CoinTick, CoinTick, CoinCode, TimeWindow> {
    @Override
    public void apply(CoinCode coinCode, TimeWindow timeWindow, Iterable<CoinTick> iterable, Collector<CoinTick> collector) throws Exception {
        Iterator<CoinTick> iterator = iterable.iterator();
        CoinTick coinTick = iterator.next();
        while (iterator.hasNext()) {
            coinTick = iterator.next();
        }
        if (coinTick != null) {
            collector.collect(coinTick);
        }
    }
}
