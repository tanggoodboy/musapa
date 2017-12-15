package wns.musapa.upbit;

import wns.musapa.upbit.rule.Rule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TelegramUser {
    private final long id; // unique id to send push message

    private Map<String, Rule> rules = new ConcurrentHashMap<>();
    private Map<String, Long> lastPushAt = new ConcurrentHashMap<>();

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

    public void markPushTime(String key, long timestamp) {
        lastPushAt.put(key, timestamp);
    }

    public long getPushTime(String key) {
        return lastPushAt.get(key);
    }
}
