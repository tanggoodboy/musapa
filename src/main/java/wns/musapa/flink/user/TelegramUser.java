package wns.musapa.flink.user;

public class TelegramUser implements User {
    private static final long DEFAULT_PUSH_INTERVAL = 60 * 1000L;
    private final long id; // unique id to send push message

    private long pushInterval = DEFAULT_PUSH_INTERVAL;

    public TelegramUser(long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getPushInterval() {
        synchronized (this) {
            return pushInterval;
        }
    }

    @Override
    public void setPushInterval(long pushInterval) {
        synchronized (this) {
            this.pushInterval = pushInterval;
        }
    }

    @Override
    public void reset() {
        setPushInterval(DEFAULT_PUSH_INTERVAL);
    }
}
