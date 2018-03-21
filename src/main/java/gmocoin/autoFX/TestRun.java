package gmocoin.autoFX;

import java.text.*;
import java.util.*;

import org.deeplearning4j.api.storage.*;
import org.deeplearning4j.nn.api.*;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.*;
import org.deeplearning4j.nn.weights.*;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.*;
import org.nd4j.linalg.activations.*;
import org.nd4j.linalg.api.ndarray.*;
import org.nd4j.linalg.dataset.*;
import org.nd4j.linalg.dataset.api.preprocessor.*;
import org.nd4j.linalg.factory.*;
import org.nd4j.linalg.indexing.*;
import org.nd4j.linalg.lossfunctions.*;

import gmocoin.autoFX.Collabo.csv.*;
import gmocoin.autoFX.strategy.ai.nlp.StockDataIterator;
import gmocoin.autoFX.strategy.common.*;

public class TestRun {
    /**
     * 标签：0多，1空，2平，3震荡
     */
    private static final String SP = System.lineSeparator();
    private static final String FILE_1_NAME = "./DATA/nlpData1.csv";
    private static final String FILE_I_NAME = "./DATA/seqs/nlpData%d.csv";
    private static final String FILE_TEST_NAME = "./DATA/nlpDataTest.csv";
    private static final int BASE_MIN = 20;
    private static final int TGRESHOLD = 10000;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH);
    private static StockDataIterator iterClassification;
    public static void main(String[] args) throws Exception {

        // int[] exampleLengths = makeTrainData(0);
        // int[] exampleLengths = new int[] { 9226, 0 };
        int numPossibleLabels = 4;
        int numEpochs = 2;
        int miniBatchSize = 1;
        int generateSamplesEveryNMinibatches = numEpochs;
        boolean regression = true;

        // CSVRecordReader reader = new CSVRecordReader(0, ",");
        // reader.initialize(new FileSplit(new File(System.getProperty("user.dir") + "/DATA/seqs/nlpData0.csv")));
        // RecordReaderDataSetIterator iterClassification = new RecordReaderDataSetIterator(reader, miniBatchSize, 0,
        // numPossibleLabels);
        /****************************************************/
        // CSVSequenceRecordReader reader = new CSVSequenceRecordReader(0, ",");
        // reader.initialize(new NumberedFileInputSplit(System.getProperty("user.dir") + "/DATA/seqs/nlpData%d.csv", 0,
        // exampleLengths[0]));
        // SequenceRecordReaderDataSetIterator iterClassification = new SequenceRecordReaderDataSetIterator(reader,
        // miniBatchSize, numPossibleLabels, 0);
        /*************************************************/
        iterClassification = new StockDataIterator();
        iterClassification.loadData(miniBatchSize, 100);
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
//         MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(12345).iterations(2)
//         .activation(Activation.TANH).weightInit(WeightInit.XAVIER).learningRate(0.001).regularization(true)
//         .l2(1e-4).list()
//         .layer(0, new GravesLSTM.Builder().nIn(20).nOut(500).build())
//         .layer(1, new DenseLayer.Builder().nIn(500).nOut(150).build())
//         .layer(2,
//         new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
//         .activation(Activation.RRELU).nIn(150).nOut(1).build())
//         .backpropType(BackpropType.Standard).tBPTTForwardLength(BASE_MIN << 6)
//         .tBPTTBackwardLength(BASE_MIN << 6).pretrain(false).backprop(true).build();
        /********************************************************************************/
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(1).learningRate(0.01)
                .activation(Activation.TANH)
//                .rmsDecay(0.5)
                .seed(12345).regularization(true).l2(0.1).weightInit(WeightInit.XAVIER)
                .updater(Updater.RMSPROP).list()
                .layer(0, new GravesLSTM.Builder().nIn(20).nOut(100).build())
                .layer(1, new GravesLSTM.Builder().nIn(100).nOut(1000).build())
                .layer(2, new GravesLSTM.Builder().nIn(1000).nOut(20).build())
                .layer(3, new GravesLSTM.Builder().nIn(20).nOut(2).build())
                .layer(4, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE).activation(Activation.IDENTITY)
                        .nIn(2).nOut(1).build())
                .backpropType(BackpropType.Standard)
                .pretrain(false).backprop(true).build();
        MultiLayerNetwork net = new MultiLayerNetwork(conf);

        net.init();
//        UIServer uiServer = UIServer.getInstance();
//        // 设置网络信息（随时间变化的梯度、分值等）的存储位置。这里将其存储于内存。
//        StatsStorage statsStorage = new InMemoryStatsStorage(); // 或者： new FileStatsStorage(File)，用于后续的保存和载入

        // 将StatsStorage实例连接至用户界面，让StatsStorage的内容能够被可视化

//        uiServer.attach(statsStorage);

//        net.setListeners(new StatsListener(statsStorage));
         net.setListeners(new ScoreIterationListener(1));

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
        DataNormalization normalizer = new NormalizerStandardize();
        int miniBatchNumber = 0;
        int trainsize = 8000;
        for (int i = 0; i < numEpochs; i++) {
            while (iterClassification.hasNext()) {
                DataSet ds = iterClassification.next();
                // normalizer.fit(ds); // Collect the statistics (mean/stdev) from the training data. This does not modify the input data
                // // // 标准化数据，应该是要转为0-1的float？？？
                // normalizer.transform(ds);
                // ds.setFeatures(pds2NdArrayRow(ds));
                // ds.setLabels(pds2NdArrayLabel(ds));
                net.fit(ds);
                if (++miniBatchNumber % generateSamplesEveryNMinibatches == 0) {

                    System.out.println("--------------------");

                    System.out.println("Completed "
                            + miniBatchNumber *100 / numEpochs / iterClassification.totalExamples()
                            + "% minibatches of size "
                            + miniBatchSize
                            + "x"
                            + iterClassification.totalExamples()
                            + " characters");
                    //
                    // System.out.println("Sampling characters from network given initialization \""
                    // + (generationInitialization == null ? "" : generationInitialization)
                    // + "\"");
                    //
                    // String[] samples = sampleCharactersFromNetwork(generationInitialization, net, iter, rng,
                    // nCharactersToSample, nSamplesToGenerate);
                    //
                    // for (int j = 0; j < samples.length; j++) {
                    // System.out.println("----- Sample " + j + " -----");
                    // System.out.println(samples[j]);
                    // System.out.println();
                    // }
                }
            }
            iterClassification.reset(); // Reset iterator for another epoch
        }
        // reader.initialize(new NumberedFileInputSplit("E:/workspace/gitAutoFX/ai/work/DATA/nlpData%d.csv", 1, 1));

        // net.rnnClearPreviousState();
        int j = 0;
//        List<DataSet> sdList = new ArrayList<>();
//        INDArray sfm = Nd4j.create(new int[] { 1, 20, trainsize + 1 }, 'f');
//        INDArray sfm1 = Nd4j.create(new int[] { 20, trainsize + 1 }, 'f');

        iterClassification.reset();
        while (iterClassification.hasNext()) {
            StringBuffer result = new StringBuffer();
            // net.rnnClearPreviousState();
            DataSet testData = iterClassification.next();
//            net.fit(testData);
            INDArray outarry = null;
            // boolean trainFlg = true;
            // if (j++ < trainsize) {
            // // sfm1.putColumn(j - 1, testData.getFeatureMatrix());
            // trainFlg = false;
            // continue;
            // } else if (j < trainsize + BASE_MIN) {
            // sdList.add(testData);
            // trainFlg = false;
            // }
            // if (j == trainsize) {
            // sfm.putRow(0, sfm1);
            // outarry = net.rnnTimeStep(sfm);
            // } else {
            // List<INDArray> list = getTestData();
            // for (INDArray arr : list) {
            // normalizer.fit(testData);
            // normalizer.transform(testData);
//             outarry = net.rnnTimeStep(testData.getFeatureMatrix());
            outarry = net.output(testData.getFeatureMatrix());
            // }
            // sdList.add(testData);
            // if (trainFlg) {
            // DataSet ds = sdList.get(0);
            // // ds.setFeatures(pds2NdArrayRow(ds));
            // // ds.setLabels(pds2NdArrayLabel(ds));
            // // net.fit(ds);
            // sdList.remove(0);
            // }
            List<Integer> val;
            // outarry = outarry.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(outarry.size(2) - 1));
            val = toPossableLabel(outarry);
            result.append(j).append(",").append(val).append(SP);
            result.append(j++).append(",").append(toPossableLabel(testData.getLabels())).append(SP).append(SP);
            System.out.println(result.toString());
            // }
        }

//        System.out.println(result.toString());
        // eval.eval(testData.getLabels(), output);
        System.out.println("-------------------------------------------------------");
        // System.out.println(eval.stats());
        System.out.println("-------------------------------------------------------");

        System.out.println("\n\nExample complete");

    }

    @SuppressWarnings("boxing")
    private static List<Integer> toPossableLabel(INDArray outarry) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < outarry.size(2); i++) {
            int val = 0;
            double a = outarry.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(i)).getDouble(0);
//            val = (int) iterClassification.antiNomolize(a,iterClassification.getMaxArr()[0]);
            if (a < 0.25) {
                val = 0;
            } else if (a < 0.5) {
                val = 1;
            } else if (a < 0.75) {
                val = 2;
            } else {
                val = 3;
            }
            result.add(val);
        }
        return result;
    }

    private static int getVal(INDArray outarry) {
        int val = -1;
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

    public static INDArray pds2NdArrayRow(DataSet sd) {
        INDArray sfm = Nd4j.create(new int[] { 1, 20, 1 }, 'f');
        INDArray sfm1 = Nd4j.create(new int[] { 20, 1 }, 'f');
        sfm1.putColumn(0, sd.getFeatureMatrix());
        sfm.putRow(0, sfm1);
        return sfm;
    }

    public static INDArray pds2NdArrayLabel(DataSet sd) {
        INDArray sfm = Nd4j.create(new int[] { 1, 4, 1 }, 'f');
        INDArray sfm1 = Nd4j.create(new int[] { 4, 1 }, 'f');
        sfm1.putColumn(0, sd.getLabels());
        sfm.putRow(0, sfm1);
        return sfm;
    }

    public static List<INDArray> getTestData() {
        List<INDArray> list = new ArrayList<>();
        RWCsv rwCsv = new RWCsv();
        List<PriceData> btcList = rwCsv.rCsv2BTCPriceData();
        List<PriceData> ethList = rwCsv.rCsv2ETHPriceData();
        List<PriceData> bchList = rwCsv.rCsv2BCHPriceData();
        List<PriceData> ltcList = rwCsv.rCsv2LTCPriceData();
        List<PriceData> xrpList = rwCsv.rCsv2XRPPriceData();
        for (PriceData btc : btcList) {
            int i = 0;
            INDArray arr = Nd4j.create(new int[] { 1, 20, 1 }, 'f');
            PriceData eth = findPriceData(ethList, btc.datetime);
            PriceData bch = findPriceData(bchList, btc.datetime);
            PriceData ltc = findPriceData(ltcList, btc.datetime);
            PriceData xrp = findPriceData(xrpList, btc.datetime);
            arr.putScalar(new int[] { 0, i++, 0 }, btc.openPrice);
            arr.putScalar(new int[] { 0, i++, 0 }, btc.closePrice);
            arr.putScalar(new int[] { 0, i++, 0 }, btc.highPrice);
            arr.putScalar(new int[] { 0, i++, 0 }, btc.lowPrice);

            arr.putScalar(new int[] { 0, i++, 0 }, eth.openPrice);
            arr.putScalar(new int[] { 0, i++, 0 }, eth.closePrice);
            arr.putScalar(new int[] { 0, i++, 0 }, eth.highPrice);
            arr.putScalar(new int[] { 0, i++, 0 }, eth.lowPrice);

            arr.putScalar(new int[] { 0, i++, 0 }, bch.openPrice);
            arr.putScalar(new int[] { 0, i++, 0 }, bch.closePrice);
            arr.putScalar(new int[] { 0, i++, 0 }, bch.highPrice);
            arr.putScalar(new int[] { 0, i++, 0 }, bch.lowPrice);

            arr.putScalar(new int[] { 0, i++, 0 }, ltc.openPrice);
            arr.putScalar(new int[] { 0, i++, 0 }, ltc.closePrice);
            arr.putScalar(new int[] { 0, i++, 0 }, ltc.highPrice);
            arr.putScalar(new int[] { 0, i++, 0 }, ltc.lowPrice);

            arr.putScalar(new int[] { 0, i++, 0 }, xrp.openPrice);
            arr.putScalar(new int[] { 0, i++, 0 }, xrp.closePrice);
            arr.putScalar(new int[] { 0, i++, 0 }, xrp.highPrice);
            arr.putScalar(new int[] { 0, i++, 0 }, xrp.lowPrice);
            list.add(arr);
        }
        return list;
    }

    public static int[] makeTrainData(double testPecent) {
        int trainSize = 0;
        int testSize = 0;
        // StringBuffer sb = new StringBuffer();
        // StringBuffer testsb = new StringBuffer();
        RWCsv rwCsv = new RWCsv();
        List<PriceData> btcList = rwCsv.rCsv2BTCPriceData();
        List<PriceData> ethList = rwCsv.rCsv2ETHPriceData();
        List<PriceData> bchList = rwCsv.rCsv2BCHPriceData();
        List<PriceData> ltcList = rwCsv.rCsv2LTCPriceData();
        List<PriceData> xrpList = rwCsv.rCsv2XRPPriceData();

        try {
            trainSize = makeCsv(rwCsv, btcList, ethList, bchList, ltcList, xrpList);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // rwCsv.deleteCsv(FILE_1_NAME);
        // rwCsv.deleteCsv(FILE_TEST_NAME);
        // rwCsv.wFile(sb, FILE_1_NAME);
        // rwCsv.wFile(testsb, FILE_TEST_NAME);
        return new int[] { --trainSize, testSize };
    }

    @SuppressWarnings("boxing")
    private static int makeCsv(RWCsv rwCsv, List<PriceData> btcList, List<PriceData> ethList, List<PriceData> bchList,
            List<PriceData> ltcList, List<PriceData> xrpList) throws NumberFormatException, ParseException {

        int maxSize = (ethList.size() - BASE_MIN);
        int fileCount = maxSize;
        for (int k = 0; k < fileCount; k++) {
            int s = maxSize / fileCount * k;
            int e = maxSize / fileCount * (k + 1);
            if (e > maxSize) {
                e = maxSize;
            }
            if (s >= e) {
                fileCount = k + 1;
                break;
            }
            StringBuffer sb = new StringBuffer();
            for (int i = s; i < e; i++) {
                StringBuffer csvsb = sb;
                PriceData currenteth = ethList.get(i);
                PriceData currentbtc = findPriceData(btcList, currenteth.datetime);
                PriceData currentbch = findPriceData(bchList, currenteth.datetime);
                PriceData currentltc = findPriceData(ltcList, currenteth.datetime);
                PriceData currentxrp = findPriceData(xrpList, currenteth.datetime);
                if (currenteth == null || currentbch == null || currentltc == null || currentxrp == null) {
                    continue;
                }

                boolean riseFlg = false;
                boolean declineFlg = false;
                long maxVal = 0l;
                long avgVal = 0l;
                long minVal = currentbtc.getAvg();
                for (int j = 1; j <= BASE_MIN; j++) {
                    String dt = sdf.format(sdf.parse(String.valueOf(Long.valueOf(currenteth.datetime) + j)));
                    PriceData jd = findPriceData(btcList, dt);
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
                for (int j = 1; j <= BASE_MIN; j++) {
                    try {
                        String dt = sdf.format(sdf.parse(String.valueOf(Long.valueOf(currenteth.datetime) + j)));
                        PriceData jd = findPriceData(btcList, dt);
                        S += ((jd.getAvg() - avgVal) * (jd.getAvg() - avgVal));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        break;
                    }
                }
                S /= BASE_MIN;
                S = (long)Math.sqrt(S);

                if (maxVal - currentbtc.getAvg() > TGRESHOLD && avgVal > (currentbtc.getAvg() + (TGRESHOLD >> 1))) {
                    riseFlg = true;
                }
                if (currentbtc.getAvg() - minVal > TGRESHOLD && avgVal < (currentbtc.getAvg() - (TGRESHOLD >> 1))) {
                    declineFlg = true;
                }
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
                // String fileName = String.format(FILE_I_NAME, trainSize++);
                // if (k == 0) {
                // rwCsv.deleteCsv(fileName);
                // }
                // rwCsv.wFile(sb, fileName);
            }

            String fileName = String.format(FILE_I_NAME, k);
            rwCsv.deleteCsv(fileName);
            rwCsv.wFile(sb, fileName);
        }

        return fileCount;
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
