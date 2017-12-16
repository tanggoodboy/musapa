package wns.musapa.upbit.command;

import wns.musapa.upbit.telegram.TelegramUser;

public interface TelegramCommand {
    String getHelp();

    String getCommand();

    String process(TelegramUser user, String[] tokens);
}
