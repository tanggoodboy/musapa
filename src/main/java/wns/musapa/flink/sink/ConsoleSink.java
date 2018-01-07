package wns.musapa.flink.sink;

import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

public class ConsoleSink<T> extends RichSinkFunction<T> {

    @Override
    public void invoke(T value, Context context) {
        // Do nothing
        // System.out.println(value);
    }
}
