package wns.musapa.upbit.command;

import wns.musapa.upbit.TelegramUser;

public class PingCommand extends AbstractTelegramCommand{
    @Override
    public String getHelp() {
        return "You say /ping, I say /pong";
    }

    @Override
    public String getCommand() {
        return "/ping";
    }

    @Override
    public String process(TelegramUser user, String[] tokens) {
        return "/pong";
    }

}
