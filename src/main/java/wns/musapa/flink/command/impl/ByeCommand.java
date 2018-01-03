package wns.musapa.flink.command.impl;

import wns.musapa.flink.bot.Reporter;
import wns.musapa.flink.command.Command;
import wns.musapa.flink.user.User;
import wns.musapa.flink.user.Users;

public class ByeCommand extends AbstractCommand implements Command.Handler {
    public ByeCommand() {
        super(null);
        setHandler(this);
    }

    @Override
    public String getHelp() {
        return "Deletes registered user information.";
    }

    @Override
    public String getCommand() {
        return "/bye";
    }

    @Override
    public void handle(Command command, String[] tokens, User user, Reporter reporter) {
        Users.getInstance().removeUser(user.getId());
        reporter.send(user, "Bye.");
    }
}
