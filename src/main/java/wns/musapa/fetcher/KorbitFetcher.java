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

public class KorbitFetcher implements CoinTickFetcher {
    private static final String URL = "https://api.korbit.co.kr/v1/ticker";

    private HttpClient httpClient = null;

    private String coinCode = "";

    private JsonParser jsonParser = null;

    public KorbitFetcher(String coinCode) {
        this.coinCode = coinCode;
    }

    @Override
    public CoinTick fetchTick() throws Exception {
        URI uri = new URIBuilder(URL).addParameter("currency_pair", this.coinCode).build();
        HttpGet request = new HttpGet(uri);
        HttpResponse response = this.httpClient.execute(request);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            StringBuffer sb = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            JsonObject tickJson = this.jsonParser.parse(sb.toString()).getAsJsonObject();
            long timestamp = tickJson.get("timestamp").getAsLong();
            double price = Double.parseDouble(tickJson.get("last").getAsString());
            // return new CoinTick(this.coinCode, timestamp, price);
            return null; // TODO
        }
    }

    @Override
    public void onStart() {
        this.jsonParser = new JsonParser();
        this.httpClient = HttpClientBuilder.create().build();
    }

    @Override
    public void onStop() {
    }
}
