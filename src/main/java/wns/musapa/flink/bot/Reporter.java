package wns.musapa.flink.bot;

import wns.musapa.flink.user.User;

public interface Reporter {
    void broadcast(String message);

    void send(User user, String message);
}
