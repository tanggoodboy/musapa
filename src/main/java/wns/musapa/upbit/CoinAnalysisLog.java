package wns.musapa.upbit;

import wns.musapa.model.CoinAnalysis;
import wns.musapa.model.CoinCode;

import java.util.concurrent.ConcurrentHashMap;

public class CoinAnalysisLog extends ConcurrentHashMap<CoinCode, CoinAnalysis> {
    private static CoinAnalysisLog instance = null;

    public synchronized static CoinAnalysisLog getInstance() {
        if (instance == null) {
            instance = new CoinAnalysisLog();
        }
        return instance;
    }

    private CoinAnalysisLog() {

    }
}
