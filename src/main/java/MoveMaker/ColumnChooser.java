package MoveMaker;

import GameObjects.BoardColumnPair;
import GameObjects.BoardWinPair;
import GameObjects.State;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.EarlyStoppingResult;
import org.deeplearning4j.earlystopping.saver.LocalFileModelSaver;
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator;
import org.deeplearning4j.earlystopping.termination.MaxEpochsTerminationCondition;
import org.deeplearning4j.earlystopping.termination.MaxTimeIterationTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by User on 01-May-17.
 */
public class ColumnChooser {
    private final int seed = 12345;
    private final int iterations = 30;
    private final double learningRate = 0.01;
    private MultiLayerNetwork net = null;
    private List<BoardColumnPair> boardColumnPairs;
    private MultiLayerConfiguration configuration;
    private File saveFile;
    private File saveFile2;
    private int height;
    private int width;


    public ColumnChooser(List<BoardColumnPair> boardColumnPairs, int height, int width, File saved) {
        this.saveFile2 = new File(System.getenv("AppData") + "\\SeniorProjectDir\\");
        this.width = width;
        this.height = height;
        this.boardColumnPairs = boardColumnPairs;
        this.saveFile = saved;
        createColumnChooser();

        //trainNN();
    }

    public ColumnChooser(File saved) {
        MultiLayerNetwork restored = null;
        try {

            restored = ModelSerializer.restoreMultiLayerNetwork(saved);
        } catch (IOException e) {
            e.printStackTrace();
        }
        net = restored;
    }


    private void createColumnChooser() {
        int totalSize = height * width;
        int numInput = totalSize;
        int nHidden = totalSize * 2;
        int numOutputs = 1;

//        if (saveFile2.exists()) {
//            try {
//                net = ModelSerializer.restoreMultiLayerNetwork(saveFile2);
//                configuration = net.getLayerWiseConfigurations();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {

            configuration = new NeuralNetConfiguration.Builder()
                    .seed(seed)
                    .iterations(iterations)
                    .optimizationAlgo(OptimizationAlgorithm.LBFGS)
                    .learningRate(learningRate)
                    .weightInit(WeightInit.XAVIER)

                    .list()
                    .layer(0, new DenseLayer.Builder().nIn(numInput).nOut(nHidden)
                            .activation(Activation.SOFTMAX)
                            .build())
                    .layer(1, new DenseLayer.Builder().nIn(nHidden).nOut(nHidden / 3)
                            .activation(Activation.SOFTMAX)
                            .build())
                    .layer(2, new DenseLayer.Builder().nIn(nHidden / 3).nOut((nHidden / 3) * 2)
                            .activation(Activation.SOFTMAX)
                            .build())

                    .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                            .activation(Activation.IDENTITY)
                            .nIn((nHidden / 3) * 2).nOut(numOutputs).build())
                    .pretrain(false).backprop(true).build();
//        }
        double[] labelArray = new double[width];
        for (int i = 0; i < labelArray.length; i++) {
            labelArray[i] = (double) i;

        }
        this.labels = Nd4j.create(labelArray);

    }

    private INDArray labels;


    /**
     * This method trains the Chooser on the records list it has as a member. Should add more to the list for more training.
     */
    private void trainNN() {
        System.out.println("entered trainNN in chooser");


        double[][] inputArray = new double[boardColumnPairs.size()][height * width];
        double[][] outputArray = new double[boardColumnPairs.size()][1];
        for (int i = 0; i < inputArray.length; i++) {
            inputArray[i] = boardColumnPairs.get(i).getBoard();
            outputArray[i] = new double[]{boardColumnPairs.get(i).getColumn()};
        }

        INDArray input = Nd4j.create(inputArray);
        INDArray output = Nd4j.create(outputArray);

        DataSet dataSet = new DataSet(input, output);
        List<DataSet> list = dataSet.asList();
        List<DataSet> trainData = list.subList(0, (list.size() / 3) * 2);
        List<DataSet> testData = list.subList((list.size() / 3) * 2, list.size());
        DataSetIterator myTrainData = new ListDataSetIterator(trainData, trainData.size());
        DataSetIterator myTestData = new ListDataSetIterator(testData, testData.size());


        EarlyStoppingConfiguration esConf = new EarlyStoppingConfiguration.Builder()
                .epochTerminationConditions(new MaxEpochsTerminationCondition(30))
                .iterationTerminationConditions(new MaxTimeIterationTerminationCondition(5, TimeUnit.MINUTES))
                .scoreCalculator(new DataSetLossCalculator(myTestData, true))
                .evaluateEveryNEpochs(1)
                .modelSaver(new LocalFileModelSaver(saveFile2.getPath()))
                .build();


        EarlyStoppingTrainer trainer = new EarlyStoppingTrainer(esConf, configuration, myTrainData);
        System.out.println("started early training");
        EarlyStoppingResult result = trainer.fit();
        net = (MultiLayerNetwork) result.getBestModel();
        System.out.println("finished early training");
        net.setListeners(new ScoreIterationListener(1));
        net.setLabels(labels);
        System.out.println("writing to file in chooser");
//        try {
//            ModelSerializer.writeModel(net, saveFile, true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        System.out.println("finished trainNN in chooser");


    }

    public double chooseColumn(State gameState) {
        System.out.println("entered chooser in chooser");
        double[][] boardArray = new double[1][gameState.getH() * gameState.getW()];
        boardArray[0] = gameState.convertToArray();

        INDArray input = Nd4j.create(boardArray);
        INDArray output = net.output(input, false);
        if (output.isScalar()) {
            return output.getDouble(0, 0);
        } else {
            return -1.0;
        }

    }

    public void doTraining(BoardColumnPair pair) {
        boardColumnPairs.add(pair);
        trainNN();
    }

    public void doTraining(List<BoardColumnPair> records) {
        this.boardColumnPairs = records;
        trainNN();
    }
}
