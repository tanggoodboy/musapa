package wns.musapa.flink.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import wns.musapa.flink.Constant;
import wns.musapa.flink.command.Command;
import wns.musapa.flink.user.TelegramUser;
import wns.musapa.flink.user.User;
import wns.musapa.flink.user.Users;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TelegramBot extends TelegramLongPollingBot implements Reporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBot.class);

    private Map<String, Command> commands = new ConcurrentHashMap<>();

    public TelegramBot() {
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                long chatId = update.getMessage().getChatId();
                User user = Users.getInstance().getUser(chatId);
                if (user == null) {
                    user = Users.getInstance().addUser(new TelegramUser(chatId));
                    send(user, "Welcome! Checkout /help \nSay /bye to unsubscribe.");
                }

                String msg = update.getMessage().getText().trim();
                String[] tokens = msg.split(" ");

                LOGGER.info("[RECV] {} / {}", user.getId(), msg);

                Command command = commands.get(tokens[0].toLowerCase());
                if (command == null) { // error if command not found
                    send(user, "Huh? Try /help");
                } else {
                    Command.Handler handler = command.getHandler();
                    if (handler == null) {
                        send(user, "No handler for " + command.getCommand());
                    } else {
                        handler.handle(command, tokens, user, this);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public String getBotUsername() {
        return Constant.MUSAPA_DEV_TELEGRAM_BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return Constant.MUSAPA_DEV_TELEGRAM_BOT_TOKEN;
    }

    @Override
    public void broadcast(String message) {
        Collection<User> users = Users.getInstance().values();
        for (User user : users) {
            send(user, message);
        }
    }

    @Override
    public void send(User user, String message) {
        SendMessage msg = new SendMessage();
        msg.setChatId(user.getId());
        msg.setText(message);
        try {
            execute(msg);
            LOGGER.info("[SEND] {} / {}", user.getId(), message);
        } catch (TelegramApiException e) {
        }
    }

    public void addCommand(Command command) {
        this.commands.put(command.getCommand(), command);
    }

    public Collection<Command> getCommands() {
        return this.commands.values();
    }
}
