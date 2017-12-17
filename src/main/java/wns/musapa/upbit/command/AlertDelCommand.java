package wns.musapa.upbit.command;

import wns.musapa.model.CoinCode;
import wns.musapa.model.code.UpbitCoinCode;
import wns.musapa.upbit.telegram.TelegramUser;

public class AlertDelCommand implements TelegramCommand {

    @Override
    public String getHelp() {
        return "Usage: /alertdel [coinCode(coinCodes separated by , or * for all)] [rise|fall]\n" +
                "E.g.: /alertdel * rise \n" +
                "E.g.: /alertdel btc,xrp rise ";
    }

    @Override
    public String getCommand() {
        return "/alertdel";
    }

    @Override
    public String process(TelegramUser user, String[] tokens) {
        if (tokens.length < 3) {
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

        // Del rules
        for (CoinCode coinCode : coinCodes) {
            String ruleName = coinCode.name() + " " + tokens[2].toLowerCase();
            user.removeRule(ruleName);
        }

        return "Ok.";
    }
}
