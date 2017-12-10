package wns.musapa.pipeline.operator;

import wns.musapa.model.CoinAnalysis;
import wns.musapa.model.CoinDecision;

public class DecisionOperator implements PipelineOperator<CoinAnalysis, CoinDecision>{
    @Override
    public void onStart() throws Exception {

    }

    @Override
    public CoinDecision process(CoinAnalysis input) {
        CoinDecision decision = new CoinDecision(input.getCode());
        if(input.getRateOfChange() >= 0.01){
            decision.setChoice(CoinDecision.Choice.BUY);
            decision.setTargetPrice(input.getClose().getPrice());
            decision.setTargetVolume(-1);
        } else if (input.getRateOfChange() <= -0.01){
            decision.setChoice(CoinDecision.Choice.SELL);
            decision.setTargetPrice(input.getClose().getPrice());
            decision.setTargetVolume(-1);
        } else {
            decision.setChoice(CoinDecision.Choice.HOLD);
        }
        return decision;
    }
}
