package wns.musapa.pipeline.operator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wns.musapa.model.CoinAnalysis;
import wns.musapa.model.CoinWindow;
import wns.musapa.model.TradePrice;

import java.util.Iterator;

public class AnalyticsOperator implements PipelineOperator<CoinWindow, CoinAnalysis> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsOperator.class);

    @Override
    public void onStart() throws Exception {

    }

    @Override
    public CoinAnalysis process(CoinWindow input) {
        CoinAnalysis coinAnalysis = new CoinAnalysis(input.getCode());

        Iterator<TradePrice> itr = input.getCoinTicks().iterator();
        while (itr.hasNext()) {
            TradePrice tradePrice = itr.next();

            if (coinAnalysis.getOpen() == null) {
                coinAnalysis.setOpen(tradePrice);
            }

            if (coinAnalysis.getLow() == null || coinAnalysis.getLow().getPrice() > tradePrice.getPrice()) {
                coinAnalysis.setLow(tradePrice);
            }

            if (coinAnalysis.getHigh() == null || coinAnalysis.getHigh().getPrice() < tradePrice.getPrice()) {
                coinAnalysis.setHigh(tradePrice);
            }

            coinAnalysis.setClose(tradePrice);
            coinAnalysis.addCount();
        }

        // Rate
        double rate = (coinAnalysis.getClose().getPrice() - coinAnalysis.getOpen().getPrice())
                / coinAnalysis.getOpen().getPrice();

        coinAnalysis.setRateOfChange(rate);

        LOGGER.info("Rate: {}", rate);
        return coinAnalysis;
    }
}
