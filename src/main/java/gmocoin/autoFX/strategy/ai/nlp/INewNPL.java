package gmocoin.autoFX.strategy.ai.nlp;

import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.*;

import gmocoin.autoFX.strategy.common.*;

public interface INewNPL {
    void training();

    public int outPut(List<PriceData[]> currentPdsList);

    public boolean fit(DataSet ds);
    
    public INDArray pds2NDArray(List<PriceData[]> currentPdsList);
}
