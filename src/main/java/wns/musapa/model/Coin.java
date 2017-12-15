package wns.musapa.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.math.BigDecimal;

public abstract class Coin {
    private static Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, typeOfSrc, context) -> {
            Number n = new Number() {
                @Override
                public long longValue() {
                    return 0;
                }

                @Override
                public int intValue() {
                    return 0;
                }

                @Override
                public float floatValue() {
                    return 0;
                }

                @Override
                public double doubleValue() {
                    return 0;
                }

                @Override
                public String toString() {
                    return new BigDecimal(src).toPlainString();
                }

            };
            return new JsonPrimitive(n);
        });
        gson = gsonBuilder.create();
    }

    private CoinCode code;

    public Coin(CoinCode code) {
        this.code = code;
    }

    public CoinCode getCode() {
        return code;
    }

    public void setCode(CoinCode code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
