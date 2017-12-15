package wns.musapa.upbit.command;

import wns.musapa.model.CoinAnalysis;
import wns.musapa.model.code.UpbitCoinCode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public abstract class AbstractTelegramCommand implements TelegramCommand {
    protected SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd'T'HHmmss");

    public AbstractTelegramCommand() {
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
    }

    protected String print(CoinAnalysis coinAnalysis) {
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
}
