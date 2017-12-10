package wns.musapa.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wns.musapa.model.Coin;
import wns.musapa.model.CoinTick;
import wns.musapa.pipeline.operator.*;

import java.util.LinkedList;
import java.util.List;

public class KorbitPipeline implements CoinPipeline<CoinTick> {
    private static final Logger LOGGER = LoggerFactory.getLogger(KorbitPipeline.class);

    private List<PipelineOperator<? extends Coin, ? extends Coin>> pipeline = null;

    @Override
    public void process(CoinTick input) throws Exception {
        Coin in = input;
        Coin out = in;
        for(PipelineOperator op : pipeline){
            out = (Coin) op.process(in);
            in = out;
        }
        //LOGGER.debug("Pipeline done.");
    }

    @Override
    public void onStart() throws Exception {
        this.pipeline = new LinkedList<>();
        this.pipeline.add(new WindowOperator());
        this.pipeline.add(new AnalyticsOperator());
        this.pipeline.add(new DecisionOperator());
        this.pipeline.add(new KorbitTrader());
    }

    @Override
    public void onStop() {

    }
}
