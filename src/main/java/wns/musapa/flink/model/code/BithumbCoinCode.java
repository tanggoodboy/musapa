package wns.musapa.flink.model.code;

import wns.musapa.flink.model.CoinCode;

public enum BithumbCoinCode implements CoinCode {
    BTC("BTC");

    String code;

    BithumbCoinCode(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return this.code;
    }
}
