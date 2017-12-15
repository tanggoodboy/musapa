package wns.musapa.upbit.rule;

public class GreaterThanRule implements Rule {
    private double val;

    public GreaterThanRule(double val) {
        this.val = val;
    }

    public boolean isFired(double rate) {
        return this.val < rate;
    }
}

