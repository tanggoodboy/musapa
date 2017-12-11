package wns.musapa.util.downloader;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;

public class PoloniexHistoryDataDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PoloniexHistoryDataDownloader.class);

    public PoloniexHistoryDataDownloader() {


    }

    public void download() throws IOException, URISyntaxException {
        HttpClient httpClient = HttpClientBuilder.create().build();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2017);
        calendar.set(Calendar.MONTH, 00);
        calendar.set(Calendar.DAY_OF_MONTH, 01);
        calendar.set(Calendar.HOUR, 00);
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.SECOND, 00);
        Date startDate = calendar.getTime();
        String filename = "./data/poloniex_usdt_btc_300_170101.csv";

        String url = "https://poloniex.com/public?command=returnChartData&currencyPair=USDT_BTC&end=9999999999&period=300&start=" + (startDate.getTime()/1000);
        System.out.println("GET " + url);
        URI uri = new URIBuilder(url).build();
        HttpGet request = new HttpGet(uri);
        HttpResponse response = httpClient.execute(request);

        JsonParser jp = new JsonParser();
        JsonArray root = jp.parse(new InputStreamReader(response.getEntity().getContent())).getAsJsonArray();

        System.out.println("Received: "+root.size());

        PrintWriter pw = new PrintWriter(new File(filename));
        pw.println("date,low,high,open,close,volume,weightedAverage");
        for (int i=0; i < root.size(); i++){
            JsonObject obj = root.get(i).getAsJsonObject();
            pw.println(String.format("%s,%s,%s,%s,%s,%s,%s",
                    obj.get("date").toString(),
                    obj.get("low").toString(),
                    obj.get("high").toString(),
                    obj.get("open").toString(),
                    obj.get("close").toString(),
                    obj.get("volume").toString(),
                    obj.get("weightedAverage").toString()));
        }
        pw.close();
        System.out.println("Done");
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        new PoloniexHistoryDataDownloader().download();
    }
}
