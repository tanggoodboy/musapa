package wns.musapa.task;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import wns.musapa.Constant;
import wns.musapa.fetcher.CoinTickFetcher;
import wns.musapa.model.CoinTick;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class KimpTask implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(KimpTask.class);

    private Queue<CoinTick> tickQueue = new LinkedBlockingQueue<>();

    private ExecutorService fetchers = Executors.newCachedThreadPool();

    private List<CoinTickFetcher> coinTickFetchers = new ArrayList<>();

    private Map<String, CoinTick> latestCoinTicks = new HashMap<>();

    private String coinCodeForBitstamp = "btcusd";
    private String coinCodeForBithumb = "";

    private Object exchangeLock = new Object();

    private double exchangeRate = 1080; // 1 USD to KRW

    private Thread exchangeThread = null;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmSS");

    private KimpTelegramBot kimpTelegramBot = new KimpTelegramBot();

    static {
        ApiContextInitializer.init();
    }

    public KimpTask(String coinCodeForBithumb, String coinCodeForBitstamp) {
        this.coinCodeForBithumb = coinCodeForBithumb;
        this.coinCodeForBitstamp = coinCodeForBitstamp;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Starting Telegram bot...");
            TelegramBotsApi botsApi = new TelegramBotsApi();
            botsApi.registerBot(this.kimpTelegramBot);

            LOGGER.info("Starting exchange rate thread...");
            this.exchangeThread = new Thread(new ExchangeRateThread());
            this.exchangeThread.start();
            for (CoinTickFetcher coinTickFetcher : coinTickFetchers) {
                LOGGER.info("Starting {} thread...", coinTickFetcher.getClass().getSimpleName());
                fetchers.execute(new CoinFetcherThread(coinTickFetcher, 1000L));
            }

            while (!Thread.currentThread().isInterrupted()) {
                while (!this.tickQueue.isEmpty()) {
                    CoinTick coinTick = this.tickQueue.poll();
                    this.latestCoinTicks.put(coinTick.getCode(), coinTick);

                    if (isReadyToCalculate()) {
                        updateKimp();
                    }
                }

                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (fetchers != null) {
                fetchers.shutdownNow();
            }
            if (exchangeThread != null) {
                exchangeThread.interrupt();
            }
        }
    }

    private void updateKimp() {
        CoinTick bitstamp = latestCoinTicks.get(coinCodeForBitstamp);
        CoinTick bithumb = latestCoinTicks.get(coinCodeForBithumb);

        double rate = getExchangeRate();
        double bitstampPrice = rate * bitstamp.getTradePrice();
        double kimpRate = calculateKimchi(bithumb.getTradePrice(), bitstampPrice);

        this.kimpTelegramBot.broadcastRate(printKimchi(), kimpRate);
    }

    private double calculateKimchi(double bithumb, double bitstampPrice) {
        return (bithumb - bitstampPrice) / bitstampPrice;
    }

    private String printKimchi() {
        CoinTick bitstamp = latestCoinTicks.get(coinCodeForBitstamp);
        CoinTick bithumb = latestCoinTicks.get(coinCodeForBithumb);

        double rate = getExchangeRate();

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("KimpRate: %.5f\n", calculateKimchi(bithumb.getTradePrice(), rate * bitstamp.getTradePrice())));
        sb.append(String.format("Bithumb: KRW %f / %s\n", bithumb.getTradePrice(),
                this.simpleDateFormat.format(new Date(bithumb.getTimestamp()))));
        sb.append(String.format("Bitstamp: KRW %f / USD %f / Rate %f / %s\n",
                rate * bitstamp.getTradePrice(),
                bitstamp.getTradePrice(),
                rate,
                this.simpleDateFormat.format(new Date(bitstamp.getTimestamp()))));
        return sb.toString();
    }

    public boolean isReadyToCalculate() {
        return this.latestCoinTicks.containsKey(coinCodeForBitstamp) // Bitstamp
                && this.latestCoinTicks.containsKey(coinCodeForBithumb); // Bithumb
    }

    public double getExchangeRate() {
        synchronized (exchangeLock) {
            return this.exchangeRate;
        }
    }

    public void setExchangeRate(double exchangeRate) {
        synchronized (exchangeLock) {
            this.exchangeRate = exchangeRate;
        }
    }

    public void addCoinTickFetcher(CoinTickFetcher coinTickFetcher) {
        this.coinTickFetchers.add(coinTickFetcher);
    }

    class KimpUser {
        long userId;
        long lastMessageAt = 0;
        double alertRate = 0;

        KimpUser() {
        }

        @Override
        public String toString() {
            return "KimpUser{" +
                    "userId=" + userId +
                    ", lastMessageAt=" + lastMessageAt +
                    ", alertRate=" + alertRate +
                    '}';
        }
    }

    class KimpTelegramBot extends TelegramLongPollingBot {
        static final int MESSAGE_INTERVAL = 3 * 60 * 1000;
        Map<Long, KimpUser> users = new HashMap<>();

        public void broadcastRate(String message, double rate) {
            for (KimpUser user : users.values()) {
                if (user.alertRate > 0
                        && Math.abs(rate) >= user.alertRate
                        && user.lastMessageAt + MESSAGE_INTERVAL <= System.currentTimeMillis()) { // send only every 5 min
                    send(user.userId, message);
                    user.lastMessageAt = System.currentTimeMillis();
                    LOGGER.info("Message sent to {} since user.alertRate={} and rate={}",
                            user.userId, user.alertRate, rate);
                }
            }
        }

        private void send(long user, String message) {
            SendMessage msg = new SendMessage();
            msg.setChatId(user);
            msg.setText(message);
            try {
                execute(msg);
            } catch (TelegramApiException e) {
            }
        }

        @Override
        public void onUpdateReceived(Update update) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String[] tokens = update.getMessage().getText().split(" ");
                String command = tokens[0].trim();
                if (command.equals("/hello")) {
                    send(update.getMessage().getChatId(), "Hello! You can send me /kimchi ");
                    send(update.getMessage().getChatId(), "Type /bye if you want to unsubscribe.");

                    KimpUser kimpUser = null;
                    if ((kimpUser = users.get(update.getMessage().getChatId())) == null) {
                        kimpUser = new KimpUser();
                        kimpUser.userId = update.getMessage().getChatId();
                        users.put(kimpUser.userId, kimpUser);
                    }

                    if (tokens.length >= 2) {
                        try {
                            double alertRate = Double.parseDouble(tokens[1].trim());
                            kimpUser.alertRate = alertRate;
                            send(update.getMessage().getChatId(), "Great. I will send you an alert when rate exceeds/drops " + alertRate + "\nTo avoid alert flooding, alerts will be sent with 3 minutes gap.");
                            LOGGER.info("User added/updated: {} / total: {} users", kimpUser.toString(), users.size());
                        } catch (Exception e) {
                            send(update.getMessage().getChatId(), "I don't understand your alert rate.");
                        }
                    }
                } else if (command.equals("/bye")) {
                    send(update.getMessage().getChatId(), "Bye.");
                    users.remove(update.getMessage().getChatId());
                    LOGGER.info("User left. total: {} users", users.size());
                } else if (command.equals("/kimchi")) {
                    send(update.getMessage().getChatId(), printKimchi());
                } else if (command.equals("/me")){
                    KimpUser user = users.get(update.getMessage().getChatId());
                    if(user == null){
                        send(update.getMessage().getChatId(), "I don't know you. Say /hello");
                    } else {
                        send(update.getMessage().getChatId(), "Gotcha. Your alert rate is " + user.alertRate +"\nIf you want to change it, just /hello (rate) me again.");
                    }
                }
            }
        }

        @Override
        public String getBotUsername() {
            return "kimchi_prem_bot";
        }

        @Override
        public String getBotToken() {
            return Constant.KIMP_TELEGRAM_BOT_TOKEN;
        }
    }

    class ExchangeRateThread implements Runnable {
        private long interval = 60000L;
        private HttpClient client = null;
        private HttpGet request = null;
        private JsonParser jsonParser = null;

        public ExchangeRateThread() {
            this(60000L);
        }

        public ExchangeRateThread(long interval) {
            this.interval = interval;
            this.jsonParser = new JsonParser();
        }

        public CloseableHttpClient getHttpClient() throws Exception {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

            }}, new java.security.SecureRandom());
            return HttpClientBuilder.create().setSSLContext(sslcontext).setSSLHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            }).build();
        }

        @Override
        public void run() {
            try {

                this.client = getHttpClient();
                URI uri = new URIBuilder("https://api.fixer.io/latest?base=USD").build();
                this.request = new HttpGet(uri);
                HttpResponse response = null;
                while (!Thread.currentThread().isInterrupted()) {
                    response = this.client.execute(request);

                    try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                        StringBuffer sb = new StringBuffer();
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }

                        JsonObject exchange = this.jsonParser.parse(sb.toString()).getAsJsonObject();
                        setExchangeRate(exchange.get("rates").getAsJsonObject().get("KRW").getAsDouble());
                    }

                    Thread.sleep(this.interval);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

    }

    class CoinFetcherThread implements Runnable {
        private long interval = 1000L;
        private CoinTickFetcher coinTickFetcher = null;

        public CoinFetcherThread(CoinTickFetcher coinTickFetcher, long interval) {
            this.coinTickFetcher = coinTickFetcher;
            this.interval = interval;
        }

        @Override
        public void run() {
            try {
                this.coinTickFetcher.onStart();

                while (!Thread.currentThread().isInterrupted()) {
                    CoinTick tick = this.coinTickFetcher.fetchTick();
                    if (tick != null) {
                        tickQueue.add(tick);
                    }

                    Thread.sleep(this.interval);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
