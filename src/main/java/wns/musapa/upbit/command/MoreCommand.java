package wns.musapa.upbit.command;

import wns.musapa.model.CoinAnalysis;
import wns.musapa.model.code.UpbitCoinCode;
import wns.musapa.upbit.CoinAnalysisLog;
import wns.musapa.upbit.telegram.TelegramUser;
import wns.musapa.upbit.UpbitUtil;

public class MoreCommand implements TelegramCommand {
    @Override
    public String getHelp() {
        return "Prints coin price details. Usage: /more [coinCode]  Example: /more btc to print bitcoin.";
    }

    @Override
    public String getCommand() {
        return "/more";
    }

    @Override
    public String process(TelegramUser user, String[] tokens) {
        if (tokens.length < 2) {
            return "Make sure to enter coin code!";
        }

        UpbitCoinCode coinCode = UpbitCoinCode.parseByName(tokens[1].toLowerCase());
        CoinAnalysis analysis = CoinAnalysisLog.getInstance().get(coinCode);
        if (analysis == null) {
            return "Not enough information is collected, just yet, for " + coinCode.name();
        } else {
            return UpbitUtil.print(analysis);
        }
    }

}
