package wns.musapa.flink;

import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import wns.musapa.flink.bot.TelegramBot;
import wns.musapa.flink.command.impl.*;
import wns.musapa.flink.model.CoinCandle;
import wns.musapa.flink.model.CoinCode;
import wns.musapa.flink.model.CoinTick;
import wns.musapa.flink.processor.CandleGenerator;
import wns.musapa.flink.sink.ConsoleSink;
import wns.musapa.flink.source.UpbitWebsocketSource;

public class FlinkMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlinkMain.class);

    public static void main(String[] args) throws Exception {
        LocalStreamEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();

        // initialize bot
        initializeBot();

        // CoinTick stream
        DataStreamSource<CoinTick> coinTicks = env.addSource(new UpbitWebsocketSource());

        // Window coin ticks by time
        WindowedStream<CoinTick, CoinCode, TimeWindow> windowedStream =
                coinTicks.keyBy((KeySelector<CoinTick, CoinCode>) coinTick -> coinTick.getCode())
                        //.timeWindow(Time.minutes(2));
                        .timeWindow(Time.seconds(10));

        // Make coin candle from coin tick window
        SingleOutputStreamOperator<CoinCandle> candles = windowedStream.apply(new CandleGenerator());

        candles.addSink(new ConsoleSink());

        JobExecutionResult result = env.execute();
        LOGGER.info(result.toString());
    }

    private static TelegramBot initializeBot() throws TelegramApiRequestException {
        ApiContextInitializer.init();

        TelegramBot bot = new TelegramBot();
        TelegramBotsApi api = new TelegramBotsApi();

        // ---------- add commands ----------
        // ping
        bot.addCommand(new PingCommand());

        // bye
        bot.addCommand(new ByeCommand());

        // help
        bot.addCommand(new HelpCommand(bot.getCommands()));

        // code
        bot.addCommand(new CodeCommand());

        // user
        bot.addCommand(new UserCommand());

        // add rule
        bot.addCommand(new RuleAddCommand());

        // del rule
        bot.addCommand(new RuleDelCommand());

        api.registerBot(bot);
        return bot;
    }
}
