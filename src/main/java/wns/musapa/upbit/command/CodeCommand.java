package wns.musapa.upbit.command;

import wns.musapa.model.code.UpbitCoinCode;
import wns.musapa.upbit.telegram.TelegramUser;

public class CodeCommand implements TelegramCommand {
    private String availableCoinCodes;

    public CodeCommand() {
        UpbitCoinCode[] codes = UpbitCoinCode.values();
        StringBuilder sb = new StringBuilder();
        for (UpbitCoinCode code : codes) {
            sb.append(String.format("%s(%s)\n", code.getKorean(), code.name()));
        }
        this.availableCoinCodes = sb.toString();
    }

    @Override
    public String getHelp() {
        return "Prints a list of available coin codes";
    }

    @Override
    public String getCommand() {
        return "/code";
    }

    @Override
    public String process(TelegramUser user, String[] tokens) {
        return availableCoinCodes;
    }
}
