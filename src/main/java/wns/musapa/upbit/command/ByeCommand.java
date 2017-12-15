package wns.musapa.upbit.command;

import wns.musapa.upbit.TelegramUser;
import wns.musapa.upbit.TelegramUsers;

public class ByeCommand extends AbstractTelegramCommand{
    @Override
    public String getHelp() {
        return "Deletes registered user information.";
    }

    @Override
    public String getCommand() {
        return "/bye";
    }

    @Override
    public String process(TelegramUser user, String[] tokens) {
        TelegramUsers.getInstance().removeUser(user.getId());
        return "Good bye.";
    }
}
