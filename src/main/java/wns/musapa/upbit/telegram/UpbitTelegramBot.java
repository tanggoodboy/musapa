package wns.musapa.upbit.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import wns.musapa.Constant;
import wns.musapa.upbit.command.TelegramCommand;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UpbitTelegramBot extends TelegramLongPollingBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitTelegramReporter.class);
    private Map<String, TelegramCommand> commands = new HashMap<>();

    public void addCommand(TelegramCommand command) {
        commands.put(command.getCommand().toLowerCase(), command);
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                long chatId = update.getMessage().getChatId();
                TelegramUsers users = TelegramUsers.getInstance();
                TelegramUser user = users.getUser(chatId);
                if (user == null) {
                    user = users.addUser(chatId);
                    send(user.getId(), "Welcome! Checkout /help \nSay /bye to unsubscribe.");
                }

                String msg = update.getMessage().getText().trim();
                String[] tokens = msg.split(" ");

                LOGGER.info("[RECV] {} / {}", user.getId(), msg);

                TelegramCommand command = commands.get(tokens[0].toLowerCase());
                if (command == null) { // error if command not found
                    send(user.getId(), "Huh? Try /help");
                } else {
                    try {
                        send(user.getId(), command.process(user, tokens));
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                        send(user.getId(), "Something is not right. Checkout /help");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void send(long chatId, String message) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(message);
        try {
            execute(msg);
            LOGGER.info("[SEND] {} / {}", chatId, message);
        } catch (TelegramApiException e) {
        }
    }

    @Override
    public String getBotUsername() {
        return Constant.MUSAPA_TELEGRAM_BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return Constant.MUSAPA_TELEGRAM_BOT_TOKEN;
    }

    public Collection<TelegramCommand> getCommands() {
        return commands.values();
    }
}
