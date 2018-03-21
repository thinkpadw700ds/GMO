package gmocoin.autoFX.strategy.common;

import org.json.*;

public class PriceData {
    public String datetime;
    public double openPrice;
    public double closePrice;
    public double highPrice;
    public double lowPrice;
    
    public PriceData() {
        this.datetime = "000000000000";
    };

    public PriceData(JSONObject dataJson) {
        try {
            this.datetime = dataJson.getString("datetime");
            this.openPrice = dataJson.getDouble("openPrice");
            this.closePrice = dataJson.getDouble("closePrice");
            this.highPrice = dataJson.getDouble("highPrice");
            this.lowPrice = dataJson.getDouble("lowPrice");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public long getAvg() {
        long avg = (long) (openPrice + closePrice + highPrice + lowPrice);
        return avg >> 2;
    }

    public double getMaxFluctuation() {
    	double max = openPrice;
    	double min = openPrice;
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

    public double getCloseOpenFluct() {
        return closePrice - openPrice;
    }

    public String toCsv() {
        StringBuffer sb = new StringBuffer();
        sb.append(openPrice).append(",").append(closePrice).append(",").append(highPrice).append(",").append(lowPrice);
        return sb.toString();
    }
}
