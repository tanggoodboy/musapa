package wns.musapa.flink.source;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wns.musapa.flink.model.CoinTick;
import wns.musapa.flink.model.code.UpbitCoinCode;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.UUID;

public class UpbitWebsocketClient extends WebSocketClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitWebsocketClient.class);
    private static final String UPBIT_WEBSOCKET = "wss://crix-websocket.upbit.com/sockjs/%d/%s/websocket";

    private final String handshakeMessage = "[\"[{\\\"ticket\\\":\\\"ram macbook\\\"},{\\\"type\\\":\\\"recentCrix\\\",\\\"codes\\\":[\\\"CRIX.BITFINEX.USD-BTC\\\",\\\"CRIX.BITFLYER.JPY-BTC\\\",\\\"CRIX.OKCOIN.CNY-BTC\\\",\\\"CRIX.KRAKEN.EUR-BTC\\\",\\\"CRIX.UPBIT.KRW-BTC\\\",\\\"CRIX.UPBIT.KRW-DASH\\\",\\\"CRIX.UPBIT.KRW-ETH\\\",\\\"CRIX.UPBIT.KRW-NEO\\\",\\\"CRIX.UPBIT.KRW-BCC\\\",\\\"CRIX.UPBIT.KRW-MTL\\\",\\\"CRIX.UPBIT.KRW-LTC\\\",\\\"CRIX.UPBIT.KRW-STRAT\\\",\\\"CRIX.UPBIT.KRW-XRP\\\",\\\"CRIX.UPBIT.KRW-ETC\\\",\\\"CRIX.UPBIT.KRW-OMG\\\",\\\"CRIX.UPBIT.KRW-SNT\\\",\\\"CRIX.UPBIT.KRW-WAVES\\\",\\\"CRIX.UPBIT.KRW-PIVX\\\",\\\"CRIX.UPBIT.KRW-XEM\\\",\\\"CRIX.UPBIT.KRW-ZEC\\\",\\\"CRIX.UPBIT.KRW-XMR\\\",\\\"CRIX.UPBIT.KRW-QTUM\\\",\\\"CRIX.UPBIT.KRW-LSK\\\",\\\"CRIX.UPBIT.KRW-STEEM\\\",\\\"CRIX.UPBIT.KRW-XLM\\\",\\\"CRIX.UPBIT.KRW-ARDR\\\",\\\"CRIX.UPBIT.KRW-KMD\\\",\\\"CRIX.UPBIT.KRW-ARK\\\",\\\"CRIX.UPBIT.KRW-STORJ\\\",\\\"CRIX.UPBIT.KRW-GRS\\\",\\\"CRIX.UPBIT.KRW-VTC\\\",\\\"CRIX.UPBIT.KRW-REP\\\",\\\"CRIX.UPBIT.KRW-EMC2\\\",\\\"CRIX.UPBIT.KRW-ADA\\\",\\\"CRIX.UPBIT.KRW-SBD\\\",\\\"CRIX.UPBIT.KRW-TIX\\\",\\\"CRIX.UPBIT.KRW-POWR\\\",\\\"CRIX.UPBIT.KRW-MER\\\",\\\"CRIX.UPBIT.KRW-BTG\\\",\\\"CRIX.COINMARKETCAP.KRW-USDT\\\"]},{\\\"type\\\":\\\"crixTrade\\\",\\\"codes\\\":[\\\"CRIX.UPBIT.KRW-BTC\\\"]},{\\\"type\\\":\\\"crixOrderbook\\\",\\\"codes\\\":[\\\"CRIX.UPBIT.KRW-BTC\\\"]}]\"]";

    private JsonParser jsonParser = new JsonParser();
    private boolean isFirstMessage = true;

    private long lastMessageReceivedAt = System.currentTimeMillis();
    private SourceFunction.SourceContext<CoinTick> sourceContext = null;

    public UpbitWebsocketClient() throws URISyntaxException {
        super(new URI(String.format(UPBIT_WEBSOCKET,
                (new Random()).nextInt(999 - 100 + 1) + 100,
                UUID.randomUUID().toString().substring(0, 8))));
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        this.isFirstMessage = true;
    }

    @Override
    public void onMessage(String s) {
        lastMessageReceivedAt = System.currentTimeMillis();

        if (this.isFirstMessage) {
            LOGGER.info("Received first message from upbit: " + s);
            send(handshakeMessage);
            this.isFirstMessage = false;
            return;
        }

        if (s.startsWith("a")) {
            String msg = s.substring(1);
            msg = msg.replaceAll("\\/", "");
            String raw = jsonParser.parse(msg).getAsJsonArray().get(0).getAsString();
            JsonObject coin = jsonParser.parse(raw).getAsJsonObject();
            if (sourceContext != null && isOfInterest(coin)) {
                UpbitCoinCode upbitCoinCode = UpbitCoinCode.parseByCode(coin.get("code").getAsString());
                if(upbitCoinCode == null){
                    return;
                }
                CoinTick coinTick = new CoinTick(upbitCoinCode,
                        coin.get("timestamp").getAsLong(),
                        coin.get("tradePrice").getAsDouble());
                sourceContext.collect(coinTick);
            }
        }
    }

    private boolean isOfInterest(JsonObject coin) {
        return coin.get("type").getAsString().equals("recentCrix")
                && coin.get("code").getAsString().contains("KRW");
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        LOGGER.info("Closed.");
    }

    @Override
    public void onError(Exception e) {
        LOGGER.error(e.getMessage(), e);
    }

    public synchronized long getLastMessageAt() {
        return lastMessageReceivedAt;
    }

    public void setSourceContext(SourceFunction.SourceContext<CoinTick> sourceContext) {
        this.sourceContext = sourceContext;
    }
}
