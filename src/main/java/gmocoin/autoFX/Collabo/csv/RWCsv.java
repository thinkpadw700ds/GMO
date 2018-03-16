package gmocoin.autoFX.Collabo.csv;

import java.io.*;
import java.util.*;

import org.json.*;

import gmocoin.autoFX.strategy.common.*;

public class RWCsv {
    private static final String SP = System.lineSeparator();
    private static final String BTC_FILE_NAME = "DATA/BTCpriceData.csv";
    private static final String ETH_FILE_NAME = "DATA/ETHpriceData.csv";
    private static final String BCH_FILE_NAME = "DATA/BCHpriceData.csv";
    private static final String LTC_FILE_NAME = "DATA/LTCpriceData.csv";
    private static final String XRP_FILE_NAME = "DATA/XRPpriceData.csv";
    private int bposition = 0;
    private int eposition = 0;
    private int bcposition = 0;
    private int ltposition = 0;
    private int xposition = 0;

    public void wFile(StringBuffer sb, String fileName) {
        BufferedWriter fileOut = null;
        try {
            File filename = new File(fileName);
            if (!filename.exists()) {
                filename.createNewFile();
            }
            fileOut = new BufferedWriter(new FileWriter(filename, true));
            fileOut.write(sb.toString());
            fileOut.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (fileOut != null) {
                try {
                    fileOut.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private List<String> rFile(String fileName) {
        BufferedReader fileIn = null;
        List<String> lineList = new ArrayList<>();
        try {
            File filename = new File(fileName);
            if (!filename.exists()) {
                filename.createNewFile();
            }
            fileIn = new BufferedReader(new FileReader(filename));
            String lineTxt = null;
            while ((lineTxt = fileIn.readLine()) != null) {
                if (!"".equals(lineTxt)) {
                    lineList.add(lineTxt);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileIn != null) {
                try {
                    fileIn.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return lineList;
    }

    public void deleteCsv(String fileName) {
        File filename = new File(fileName);
        if (filename.exists()) {
            filename.delete();
        }
    }

    public void wBTCPriceData2Csv(List<PriceData> pdList) {
        bposition = wPriceData2Csv(bposition, pdList, BTC_FILE_NAME);
    }

    public void wETHPriceData2Csv(List<PriceData> pdList) {
        eposition = wPriceData2Csv(eposition, pdList, ETH_FILE_NAME);
    }

    public void wBCHPriceData2Csv(List<PriceData> pdList) {
        bcposition = wPriceData2Csv(bcposition, pdList, BCH_FILE_NAME);
    }

    public void wLTCPriceData2Csv(List<PriceData> pdList) {
        ltposition = wPriceData2Csv(ltposition, pdList, LTC_FILE_NAME);
    }

    public void wXRPPriceData2Csv(List<PriceData> pdList) {
        xposition = wPriceData2Csv(xposition, pdList, XRP_FILE_NAME);
    }

    private int wPriceData2Csv(int position, List<PriceData> pdList, String fileName) {
        StringBuffer sb = new StringBuffer();
        if (position == 0) {
            deleteCsv(fileName);
            sb.append("datetime").append(",").append("openPrice").append(",").append("closePrice").append(",")
                    .append("highPrice").append(",").append("lowPrice").append(SP);
        }
        for (; position < pdList.size() - 1; position++) {
            PriceData pd = pdList.get(position);
            sb.append(pd.datetime).append(",");
            sb.append(pd.openPrice).append(",");
            sb.append(pd.closePrice).append(",");
            sb.append(pd.highPrice).append(",");
            sb.append(pd.lowPrice).append(SP);
        }
        if (!"".equals(sb.toString())) {
            wFile(sb, fileName);
        }
        return position;
    }

    public List<PriceData> rCsv2BTCPriceData() {
        return rCsv2PriceData(BTC_FILE_NAME);
    }

    public List<PriceData> rCsv2ETHPriceData() {
        return rCsv2PriceData(ETH_FILE_NAME);
    }

    public List<PriceData> rCsv2BCHPriceData() {
        return rCsv2PriceData(BCH_FILE_NAME);
    }

    public List<PriceData> rCsv2LTCPriceData() {
        return rCsv2PriceData(LTC_FILE_NAME);
    }

    public List<PriceData> rCsv2XRPPriceData() {
        return rCsv2PriceData(XRP_FILE_NAME);
    }

    private List<PriceData> rCsv2PriceData(String fileName) {
        List<String> lineList = rFile(fileName);
        List<PriceData> pdList = new ArrayList<PriceData>();
        try {
            for (int i = 1; i < lineList.size(); i++) {
                String line = lineList.get(i);
                if (line != null && !"".equals(line)) {
                    String[] datas = line.split(",");
                    if (datas.length < 5) {
                        continue;
                    }
                    JSONObject dj = new JSONObject();
                    dj.put("datetime", datas[0]);
                    dj.put("openPrice", datas[1]);
                    dj.put("closePrice", datas[2]);
                    dj.put("highPrice", datas[3]);
                    dj.put("lowPrice", datas[4]);
                    PriceData priceData = new PriceData(dj);
                    pdList.add(priceData);
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return pdList;
    }

    public void resetBTC() {
        this.bposition = 0;
    }

    public void resetETH() {
        this.eposition = 0;
    }

    public void resetBCH() {
        this.bcposition = 0;
    }

    public void resetLTC() {
        this.ltposition = 0;
    }

    public void resetXRP() {
        this.xposition = 0;
    }
}
