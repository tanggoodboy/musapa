package wns.musapa.upbit.command;

import wns.musapa.upbit.telegram.TelegramUser;
import wns.musapa.upbit.telegram.TelegramUsers;

public class ByeCommand implements TelegramCommand {
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
