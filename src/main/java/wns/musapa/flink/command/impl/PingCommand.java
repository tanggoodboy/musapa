package wns.musapa.flink.command.impl;

import wns.musapa.flink.bot.Reporter;
import wns.musapa.flink.command.Command;
import wns.musapa.flink.user.User;

public class PingCommand extends AbstractCommand implements Command.Handler {
    public PingCommand() {
        super(null);
        setHandler(this);
    }

    @Override
    public String getHelp() {
        return "You say /ping, I say /pong";
    }

    @Override
    public String getCommand() {
        return "/ping";
    }

    @Override
    public void handle(Command command, String[] tokens, User user, Reporter reporter) {
        reporter.send(user, "/pong");
    }
}
