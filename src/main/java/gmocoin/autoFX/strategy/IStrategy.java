package gmocoin.autoFX.strategy;

import java.util.*;

import gmocoin.autoFX.strategy.common.*;

public interface IStrategy {
	public void init();
	
    public void traing();

    public void run();

    public int getProfitLoss();

    public int getValidTradeCount();

    public int getSettledTradeCount();

    public int getLivededTradeCount();

    public int getLostCutTradeCount();

    public int getVaildProfitLoss();

    public List<Trade> getValidTradeList();

}
