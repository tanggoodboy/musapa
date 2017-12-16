package wns.musapa.upbit.rule;

public abstract class AbstractRule implements Rule {
    private long lastPushAt = 0;

    public AbstractRule() {

    }

    @Override
    public long getLastPushAt() {
        return this.lastPushAt;
    }

    @Override
    public void updateLastPushAtToNow() {
        this.lastPushAt = System.currentTimeMillis();
    }
}
