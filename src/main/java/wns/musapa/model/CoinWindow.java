package wns.musapa.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

public class CoinWindow extends Coin {
    private Queue<TradePrice> prices;

    public CoinWindow(String coinCode){
        super(coinCode);
        this.prices = new LinkedList<>();
    }

    public void addCoinTicks(Collection<CoinTick> ticks) {
        for(CoinTick tick : ticks){
            this.prices.add(new TradePrice(tick.getTimestamp(), tick.getTradePrice()));
        }
    }

    public Collection<TradePrice> getCoinTicks() {
        return Collections.unmodifiableCollection(this.prices);
    }
}
