package wns.musapa.flink.rule.impl;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import wns.musapa.flink.bot.Reporter;
import wns.musapa.flink.user.User;

public abstract class AbstractRule implements Rule {
    protected final User user;
    protected final Reporter reporter;

    private long lastFiredAt;

    public AbstractRule(User user, Reporter reporter) {
        this.user = user;
        this.reporter = reporter;

        this.lastFiredAt = 0;
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

    @Override
    public void execute(Facts facts) throws Exception {
        if (this.reporter == null || this.user == null ||
                this.lastFiredAt + this.user.getPushInterval() > System.currentTimeMillis()) {
            // too early to fire. ignore
            return;
        }

        // fire
        String result = executeResult(facts);
        if (result != null && !result.isEmpty()) {
            this.reporter.send(this.user, result);
            this.lastFiredAt = System.currentTimeMillis();
        }
    }

    protected abstract String executeResult(Facts facts);
}
