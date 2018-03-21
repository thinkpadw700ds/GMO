package gmocoin.autoFX.Collabo.sp;

import java.text.*;
import java.util.*;

import org.json.*;

import gmocoin.autoFX.Collabo.*;
import gmocoin.autoFX.Collabo.csv.*;
import gmocoin.autoFX.control.*;
import gmocoin.autoFX.strategy.*;
import gmocoin.autoFX.strategy.ai.*;
import gmocoin.autoFX.strategy.common.*;

public class SpService implements IService, Runnable {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH);
    private ISession session;
    private StatisticsData data;
    private List<IStrategy> strategyList;
    private static final int RETRY_TIMES = 10;
    private static final String BTC = "10001";
    private static final String ETH = "1002";
    private static final String BCH = "1003";
    private static final String LTC = "1004";
    private static final String XRP = "1005";
    private static int DIFFERENCE;
    private boolean isTest = true;
    private Control control;

    public SpService(ISession session) {
        this.session = session;
        this.data = StatisticsData.getInstance();
        this.strategyList = new ArrayList<>();
        this.control = Control.getInstance();
        this.isTest = Boolean.valueOf(control.getProperty("isTest")).booleanValue();
        DIFFERENCE = Integer.valueOf(control.getProperty("DIFFERENCE")).intValue();
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub

    }

    @Override
    public void run() {
        RWCsv rwCsv = new RWCsv();
        try {
            List<PriceData> btcpdList = rwCsv.rCsv2BTCPriceData();
            List<PriceData> ethpdList = rwCsv.rCsv2ETHPriceData();
            List<PriceData> bchpdList = rwCsv.rCsv2BCHPriceData();
            List<PriceData> ltcpdList = rwCsv.rCsv2LTCPriceData();
            List<PriceData> xrppdList = rwCsv.rCsv2XRPPriceData();
            // 加载策略
            IStrategy strategy = new AiStrategy(this);
            /***********************************************/
            strategyList.add(strategy);
            for (int i = 0; i < btcpdList.size(); i++) {
                // PriceData pd = btcpdList.get(i);
                // data.add(pd);
                // if (i > 150) {
                // if (this.isTest) {
                // // this.strategyList.stream().forEach(s -> s.run());
                // }
                // }
            }

            JSONArray arrayb = getLast1000Data(BTC);
            JSONArray arraye = getLast1000Data(ETH);
            JSONArray arraybc = getLast1000Data(BCH);
            JSONArray arrayl = getLast1000Data(LTC);
            JSONArray arrayx = getLast1000Data(XRP);

            for (int i = 0; i < arrayb.length(); i++) {
                JSONObject pdj = arrayb.getJSONObject(i);
                addToList(btcpdList, pdj);
                // if (data.getLastVal() == null || (Long.valueOf(data.getLastVal().datetime) < pdj.getLong("datetime"))) {
                // PriceData priceData = new PriceData(pdj);
                // data.add(priceData);
                // if (this.isTest) {
                // // this.strategyList.stream().forEach(s -> s.run());
                // }
                // }
            }
            rwCsv.wBTCPriceData2Csv(btcpdList);

            for (int i = 0; i < arraye.length(); i++) {
                JSONObject pdj = arraye.getJSONObject(i);
                addToList(ethpdList, pdj);
            }
            rwCsv.wETHPriceData2Csv(ethpdList);

            for (int i = 0; i < arraybc.length(); i++) {
                JSONObject pdj = arraybc.getJSONObject(i);
                addToList(bchpdList, pdj);
            }
            rwCsv.wBCHPriceData2Csv(bchpdList);

            for (int i = 0; i < arrayl.length(); i++) {
                JSONObject pdj = arrayl.getJSONObject(i);
                addToList(ltcpdList, pdj);
            }
            rwCsv.wLTCPriceData2Csv(ltcpdList);

            for (int i = 0; i < arrayx.length(); i++) {
                JSONObject pdj = arrayx.getJSONObject(i);
                addToList(xrppdList, pdj);
            }
            rwCsv.wXRPPriceData2Csv(xrppdList);

            // 最適化
            this.strategyList.stream().forEach(s -> s.traing());
            Thread.sleep(10000);
            this.strategyList.stream().forEach(s -> s.init());

            String prevDataTime = btcpdList.get(btcpdList.size() - 1).datetime;
            while (true) {
                String dataTime = sdf.format(sdf.parse(String.valueOf(Long.valueOf(prevDataTime).longValue() + 1)));
                PriceData priceData = getCurrentBTCData(null);
                if (priceData == null) {
                    Thread.sleep(1000);
                    continue;
                }
                data.add(priceData);
                PriceData priceData1 = getCurrentETHData(dataTime);
                if (priceData1 != null) {
                    addToList(ethpdList, priceData1);
                    rwCsv.wETHPriceData2Csv(ethpdList);
                    PriceData priceData2 = getCurrentBCHData(dataTime);
                    addToList(bchpdList, priceData2);
                    rwCsv.wBCHPriceData2Csv(bchpdList);
                    PriceData priceData3 = getCurrentLTCData(dataTime);
                    addToList(ltcpdList, priceData3);
                    rwCsv.wLTCPriceData2Csv(ltcpdList);
                    PriceData priceData4 = getCurrentXRPData(dataTime);
                    addToList(xrppdList, priceData4);
                    rwCsv.wXRPPriceData2Csv(xrppdList);
                    PriceData priceData5 = getCurrentBTCData(dataTime);
                    addToList(btcpdList, priceData5);
                    rwCsv.wBTCPriceData2Csv(btcpdList);
                    prevDataTime = dataTime;
                }
                this.strategyList.stream().forEach(s -> s.run());
                Thread.sleep(1500);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("boxing")
    private void addToList(List<PriceData> list, JSONObject pdj) throws Exception {
        if (list.size() > 0) {
            PriceData ld = list.get(list.size() - 1);
            if (Long.valueOf(ld.datetime) < pdj.getLong("datetime")) {
                PriceData priceData = new PriceData(pdj);
                list.add(priceData);
            }
        } else {
            PriceData priceData = new PriceData(pdj);
            list.add(priceData);
        }
    }

    @SuppressWarnings("boxing")
    private void addToList(List<PriceData> list, PriceData pd) {
        if (pd == null) {
            return;
        }
        if (list.size() > 0) {
            PriceData ld = list.get(list.size() - 1);
            if (Long.valueOf(ld.datetime) < Long.valueOf(pd.datetime)) {
                list.add(pd);
            }
        } else {
            list.add(pd);
        }
    }

    private JSONArray getLast1000Data(String type) throws JSONException {

        StringBuffer html;
        JSONObject dataJson;
        Map<String, String> params1 = new HashMap<>();
        params1.put("productId", type);
        params1.put("multiBandType", "0");
        params1.put("bidAskType", "1");
        params1.put("chartType", "1");
        params1.put("size", "100000");
        html = this.session.sendGet(SpComConstants.GET_CHART_URL, params1);
        dataJson = new JSONObject(html.toString());
        JSONArray array = dataJson.getJSONArray("data");
        return array;
    }

    private String btcDataTime = "";

    private PriceData getCurrentBTCData(String iDataTime) throws JSONException {
        StringBuffer html;
        Map<String, String> params = new HashMap<>();
        params.put("productId", "10001");
        params.put("multiBandType", "0");
        params.put("bidAskType", "1");
        params.put("chartType", "1");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        if (iDataTime != null) {
            params.put("datetime", iDataTime);
        } else {
            if ("".equals(btcDataTime)) {
                this.btcDataTime = sdf.format(calendar.getTime());
            }
            params.put("datetime", this.btcDataTime);
        }
        PriceData priceData = null;
        int times = 0;
        while (priceData == null && times <= RETRY_TIMES) {
            html = this.session.sendGet(SpComConstants.GET_CHART_URL, params);
            JSONObject dataJson = new JSONObject(html.toString());
            JSONArray array = dataJson.getJSONArray("data");
            if (dataJson.getInt("status") == 0 && array.length() > 0) {
                priceData = new PriceData((JSONObject)array.get(0));
                this.btcDataTime = sdf.format(calendar.getTime());
                return priceData;
            }
            times++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String ethDataTime = "";

    private PriceData getCurrentETHData(String iDataTime) throws JSONException {
        try {
            StringBuffer html;
            Map<String, String> params = new HashMap<>();
            params.put("productId", ETH);
            params.put("multiBandType", "0");
            params.put("bidAskType", "1");
            params.put("chartType", "1");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.MINUTE, -1);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            if (iDataTime != null) {
                params.put("datetime", iDataTime);
            } else {
                if ("".equals(ethDataTime)) {
                    this.ethDataTime = sdf.format(calendar.getTime());
                }
                params.put("datetime", this.ethDataTime);
            }
            PriceData priceData = null;
            int times = 0;
            while (priceData == null && times <= RETRY_TIMES) {
                html = this.session.sendGet(SpComConstants.GET_CHART_URL, params);
                JSONObject dataJson = new JSONObject(html.toString());
                JSONArray array = dataJson.getJSONArray("data");
                if (dataJson.getInt("status") == 0 && array.length() > 0) {
                    priceData = new PriceData((JSONObject)array.get(0));
                    this.ethDataTime = sdf.format(calendar.getTime());
                    return priceData;
                }
                times++;
                Thread.sleep(1000);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String bchDataTime = "";

    private PriceData getCurrentBCHData(String iDataTime) throws JSONException {
        try {
            StringBuffer html;
            Map<String, String> params = new HashMap<>();
            params.put("productId", BCH);
            params.put("multiBandType", "0");
            params.put("bidAskType", "1");
            params.put("chartType", "1");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.MINUTE, -1);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            if (iDataTime != null) {
                params.put("datetime", iDataTime);
            } else {
                if ("".equals(bchDataTime)) {
                    this.bchDataTime = sdf.format(calendar.getTime());
                }
                params.put("datetime", this.bchDataTime);
            }
            PriceData priceData = null;
            int times = 0;
            while (priceData == null && times <= RETRY_TIMES) {
                html = this.session.sendGet(SpComConstants.GET_CHART_URL, params);
                JSONObject dataJson = new JSONObject(html.toString());
                JSONArray array = dataJson.getJSONArray("data");
                if (dataJson.getInt("status") == 0 && array.length() > 0) {
                    priceData = new PriceData((JSONObject)array.get(0));
                    this.bchDataTime = sdf.format(calendar.getTime());
                    return priceData;
                }
                times++;
                Thread.sleep(1000);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String ltcDataTime = "";

    private PriceData getCurrentLTCData(String iDataTime) throws JSONException {
        try {
            StringBuffer html;
            Map<String, String> params = new HashMap<>();
            params.put("productId", LTC);
            params.put("multiBandType", "0");
            params.put("bidAskType", "1");
            params.put("chartType", "1");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.MINUTE, -1);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            if (iDataTime != null) {
                params.put("datetime", iDataTime);
            } else {
                if ("".equals(ltcDataTime)) {
                    this.ltcDataTime = sdf.format(calendar.getTime());
                }
                params.put("datetime", this.ltcDataTime);
            }
            PriceData priceData = null;
            int times = 0;
            while (priceData == null && times <= RETRY_TIMES) {
                html = this.session.sendGet(SpComConstants.GET_CHART_URL, params);
                JSONObject dataJson = new JSONObject(html.toString());
                JSONArray array = dataJson.getJSONArray("data");
                if (dataJson.getInt("status") == 0 && array.length() > 0) {
                    priceData = new PriceData((JSONObject)array.get(0));
                    this.ltcDataTime = sdf.format(calendar.getTime());
                    return priceData;
                }
                times++;
                Thread.sleep(1000);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String xrpDataTime = "";

    private PriceData getCurrentXRPData(String iDataTime) throws JSONException {
        try {
            StringBuffer html;
            Map<String, String> params = new HashMap<>();
            params.put("productId", XRP);
            params.put("multiBandType", "0");
            params.put("bidAskType", "1");
            params.put("chartType", "1");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.MINUTE, -1);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            if (iDataTime != null) {
                params.put("datetime", iDataTime);
            } else {
                if ("".equals(xrpDataTime)) {
                    this.xrpDataTime = sdf.format(calendar.getTime());
                }
                params.put("datetime", this.xrpDataTime);
            }
            PriceData priceData = null;
            int times = 0;
            while (priceData == null && times <= RETRY_TIMES) {
                html = this.session.sendGet(SpComConstants.GET_CHART_URL, params);
                JSONObject dataJson = new JSONObject(html.toString());
                JSONArray array = dataJson.getJSONArray("data");
                if (dataJson.getInt("status") == 0 && array.length() > 0) {
                    priceData = new PriceData((JSONObject)array.get(0));
                    this.xrpDataTime = sdf.format(calendar.getTime());
                    return priceData;
                }
                times++;
                Thread.sleep(1000);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public StatisticsData getData() {
        return this.data;
    }

    public int getProfitLoss() {
        int result = 0;
        for (IStrategy strategy : strategyList) {
            result += strategy.getProfitLoss();
        }
        return result;
    }

    public int getVaildProfitLoss() {
        int result = 0;
        for (IStrategy strategy : strategyList) {
            result += strategy.getVaildProfitLoss();
        }
        return result;
    }

    public Trade newTread(boolean isBuy, float quantit) {
        JSONObject param = new JSONObject();
        try {
            PriceData priceData = getCurrentBTCData(null);
            int price = 0;
            if (isBuy) {
                price = (int) (priceData.closePrice + DIFFERENCE / 2 + 1);
            } else {
                price = (int) (priceData.closePrice - DIFFERENCE / 2 - 1);
            }
            param.put("buySellType", isBuy ? 1 : 2);
            param.put("multiBandType", 0);
            param.put("orderQuantity", String.valueOf(quantit));
            param.put("orderRate", price + "");
            param.put("productId", 10001);
            String res = session.sendPost(SpComConstants.MAKE_ORDER_URL, param).toString();
            JSONObject JsonRes = new JSONObject(res);
            if (JsonRes.getInt("status") == 0) {
                return new Trade(isBuy, priceData, JsonRes.getJSONObject("data").getInt("executionRate"), quantit);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public boolean settleTread(Trade trade) {
        try {
            PriceData priceData = getCurrentBTCData(null);
            if (priceData == null) {
                return false;
            }
            int price = 0;
            if (trade.isBuy()) {
                price = (int) (priceData.closePrice - DIFFERENCE / 2 - 1);
            } else {
                price = (int) (priceData.closePrice + DIFFERENCE / 2 + 1);
            }
            String res = session.sendGet(SpComConstants.POSITIONS_URL, new HashMap<>()).toString();
            JSONObject JsonRes;
            JsonRes = new JSONObject(res);
            if (JsonRes.getInt("status") == 0) {
                JSONArray tradeList = JsonRes.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < tradeList.length(); i++) {
                    JSONObject tradeJson = tradeList.getJSONObject(i);
                    if (tradeJson.getInt("positionRate") == trade.getPrice()
                            && tradeJson.getString("positionQuantity").contains(String.valueOf(trade.getQuantity()))
                            && ((tradeJson.getInt("buySellType") == 1) == trade.isBuy())) {
                        JSONObject param = new JSONObject();
                        JSONArray positionArray = new JSONArray();
                        JSONObject positionJson = new JSONObject();
                        positionArray.put(positionJson);
                        positionJson.put("orderQuantity", tradeJson.getString("positionQuantity"));
                        positionJson.put("positionId", tradeJson.getLong("positionId"));
                        param.put("buySellType", trade.isBuy() ? 2 : 1);
                        param.put("multiBandType", 0);
                        param.put("orderRate", price + "");
                        param.put("productId", 10001);
                        param.put("settlePosition", positionArray);
                        String res1 = session.sendPost(SpComConstants.SETTLE_URL, param).toString();
                        JSONObject JsonRes1 = new JSONObject(res1);
                        if (JsonRes1.getInt("status") == 0) {
                            trade.doSettlement(JsonRes1.getJSONObject("data").getInt("executionRate"));
                            return true;
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public int getValidTradeCount() {
        int count = 0;
        for (IStrategy s : this.strategyList) {
            count += s.getValidTradeCount();
        }
        return count;
    }

    public int getSettledTradeCount() {
        int count = 0;
        for (IStrategy s : this.strategyList) {
            count += s.getSettledTradeCount();
        }
        return count;
    }

    public List<Trade> getValidTradeList() {
        List<Trade> result = new ArrayList<>();
        for (IStrategy s : this.strategyList) {
            result.addAll(s.getValidTradeList());
        }
        return result;
    }

    public PriceData[] getCurrentPDs() throws JSONException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, -1);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateTime = ltcDataTime = sdf.format(calendar.getTime());
        PriceData priceData0 = getCurrentBTCData(dateTime);
        PriceData priceData1 = getCurrentETHData(dateTime);
        PriceData priceData2 = getCurrentBCHData(dateTime);
        PriceData priceData3 = getCurrentLTCData(dateTime);
        PriceData priceData4 = getCurrentXRPData(dateTime);
        return new PriceData[] { priceData0, priceData1, priceData2, priceData3, priceData4 };
    }

    public PriceData[] getCurrentPDs(Date date) throws JSONException {
        String dataTime = sdf.format(date);
        PriceData priceData0 = getCurrentBTCData(dataTime);
        PriceData priceData1 = getCurrentETHData(dataTime);
        PriceData priceData2 = getCurrentBCHData(dataTime);
        PriceData priceData3 = getCurrentLTCData(dataTime);
        PriceData priceData4 = getCurrentXRPData(dataTime);
        return new PriceData[] { priceData0, priceData1, priceData2, priceData3, priceData4 };
    }

}
