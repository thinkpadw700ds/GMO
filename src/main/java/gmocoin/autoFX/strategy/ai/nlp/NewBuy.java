package gmocoin.autoFX.strategy.ai.nlp;

import java.text.*;
import java.util.*;

import org.datavec.api.records.reader.impl.csv.*;
import org.datavec.api.split.*;
import org.deeplearning4j.api.storage.*;
import org.deeplearning4j.datasets.datavec.*;
import org.deeplearning4j.nn.api.*;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.*;
import org.deeplearning4j.nn.weights.*;
import org.deeplearning4j.ui.api.*;
import org.deeplearning4j.ui.stats.*;
import org.deeplearning4j.ui.storage.*;
import org.nd4j.linalg.activations.*;
import org.nd4j.linalg.api.ndarray.*;
import org.nd4j.linalg.dataset.api.*;
import org.nd4j.linalg.factory.*;
import org.nd4j.linalg.indexing.*;
import org.nd4j.linalg.lossfunctions.*;
import org.slf4j.*;

import gmocoin.autoFX.Collabo.csv.*;
import gmocoin.autoFX.control.*;
import gmocoin.autoFX.strategy.ai.*;
import gmocoin.autoFX.strategy.common.*;

public class NewBuy implements INewNPL {

    // private ComputationGraphConfiguration conf;
    /**
     * 标签：0多，1空，2平，3震荡
     */
    private static final String SP = System.lineSeparator();
    private static final String FILE_I_NAME = "./DATA/seqs/nlpData%d.csv";
    private static final String FILE_TEST_NAME = "./DATA/nlpDataTest.csv";
    private static final int BASE_MIN;
    private static final int TGRESHOLD;
    private static Control control;
    static {
        control = Control.getInstance();
        BASE_MIN = Integer.valueOf(Control.getInstance().getProperty("DL_BASE_MIN")).intValue();
        TGRESHOLD = Integer.valueOf(Control.getInstance().getProperty("DL_TGRESHOLD")).intValue();
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH);
    private boolean isTraining = false;
    private MultiLayerConfiguration conf;
    private MultiLayerNetwork net;
    private String startDateTime = null;
    private List<DataSet> fitDSList;
    private SequenceRecordReaderDataSetIterator allIterClassification;
    private int seqSize;

    public NewBuy() {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.fitDSList = new ArrayList<DataSet>();
        try {
            // reader.initialize(new NumberedFileInputSplit("E:/workspace/gitAutoFX/ai/work/DATA/nlpData%d.csv", 1, 1));

            // DataSet allData = iterClassification.next();
            // SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.7); // Use 70% of data for training
            //
            // DataSet trainingData = testAndTrain.getTrain();
            // DataSet testData = testAndTrain.getTest();

            // Let's view the example metadata in the training and test sets:
            // List<RecordMetaData> trainMetaData = trainingData.getExampleMetaData(RecordMetaData.class);
            // List<RecordMetaData> testMetaData = testData.getExampleMetaData(RecordMetaData.class);

            // MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            // .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(100).learningRate(0.1)
            // .seed(12345).regularization(true).l2(0.001).weightInit(WeightInit.XAVIER).updater(Updater.RMSPROP)
            // .list()
            // .layer(0,
            // new GravesLSTM.Builder().nIn(iterClassification.inputColumns()).nOut(20)
            // .activation(Activation.TANH).build())
            // .layer(1, new GravesLSTM.Builder().nIn(20).nOut(100).activation(Activation.TANH).build())
            // .layer(2,
            // new RnnOutputLayer.Builder(LossFunction.MCXENT).activation(Activation.SOFTMAX) // MCXENT + softmax for classification
            // .nIn(100).nOut(4).build())
            // .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(BASE_MIN << 1)
            // .tBPTTBackwardLength(BASE_MIN << 1).pretrain(false).backprop(true).build();
            /********************************************************************************/
            // MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(12345).iterations(20)
            // .activation(Activation.TANH).weightInit(WeightInit.XAVIER).learningRate(0.001).regularization(true)
            // .l2(1e-4).list()
            // .layer(0, new GravesLSTM.Builder().nIn(iterClassification.inputColumns()).nOut(500).build())
            // .layer(1, new DenseLayer.Builder().nIn(500).nOut(150).build())
            // .layer(2,
            // new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
            // .activation(Activation.SOFTMAX).nIn(150).nOut(numPossibleLabels).build())
            // .backpropType(BackpropType.Standard).tBPTTForwardLength(BASE_MIN << 6)
            // .tBPTTBackwardLength(BASE_MIN << 6).pretrain(true).backprop(true).build();
            /********************************************************************************/
            // this.conf = new NeuralNetConfiguration.Builder().seed(12345).iterations(20).learningRate(0.01)
            // .updater(Updater.ADAM) // To configure: .updater(Adam.builder().beta1(0.9).beta2(0.999).build())
            // .regularization(true).l2(1e-5).weightInit(WeightInit.XAVIER)
            // .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
            // .gradientNormalizationThreshold(1.0)
            // .trainingWorkspaceMode(WorkspaceMode.SEPARATE).inferenceWorkspaceMode(WorkspaceMode.SEPARATE) // https://deeplearning4j.org/workspaces
            // .list()
            // .layer(0, new GravesLSTM.Builder().nIn(20).nOut(200).activation(Activation.TANH).build())
            // .layer(1, new GravesLSTM.Builder().nIn(200).nOut(200).activation(Activation.TANH).build())
            // .layer(2, new RnnOutputLayer.Builder().activation(Activation.SOFTMAX)
            // .lossFunction(LossFunctions.LossFunction.MCXENT).nIn(200).nOut(4).build())
            // .pretrain(false).backprop(true).tBPTTForwardLength(BASE_MIN << 8).tBPTTBackwardLength(0)
            // .build();
            this.conf = new NeuralNetConfiguration.Builder()
                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
                    .learningRate(0.01).rmsDecay(0.5).seed(12345).regularization(true).l2(0.001)
                    .weightInit(WeightInit.XAVIER).updater(Updater.RMSPROP).list()
                    .layer(0, new GravesLSTM.Builder().nIn(20).nOut(50).activation(Activation.TANH).build())
                    .layer(1, new GravesLSTM.Builder().nIn(50).nOut(50).activation(Activation.TANH).build())
                    .layer(2, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE).activation(Activation.IDENTITY)
                            .nIn(50).nOut(4).build())
                    .pretrain(false).backprop(true).build();
            this.net = new MultiLayerNetwork(conf);

            this.net.init();
            // this.net.setListeners(new ScoreIterationListener(1));
            UIServer uiServer = UIServer.getInstance();
            // 设置网络信息（随时间变化的梯度、分值等）的存储位置。这里将其存储于内存。
            StatsStorage statsStorage = new InMemoryStatsStorage(); // 或者： new FileStatsStorage(File)，用于后续的保存和载入
            // 将StatsStorage实例连接至用户界面，让StatsStorage的内容能够被可视化
            uiServer.attach(statsStorage);
            this.net.setListeners(new StatsListener(statsStorage));

            // Print the number of parameters in the network (and for each layer)

            Layer[] layers = net.getLayers();
            int totalNumParams = 0;
            for (int i = 0; i < layers.length; i++) {
                int nParams = layers[i].numParams();
                System.out.println("Number of parameters in layer " + i + ": " + nParams);
                totalNumParams += nParams;
            }
            System.out.println("Total number of network parameters: " + totalNumParams);

            // Do training, and then generate and print samples from network

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int makeTrainData() {
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
            S = (long)Math.sqrt(S);

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

    @Override
    public void training() {
        System.out.println("---------training...----------");
        this.isTraining = true;
        int exampleLengths = makeTrainData();
        this.seqSize = exampleLengths + 1;
        int miniBatchSize = 1;
        int numPossibleLabels = 4;
        int numEpochs = 100;
        CSVSequenceRecordReader reader = new CSVSequenceRecordReader(0, ",");
        try {
            reader.initialize(new NumberedFileInputSplit(System.getProperty("user.dir") + "/DATA/seqs/nlpData%d.csv", 0,
                    exampleLengths));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        this.allIterClassification = new SequenceRecordReaderDataSetIterator(reader, miniBatchSize, numPossibleLabels,
                0);

        int j = 0;
        for (int i = 0; i < numEpochs; i++) {
            this.net.rnnClearPreviousState();
            while (this.allIterClassification.hasNext()) {
                DataSet ds = null;
                try {
                    ds = this.allIterClassification.next();
                    this.net.fit(ds);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (j++ % 50 == 0) {
                    System.out.println(j * miniBatchSize * 100 / exampleLengths / numEpochs + "%" + " completed");
                }
            }
            this.allIterClassification.reset(); // Reset iterator for another epoch
        }
        System.out.println("---------train end----------");
        this.isTraining = false;
    }

    public boolean fit(DataSet ds) {
        this.fitDSList.add(ds);
        if (!this.isTraining) {
            this.isTraining = true;
            while (this.fitDSList.size() > 0) {
                // this.net.fit(this.fitDSList.get(0));
                this.fitDSList.remove(0);
            }
            this.isTraining = false;
        }
        return false;
    }

    /**
     * currentPds[btc,eth,bch,ltc,xrp]
     * 
     * @param currentPds
     * @return
     */
    public int outPut(List<PriceData[]> currentPdsList) {
        if (this.net == null || this.isTraining) {
            return -1;
        }
        INDArray featureMatrix = null;
        INDArray outarry = null;
        featureMatrix = pds2NDArray(currentPdsList);
        outarry = net.rnnTimeStep(featureMatrix);
        int val = -1;
        outarry = outarry.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(outarry.size(2) - 1));
        double a = outarry.getDouble(0);
        double b = outarry.getDouble(1);
        double c = outarry.getDouble(2);
        double d = outarry.getDouble(3);
        double max = a;
        if (b > max) {
            max = b;
        }
        if (c > max) {
            max = c;
        }
        if (d > max) {
            max = d;
        }
        if (a == max) {
            val = 0;
        } else if (b == max) {
            val = 1;
        } else if (c == max) {
            val = 2;
        } else {
            val = 3;
        }
        return val;
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
}
