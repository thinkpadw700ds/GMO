package gmocoin.autoFX.strategy;

import java.util.*;
import java.util.stream.*;

import gmocoin.autoFX.Collabo.sp.*;
import gmocoin.autoFX.control.*;
import gmocoin.autoFX.strategy.common.*;

@SuppressWarnings("boxing")
public abstract class absStrategy implements IStrategy {
    protected static int DIFFERENCE;
    protected static int lostCut;
    protected static Control control;
    static {
        control = Control.getInstance();
        DIFFERENCE = Integer.valueOf(control.getProperty("DIFFERENCE"));
        lostCut = Integer.valueOf(control.getProperty("lostCut"));
    }
    protected List<Trade> tradeList;
    protected SpService service;
    protected float singleQuantity;

    protected int maxTradeSize;
    protected int liveTradeSize = 0;

    protected StatisticsData sdata;

    public absStrategy(SpService service) {
        this.service = service;
        this.tradeList = new ArrayList<Trade>();
        this.maxTradeSize = Integer.valueOf(control.getProperty("maxTradeCount"));
        this.singleQuantity = Float.valueOf(control.getProperty("singleQuantity"));
        this.sdata = service.getData();
    }

    public void run() {
        doTread();
        doSettlement();
    }

    protected abstract boolean doTread();

    protected abstract boolean doSettlement();

    public int getProfitLoss() {
    	double current = this.sdata.getCurrent();
        int result = 0;
        for (Trade trade : tradeList) {
            if (trade.isBuy()) {
                result += trade.getProfitLoss(current - DIFFERENCE / 2);
            } else {
                result += trade.getProfitLoss(current + DIFFERENCE / 2);
            }
        }
        return result;
    }

    public int getValidTradeCount() {
        int count = 0;
        for (Trade trade : tradeList) {
            if (!trade.isSettlement()) {
                count++;
            }
        }
        return count;
    }

    public int getSettledTradeCount() {
        int count = 0;
        for (Trade trade : tradeList) {
            if (trade.isSettlement()) {
                count++;
            }
        }
        return count;
    }

    public int getLivededTradeCount() {
        int count = 0;
        for (Trade trade : tradeList) {
            if (!trade.isSettlement()) {
                count++;
            }
        }
        return count;
    }

    public int getLostCutTradeCount() {
        int count = 0;
        for (Trade trade : tradeList) {
            if (trade.isSettlement() && trade.getProfitLoss(0) < 0) {
                count++;
            }
        }
        return count;
    }

    public int getVaildProfitLoss() {
    	double current = this.sdata.getCurrent();
        int result = 0;
        for (Trade trade : tradeList) {
            if (trade.isSettlement()) {
                continue;
            }
            if (trade.isBuy()) {
                result += trade.getProfitLoss(current - DIFFERENCE / 2);
            } else {
                result += trade.getProfitLoss(current + DIFFERENCE / 2);
            }
        }
        return result;
    }

    public List<Trade> getValidTradeList() {
        return tradeList.stream().filter(t -> !t.isSettlement()).collect(Collectors.toList());
    }
}
