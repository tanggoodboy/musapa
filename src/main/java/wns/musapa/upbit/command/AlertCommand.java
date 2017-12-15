package wns.musapa.upbit.command;

import wns.musapa.upbit.TelegramUser;
import wns.musapa.upbit.rule.GreaterThanRule;

public class AlertCommand extends AbstractTelegramCommand {

    @Override
    public String getHelp() {
        return "Alerts if ANY coin rises above specified rate in %.\n" +
                "Usage: /alert_{rate} Example: /alert_3.14 to listen for coins with 3.14 or more % increase.\n" +
                "Note multiple alerts for same coin will not be sent consecutively within 2 minutes.";
    }

    @Override
    public String getCommand() {
        return "/alert";
    }

    @Override
    public String process(TelegramUser user, String[] tokens) {
        if (tokens.length < 2) {
            return "Make sure to enter rate of change!";
        }
        user.addRule(String.join("_", tokens[0], tokens[1]),
                new GreaterThanRule(Double.parseDouble(tokens[1])));
        return "Got it.";
    }
}
