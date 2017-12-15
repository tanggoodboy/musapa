package wns.musapa.upbit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TelegramUsers {
    private static TelegramUsers instance = null;

    private TelegramUsers() {
    }

    public synchronized static TelegramUsers getInstance() {
        if (instance == null) {
            instance = new TelegramUsers();
        }

        return instance;
    }

    private Map<Long, TelegramUser> users = new ConcurrentHashMap<>();

    public TelegramUser getUser(long id) {
        return this.users.get(id);
    }

    public TelegramUser addUser(long id) {
        TelegramUser user = new TelegramUser(id);
        this.users.put(id, user);
        return user;
    }

    public void removeUser(long id) {
        this.users.remove(id);
    }
}
