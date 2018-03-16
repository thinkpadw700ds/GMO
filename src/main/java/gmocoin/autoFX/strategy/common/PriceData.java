package gmocoin.autoFX.strategy.common;

import org.json.*;

public class PriceData {
    public PriceData() {
        this.datetime = "000000000000";
    };

    public PriceData(JSONObject dataJson) {
        try {
            this.datetime = dataJson.getString("datetime");
            this.openPrice = dataJson.getInt("openPrice");
            this.closePrice = dataJson.getInt("closePrice");
            this.highPrice = dataJson.getInt("highPrice");
            this.lowPrice = dataJson.getInt("lowPrice");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String datetime;
    public int openPrice;
    public int closePrice;
    public int highPrice;
    public int lowPrice;

    public long getAvg() {
        int avg = openPrice + closePrice + highPrice + lowPrice;
        return avg >> 2;
    }

    public int getMaxFluctuation() {
        int max = openPrice;
        int min = openPrice;
        if (closePrice > max) {
            max = closePrice;
        }
        if (highPrice > max) {
            max = highPrice;
        }
        if (lowPrice > max) {
            max = lowPrice;
        }
        if (closePrice < min) {
            min = closePrice;
        }
        if (highPrice < min) {
            min = highPrice;
        }
        if (lowPrice < min) {
            min = lowPrice;
        }
        return max - min;
    }

    public int getCloseOpenFluct() {
        return closePrice - openPrice;
    }

    public String toCsv() {
        StringBuffer sb = new StringBuffer();
        sb.append(openPrice).append(",").append(closePrice).append(",").append(highPrice).append(",").append(lowPrice);
        return sb.toString();
    }
}
