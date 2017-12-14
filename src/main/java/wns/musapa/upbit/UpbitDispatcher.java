package wns.musapa.upbit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wns.musapa.model.CoinTick;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UpbitDispatcher implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitDispatcher.class);
    private BlockingQueue<CoinTick> unprocessedCoinTicks = new LinkedBlockingQueue<>();
    private Map<String, UpbitPipeline> coinPipelines = new HashMap<>();

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                CoinTick coinTick = unprocessedCoinTicks.take();

                UpbitPipeline pipeline = coinPipelines.get(coinTick.getCode());
                if (pipeline == null) {
                    LOGGER.debug("No pipeline available for {}", coinTick.getCode());
                    continue;
                }

                pipeline.addCoinTick(coinTick);
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public void addCoinTick(CoinTick coinTick) {
        try {
            unprocessedCoinTicks.put(coinTick);
        } catch (InterruptedException e) {
            LOGGER.error("Failed to put coinTick. Interrupted.");
        }
    }

    public void addCoinPipeline(String coinCode, UpbitPipeline pipeline) {
        this.coinPipelines.put(coinCode, pipeline);
    }
}
