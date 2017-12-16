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
import wns.musapa.UpbitMain;
import wns.musapa.model.CoinAnalysis;
import wns.musapa.model.CoinCode;
import wns.musapa.model.code.UpbitCoinCode;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UpbitTelegramReporter implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitTelegramReporter.class);
    private Map<Long, UserInfo> users = new ConcurrentHashMap<>();
    private UpbitTelegramBot upbitTelegramBot = new UpbitTelegramBot();
    private Map<CoinCode, CoinAnalysis> coinAnalysisMap = new ConcurrentHashMap<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd'T'HHmmss");

    private final long windowSize;

    static {
        ApiContextInitializer.init();
    }

    public UpbitTelegramReporter() {
        this(UpbitMain.DEFAULT_WINDOW_SIZE);
    }

    public UpbitTelegramReporter(long windowSize) {
        this.windowSize = windowSize;

        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
    }

    public void report(CoinAnalysis coinAnalysis) {
        this.coinAnalysisMap.put(coinAnalysis.getCode(), coinAnalysis);
        fireRules(coinAnalysis);
    }

    private void fireRules(CoinAnalysis coinAnalysis) {
        for (UserInfo user : users.values()) {
            // Check inc rate
            Double incRate = user.incRateThreshold.get(coinAnalysis.getCode());
            if (incRate != null && incRate <= coinAnalysis.getRateOfChange() && user.isReadyForIncAlert(coinAnalysis.getCode())) {
                this.upbitTelegramBot.send(user.id, print(coinAnalysis));
                user.updateLastIncAlertAt(coinAnalysis.getCode());
            }

            // Check dec rate
            Double decRate = user.decRateThreshold.get(coinAnalysis.getCode());
            if (decRate != null && decRate <= coinAnalysis.getRateOfChange() && user.isReadyForDecAlert(coinAnalysis.getCode())) {
                this.upbitTelegramBot.send(user.id, print(coinAnalysis));
                user.updateLastDecAlertAt(coinAnalysis.getCode());
            }

            // Check alert rate for all coins
            Double alertRate = user.alertRate;
            if (alertRate != null && user.alertRate <= coinAnalysis.getRateOfChange() && user.isReadyForAlertAlert(coinAnalysis.getCode())) {
                this.upbitTelegramBot.send(user.id, print(coinAnalysis));
                user.updateLastAlertAlertAt(coinAnalysis.getCode());
            }
        }
    }

    private String print(CoinAnalysis coinAnalysis) {
        StringBuilder sb = new StringBuilder();
        UpbitCoinCode upbitCoinCode = (UpbitCoinCode) coinAnalysis.getCode();
        sb.append(upbitCoinCode.getKorean() + " (" + upbitCoinCode.name() + ")\n");
        sb.append(String.format("Rate: %f\n", coinAnalysis.getRateOfChange()));
        sb.append(String.format("Close: %f (%s)\n", coinAnalysis.getClose().getPrice(), sdf.format(new Date(coinAnalysis.getClose().getTimestamp()))));
        sb.append(String.format("Open: %f (%s)\n", coinAnalysis.getOpen().getPrice(), sdf.format(new Date(coinAnalysis.getOpen().getTimestamp()))));
        sb.append(String.format("Low: %f (%s)\n", coinAnalysis.getLow().getPrice(), sdf.format(new Date(coinAnalysis.getLow().getTimestamp()))));
        sb.append(String.format("High: %f (%s)\n", coinAnalysis.getHigh().getPrice(), sdf.format(new Date(coinAnalysis.getHigh().getTimestamp()))));
        sb.append(String.format("Count: %d", coinAnalysis.getCount()));
        return sb.toString();
    }

    private String printPrices() {
        StringBuilder sb = new StringBuilder();
        sb.append("Last " + this.windowSize / 60000 + " minutes...\n");
        List<CoinAnalysis> analysisList = new ArrayList<>(coinAnalysisMap.values());
        analysisList.sort(new Comparator<CoinAnalysis>() {
            @Override
            public int compare(CoinAnalysis o1, CoinAnalysis o2) {
                if (o1.getRateOfChange() > o2.getRateOfChange()) {
                    return -1;
                } else if (o1.getRateOfChange() < o2.getRateOfChange()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        // Print top 5
        for (int i = 0; i < 5 && i < analysisList.size(); i++) {
            CoinAnalysis analysis = analysisList.get(i);
            UpbitCoinCode upbitCoinCode = (UpbitCoinCode) analysis.getCode();
            sb.append(String.format("%s(%s): %.0f (%.4f%%) %s\n",
                    upbitCoinCode.getKorean(), upbitCoinCode.name(), analysis.getClose().getPrice(),
                    analysis.getRateOfChange() * 100, sdf.format(new Date(analysis.getClose().getTimestamp()))));
        }
        sb.append("...\n");
        // Print bottom 5
        for (int i = analysisList.size() - Math.min(4, analysisList.size() - 1); i < analysisList.size(); i++) {
            CoinAnalysis analysis = analysisList.get(i);
            UpbitCoinCode upbitCoinCode = (UpbitCoinCode) analysis.getCode();
            sb.append(String.format("%s(%s): %.0f (%.4f%%) %s\n",
                    upbitCoinCode.getKorean(), upbitCoinCode.name(), analysis.getClose().getPrice(),
                    analysis.getRateOfChange() * 100, sdf.format(new Date(analysis.getClose().getTimestamp()))));
        }
        return sb.toString();
    }

    private String printMore(UserInfo userInfo, String[] tokens) {
        try {
            UpbitCoinCode upbitCoinCode = UpbitCoinCode.parseByName(tokens[1].trim());
            CoinAnalysis analysis = coinAnalysisMap.get(upbitCoinCode);
            if (analysis == null) {
                return "Not enough data, just yet.";
            } else {
                return print(analysis);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return "Not sure I understand. Try /help";
        }
    }


    @Override
    public void run() {
        try {
            LOGGER.info("Starting Telegram bot...");
            TelegramBotsApi botsApi = new TelegramBotsApi();
            botsApi.registerBot(this.upbitTelegramBot);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    class UserInfo {
        long id;
        Map<CoinCode, Double> incRateThreshold;
        Map<CoinCode, Double> decRateThreshold;
        Map<CoinCode, Long> lastIncAlertAt;
        Map<CoinCode, Long> lastDecAlertAt;
        Map<CoinCode, Long> lastAlertAlertAt;
        long alertInterval = 3 * 60 * 1000L;
        public Double alertRate = null;

        UserInfo(long id) {
            this.id = id;
            this.incRateThreshold = new HashMap<>();
            this.decRateThreshold = new HashMap<>();
            this.lastIncAlertAt = new HashMap<>();
            this.lastDecAlertAt = new HashMap<>();
            this.lastAlertAlertAt = new HashMap<>();
        }

        public boolean isReadyForIncAlert(CoinCode code) {
            Long last = lastIncAlertAt.get(code);
            return last == null || last + alertInterval <= System.currentTimeMillis();
        }

        public void updateLastIncAlertAt(CoinCode code) {
            lastIncAlertAt.put(code, System.currentTimeMillis());
        }

        public boolean isReadyForDecAlert(CoinCode code) {
            Long last = lastDecAlertAt.get(code);
            return last == null || last + alertInterval <= System.currentTimeMillis();
        }

        public void updateLastDecAlertAt(CoinCode code) {
            lastDecAlertAt.put(code, System.currentTimeMillis());
        }

        public boolean isReadyForAlertAlert(CoinCode code) {
            Long last = lastAlertAlertAt.get(code);
            return last == null || last + alertInterval <= System.currentTimeMillis();
        }

        public void updateLastAlertAlertAt(CoinCode code) {
            lastAlertAlertAt.put(code, System.currentTimeMillis());
        }

        public void reset() {
            incRateThreshold.clear();
            decRateThreshold.clear();
            lastIncAlertAt.clear();
            lastDecAlertAt.clear();
            lastAlertAlertAt.clear();
            alertRate = null;
        }
    }

    class UpbitTelegramBot extends TelegramLongPollingBot {

        @Override
        public void onUpdateReceived(Update update) {
            try {
                if (update.hasMessage() && update.getMessage().hasText()) {
                    long chatId = update.getMessage().getChatId();
                    UserInfo userInfo = users.get(chatId);

                    String msg = update.getMessage().getText().trim();
                    String[] tokens = msg.split(" ");
                    String command = tokens[0].trim();
                    switch (command.toLowerCase()) {
                        case "/hello":
                            if (userInfo == null) {
                                users.put(chatId, new UserInfo(chatId));
                                LOGGER.info("New user added. Total: " + users.size());
                            }
                            send(chatId, "Welcome! Type /help for help.");
                            break;
                        case "/bye":
                            users.remove(chatId);
                            LOGGER.info("User left. Total: " + users.size());
                            send(chatId, "Bye.");
                            break;
                        case "/code":
                            send(chatId, UpbitCoinCode.print());
                            break;
                        case "/help":
                            send(chatId, printHelp());
                            break;
                        case "/ruleadd":
                            if (userInfo == null) {
                                send(chatId, "Say /hello first!");
                            } else {
                                send(chatId, addRule(userInfo, tokens));
                            }
                            break;
                        case "/ruledel":
                            if (userInfo == null) {
                                send(chatId, "Huh? Who are you?");
                            } else {
                                send(chatId, deleteRule(userInfo, tokens));
                            }
                            break;
                        case "/rules":
                            if (userInfo == null) {
                                send(chatId, "Huh? Who are you?");
                            } else {
                                send(chatId, printRules(userInfo));
                            }
                            break;
                        case "/alert":
                            if (userInfo == null) {
                                send(chatId, "Huh? Who are you?");
                            } else {
                                send(chatId, alertRule(userInfo, tokens));
                            }
                            break;
                        case "/reset":
                            if (userInfo == null) {
                                send(chatId, "Huh? Who are you?");
                            } else {
                                send(chatId, reset(userInfo));
                            }
                            break;
                        case "/more":
                            send(chatId, printMore(userInfo, tokens));
                            break;
                        case "/now":
                            send(chatId, printPrices());
                            break;
                        default:
                            send(chatId, "No idea what you are talking about.");
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        private String alertRule(UserInfo userInfo, String[] tokens) {
            try {
                double rate = Double.parseDouble(tokens[1].trim());
                userInfo.alertRate = rate;
                return "Got it. Alert rate at " + rate + " increase.";
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return "Not sure I understand. Try /help";
            }
        }

        private String reset(UserInfo userInfo) {
            userInfo.reset();
            return "Alright.";
        }

        private String printRules(UserInfo userInfo) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<CoinCode, Double> entry : userInfo.incRateThreshold.entrySet()) {
                UpbitCoinCode upbitCoinCode = (UpbitCoinCode) entry.getKey();
                sb.append(String.format("%s(%s): INC %f\n", upbitCoinCode.getKorean(), upbitCoinCode.getCode(), entry.getValue()));
            }
            for (Map.Entry<CoinCode, Double> entry : userInfo.decRateThreshold.entrySet()) {
                UpbitCoinCode upbitCoinCode = (UpbitCoinCode) entry.getKey();
                sb.append(String.format("%s(%s): DEC %f\n", upbitCoinCode.getKorean(), upbitCoinCode.getCode(), entry.getValue()));
            }
            if (userInfo.alertRate != null) {
                sb.append(String.format("Alert at " + userInfo.alertRate));
            }
            return sb.toString();
        }

        private String deleteRule(UserInfo userInfo, String[] tokens) {
            try {
                String coinCode = tokens[1].trim();
                UpbitCoinCode upbitCoinCode = UpbitCoinCode.parseByName(coinCode);
                if (upbitCoinCode == null) {
                    return "Invalid coin code. Try /code";
                }

                String condition = tokens[2].trim();
                if (condition.equalsIgnoreCase("inc")) {
                    userInfo.incRateThreshold.remove(upbitCoinCode.getCode());
                    return String.format("%s %s deleted.", coinCode, condition);
                } else if (condition.equalsIgnoreCase("dec")) {
                    userInfo.decRateThreshold.remove(upbitCoinCode.getCode());
                    return String.format("%s %s deleted.", coinCode, condition);
                } else {
                    throw new Exception("Invalid condition.");
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return "Not sure I understand. Try /help";
            }
        }

        private String addRule(UserInfo userInfo, String[] tokens) {
            try {
                String coinCode = tokens[1].trim();
                UpbitCoinCode upbitCoinCode = UpbitCoinCode.parseByName(coinCode);
                if (upbitCoinCode == null) {
                    return "Invalid coin code. Try /code";
                }

                String condition = tokens[2].trim();
                Double rate = Double.parseDouble(tokens[3].trim());
                if (condition.equalsIgnoreCase("inc")) {
                    userInfo.incRateThreshold.put(upbitCoinCode, rate);
                    return String.format("%s %s %f added.", coinCode, condition, rate);
                } else if (condition.equalsIgnoreCase("dec")) {
                    userInfo.decRateThreshold.put(upbitCoinCode, rate);
                    return String.format("%s %s %f added.", coinCode, condition, rate);
                } else {
                    throw new Exception("Invalid condition.");
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return "Not sure I understand. Try /help";
            }
        }

        private String printHelp() {
            StringBuilder sb = new StringBuilder();
            sb.append("/code").append("\t").append("get available codes.").append("\n");
            sb.append("/reset").append("\t").append("reset account info.").append("\n");
            sb.append("/ruleadd").append("\t").append("[inc/dec] [rate]").append("\n");
            sb.append("/ruledel").append("\t").append("[inc/dec]").append("\n");
            sb.append("/now").append("\t").append("just try it\n");
            sb.append("/more").append("\t").append("[coinCode]").append("\n");
            sb.append("/alert").append("\t").append("[rate]");
            return sb.toString();
        }

        private void send(long chatId, String message) {
            SendMessage msg = new SendMessage();
            msg.setChatId(chatId);
            msg.setText(message);
            try {
                execute(msg);
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
