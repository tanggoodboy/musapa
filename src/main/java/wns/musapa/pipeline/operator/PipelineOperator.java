package wns.musapa.pipeline.operator;

public interface PipelineOperator<IN, OUT> {
    void onStart() throws Exception;
    OUT process(IN input);
}
