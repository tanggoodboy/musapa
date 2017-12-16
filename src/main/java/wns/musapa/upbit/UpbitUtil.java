package wns.musapa.upbit;

import wns.musapa.model.CoinAnalysis;
import wns.musapa.model.code.UpbitCoinCode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class UpbitUtil {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss");

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
    }

    public static String print(CoinAnalysis coinAnalysis) {
        StringBuilder sb = new StringBuilder();
        UpbitCoinCode upbitCoinCode = (UpbitCoinCode) coinAnalysis.getCode();
        sb.append(upbitCoinCode.getKorean() + " (" + upbitCoinCode.name() + ")\n");
        sb.append(String.format("Rate: %.3f%%\n", coinAnalysis.getRateOfChange()));
        sb.append(String.format("Close: %,d (%s)\n", (long)coinAnalysis.getClose().getPrice(), sdf.format(new Date(coinAnalysis.getClose().getTimestamp()))));
        sb.append(String.format("Open: %,d (%s)\n", (long)coinAnalysis.getOpen().getPrice(), sdf.format(new Date(coinAnalysis.getOpen().getTimestamp()))));
        sb.append(String.format("Low: %,d (%s)\n", (long)coinAnalysis.getLow().getPrice(), sdf.format(new Date(coinAnalysis.getLow().getTimestamp()))));
        sb.append(String.format("High: %,d (%s)\n", (long)coinAnalysis.getHigh().getPrice(), sdf.format(new Date(coinAnalysis.getHigh().getTimestamp()))));
        return sb.toString();
    }

    public static String printTimestamp(long timestamp) {
        return sdf.format(new Date(timestamp));
    }
}
