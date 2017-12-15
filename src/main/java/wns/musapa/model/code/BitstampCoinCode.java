package wns.musapa.model.code;

import wns.musapa.model.CoinCode;

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

