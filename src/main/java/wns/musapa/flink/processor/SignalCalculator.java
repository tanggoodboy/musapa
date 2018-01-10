package wns.musapa.flink.processor;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import wns.musapa.flink.model.CoinMACD;
import wns.musapa.flink.model.EMA;

public class SignalCalculator implements FlatMapFunction<CoinMACD, CoinMACD> {
    private EMA signal;

    public SignalCalculator() {
        this.signal = new EMA(9);
    }

    @Override
    public void flatMap(CoinMACD coinMACD, Collector<CoinMACD> collector) throws Exception {
        Double s = this.signal.add(coinMACD.getMACD());
        if (s != null) {
            coinMACD.setSignal(s);
            collector.collect(coinMACD);
        }
    }
}
