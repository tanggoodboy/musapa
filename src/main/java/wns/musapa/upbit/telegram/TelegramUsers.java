package wns.musapa.upbit.telegram;

import java.util.concurrent.ConcurrentHashMap;

public class TelegramUsers extends ConcurrentHashMap<Long, TelegramUser> {
    private static TelegramUsers instance = null;

    private TelegramUsers() {
    }

    public synchronized static TelegramUsers getInstance() {
        if (instance == null) {
            instance = new TelegramUsers();
        }

        return instance;
    }

    public TelegramUser getUser(long id) {
        return this.get(id);
    }

    public TelegramUser addUser(long id) {
        TelegramUser user = new TelegramUser(id);
        this.put(id, user);
        return user;
    }

    public void removeUser(long id) {
        this.remove(id);
    }
}
