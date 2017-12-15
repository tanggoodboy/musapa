package wns.musapa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wns.musapa.model.code.UpbitCoinCode;
import wns.musapa.upbit.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpbitMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitMain.class);
    public static final long DEFAULT_INTERVAL = 10 * 1000L;
    public static final long DEFAULT_WINDOW_SIZE = 2 * 60 * 1000L;

    public static void main(String[] args) throws Exception {
        System.out.println("Hello");
        UpbitWebsocketClientLauncher websocketClientLauncher = new UpbitWebsocketClientLauncher();
        UpbitDispatcher dispatcher = new UpbitDispatcher();
        websocketClientLauncher.setUpbitDispatcher(dispatcher);

        UpbitTelegramReporter reporter = new UpbitTelegramReporter();

        UpbitCoinCode[] interest = UpbitCoinCode.values();
        UpbitPipeline[] pipelines = new UpbitPipeline[interest.length];
        for (int i = 0; i < interest.length; i++) {
            pipelines[i] = new UpbitPipeline(interest[i]);
            pipelines[i].setReporter(reporter);
            LOGGER.info("Adding pipeline: {}", interest[i].name());
            dispatcher.addCoinPipeline(interest[i], pipelines[i]);
        }

        ExecutorService executors = Executors.newCachedThreadPool();
        executors.execute(reporter);
        for (int i = 0; i < pipelines.length; i++) {
            LOGGER.info("Executing {}", pipelines[i].getCoinCode());
            executors.execute(pipelines[i]);
        }
        LOGGER.info("Executing dispatcher.");
        executors.execute(dispatcher);
        LOGGER.info("Executing websocket client launcher.");
        executors.execute(websocketClientLauncher);
        LOGGER.info("Connected!");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                executors.shutdownNow();
                System.out.println("Bye.");
            }
        });
    }
}
