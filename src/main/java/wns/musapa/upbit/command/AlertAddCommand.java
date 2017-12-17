package wns.musapa.upbit.command;

import wns.musapa.model.CoinCode;
import wns.musapa.model.code.UpbitCoinCode;
import wns.musapa.upbit.rule.RateOfChangeRule;
import wns.musapa.upbit.telegram.TelegramUser;

public class AlertAddCommand implements TelegramCommand {

    @Override
    public String getHelp() {
        return "Usage: /alertadd [coinCode(coinCodes separated by , or * for all)] [rise|fall] [rateOfChange]\n" +
                "E.g.: /alertadd * rise 2.00\n" +
                "E.g.: /alertadd btc,xrp rise 2.00";
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
            String[] coinTokens = tokens[1].split(",");
            coinCodes = new CoinCode[coinTokens.length];
            for (int i = 0; i < coinTokens.length; i++) {
                coinCodes[i] = UpbitCoinCode.parseByName(coinTokens[i]);
            }
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
