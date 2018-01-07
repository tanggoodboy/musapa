package wns.musapa.flink.rule.impl;

import org.jeasy.rules.api.Facts;
import wns.musapa.flink.bot.Reporter;
import wns.musapa.flink.model.CoinCandle;
import wns.musapa.flink.model.CoinCode;
import wns.musapa.flink.user.User;

public class RateOfChangeRule extends AbstractRule {
    public enum Direction {
        RISE, FALL
    }

    private final double threshold;

    private final String ruleName;
    private final String factKey;
    private final Direction direction;
    private final CoinCode coinCode;

    private long lastFiredAt;

    public RateOfChangeRule(User user, Reporter reporter, CoinCode coinCode, Direction direction, String ruleName, double threshold) {
        super(user, reporter);
        this.threshold = threshold;
        this.direction = direction;
        this.coinCode = coinCode;

        this.ruleName = ruleName;
        this.factKey = getFactKey(coinCode);
        this.lastFiredAt = 0;
    }

    public static String makeRuleName(User user, CoinCode coinCode, Direction direction) {
        return user.getId() + "_" + coinCode.name() + "_" + direction.name() + "_rate";
    }

    public static String getFactKey(CoinCode coinCode) {
        return coinCode.name() + "_roc";
    }

    @Override
    public String getName() {
        return this.ruleName;
    }

    @Override
    public String getDescription() {
        return this.ruleName;
    }

    @Override
    public boolean evaluate(Facts facts) {
        CoinCandle coinCandle = facts.get(this.factKey);
        if (coinCandle == null) {
            return false;
        }

        double rate = coinCandle.getRateOfChange();
        if (this.direction == Direction.RISE
                && rate > 0
                && rate >= this.threshold) {
            return true;
        } else if (this.direction == Direction.FALL
                && rate < 0
                && Math.abs(rate) >= this.threshold) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected String executeResult(Facts facts) {
        CoinCandle coinCandle = facts.get(this.factKey);
        return coinCandle.toAlertString();
    }

    @Override
    public String toString() {
        return this.coinCode + " " + this.direction.name().toLowerCase() + " " + this.threshold;
    }
}
