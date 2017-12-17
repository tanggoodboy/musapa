package wns.musapa.upbit.command;

import wns.musapa.upbit.rule.Rule;
import wns.musapa.upbit.telegram.TelegramUser;

import java.util.Collection;

public class UserCommand implements TelegramCommand {
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
    public String process(TelegramUser user, String[] tokens) {
        if (tokens.length == 1) {
            // print profile
            Collection<Rule> rules = user.getRules();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Rules (%d): \n", rules.size()));
            for (Rule rule : rules) {
                sb.append(String.format("  %s\n", rule.toString()));
            }
            sb.append(String.format("Push interval: %d", user.getPushInterval()));
            return sb.toString();
        }

        if (tokens[1].equalsIgnoreCase("reset")) {
            user.reset();
            return "Done.";
        } else if (tokens[1].equalsIgnoreCase("push")) {
            long interval = Long.parseLong(tokens[2]);
            user.setPushInterval(interval);
            return "Push interval updated.";
        }

        return "Huh?";
    }
}
