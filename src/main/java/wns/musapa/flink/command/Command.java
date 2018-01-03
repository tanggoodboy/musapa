package wns.musapa.flink.command;


import wns.musapa.flink.bot.Reporter;
import wns.musapa.flink.user.User;

public interface Command {
    String getHelp();

    String getCommand();

    Handler getHandler();

    interface Handler {
        void handle(Command command, String[] tokens, User user, Reporter reporter);
    }
}
