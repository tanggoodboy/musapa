package wns.musapa.flink;

import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import wns.musapa.flink.bot.TelegramBot;
import wns.musapa.flink.command.impl.*;
import wns.musapa.flink.model.CoinCandle;
import wns.musapa.flink.model.CoinCode;
import wns.musapa.flink.model.CoinMACD;
import wns.musapa.flink.model.CoinTick;
import wns.musapa.flink.processor.CandleGenerator;
import wns.musapa.flink.processor.MACDCalculator;
import wns.musapa.flink.processor.MACDProcessor;
import wns.musapa.flink.sink.ConsoleSink;
import wns.musapa.flink.sink.NowDashboardSink;
import wns.musapa.flink.source.UpbitWebsocketSource;

public class FlinkMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlinkMain.class);

    public static void main(String[] args) throws Exception {
        final long coinTickWindowSize = 20 * 1000L;

        LocalStreamEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();

        // initialize bot
        TelegramBot bot = initializeBot();

        // CoinTick stream
        DataStreamSource<CoinTick> coinTicks = env.addSource(new UpbitWebsocketSource());

        // -----
        // Make Rate of change stream
        NowDashboardSink nowDashboardSink = new NowDashboardSink(coinTickWindowSize);
        coinTicks.keyBy((KeySelector<CoinTick, CoinCode>) coinTick -> coinTick.getCode())
                .timeWindow(Time.milliseconds(coinTickWindowSize))
                .apply(new CandleGenerator())
                .addSink(nowDashboardSink);

        // -----
        //  Make MACD stream
        coinTicks.keyBy((KeySelector<CoinTick, CoinCode>) coinTick -> coinTick.getCode())
                .timeWindow(Time.minutes(30))
                .apply(new CandleGenerator())
                .keyBy((KeySelector<CoinCandle, CoinCode>) coinCandle -> coinCandle.getCode())
                .countWindow(26, 1)
                .apply(new MACDProcessor())
                .keyBy((KeySelector<CoinMACD, CoinCode>) coinMACD -> coinMACD.getCode())
                .countWindow(2, 1)
                .apply(new MACDCalculator())
                .addSink(new ConsoleSink<>());

        // ---------- add commands ----------
        bot.addCommand(new PingCommand());
        bot.addCommand(new ByeCommand());
        bot.addCommand(new HelpCommand(bot.getCommands()));
        bot.addCommand(new CodeCommand());
        bot.addCommand(new UserCommand());
        bot.addCommand(new NowCommand(nowDashboardSink));
        bot.addCommand(new RuleAddCommand());
        bot.addCommand(new RuleDelCommand());

        JobExecutionResult result = env.execute();
        LOGGER.info(result.toString());
    }

    private static TelegramBot initializeBot() throws TelegramApiRequestException {
        ApiContextInitializer.init();
        TelegramBot bot = new TelegramBot();
        TelegramBotsApi api = new TelegramBotsApi();
        api.registerBot(bot);
        return bot;
    }
}
