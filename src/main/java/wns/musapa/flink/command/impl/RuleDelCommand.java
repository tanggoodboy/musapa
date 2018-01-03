package wns.musapa.flink.command.impl;

import wns.musapa.flink.bot.Reporter;
import wns.musapa.flink.command.Command;
import wns.musapa.flink.model.CoinCode;
import wns.musapa.flink.model.code.UpbitCoinCode;
import wns.musapa.flink.rule.RuleManager;
import wns.musapa.flink.rule.impl.RateOfChangeRule;
import wns.musapa.flink.user.User;

public class RuleDelCommand extends AbstractCommand implements Command.Handler {
    public RuleDelCommand() {
        super(null);
        setHandler(this);
    }

    @Override
    public String getHelp() {
        return "Usage: /delrule roc [coinCode(coinCodes separated by , or * for all)] [rise|fall]\n" +
                "E.g.: /delrule roc * rise \n" +
                "E.g.: /delrule roc btc,xrp rise ";
    }

    @Override
    public String getCommand() {
        return "/delrule";
    }

    @Override
    public void handle(Command command, String[] tokens, User user, Reporter reporter) {
        if (tokens.length < 2) {
            reporter.send(user, "Invalid usage!");
            return;
        }

        if (tokens[1].toLowerCase().equals("roc")) {
            handleRateOfChange(command, tokens, user, reporter);
        } else {
            reporter.send(user, "Invalid usage!");
            return;
        }

    }

    private void handleRateOfChange(Command command, String[] tokens, User user, Reporter reporter) {
        if (tokens.length < 4) {
            reporter.send(user, "Invalid usage!");
            return;
        }

        CoinCode[] coinCodes;
        if (tokens[2].equalsIgnoreCase("*")) {
            coinCodes = UpbitCoinCode.values();
        } else {
            String[] coinTokens = tokens[2].split(",");
            coinCodes = new CoinCode[coinTokens.length];
            for (int i = 0; i < coinTokens.length; i++) {
                coinCodes[i] = UpbitCoinCode.parseByName(coinTokens[i]);
            }
        }

        RateOfChangeRule.Direction direction;
        if (tokens[3].equalsIgnoreCase("rise")) {
            direction = RateOfChangeRule.Direction.RISE;
        } else if (tokens[3].equalsIgnoreCase("fall")) {
            direction = RateOfChangeRule.Direction.FALL;
        } else {
            reporter.send(user, "Invalid usage!");
            return;
        }

        // Del rules
        for (CoinCode coinCode : coinCodes) {
            String ruleName = RateOfChangeRule.makeRuleName(user, coinCode, direction);
            RuleManager.getInstance().removeRule(ruleName);
        }

        reporter.send(user, "Ok.");
        return;
    }
}
