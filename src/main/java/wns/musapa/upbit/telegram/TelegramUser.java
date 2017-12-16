package wns.musapa.upbit.telegram;

import wns.musapa.model.CoinAnalysis;
import wns.musapa.upbit.rule.Rule;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TelegramUser {
    private static final long DEFAULT_PUSH_INTERVAL = 60 * 1000L;
    private final long id; // unique id to send push message

    private Map<String, Rule> rules = new ConcurrentHashMap<>();

    private long pushInterval = DEFAULT_PUSH_INTERVAL;

    public TelegramUser(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void addRule(String ruleName, Rule rule) {
        rules.put(ruleName, rule);
    }

    public void clearRules() {
        rules.clear();
    }

    public long getPushInterval() {
        synchronized (this) {
            return pushInterval;
        }
    }

    public void setPushInterval(long pushInterval) {
        synchronized (this) {
            this.pushInterval = pushInterval;
        }
    }

    public void fireRules(CoinAnalysis lastUpdated, UpbitTelegramBot upbitTelegramBot) {
        for (Rule rule : this.rules.values()) {
            if (rule.isMatch(lastUpdated) && rule.getLastPushAt() + getPushInterval() <= System.currentTimeMillis()) {
                String message = rule.fire(lastUpdated);
                if (message != null && !message.isEmpty()) {
                    upbitTelegramBot.send(getId(), message);
                }
                rule.updateLastPushAtToNow();
            }
        }
    }

    public void removeRule(String ruleName) {
        rules.remove(ruleName);
    }

    public Collection<Rule> getRules() {
        return rules.values();
    }

    public void reset() {
        clearRules();
        setPushInterval(DEFAULT_PUSH_INTERVAL);
    }
}
