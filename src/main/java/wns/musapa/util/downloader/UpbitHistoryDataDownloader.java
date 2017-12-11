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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UpbitHistoryDataDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitHistoryDataDownloader.class);
    private static final String CODE = "CRIX.UPBIT.KRW-BTC";
    private static final String URL = "https://crix-api-endpoint.upbit.com/v1/crix/candles/minutes/1?code=%s&count=200&to=%sT%s.000Z";

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat sdfUpbit = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    public UpbitHistoryDataDownloader() {

    }

    public void run(String code, String sDate) throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();
        Date currentDate = simpleDateFormat.parse(sDate);
        Date now = new Date();

        long timestamp = currentDate.getTime();
        PrintWriter pw = new PrintWriter(new File("./data/" + code + "_" + simpleDateFormat.format(currentDate) + ".csv"));
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
            for (int i = 0; i < root.size(); i++) {
                //convert
                JsonObject jsonObject = root.get(i).getAsJsonObject();
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

        UpbitHistoryDataDownloader downloader = new UpbitHistoryDataDownloader();
        downloader.run("CRIX.UPBIT.KRW-ETH", "2017-09-26"); // Sept 26 is the earliest possible.
        System.out.println("Done");
    }
}
