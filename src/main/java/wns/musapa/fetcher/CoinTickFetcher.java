package wns.musapa.fetcher;

import wns.musapa.model.CoinTick;

public interface CoinTickFetcher {
    CoinTick fetchTick() throws Exception;
    void onStart() throws Exception;
    void onStop();
}
