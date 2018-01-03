package wns.musapa.flink.command.impl;

import org.jeasy.rules.api.Rule;
import wns.musapa.flink.bot.Reporter;
import wns.musapa.flink.command.Command;
import wns.musapa.flink.rule.RuleManager;
import wns.musapa.flink.user.User;

import java.util.Collection;

public class UserCommand extends AbstractCommand implements Command.Handler {
    public UserCommand() {
        super(null);
        setHandler(this);
    }

    @Override
    public String getHelp() {
        return "Usage: /me : prints my profile\n"
                + "Usage: /me reset : resets profile\n"
                + "Usage: /me push {milliseconds} : change minimum push interval";
    }

    @Override
    public String getCommand() {
        return "/me";
    }


    @Override
    public void handle(Command command, String[] tokens, User user, Reporter reporter) {
        if (tokens.length == 1) {
            // print profile
            Collection<Rule> rules = RuleManager.getInstance().getRules(user);
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Rules (%d): \n", rules.size()));
            for (Rule rule : rules) {
                sb.append(String.format("  %s\n", rule.toString()));
            }
            sb.append(String.format("Push interval: %d", user.getPushInterval()));
            reporter.send(user, sb.toString());
            return;
        }

        if (tokens[1].equalsIgnoreCase("reset")) {
            user.reset();
            reporter.send(user, "Done.");
            return;
        } else if (tokens[1].equalsIgnoreCase("push")) {
            long interval = Long.parseLong(tokens[2]);
            user.setPushInterval(interval);
            reporter.send(user, "Push interval updated.");
            return;
        }

        reporter.send(user, "Huh?");
        return;
    }
}
