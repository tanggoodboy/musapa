package wns.musapa.upbit.rule;

public class LessThanRule implements Rule {
    private double val;

    public LessThanRule(double val) {
        this.val = val;
    }

    public boolean isFired(double rate) {
        return this.val > rate;
    }
}

