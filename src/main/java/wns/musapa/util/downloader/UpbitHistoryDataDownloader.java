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
import wns.musapa.model.code.UpbitCoinCode;

import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpbitHistoryDataDownloader implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitHistoryDataDownloader.class);
    private static final String URL = "https://crix-api-endpoint.upbit.com/v1/crix/candles/minutes/1?code=%s&count=200&to=%sT%s.000Z";

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat sdfUpbit = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    private final String code;
    private final String sDate;

    public UpbitHistoryDataDownloader(String code, String sDate) {
        this.code = code;
        this.sDate = sDate;
    }

    @Override
    public void run() {
        try {
            download(code, sDate);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void download(String code, String sDate) throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();
        Date currentDate = simpleDateFormat.parse(sDate);
        Date now = new Date();

        long timestamp = currentDate.getTime();
        String filename = String.format("./data/%s_%s_%s.csv", code, simpleDateFormat.format(currentDate), simpleDateFormat.format(now));
        PrintWriter pw = new PrintWriter(new File(filename));
        pw.println("date,low,high,open,close,volume,weightedAverage");
        while (timestamp < now.getTime()) {
            currentDate = new Date(timestamp);
            String url = String.format(URL, code, simpleDateFormat.format(currentDate), simpleTimeFormat.format(currentDate));
            LOGGER.info(url);
            URI uri = new URIBuilder(url).build();
            HttpGet request = new HttpGet(uri);
            HttpResponse response = httpClient.execute(request);

            JsonParser jp = new JsonParser();
            JsonArray root = jp.parse(new InputStreamReader(response.getEntity().getContent())).getAsJsonArray();
            if (root.size() == 0) {
                // No data available
                timestamp += 24 * 60 * 60 * 1000; // Slide a day
                continue;
            }

            // Sort
            List<JsonObject> jsonList = new ArrayList<>();
            for (int i = 0; i < root.size(); i++) {
                jsonList.add(root.get(i).getAsJsonObject());
            }
            jsonList.sort(Comparator.comparing(o -> o.get("candleDateTimeKst").getAsString()));

            // Print
            for (JsonObject jsonObject : jsonList) {
                //convert
                String d = String.valueOf(sdfUpbit.parse(jsonObject.get("candleDateTimeKst").getAsString()).getTime());
                pw.println(String.format("%s,%s,%s,%s,%s,%s,%s",
                        d,
                        jsonObject.get("lowPrice").getAsString(),
                        jsonObject.get("highPrice").getAsString(),
                        jsonObject.get("openingPrice").getAsString(),
                        jsonObject.get("tradePrice").getAsString(),
                        jsonObject.get("candleAccTradeVolume").getAsString(),
                        jsonObject.get("candleAccTradePrice").getAsString()));
            }
            timestamp += root.size() * 60000;
            Thread.sleep(20);
        }
        pw.close();
    }

    public static void main(String[] args) throws Exception {

        // UpbitHistoryDataDownloader downloader = new UpbitHistoryDataDownloader();
        UpbitCoinCode[] coinCodes = UpbitCoinCode.values();

        ExecutorService executors = Executors.newCachedThreadPool();

        for (UpbitCoinCode coinCode : coinCodes) {
            executors.execute(new UpbitHistoryDataDownloader(coinCode.getCode(), "2017-11-01"));
        }

        System.out.println("Done");
    }
}
