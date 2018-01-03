package wns.musapa.flink.rule.impl;

import org.jeasy.rules.api.Rule;
import wns.musapa.flink.bot.Reporter;
import wns.musapa.flink.user.User;

public abstract class AbstractRule implements Rule {
    protected final User user;
    protected final Reporter reporter;

    public AbstractRule(User user, Reporter reporter) {
        this.user = user;
        this.reporter = reporter;
    }

    public User getUser() {
        return user;
    }

    public Reporter getReporter() {
        return this.reporter;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int compareTo(Rule o) {
        return getName().compareTo(o.getName());
    }
}
