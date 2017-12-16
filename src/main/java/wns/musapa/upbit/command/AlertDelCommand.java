package wns.musapa.upbit.command;

import wns.musapa.model.CoinCode;
import wns.musapa.model.code.UpbitCoinCode;
import wns.musapa.upbit.telegram.TelegramUser;

public class AlertDelCommand implements TelegramCommand {

    @Override
    public String getHelp() {
        return "Usage: /alertdel [coinCode] [rise|fall]";
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
            coinCodes = new CoinCode[]{UpbitCoinCode.parseByName(tokens[1])};
        }

        // Del rules
        for (CoinCode coinCode : coinCodes) {
            String ruleName = coinCode.name() + " " + tokens[2].toLowerCase();
            user.removeRule(ruleName);
        }

        return "Ok.";
    }
}
