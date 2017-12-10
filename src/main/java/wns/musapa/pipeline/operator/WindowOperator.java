package wns.musapa.pipeline.operator;

import wns.musapa.model.CoinTick;
import wns.musapa.model.CoinWindow;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

public class WindowOperator implements PipelineOperator<CoinTick, CoinWindow> {
    private Queue<CoinTick> coins = new LinkedList<>();

    private long windowSize = 30 * 1000;

    public WindowOperator() {

    }

    @Override
    public void onStart() throws Exception {
    }

    @Override
    public CoinWindow process(CoinTick input) {
        this.coins.add(input);

        // Pop deprecated ticks
        long minTimestamp = input.getTimestamp() - this.windowSize;
        while (!this.coins.isEmpty() && this.coins.peek().getTimestamp() < minTimestamp) {
            this.coins.poll();
        }

        CoinWindow coinWindow = new CoinWindow(input.getCode());
        coinWindow.addCoinTicks(Collections.unmodifiableCollection(this.coins));

        return coinWindow;
    }
}
