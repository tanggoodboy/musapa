package wns.musapa.fetcher;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import wns.musapa.model.CoinTick;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

public class BitstampFetcher implements CoinTickFetcher {
    private static final String URL = "https://www.bitstamp.net/api/v2/ticker/%s/";

    private HttpClient httpClient = null;

    private String coinCode = "btcusd";

    private JsonParser jsonParser = null;
    private HttpGet request = null;

    public BitstampFetcher(String coinCode) {
        this.coinCode = coinCode;
    }

    @Override
    public CoinTick fetchTick() throws Exception {
        HttpResponse response = this.httpClient.execute(request);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            StringBuffer sb = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            JsonObject tickJson = this.jsonParser.parse(sb.toString()).getAsJsonObject();
            long timestamp = Long.parseLong(tickJson.get("timestamp").getAsString()) * 1000;
            double price = Double.parseDouble(tickJson.get("last").getAsString());
            return new CoinTick(this.coinCode, timestamp, price);
        }
    }

    @Override
    public void onStart() throws URISyntaxException {
        this.jsonParser = new JsonParser();
        this.httpClient = HttpClientBuilder.create().build();
        URI uri = new URIBuilder(String.format(URL, coinCode)).build();
        this.request = new HttpGet(uri);
    }

    @Override
    public void onStop() {
    }
}
