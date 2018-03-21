package gmocoin.autoFX.strategy.ai;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.json.*;
import org.nd4j.linalg.api.ndarray.*;
import org.nd4j.linalg.cpu.nativecpu.*;
import org.nd4j.linalg.dataset.*;

import gmocoin.autoFX.Collabo.sp.*;
import gmocoin.autoFX.control.*;
import gmocoin.autoFX.strategy.*;
import gmocoin.autoFX.strategy.ai.nlp.*;
import gmocoin.autoFX.strategy.common.*;

public class AiStrategy extends absStrategy {
    private NewBuy newBuy;
    private int prediction = -1;
    private long buyTimescap = 0;
    private long prevPrediction = -1;
    private String datetime = "";

    private List<PriceData[]> trainList;
    private List<PriceData> btcList_BASE_MIN;
    private List<PriceData> ethList_BASE_MIN;
    private List<PriceData> bchList_BASE_MIN;
    private List<PriceData> ltcList_BASE_MIN;
    private List<PriceData> xrpList_BASE_MIN;

    private static final int BASE_MIN;
    private static final int TGRESHOLD;
    private static Control control;
    static {
        control = Control.getInstance();
        BASE_MIN = Integer.valueOf(Control.getInstance().getProperty("DL_BASE_MIN")).intValue();
        TGRESHOLD = Integer.valueOf(Control.getInstance().getProperty("DL_TGRESHOLD")).intValue();
    }
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH);
    public AiStrategy(SpService service) {
        super(service);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.btcList_BASE_MIN = new ArrayList<>();
        this.ethList_BASE_MIN = new ArrayList<>();
        this.bchList_BASE_MIN = new ArrayList<>();
        this.ltcList_BASE_MIN = new ArrayList<>();
        this.xrpList_BASE_MIN = new ArrayList<>();
        this.trainList = new ArrayList<>();
        this.newBuy = new NewBuy();
        this.buyTimescap = Calendar.getInstance().getTime().getTime() + BASE_MIN * 10000l;
    }
    public void init(){

        try {
	    	PriceData[] currentPDs = this.service.getCurrentPDs();
	    	if(currentPDs[0]!= null){
//	            Date lastTrainDateTime = this.newBuy.getStartDateTime();
				Date currentDate = sdf.parse(currentPDs[0].datetime);
				Calendar cal = Calendar.getInstance();
				cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				cal.setTime(currentDate);
				cal.add(Calendar.MINUTE,-(BASE_MIN<<3));
				Date lastTrainDateTime = cal.getTime();
				while(!lastTrainDateTime.equals(currentDate)){
					cal.setTime(lastTrainDateTime);
					cal.add(Calendar.MINUTE,1);
					lastTrainDateTime = cal.getTime();
					PriceData[] pastPDs = this.service.getCurrentPDs(lastTrainDateTime);
					this.trainList.add(pastPDs);
					doFit(pastPDs);
				}
				cal.setTime(new Date());
	        }
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    private boolean firstOutFlg = true;
    public void run() {
        try {
            PriceData[] currentPDs = this.service.getCurrentPDs();
            doFit(currentPDs);
            if(currentPDs[0]!= null && !currentPDs[0].datetime.equals(this.datetime)){
            	this.datetime = currentPDs[0].datetime;
                this.trainList.add(currentPDs);
                this.prevPrediction = this.prediction;
                if(trainList.size() >= (BASE_MIN<<2)){
                	this.prediction = newBuy.outPut(trainList);
                	trainList.remove(0);
                }
				if(this.prediction > -1){
                	firstOutFlg = false;
                }
                System.out.println("label:" + this.prediction);
            }
            super.run();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("boxing")
    private boolean doFit(PriceData[] currentPDs) {
        PriceData fristBtcD = null;
        if (btcList_BASE_MIN.size() > 0) {
            fristBtcD = btcList_BASE_MIN.get(0);
        }
        if (currentPDs[0] == null 
        		|| (fristBtcD != null
                && Long.valueOf(currentPDs[0].datetime) <= Long
                        .valueOf(btcList_BASE_MIN.get(btcList_BASE_MIN.size() - 1).datetime))) {
            return false;
        }
        btcList_BASE_MIN.add(currentPDs[0]);
        ethList_BASE_MIN.add(currentPDs[1]);
        bchList_BASE_MIN.add(currentPDs[2]);
        ltcList_BASE_MIN.add(currentPDs[3]);
        xrpList_BASE_MIN.add(currentPDs[4]);
        int trend = currentTrend();
        System.out.println("¥treal label:" + trend);
        if (trend < 0) {
            return false;
        }
        DataSet labelDs = new DataSet();
        PriceData currenteth = findPriceData(ethList_BASE_MIN, fristBtcD.datetime);
        PriceData currentbch = findPriceData(bchList_BASE_MIN, fristBtcD.datetime);
        PriceData currentltc = findPriceData(ltcList_BASE_MIN, fristBtcD.datetime);
        PriceData currentxrp = findPriceData(xrpList_BASE_MIN, fristBtcD.datetime);
        INDArray featureMatrix = this.newBuy
                .pds2NdArrayRow(new PriceData[] { fristBtcD, currenteth, currentbch, currentltc, currentxrp });
        INDArray label = new NDArray(1, 4);
        for (int i = 0; i < 4; i++) {
            if (i == trend) {
                label.putScalar(i, 1);
            } else {
                label.putScalar(i, 0);
            }
        }

        labelDs.setLabels(label);
        labelDs.setFeatures(featureMatrix);
        this.newBuy.fit(labelDs);
        
        return true;
    }

    @Override
    public void traing() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                newBuy.training();
            }
        }).start();
    }

    @Override
    protected boolean doTread() {
        long minSpan = (BASE_MIN >> 1) * 100000l;
        if (Calendar.getInstance().getTime().getTime() - buyTimescap > minSpan) {
            if (this.liveTradeSize < this.maxTradeSize && this.prediction == this.prevPrediction) {
                switch (this.prediction) {
                case 0:
                    newTrade(true, this.sdata);
                	break;
                case 1:
                    newTrade(false, this.sdata);
                    break;
                }
            }
        }
        return false;
    }

    private void newTrade(boolean isbuy, StatisticsData statisticsData) {
        int price = 0;
        if (isbuy) {
            price = (int) (statisticsData.getCurrent() + DIFFERENCE / 2);
        } else {
            price = (int) (statisticsData.getCurrent() - DIFFERENCE / 2);
        }
        Trade trade;
        trade = new Trade(isbuy, statisticsData.getLastVal(), price, this.singleQuantity);
        // trade = service.newTread(isbuy, this.singleQuantity);
        if (trade != null) {
            tradeList.add(trade);
            liveTradeSize++;
            buyTimescap = Calendar.getInstance().getTime().getTime() ;
        }
    }

    @Override
    protected boolean doSettlement() {
    	double currentPrice = this.sdata.getCurrent();
        int prof = (int)(30f * 100.0 * this.singleQuantity);
        double price = 0;
        for (Trade trade : tradeList) {
            int cut = 0;
            boolean doSettle = false;
            if (trade.isBuy()) {
                price = currentPrice - DIFFERENCE / 2;
                cut = (int) (100 - currentPrice * 100 / trade.getPrice());
                if (this.prediction != 0 && this.prediction > -1) {
                    doSettle = true;
                }
            } else {
                price = currentPrice + DIFFERENCE / 2;
                cut = (int) (currentPrice * 100 / trade.getPrice() - 100);
                if (this.prediction != 1 && this.prediction > -1) {
                    doSettle = true;
                }
            }
            if (trade.getProfitLoss(price) < prof) {
                doSettle = false;
            } else if (cut >= lostCut) {
                doSettle = true;
            }
            if (doSettle) {
                trade.doSettlement((int)price);
                liveTradeSize--;
            }
        }
        return false;
    }

    private int currentTrend() {
        int val = -1;
        if (btcList_BASE_MIN.size() < BASE_MIN) {
            return val;
        }
        PriceData currentbtc = btcList_BASE_MIN.get(0);
        PriceData currenteth = findPriceData(ethList_BASE_MIN, currentbtc.datetime);
        PriceData currentbch = findPriceData(bchList_BASE_MIN, currentbtc.datetime);
        PriceData currentltc = findPriceData(ltcList_BASE_MIN, currentbtc.datetime);
        PriceData currentxrp = findPriceData(xrpList_BASE_MIN, currentbtc.datetime);
        if (currenteth == null || currentbch == null || currentltc == null || currentxrp == null) {
            return val;
        }
        boolean riseFlg = false;
        boolean declineFlg = false;
        long maxVal = 0l;
        long avgVal = 0l;
        long minVal = currentbtc.getAvg();
        for (int j = 0; j < btcList_BASE_MIN.size(); j++) {
            PriceData jd = btcList_BASE_MIN.get(j);
            if (jd.getAvg() > maxVal) {
                maxVal = jd.getAvg();
            }
            if (jd.getAvg() < minVal) {
                minVal = jd.getAvg();
            }
            avgVal += jd.getAvg();
        }
        avgVal /= btcList_BASE_MIN.size();

        long S = 0l;
        for (int j = 0; j < btcList_BASE_MIN.size(); j++) {
            PriceData jd = btcList_BASE_MIN.get(j);
            S += ((jd.getAvg() - avgVal) * (jd.getAvg() - avgVal));
        }
        S /= btcList_BASE_MIN.size();
        S = (long)Math.sqrt(S);

        if (maxVal - currentbtc.getAvg() > TGRESHOLD && avgVal > (currentbtc.getAvg() + (TGRESHOLD >> 1))) {
            riseFlg = true;
        }
        if (currentbtc.getAvg() - minVal > TGRESHOLD && avgVal < (currentbtc.getAvg() - (TGRESHOLD >> 1))) {
            declineFlg = true;
        }
        if (riseFlg) {
            val = 0;
        } else if (declineFlg) {
            val = 1;
        } else if (S <= (TGRESHOLD >> 1)) {
            val = 2;
        } else {
            val = 3;
        }
        btcList_BASE_MIN.remove(0);
        return val;
    }

    private static PriceData findPriceData(List<PriceData> list, String datatime) {
        if (list == null) {
            return null;
        }
        Optional<PriceData> opd = list.stream().filter(p -> p.datetime.equals(datatime)).findFirst();
        if (opd.isPresent()) {
            return opd.get();
        }
        return null;
    }

}
