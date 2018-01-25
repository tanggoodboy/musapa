package wns.musapa.keras;

import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

public class KerasTest {
    public static void main(String[] args) throws Exception {
        MultiLayerNetwork network = KerasModelImport.importKerasSequentialModelAndWeights("./python/btc_model.h5", false);
        System.out.println(network);
    }
}
