package wns.musapa.flink.model.code;

import wns.musapa.flink.model.CoinCode;

public enum BitstampCoinCode implements CoinCode {
    BTC("");

    String code;

    BitstampCoinCode(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return this.code;
    }
}

