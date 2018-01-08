package wns.musapa.flink.processor;

import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.util.Collector;
import wns.musapa.flink.model.CoinMACD;
import wns.musapa.flink.rule.RuleManager;
import wns.musapa.flink.rule.impl.MACDRule;

import java.util.Iterator;

public class MACDCalculator implements AllWindowFunction<CoinMACD, CoinMACD.Pair, GlobalWindow> {

    @Override
    public void apply(GlobalWindow globalWindow, Iterable<CoinMACD> iterable, Collector<CoinMACD.Pair> collector) throws Exception {
        try {
            CoinMACD last;
            CoinMACD current;
            Iterator<CoinMACD> itr = iterable.iterator();
            last = itr.next();
            current = itr.next();

            CoinMACD.Pair pair = new CoinMACD.Pair(last, current);

            RuleManager.getInstance().addFact(MACDRule.getFactKey(current.getCode()), pair);

            collector.collect(pair);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
