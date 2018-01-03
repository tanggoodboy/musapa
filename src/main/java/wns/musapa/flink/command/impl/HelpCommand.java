package wns.musapa.flink.command.impl;

import wns.musapa.flink.bot.Reporter;
import wns.musapa.flink.command.Command;
import wns.musapa.flink.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class HelpCommand extends AbstractCommand implements Command.Handler {
    private final Collection<Command> commands;

    public HelpCommand(Collection<Command> commands) {
        super(null);
        setHandler(this);

        this.commands = commands;
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
    public void handle(Command command, String[] tokens, User user, Reporter reporter) {
        List<Command> sorted = new ArrayList<>(this.commands);
        sorted.sort(Comparator.comparing(Command::getCommand));
        StringBuilder sb = new StringBuilder();
        for (Command c : sorted) {
            sb.append(String.format("%s: %s\n\n", c.getCommand(), c.getHelp()));
        }
        reporter.send(user, sb.toString());
    }
}
