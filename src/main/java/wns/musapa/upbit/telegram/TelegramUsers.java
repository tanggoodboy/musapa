package wns.musapa.upbit.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class TelegramUsers extends ConcurrentHashMap<Long, TelegramUser> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramUsers.class);
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
        synchronized (this) {
            this.put(id, user);
            LOGGER.info("New user added. Total users: {}", this.size());
        }
        return user;
    }

    public void removeUser(long id) {
        synchronized (this) {
            this.remove(id);
            LOGGER.info("User removed Total users: {}", this.size());
        }
    }
}
