package wns.musapa.flink.source;

import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wns.musapa.flink.model.CoinTick;

public class UpbitWebsocketSource extends RichSourceFunction<CoinTick> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitWebsocketSource.class);
    private static final long TIME_TO_LIVE = 30 * 1000L; // 30 seconds

    private UpbitWebsocketClient webSocketClient = null;
    //private Pipeline pipeline = null;

    public UpbitWebsocketSource() {
    }

    @Override
    public void run(SourceContext<CoinTick> sourceContext) throws Exception {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (this.webSocketClient == null) {
                    LOGGER.info("Starting websocket client.");
                    this.webSocketClient = new UpbitWebsocketClient();
                    this.webSocketClient.setSourceContext(sourceContext);
                    this.webSocketClient.connectBlocking();
                } else if (this.webSocketClient.isClosed() ||
                        webSocketClient.getLastMessageAt() + TIME_TO_LIVE < System.currentTimeMillis()) {
                    LOGGER.info("Attempting to reconnect to upbit server.");
                    // Reconnect. Time out.
                    if (!this.webSocketClient.isClosed()) {
                        this.webSocketClient.closeBlocking();
                    }
                    // Make new client (old client is not reusable)
                    this.webSocketClient = new UpbitWebsocketClient();
                    //this.webSocketClient.setPipeline(this.pipeline);
                    this.webSocketClient.connectBlocking();
                    LOGGER.info("Reconnected.");
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void cancel() {
        Thread.currentThread().interrupt();
    }
}
