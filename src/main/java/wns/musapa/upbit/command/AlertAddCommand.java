package wns.musapa.upbit.command;

import wns.musapa.model.CoinCode;
import wns.musapa.model.code.UpbitCoinCode;
import wns.musapa.upbit.telegram.TelegramUser;
import wns.musapa.upbit.rule.RateOfChangeRule;

public class AlertAddCommand implements TelegramCommand {

    @Override
    public String getHelp() {
        return "Usage: /alertadd [coinCode] [rise|fall] 1.24";
    }

    @Override
    public String getCommand() {
        return "/alertadd";
    }

    @Override
    public String process(TelegramUser user, String[] tokens) {
        if (tokens.length < 4) {
            return "Invalid usage!";
        }

        CoinCode[] coinCodes;
        if (tokens[1].equalsIgnoreCase("*")) {
            coinCodes = UpbitCoinCode.values();
        } else {
            coinCodes = new CoinCode[]{UpbitCoinCode.parseByName(tokens[1])};
        }

        double rateOfChange = Double.parseDouble(tokens[3]);
        RateOfChangeRule.Direction direction;
        if (tokens[2].equalsIgnoreCase("rise")) {
            direction = RateOfChangeRule.Direction.Rise;
        } else if (tokens[2].equalsIgnoreCase("fall")) {
            direction = RateOfChangeRule.Direction.Fall;
        } else {
            return "Invalid usage";
        }

        // Add rules
        for (CoinCode coinCode : coinCodes) {
            String ruleName = coinCode.name() + " " + tokens[2].toLowerCase();
            user.addRule(ruleName, new RateOfChangeRule(direction, coinCode, rateOfChange));
        }

        return "Ok.";
    }
}
