package wns.musapa;

import wns.musapa.fetcher.BithumbFetcher;
import wns.musapa.fetcher.BitstampFetcher;
import wns.musapa.model.code.BithumbCoinCode;
import wns.musapa.model.code.BitstampCoinCode;
import wns.musapa.task.KimpTask;

public class KimpAlertMain {
    public static void main(String[] args) {
        System.out.println("Hello");

        KimpTask kimpTask = new KimpTask("BTC","btcusd");
        kimpTask.addCoinTickFetcher(new BitstampFetcher(BitstampCoinCode.BTC));
        kimpTask.addCoinTickFetcher(new BithumbFetcher(BithumbCoinCode.BTC));

        final Thread t = new Thread(kimpTask);
        t.start();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                t.interrupt();
                System.out.println("Bye");
            }
        });
    }
}
