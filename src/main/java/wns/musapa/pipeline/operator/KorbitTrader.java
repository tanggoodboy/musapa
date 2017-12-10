package wns.musapa.pipeline.operator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wns.musapa.model.CoinDecision;

public class KorbitTrader implements PipelineOperator<CoinDecision, CoinDecision> {
    private static final Logger LOGGER = LoggerFactory.getLogger(KorbitTrader.class);

    @Override
    public void onStart() throws Exception {

    }

    @Override
    public CoinDecision process(CoinDecision input) {
        LOGGER.info("{}", input.toString());
        return input;
    }
}
