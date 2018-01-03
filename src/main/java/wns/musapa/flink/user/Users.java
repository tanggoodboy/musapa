package wns.musapa.flink.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class Users extends ConcurrentHashMap<Long, User> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Users.class);
    private static Users instance = null;

    private Users() {
    }

    public synchronized static Users getInstance() {
        if (instance == null) {
            instance = new Users();
        }

        return instance;
    }

    public User getUser(long id) {
        return this.get(id);
    }

    public User addUser(User user) {
        if (user == null) {
            return null;
        }

        synchronized (this) {
            this.put(user.getId(), user);
            LOGGER.info("New user {} added. Total users: {}", user.getId(), this.size());
        }
        return user;
    }

    public void removeUser(long id) {
        synchronized (this) {
            this.remove(id);
            LOGGER.info("User {} removed Total users: {}", id, this.size());
        }
    }
}
