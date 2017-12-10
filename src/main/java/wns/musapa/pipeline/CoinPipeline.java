package wns.musapa.pipeline;

public interface CoinPipeline<IN> {
    void process(IN input) throws Exception;
    void onStart() throws Exception;
    void onStop();
}
