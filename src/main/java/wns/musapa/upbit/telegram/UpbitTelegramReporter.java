package wns.musapa.upbit.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import wns.musapa.model.CoinAnalysis;
import wns.musapa.upbit.CoinAnalysisLog;
import wns.musapa.upbit.command.*;

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

        for (TelegramUser user : TelegramUsers.getInstance().values()) {
            user.fireRules(coinAnalysis, this.upbitTelegramBot);
        }
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Starting Telegram bot...");
            TelegramBotsApi botsApi = new TelegramBotsApi();
            this.upbitTelegramBot.addCommand(new PingCommand());
            this.upbitTelegramBot.addCommand(new MoreCommand());
            this.upbitTelegramBot.addCommand(new NowCommand(this.windowSize));
            this.upbitTelegramBot.addCommand(new AlertAddCommand());
            this.upbitTelegramBot.addCommand(new AlertDelCommand());
            this.upbitTelegramBot.addCommand(new ByeCommand());
            this.upbitTelegramBot.addCommand(new UserCommand());
            this.upbitTelegramBot.addCommand(new CodeCommand());
            this.upbitTelegramBot.addCommand(new HelpCommand(this.upbitTelegramBot.getCommands())); // Always last command
            botsApi.registerBot(this.upbitTelegramBot);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
