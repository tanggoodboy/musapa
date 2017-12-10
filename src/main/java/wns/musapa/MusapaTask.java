package wns.musapa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wns.musapa.fetcher.CoinTickFetcher;
import wns.musapa.model.CoinTick;
import wns.musapa.pipeline.CoinPipeline;

public class MusapaTask implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MusapaTask.class);

    private CoinTickFetcher coinTickFetcher = null;
    private CoinPipeline<CoinTick> pipeline = null;

    public MusapaTask() {
    }

    @Override
    public void run() {
        try {
            this.coinTickFetcher.onStart();
            this.pipeline.onStart();
        } catch (Exception e){
            LOGGER.error(e.getMessage(), e);
        }

        while(!Thread.currentThread().isInterrupted()) {
            try {
                CoinTick tick = this.coinTickFetcher.fetchTick();
                this.pipeline.process(tick);

                Thread.sleep(1000);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        this.pipeline.onStop();
        this.coinTickFetcher.onStop();
    }

    public void setCoinTickFetcher(CoinTickFetcher coinTickFetcher) {
        this.coinTickFetcher = coinTickFetcher;
    }

    public void setCoinPipeline(CoinPipeline pipeline) {
        this.pipeline = pipeline;
    }
}
