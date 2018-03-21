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
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.*;
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

public class NewBuy extends absNlpUtil implements INewNPL {

    // private ComputationGraphConfiguration conf;
    private boolean isTraining = false;
    private MultiLayerConfiguration conf;
    private MultiLayerNetwork net;
    private List<DataSet> fitDSList;
    private StockDataIterator allIterClassification;
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
                    .learningRate(0.001).rmsDecay(0.5).seed(12345).regularization(true).l2(0.001)
                    .weightInit(WeightInit.XAVIER).updater(Updater.RMSPROP).list()
                    .layer(0, new GravesLSTM.Builder().nIn(20).nOut(50).activation(Activation.TANH).build())
                    .layer(1, new GravesLSTM.Builder().nIn(50).nOut(50).activation(Activation.TANH).build())
                    .layer(2, new GravesLSTM.Builder().nIn(50).nOut(10).activation(Activation.TANH).build())
                    .layer(3, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE).activation(Activation.IDENTITY)
                            .nIn(10).nOut(1).build())
                    .pretrain(false).backprop(true).build();
            this.net = new MultiLayerNetwork(conf);

            this.net.init();
            // this.net.setListeners(new ScoreIterationListener(1));
//            UIServer uiServer = UIServer.getInstance();
//            // 设置网络信息（随时间变化的梯度、分值等）的存储位置。这里将其存储于内存。
//            StatsStorage statsStorage = new InMemoryStatsStorage(); // 或者： new FileStatsStorage(File)，用于后续的保存和载入
            // 将StatsStorage实例连接至用户界面，让StatsStorage的内容能够被可视化
//            uiServer.attach(statsStorage);
//            this.net.setListeners(new StatsListener(statsStorage));
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    

    

    @Override
    public void training() {
        System.out.println("---------training...----------");
        this.isTraining = true;
//        int exampleLengths = makeTrainData();
//        this.seqSize = exampleLengths + 1;
        int miniBatchSize = 1;
        int numPossibleLabels = 4;
        int numEpochs = 100;
//        CSVSequenceRecordReader reader = new CSVSequenceRecordReader(0, ",");
//        try {
//            reader.initialize(new NumberedFileInputSplit(System.getProperty("user.dir") + "/DATA/seqs/nlpData%d.csv", 0,
//                    exampleLengths));
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }
        this.allIterClassification = new StockDataIterator();
        this.allIterClassification.loadData(miniBatchSize, BASE_MIN <<3);
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
                    System.out.println(j * miniBatchSize * 100 / this.allIterClassification.numExamples() / numEpochs + "%" + " completed");
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
        featureMatrix = this.allIterClassification.list2NDArray(currentPdsList);
//        outarry = net.rnnTimeStep(featureMatrix);
        outarry = net.output(featureMatrix);
        int val = -1;
        outarry = outarry.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(outarry.size(2) - 1));
        val = toPossableLabel(outarry);
        return val;
    }
}
