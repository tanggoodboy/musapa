package wns.musapa.flink.model;

import java.io.Serializable;

public class EMA implements Serializable {
    private final int size;
    private final double alpha;

    private double value;
    private int count = 0;

    public EMA(int size) {
        this(size, (2.0 / (size + 1)));
    }

    public EMA(int size, double alpha) {
        this.size = size;
        this.alpha = alpha;

        this.value = 0;
        this.count = 0;
    }

    public Double add(double price) {
        if (count < size) {
            value = (value * count + value) / (count + 1);
            count++;
            return null;
        } else {
            value = price * alpha + (1 - alpha) * value;
            return value;
        }
    }

}
