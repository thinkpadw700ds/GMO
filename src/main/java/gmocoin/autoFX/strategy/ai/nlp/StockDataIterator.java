package gmocoin.autoFX.strategy.ai.nlp;

import java.io.*;
import java.util.*;

import org.nd4j.linalg.api.ndarray.*;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.*;
import org.nd4j.linalg.dataset.api.iterator.*;
import org.nd4j.linalg.factory.*;

import gmocoin.autoFX.Collabo.csv.*;
import gmocoin.autoFX.control.*;
import gmocoin.autoFX.strategy.common.*;

public class StockDataIterator implements DataSetIterator {
    private static final int BASE_MIN;
    private static final int TGRESHOLD;
    private static final int VECTOR_SIZE = 20;
    static {
        Control contr = Control.getInstance();
        BASE_MIN = Integer.valueOf(contr.getProperty("DL_BASE_MIN")).intValue();
        TGRESHOLD = Integer.valueOf(contr.getProperty("DL_TGRESHOLD")).intValue();
    }
    // 每批次的训练数据组数
    private int batchNum;

    // 每组训练数据长度(DailyData的个数)
    private int exampleLength;

    // 数据集
    private List<PriceData[]> dataList;

    // 存放剩余数据组的index信息
    private List<Integer> dataRecord;

    private double[] maxNum;

    /**
     * 构造方法
     */
    public StockDataIterator() {
        dataRecord = new ArrayList<>();
    }

    /**
     * 加载数据并初始化
     */
    public boolean loadData(int batchNum, int exampleLength) {
        this.batchNum = batchNum;
        this.exampleLength = exampleLength;
        maxNum = new double[20];
        // 加载文件中的股票数据
        try {
            readDataFromFile();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        // 重置训练批次列表
        resetDataRecord();
        return true;
    }

    /**
     * 重置训练批次列表
     */
    @SuppressWarnings("boxing")
    private void resetDataRecord() {
        dataRecord.clear();
        int total = (dataList.size() - BASE_MIN)  / exampleLength + 1;
        for (int i = 0; i < total; i++) {
        	if (i < totalExamples()){
        		dataRecord.add(i * exampleLength );
        	}
        }
    }

    /**
     * 从文件中读取股票数据
     */
    public List<PriceData[]> readDataFromFile() throws IOException {
        RWCsv rwcsv = new RWCsv();
        dataList = new ArrayList<>();
        List<PriceData> btcList = rwcsv.rCsv2BTCPriceData();
        List<PriceData> ethList = rwcsv.rCsv2ETHPriceData();
        List<PriceData> bchList = rwcsv.rCsv2BCHPriceData();
        List<PriceData> ltcList = rwcsv.rCsv2LTCPriceData();
        List<PriceData> xrpList = rwcsv.rCsv2XRPPriceData();
        btcList.sort(new Comparator<PriceData>() {
			@Override
			public int compare(PriceData o1, PriceData o2) {
				return (int) (Long.valueOf(o1.datetime)-Long.valueOf(o2.datetime));
			}
		});
        for (PriceData btc : btcList) {
            PriceData eth = findPriceData(ethList, btc.datetime);
            PriceData bch = findPriceData(bchList, btc.datetime);
            PriceData ltc = findPriceData(ltcList, btc.datetime);
            PriceData xrp = findPriceData(xrpList, btc.datetime);
            PriceData[] pds = new PriceData[] { btc, eth, bch, ltc, xrp };
            int i = -1;
            for (PriceData pd : pds) {
                if (pd.openPrice > maxNum[++i]) {
                    maxNum[i] = pd.openPrice;
                }
                if (pd.closePrice > maxNum[++i]) {
                    maxNum[i] = pd.closePrice;
                }
                if (pd.highPrice > maxNum[++i]) {
                    maxNum[i] = pd.highPrice;
                }
                if (pd.lowPrice > maxNum[++i]) {
                    maxNum[i] = pd.lowPrice;
                }
            }
            dataList.add(pds);
        }
        return dataList;
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

    public double[] getMaxArr() {
        return this.maxNum;
    }

    public void reset() {
        resetDataRecord();
    }

    public boolean hasNext() {
        return dataRecord.size() > 0;
    }

    public DataSet next() {
        return next(batchNum);
    }

    /**
     * 获得接下来一次的训练数据集
     */
    @SuppressWarnings("boxing")
    public DataSet next(int num) {
        if (dataRecord.size() <= 0) {
            throw new NoSuchElementException();
        }
        int actualBatchSize = Math.min(num, dataRecord.size());
        int actualLength = Math.min(exampleLength, dataList.size() - dataRecord.get(0) - 1 - BASE_MIN);
        INDArray input = Nd4j.create(new int[] { actualBatchSize, VECTOR_SIZE, actualLength }, 'f');
        INDArray label = Nd4j.create(new int[] { actualBatchSize, 1, actualLength }, 'f');
        PriceData[] nextData = null, curData = null;
        // 获取每批次的训练数据和标签数据
        for (int i = 0; i < actualBatchSize; i++) {
            int index = dataRecord.remove(0);
            int endIndex = Math.min(index + exampleLength, dataList.size() - 1 - BASE_MIN);
            curData = dataList.get(index);
            for (int j = index; j < endIndex; j++) {
                // 获取数据信息
                nextData = dataList.get(j + 1);
                // 构造训练向量
                int c = endIndex - j - 1;

                boolean riseFlg = false;
                boolean declineFlg = false;
                long maxVal = 0l;
                long avgVal = 0l;
                long minVal = curData[0].getAvg();
                for (int k = 1; k <= BASE_MIN; k++) {
                    PriceData jd = dataList.get(j + k)[0];
                    try {
                        if (jd.getAvg() > maxVal) {
                            maxVal = jd.getAvg();
                        }
                        if (jd.getAvg() < minVal) {
                            minVal = jd.getAvg();
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        break;
                    }
                    avgVal += jd.getAvg();
                }
                avgVal /= BASE_MIN;

                long S = 0l;
                for (int k = 1; k <= BASE_MIN; k++) {
                    try {
                        PriceData jd = dataList.get(j + k)[0];
                        S += ((jd.getAvg() - avgVal) * (jd.getAvg() - avgVal));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        break;
                    }
                }
                S /= BASE_MIN;
                S = (long)Math.sqrt(S);

                if (maxVal - curData[0].getAvg() > TGRESHOLD && avgVal > (curData[0].getAvg() + (TGRESHOLD >> 1))) {
                    riseFlg = true;
                }
                if (curData[0].getAvg() - minVal > TGRESHOLD && avgVal < (curData[0].getAvg() - (TGRESHOLD >> 1))) {
                    declineFlg = true;
                }
                double val = 0;
                if (S >= (Math.abs(avgVal - curData[0].getAvg()) << 1)){
                	val = 0.875;
                }else if (riseFlg) {
                    val = 0.125;
                } else if (declineFlg) {
                    val = 0.375;
                } else if (S <= (TGRESHOLD >> 1)) {
                    val = 0.625;
                } else {
                    val = 0.875;
                }

                int d = -1;
                for (PriceData pd : curData) {
                    input.putScalar(new int[] { i, ++d, c }, nomolize(pd.openPrice,maxNum[d]));
                    input.putScalar(new int[] { i, ++d, c }, nomolize(pd.closePrice,maxNum[d]));
                    input.putScalar(new int[] { i, ++d, c }, nomolize(pd.highPrice,maxNum[d]));
                    input.putScalar(new int[] { i, ++d, c }, nomolize(pd.lowPrice,maxNum[d]));
                }

                // 构造label向量
                label.putScalar(new int[] { i, 0, c }, val);
//                label.putScalar(new int[] { i, 0, c }, nomolize(avgVal,maxNum[0]));
                curData = nextData;
            }
            if (dataRecord.size() <= 0) {
                break;
            }
        }

        return new DataSet(input, label);
    }

    private double nomolize(double a, double max){
    	double val = a / max*a / max*a / max *2-1;
    	return val;
    }
    
    public double antiNomolize(double a, double max){
    	double val = Math.pow((a + 1)/2, 1.0/3.0)*max;
    	return val;
    }
    
    public int batch() {
        return batchNum;
    }

    public int cursor() {
        return totalExamples() - dataRecord.size();
    }

    public int numExamples() {
        return totalExamples();
    }

    public void setPreProcessor(DataSetPreProcessor preProcessor) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int totalExamples() {
        return (dataList.size() - BASE_MIN)  / exampleLength;
    }

    public int inputColumns() {
        return dataList.size();
    }

    public int totalOutcomes() {
        return 1;
    }

    @Override
    public List<String> getLabels() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean asyncSupported() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public DataSetPreProcessor getPreProcessor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean resetSupported() {
        // TODO Auto-generated method stub
        return false;
    }
    

    public INDArray list2NDArray(List<PriceData[]> currentPdsList){
    	INDArray input = Nd4j.create(new int[] { 1, VECTOR_SIZE, currentPdsList.size() }, 'f');
    	int i=-1;
    	for(PriceData[] pds:currentPdsList){
    		int j=-1;
    		i++;
    		for(PriceData pd:pds){
    			input.putScalar(new int[] { 1, ++j, i }, nomolize(pd.openPrice,maxNum[j]));
                input.putScalar(new int[] { 1, ++j, i }, nomolize(pd.closePrice,maxNum[j]));
                input.putScalar(new int[] { 1, ++j, i }, nomolize(pd.highPrice,maxNum[j]));
                input.putScalar(new int[] { 1, ++j, i }, nomolize(pd.lowPrice,maxNum[j]));
    		}
    	}
    	return input;
    }
}