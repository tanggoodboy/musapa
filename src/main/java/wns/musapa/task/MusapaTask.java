package wns.musapa.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wns.musapa.fetcher.CoinTickFetcher;
import wns.musapa.model.CoinTick;
import wns.musapa.pipeline.CoinPipeline;

public class MusapaTask implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MusapaTask.class);

    private CoinTickFetcher coinTickFetcher = null;
    private CoinPipeline<CoinTick> pipeline = null;

    private long interval = 1000L;

    public MusapaTask() {
        this(1000L);
    }

    public MusapaTask(long interval) {
        this.interval = interval;
    }

    @Override
    public void run() {
        try {
            this.coinTickFetcher.onStart();
            this.pipeline.onStart();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        while (!Thread.currentThread().isInterrupted()) {
            try {
                CoinTick tick = this.coinTickFetcher.fetchTick();
                this.pipeline.process(tick);

                Thread.sleep(this.interval);
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
