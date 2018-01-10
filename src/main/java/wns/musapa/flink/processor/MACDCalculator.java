package wns.musapa.flink.processor;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import wns.musapa.flink.model.CoinMACD;
import wns.musapa.flink.model.CoinTick;
import wns.musapa.flink.model.EMA;

public class MACDCalculator implements FlatMapFunction<CoinTick, CoinMACD> {
    private EMA e12;
    private EMA e26;

    public MACDCalculator() {
        this.e12 = new EMA(12);
        this.e26 = new EMA(26);
    }

    @Override
    public void flatMap(CoinTick coinTick, Collector<CoinMACD> collector) throws Exception {
        Double v12 = this.e12.add(coinTick.getTradePrice().getPrice());
        Double v26 = this.e26.add(coinTick.getTradePrice().getPrice());
        if (v12 != null && v26 != null) {
            CoinMACD coinMACD = new CoinMACD(coinTick.getCode());
            coinMACD.setEma12(v12);
            coinMACD.setEma26(v26);
            coinMACD.setTradePrice(coinTick.getTradePrice());
            collector.collect(coinMACD);
        }
    }
}
