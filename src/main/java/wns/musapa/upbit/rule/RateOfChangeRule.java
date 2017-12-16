package wns.musapa.upbit.rule;

import wns.musapa.model.CoinAnalysis;
import wns.musapa.model.CoinCode;
import wns.musapa.model.code.UpbitCoinCode;
import wns.musapa.upbit.UpbitUtil;

public class RateOfChangeRule extends AbstractRule {
    public enum Direction {
        Rise, Fall
    }

    private double targetRateOfChange = 0;
    private Direction direction;
    private CoinCode coinCode;

    public RateOfChangeRule(Direction direction, CoinCode coinCode, double targetRateOfChange) {
        this.direction = direction;
        this.targetRateOfChange = targetRateOfChange;
        this.coinCode = coinCode;
    }

    @Override
    public boolean isMatch(CoinAnalysis coinAnalysis) {
        if (coinAnalysis.getCode() != this.coinCode) {
            return false;
        }

        if (this.direction == Direction.Rise
                && coinAnalysis.getRateOfChange() > 0
                && coinAnalysis.getRateOfChange() >= this.targetRateOfChange) {
            return true;
        } else if (this.direction == Direction.Fall
                && coinAnalysis.getRateOfChange() < 0
                && Math.abs(coinAnalysis.getRateOfChange()) >= this.targetRateOfChange) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String fire(CoinAnalysis coinAnalysis) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.direction.name().toUpperCase() + ": " + coinAnalysis.getCode().name()).append("\n");

        sb.append(UpbitUtil.print(coinAnalysis));
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("%s %s %.3f%%", ((UpbitCoinCode)coinCode).getKorean(), direction.name().toLowerCase(), targetRateOfChange);
    }
}
