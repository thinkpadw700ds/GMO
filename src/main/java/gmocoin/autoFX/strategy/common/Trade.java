package gmocoin.autoFX.strategy.common;

import java.util.*;

public class Trade {
    private boolean isBuy = true;
    private PriceData priceData;
    // 价格
    private int price;
    private int settlePrice;
    // 损益
    private int profitLoss;
    // 数量
    private float quantity;
    private Date tradeDate;
    private boolean settlement = false;

    public Trade(boolean isBuy, PriceData priceData, int price, float quantity) {
        // System.out.println("new trade :" + (Long.valueOf(priceData.datetime) + 900) + " " + price);
        // System.out.println();
        this.isBuy = isBuy;
        this.priceData = priceData;
        this.price = price;
        this.quantity = quantity;
        this.tradeDate = Calendar.getInstance().getTime();
    }

    public Date getTradeDate() {
        return tradeDate;
    }

    public int getPrice() {
        return price;
    }

    public boolean isBuy() {
        return isBuy;
    }

    public PriceData getPriceData() {
        return priceData;
    }

    public int getProfitLoss(double d) {
        int val = 0;
        if (settlement) {
            return this.profitLoss;
        }
        val = (int)((d - price) * quantity);
        if (!isBuy) {
            val = 0 - val;
        }
        profitLoss = val;
        return this.profitLoss;
    }

    public float getQuantity() {
        return quantity;
    }

    public boolean isSettlement() {
        return settlement;
    }

    public void doSettlement(int current) {
        getProfitLoss(current);
        // System.out.println("決済:" + this.profitLoss + " " + (isBuy ? "多" : "空") + " " + price + "～" + current);
        // System.out.println((Long.valueOf(priceData.datetime) + 900) + " ");
        // System.out.println();
        this.settlePrice = current;
        this.settlement = true;
    }
}
