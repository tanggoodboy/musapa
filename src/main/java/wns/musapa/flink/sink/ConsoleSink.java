package wns.musapa.flink.sink;

import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import wns.musapa.flink.model.Coin;

public class ConsoleSink<T extends Coin> extends RichSinkFunction<T> {

    @Override
    public void invoke(T value, Context context) {
        System.out.println(value);
    }
}
