package wns.musapa.upbit.command;

import wns.musapa.model.CoinAnalysis;
import wns.musapa.model.code.UpbitCoinCode;
import wns.musapa.upbit.CoinAnalysisLog;
import wns.musapa.upbit.TelegramUser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class NowCommand extends AbstractTelegramCommand {
    private long windowSize;

    public NowCommand(long windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public String getHelp() {
        return "Prints a list of coins with highest and lowest rates of change.";
    }

    @Override
    public String getCommand() {
        return "/now";
    }

    @Override
    public String process(TelegramUser user, String[] tokens) {
        StringBuilder sb = new StringBuilder();
        sb.append("Last " + this.windowSize / 60000 + " minutes...\n");
        List<CoinAnalysis> analysisList = new ArrayList<>(CoinAnalysisLog.getInstance().values());
        analysisList.sort(new Comparator<CoinAnalysis>() {
            @Override
            public int compare(CoinAnalysis o1, CoinAnalysis o2) {
                if (o1.getRateOfChange() > o2.getRateOfChange()) {
                    return -1;
                } else if (o1.getRateOfChange() < o2.getRateOfChange()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        // Print top 5
        for (int i = 0; i < 5 && i < analysisList.size(); i++) {
            CoinAnalysis analysis = analysisList.get(i);
            UpbitCoinCode upbitCoinCode = (UpbitCoinCode) analysis.getCode();
            sb.append(String.format("%s(%s): %.0f (%.4f%%) %s\n",
                    upbitCoinCode.getKorean(), upbitCoinCode.name(), analysis.getClose().getPrice(),
                    analysis.getRateOfChange() * 100, sdf.format(new Date(analysis.getClose().getTimestamp()))));
        }
        sb.append("...\n");
        // Print bottom 5
        for (int i = analysisList.size() - Math.min(4, analysisList.size() - 1); i < analysisList.size(); i++) {
            CoinAnalysis analysis = analysisList.get(i);
            UpbitCoinCode upbitCoinCode = (UpbitCoinCode) analysis.getCode();
            sb.append(String.format("%s(%s): %.0f (%.4f%%) %s\n",
                    upbitCoinCode.getKorean(), upbitCoinCode.name(), analysis.getClose().getPrice(),
                    analysis.getRateOfChange() * 100, sdf.format(new Date(analysis.getClose().getTimestamp()))));
        }
        return sb.toString();
    }

}
