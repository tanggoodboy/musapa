package wns.musapa.flink.user;

public interface User {
    long getId();

    long getPushInterval();

    void reset();

    void setPushInterval(long interval);
}
