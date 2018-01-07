package wns.musapa.flink.rule.impl;

import org.jeasy.rules.api.Facts;
import wns.musapa.flink.bot.Reporter;
import wns.musapa.flink.model.CoinCode;
import wns.musapa.flink.model.CoinMACD;
import wns.musapa.flink.user.User;

public class MACDRule extends AbstractRule {
    private final CoinCode coinCode;

    public MACDRule(User user, Reporter reporter, CoinCode coinCode) {
        super(user, reporter);
        this.coinCode = coinCode;
    }

    public static String makeRuleName(User user, CoinCode coinCode) {
        return user.getId() + "_" + coinCode.name() + "_macd";
    }

    public static String getFactKey(CoinCode coinCode) {
        return coinCode.name() + "_macd";
    }

    @Override
    public String getName() {
        return makeRuleName(user, coinCode);
    }

    @Override
    public String getDescription() {
        return makeRuleName(user, coinCode);
    }

    @Override
    public boolean evaluate(Facts facts) {
        CoinMACD.Pair pair = facts.get(getFactKey(this.coinCode));
        if (pair == null) {
            return false;
        }

        return pair.getPrevious().getHeight() * pair.getCurrent().getHeight() < 0; // different sign
    }

    @Override
    protected String executeResult(Facts facts) {
        CoinMACD.Pair pair = facts.get(getFactKey(this.coinCode));
        if (pair == null) {
            return null;
        } else if (pair.getPrevious().getHeight() * pair.getCurrent().getHeight() >= 0) {
            return null;
        }

        return pair.toAlertString();
    }

    @Override
    public String toString() {
        return this.coinCode.name() + " MACD";
    }
}
