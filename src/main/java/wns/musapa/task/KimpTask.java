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
    private double kimpRate = 0;

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
            TelegramBotsApi botsApi = new TelegramBotsApi();
            botsApi.registerBot(this.kimpTelegramBot);

            this.exchangeThread = new Thread(new ExchangeRateThread());
            this.exchangeThread.start();
            for (CoinTickFetcher coinTickFetcher : coinTickFetchers) {
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
        this.kimpRate = calculateKimchi(bithumb.getTradePrice(), bitstampPrice);

        LOGGER.debug(printKimchi());

        if (Math.abs(this.kimpRate) > 0.02) {
            //TODO
        }
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

    class KimpTelegramBot extends TelegramLongPollingBot {
        Set<Long> users = new LinkedHashSet<>();

        public void broadcast(String message) {
            for (long user : users) {
                send(user, message);
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
                String command = update.getMessage().getText();
                if (command.equals("/hello")) {
                    send(update.getMessage().getChatId(), "Hello! You can send me /kimchi ");
                    users.add(update.getMessage().getChatId());
                } else if (command.equals("/bye")) {
                    send(update.getMessage().getChatId(), "Bye.");
                    users.remove(update.getMessage().getChatId());
                } else if (command.equals("/kimchi")) {
                    send(update.getMessage().getChatId(), printKimchi());
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
