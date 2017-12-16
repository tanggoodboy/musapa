package wns.musapa.upbit.rule;

import wns.musapa.model.CoinAnalysis;
import wns.musapa.model.CoinCode;

import java.util.Map;

public interface Rule {
    boolean isMatch(CoinAnalysis coinAnalysis);

    long getLastPushAt();

    String fire(CoinAnalysis coinAnalysis);

    void updateLastPushAtToNow();

    String toString();
}
