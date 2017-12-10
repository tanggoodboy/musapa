package wns.musapa.model;

public class CoinDecision extends Coin {
    private Choice choice = Choice.HOLD;

    private double targetPrice = 0;
    private double targetVolume = 0;

    public CoinDecision(String code) {
        super(code);
    }

    public Choice getChoice() {
        return choice;
    }

    public void setChoice(Choice choice) {
        this.choice = choice;
    }

    public double getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(double targetPrice) {
        this.targetPrice = targetPrice;
    }

    public double getTargetVolume() {
        return targetVolume;
    }

    public void setTargetVolume(double targetVolume) {
        this.targetVolume = targetVolume;
    }

    public enum Choice {
        BUY, SELL, HOLD
    }

}
