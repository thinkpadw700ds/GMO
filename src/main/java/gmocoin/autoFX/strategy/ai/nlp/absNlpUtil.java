package gmocoin.autoFX.strategy.ai.nlp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

import gmocoin.autoFX.Collabo.csv.RWCsv;
import gmocoin.autoFX.control.Control;
import gmocoin.autoFX.strategy.common.PriceData;

public abstract class absNlpUtil {

	/**
	 * 标签：0多，1空，2平，3震荡
	 */
	protected static final String SP = System.lineSeparator();
	protected static final String FILE_I_NAME = "./DATA/seqs/nlpData%d.csv";
	protected static final String FILE_TEST_NAME = "./DATA/nlpDataTest.csv";
	protected static final int BASE_MIN;
	protected static final int TGRESHOLD;
	protected static Control control;
	protected static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH);
	static {
		control = Control.getInstance();
		BASE_MIN = Integer.valueOf(Control.getInstance().getProperty("DL_BASE_MIN")).intValue();
		TGRESHOLD = Integer.valueOf(Control.getInstance().getProperty("DL_TGRESHOLD")).intValue();
	}

	private String startDateTime = null;

	protected int makeTrainData() {
		int trainSize = 0;
		// int testSize = 0;
		// StringBuffer sb = new StringBuffer();
		// StringBuffer testsb = new StringBuffer();
		RWCsv rwCsv = new RWCsv();
		List<PriceData> btcList = rwCsv.rCsv2BTCPriceData();
		List<PriceData> ethList = rwCsv.rCsv2ETHPriceData();
		List<PriceData> bchList = rwCsv.rCsv2BCHPriceData();
		List<PriceData> ltcList = rwCsv.rCsv2LTCPriceData();
		List<PriceData> xrpList = rwCsv.rCsv2XRPPriceData();

		// int maxTestSize = 0;
		// if (testPecent > 0.0) {
		// maxTestSize = (int)(testPecent * ethList.size());
		// }
		String lastDateTime = null;
		for (int i = 0; i < (btcList.size() - BASE_MIN); i++) {
			StringBuffer csvsb = new StringBuffer();
			PriceData currentbtc = btcList.get(i);
			PriceData currenteth = findPriceData(ethList, currentbtc.datetime);
			PriceData currentbch = findPriceData(bchList, currentbtc.datetime);
			PriceData currentltc = findPriceData(ltcList, currentbtc.datetime);
			PriceData currentxrp = findPriceData(xrpList, currentbtc.datetime);
			if (currenteth == null || currentbch == null || currentltc == null || currentxrp == null) {
				continue;
			}
			lastDateTime = currentbtc.datetime;
			boolean riseFlg = false;
			boolean declineFlg = false;
			long maxVal = 0l;
			long avgVal = 0l;
			long minVal = currentbtc.getAvg();
			for (int j = 1; j <= BASE_MIN; j++) {
				PriceData jd = btcList.get(i + j);
				if (jd.getAvg() > maxVal) {
					maxVal = jd.getAvg();
				}
				if (jd.getAvg() < minVal) {
					minVal = jd.getAvg();
				}
				avgVal += jd.getAvg();
			}
			avgVal /= BASE_MIN;

			long S = 0l;
			for (int j = 1; j <= BASE_MIN; j++) {
				PriceData jd = btcList.get(i + j);
				S += ((jd.getAvg() - avgVal) * (jd.getAvg() - avgVal));
			}
			S /= BASE_MIN;
			S = (long) Math.sqrt(S);

			if (maxVal - currentbtc.getAvg() > TGRESHOLD && avgVal > (currentbtc.getAvg() + (TGRESHOLD >> 1))) {
				riseFlg = true;
			}
			if (currentbtc.getAvg() - minVal > TGRESHOLD && avgVal < (currentbtc.getAvg() - (TGRESHOLD >> 1))) {
				declineFlg = true;
			}
			// if (avgVal > currentbtc.getAvg()) {
			// riseFlg = true;
			// }
			// if (avgVal < currentbtc.getAvg()) {
			// declineFlg = true;
			// }

			// if ((btcList.size() - BASE_MIN - i) <= maxTestSize) {
			// csvsb = testsb;
			// testSize++;
			// } else {
			// trainSize++;
			// }
			if (riseFlg) {
				csvsb.append(0);
			} else if (declineFlg) {
				csvsb.append(1);
			} else if (S <= (TGRESHOLD >> 1)) {
				csvsb.append(2);
			} else {
				csvsb.append(3);
			}
			csvsb.append(",").append(currentbtc.toCsv()).append(",").append(currenteth.toCsv()).append(",")
					.append(currentbch.toCsv()).append(",").append(currentltc.toCsv()).append(",")
					.append(currentxrp.toCsv()).append(SP);
			@SuppressWarnings("boxing")
			String fileName = String.format(FILE_I_NAME, trainSize++);
			rwCsv.deleteCsv(fileName);
			rwCsv.wFile(csvsb, fileName);
		}
		this.startDateTime = lastDateTime;
		// rwCsv.deleteCsv(FILE_1_NAME);
		// rwCsv.deleteCsv(FILE_TEST_NAME);
		// rwCsv.wFile(sb, FILE_1_NAME);
		// rwCsv.wFile(testsb, FILE_TEST_NAME);
		return --trainSize;
	}

	protected static PriceData findPriceData(List<PriceData> list, String datatime) {
		if (list == null) {
			return null;
		}
		Optional<PriceData> opd = list.stream().filter(p -> p.datetime.equals(datatime)).findFirst();
		if (opd.isPresent()) {
			return opd.get();
		}
		return null;
	}

	public INDArray pds2NDArray(List<PriceData[]> currentPdsList) {
		INDArray sfm = Nd4j.create(new int[] { 1, 20, currentPdsList.size() }, 'f');
		INDArray sfm1 = Nd4j.create(new int[] { 20, currentPdsList.size() }, 'f');
		int i = 0;
		for (PriceData[] currentPds : currentPdsList) {
			INDArray featureRow = pds2NdArrayRow(currentPds);
			sfm1.putColumn(i++, featureRow);
		}
		sfm.putRow(0, sfm1);
		return sfm;
	}

	public INDArray pds2NdArrayRow(PriceData[] currentPds) {
		INDArray sfm = Nd4j.create(new int[] { 1, 20, 1 }, 'f');
		int i = 0;
		for (PriceData pd : currentPds) {
			sfm.putScalar(new int[] { 1, i++, 1 }, pd.openPrice);
			sfm.putScalar(new int[] { 1, i++, 1 }, pd.closePrice);
			sfm.putScalar(new int[] { 1, i++, 1 }, pd.highPrice);
			sfm.putScalar(new int[] { 1, i++, 1 }, pd.lowPrice);
		}
		return sfm;
	}

	@Deprecated
	public Date getStartDateTime() {
		try {
			while (this.startDateTime == null) {
				Thread.sleep(1000);
			}
			return sdf.parse(this.startDateTime);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	@Deprecated
	public long getStartDateTimeLong() {
		while (this.startDateTime == null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return Long.valueOf(this.startDateTime).longValue();
	}

	@SuppressWarnings("boxing")
	protected int toPossableLabel(INDArray outarry) {
		int val = 0;
		double a = outarry.getDouble(0);
		if (a < 0.25) {
			val = 0;
		} else if (a < 0.5) {
			val = 1;
		} else if (a < 0.75) {
			val = 2;
		} else {
			val = 3;
		}
		return val;
	}
}
