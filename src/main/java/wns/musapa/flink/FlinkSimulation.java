package wns.musapa.flink;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.evictors.CountEvictor;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wns.musapa.flink.model.CoinCode;
import wns.musapa.flink.model.CoinMACD;
import wns.musapa.flink.model.CoinTick;
import wns.musapa.flink.model.code.UpbitCoinCode;
import wns.musapa.flink.processor.GetLastCoinTickProcessor;
import wns.musapa.flink.processor.MACDCalculator;
import wns.musapa.flink.processor.MACDDecisionMaker;
import wns.musapa.flink.processor.SignalCalculator;

import java.util.Arrays;
import java.util.Iterator;


public class FlinkSimulation {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlinkSimulation.class);

    public static void main(String[] args) throws Exception {
        LocalStreamEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();

        final String CSV_FILE = "./data/CRIX.UPBIT.KRW-ADA_2017-09-26_2017-12-17.csv";
        final UpbitCoinCode coinCode = UpbitCoinCode.ADA;

        double[] wallet = {1_000_000, 0.0}; // balance, coin count

        env.setParallelism(1);

        DataStream<CoinMACD.Pair> macdPipeline = env.readTextFile(CSV_FILE)
                .filter((FilterFunction<String>) s -> !s.startsWith("date")) // header
                .map((MapFunction<String, CoinTick>) s -> {
                    String[] tokens = s.split(",");
                    CoinTick coinTick = new CoinTick(coinCode, Long.parseLong(tokens[0]), Double.parseDouble(tokens[4]));
                    return coinTick;
                }) // make coin tick
                .keyBy((KeySelector<CoinTick, CoinCode>) coinTick -> coinTick.getCode())
                .countWindow(30) // mimic 30 minutes
                .evictor(CountEvictor.of(1))
                .apply(new GetLastCoinTickProcessor<>())
                .flatMap(new MACDCalculator())
                .flatMap(new SignalCalculator())
                .countWindowAll(2, 1)
                .apply(new MACDDecisionMaker());
        macdPipeline.addSink(new SinkFunction<CoinMACD.Pair>() {
            @Override
            public void invoke(CoinMACD.Pair value, Context context) throws Exception {
                CoinMACD prev = value.getPrevious();
                CoinMACD current = value.getCurrent();
                //System.out.println("prev: " + prev.getHeight() + " / current: " + current.getHeight());

                if (prev.getHeight() * current.getHeight() >= 0) {
                    return; // HOLD
                } else if (prev.getHeight() > current.getHeight()) {
                    // SELL
                    if (wallet[1] > 0) {
                        System.out.println("Selling " + wallet[1] + " at " + current.getTradePrice().getPrice());
                        wallet[0] += Math.round(current.getTradePrice().getPrice() * wallet[1]);
                        wallet[1] = 0;
                    }
                } else {
                    // BUY
                    if (wallet[0] > 0 && current.getSignal() > 0) {
                        System.out.println("Buying " + wallet[0] + " at " + current.getTradePrice().getPrice());
                        wallet[1] += wallet[0] / current.getTradePrice().getPrice();
                        wallet[0] = 0;
                    }
                }
                System.out.println("Wallet: " + Arrays.toString(wallet));
            }
        });

        //macdPipeline.writeAsText("./out/ada.csv");

        env.execute();
/*
        env.addSource(new SampleSource())
                .keyBy((KeySelector<Tuple, Integer>) tuple -> tuple.key)
                .countWindow(10000)
                .evictor(CountEvictor.of(1))
                .apply(new LastElementPicker())
                .countWindowAll(10, 1)
                .apply(new WindowCounter())
                .print();
        env.execute();
*/
    }

    static class WindowCounter<T extends Window> implements AllWindowFunction<Integer, String, T> {

        @Override
        public void apply(T t, Iterable<Integer> iterable, Collector<String> collector) throws Exception {
            StringBuilder sb = new StringBuilder();
            Iterator<Integer> iterator = iterable.iterator();
            while (iterator.hasNext()) {
                sb.append(iterator.next()).append(", ");
            }
            collector.collect(sb.toString());
        }
    }

    static class LastElementPicker<T extends Window> implements WindowFunction<Tuple, Integer, Integer, T> {

        @Override
        public void apply(Integer integer, T window, Iterable<Tuple> iterable, Collector<Integer> collector) throws Exception {
            Integer last = null;
            Iterator<Tuple> iterator = iterable.iterator();
            int count = 0;
            while (iterator.hasNext()) {
                last = iterator.next().val;
                count++;
                if (count >= 2) {
                    System.out.println("Counted more! " + count);
                }
            }
            collector.collect(last);
        }
    }

    static class SampleSource implements SourceFunction<Tuple> {

        private boolean isRunning = true;
        private static int COUNT = 1;

        @Override
        public void run(SourceContext<Tuple> sourceContext) throws Exception {
            while (isRunning) {
                sourceContext.collect(new Tuple(1, COUNT++));
            }
        }

        @Override
        public void cancel() {
            isRunning = false;
        }
    }

    static class Tuple {
        Integer key;
        Integer val;

        public Tuple(Integer k, Integer v) {
            key = k;
            val = v;
        }

        @Override
        public String toString() {
            return key + ":\t" + val;
        }
    }
}
