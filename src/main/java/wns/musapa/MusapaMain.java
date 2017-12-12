package wns.musapa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wns.musapa.fetcher.KorbitFetcher;
import wns.musapa.pipeline.KorbitPipeline;
import wns.musapa.task.MusapaTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusapaMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(MusapaMain.class);

    public static void main(String[] args) {
        LOGGER.info("Hello!");

        MusapaTask korbitTask = new MusapaTask();
        korbitTask.setCoinTickFetcher(new KorbitFetcher("btc_krw"));
        korbitTask.setCoinPipeline(new KorbitPipeline());

        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(korbitTask);

        executorService.shutdown();

        LOGGER.info("Bye!");
    }
}
