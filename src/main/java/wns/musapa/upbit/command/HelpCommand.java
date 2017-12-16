package wns.musapa.upbit.command;

import wns.musapa.upbit.telegram.TelegramUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class HelpCommand implements TelegramCommand {

    private String helpText;

    public HelpCommand(Collection<TelegramCommand> commands) {
        List<TelegramCommand> sorted = new ArrayList<>(commands);
        sorted.sort(Comparator.comparing(TelegramCommand::getCommand));
        StringBuilder sb = new StringBuilder();
        for (TelegramCommand command : sorted) {
            sb.append(String.format("%s: %s\n\n", command.getCommand(), command.getHelp()));
        }
        this.helpText = sb.toString();
    }

    @Override
    public String getHelp() {
        return "This is /help";
    }

    @Override
    public String getCommand() {
        return "/help";
    }

    @Override
    public String process(TelegramUser user, String[] tokens) {
        return helpText;
    }
}
