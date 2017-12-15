package wns.musapa.upbit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import wns.musapa.Constant;
import wns.musapa.model.CoinAnalysis;
import wns.musapa.upbit.command.*;

import java.util.HashMap;
import java.util.Map;

public class UpbitTelegramReporter implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitTelegramReporter.class);
    private UpbitTelegramBot upbitTelegramBot = new UpbitTelegramBot();
    private long windowSize;

    static {
        ApiContextInitializer.init();
    }

    public UpbitTelegramReporter(long windowSize) {
        this.windowSize = windowSize;
    }


    public void report(CoinAnalysis coinAnalysis) {
        CoinAnalysisLog.getInstance().put(coinAnalysis.getCode(), coinAnalysis);


    }

    @Override
    public void run() {
        try {
            LOGGER.info("Starting Telegram bot...");
            TelegramBotsApi botsApi = new TelegramBotsApi();
            this.upbitTelegramBot.addCommand(new PingCommand());
            this.upbitTelegramBot.addCommand(new MoreCommand());
            this.upbitTelegramBot.addCommand(new NowCommand(this.windowSize));
            this.upbitTelegramBot.addCommand(new AlertCommand());
            this.upbitTelegramBot.addCommand(new ByeCommand());
            botsApi.registerBot(this.upbitTelegramBot);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    class UpbitTelegramBot extends TelegramLongPollingBot {
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
                    String[] tokens = msg.split("_");

                    LOGGER.info("[RECV] {} / {}", user.getId(), msg);

                    if (tokens[0].toLowerCase().equalsIgnoreCase("/help")) {
                        // print help
                        StringBuilder sb = new StringBuilder();
                        for (TelegramCommand command : commands.values()) {
                            sb.append(command.getCommand() + "\t" + command.getHelp() + "\n");
                        }
                        send(user.getId(), sb.toString());
                    } else {
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
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        private void send(long chatId, String message) {
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
            return "musapa_bot";
        }

        @Override
        public String getBotToken() {
            return Constant.MUSAPA_TELEGRAM_BOT_TOKEN;
        }
    }

}
