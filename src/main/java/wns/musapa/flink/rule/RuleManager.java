package wns.musapa.flink.rule;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wns.musapa.flink.rule.impl.AbstractRule;
import wns.musapa.flink.user.User;

import java.util.ArrayList;
import java.util.Collection;

public class RuleManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuleManager.class);
    private static RuleManager instance = null;

    public synchronized static RuleManager getInstance() {
        if (instance == null) {
            instance = new RuleManager();
        }

        return instance;
    }

    private RulesEngine rulesEngine;
    private Rules rules;
    private Facts facts;

    private RuleManager() {
        this.rulesEngine = new DefaultRulesEngine();
        this.rules = new Rules();
        this.facts = new Facts();
    }

    public synchronized void addFact(String key, Object val) {
        this.facts.put(key, val);
    }

    public synchronized void removeFact(String key) {
        this.facts.remove(key);
    }

    public synchronized void addRule(Rule rule) {
        this.rules.unregister(rule.getName());
        this.rules.register(rule);
    }

    public synchronized Collection<Rule> getRules(User user) {
        Collection<Rule> userRules = new ArrayList<>();
        for (Rule rule : this.rules) {
            if (!(rule instanceof AbstractRule)) {
                continue;
            }
            if (((AbstractRule) rule).getUser() == user) {
                userRules.add(rule);
            }
        }
        return userRules;
    }

    public synchronized void removeRule(String ruleName) {
        this.rules.unregister(ruleName);
    }

    public synchronized void fireRules() {
        if (!rules.isEmpty()) {
            this.rulesEngine.fire(rules, facts);
        }
    }
}
