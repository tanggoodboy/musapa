package wns.musapa.upbit.rule;

public class EqualToRule implements Rule {
    private double val;

    public EqualToRule(double val) {
        this.val = val;
    }

    public boolean isFired(double rate) {
        return this.val == rate;
    }
}
