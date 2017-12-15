package wns.musapa.upbit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpbitWebsocketClientLauncher implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitWebsocketClientLauncher.class);
    private static final long TIME_TO_LIVE = 30 * 1000L; // 30 seconds

    private UpbitWebsocketClient webSocketClient = null;
    private UpbitDispatcher upbitDispatcher = null;

    public UpbitWebsocketClientLauncher() {
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (this.webSocketClient == null) {
                    LOGGER.info("Starting websocket client.");
                    this.webSocketClient = new UpbitWebsocketClient();
                    this.webSocketClient.setUpbitDispatcher(this.upbitDispatcher);
                    this.webSocketClient.connectBlocking();
                } else if (this.webSocketClient.isClosed() ||
                        webSocketClient.getLastMessageAt() + TIME_TO_LIVE < System.currentTimeMillis()) {
                    LOGGER.info("Attempting to reconnect to upbit server.");
                    // Reconnect. Time out.
                    if (!this.webSocketClient.isClosed()) {
                        this.webSocketClient.closeBlocking();
                    }
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

    public void setUpbitDispatcher(UpbitDispatcher upbitDispatcher) {
        this.upbitDispatcher = upbitDispatcher;
    }
}
