package wns.musapa.fetcher;

import wns.musapa.model.CoinTick;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class HistoryFetcher implements CoinTickFetcher {
    private BufferedReader br = null;

    public HistoryFetcher() {

    }

    @Override
    public CoinTick fetchTick() throws Exception {
        String line = br.readLine();
        String[] tokens = line.split(",");
        // date, open, close, low, high, volume
        CoinTick tick = new CoinTick("kRW-BTC", Long.parseLong(tokens[0]), Double.parseDouble(tokens[2]));
        return tick;
    }

    @Override
    public void onStart() throws Exception {
        br = new BufferedReader(new FileReader(new File("./data/CRIX.UPBIT.KRW-BTC_2017-09-26.csv")));
        br.readLine(); // discard header
    }

    @Override
    public void onStop() {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
            }
        }
    }
}
