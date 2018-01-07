package wns.musapa.flink.processor;

import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.util.Collector;
import wns.musapa.flink.model.CoinCandle;
import wns.musapa.flink.model.CoinCode;
import wns.musapa.flink.model.CoinMACD;

public class MACDProcessor implements WindowFunction<CoinCandle, CoinMACD, CoinCode, GlobalWindow> {

    public MACDProcessor() {

    }

    @Override
    public void apply(CoinCode coinCode, GlobalWindow globalWindow, Iterable<CoinCandle> iterable, Collector<CoinMACD> collector) throws Exception {
        CoinMACD macd = new CoinMACD(coinCode);
        for (CoinCandle coinCandle : iterable) {
            macd.add(coinCandle);
        }
        collector.collect(macd);
    }
}
