package evaluator;

import gameObjects.BoardWinPair;
import gameObjects.State;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by itamar on 24-Apr-17.
 */
public class EvaluatorNN {
    private static final int seed = 12345;
    private static final int iterations = 1;
    private static final double learningRate = 0.75;
    private static MultiLayerNetwork net = null;
    private static List<BoardWinPair> recordsWinPairs = new ArrayList<>();
    private static File recordFile = new File(System.getenv("AppData") + "\\SeniorProjectDir\\recordsWinPairs.txt");
    private static StatsStorage storage;

    public static MultiLayerNetwork getNet() {
        return net;
    }

    public static void setStats(StatsStorage stats) {
        storage = stats;
    }

    public static void loadNN(File model) {
        try {

            MultiLayerNetwork restored = ModelSerializer.restoreMultiLayerNetwork(model);
            net = restored.clone();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadNN(File model, List<BoardWinPair> records) {
        firstNeuralTest(records, 42, model);
    }

    public static void train(File model, int totalSize) {

        double[][] inputData = new double[recordsWinPairs.size()][totalSize];
        double[][] outputData = new double[recordsWinPairs.size()][1];

        for (int i = 0; i < inputData.length; i++) {
            inputData[i] = recordsWinPairs.get(i).getBoard();
            outputData[i] = new double[]{recordsWinPairs.get(i).getOutcome()};
        }


        INDArray input = Nd4j.create(inputData);
        INDArray output = Nd4j.create(outputData);
        DataSet dataSet = new DataSet(input, output);
        List<DataSet> list = dataSet.asList();
        Collections.shuffle(list);
        Collections.shuffle(list);
        DataSetIterator iterator = new ListDataSetIterator(list, list.size());
        System.out.println("start training #2");
        int epoch = 0;
        double score = -1;
        do {

            iterator.reset();
            net.fit(iterator);
            if (net.score() == score) {
                break;
            }
            score = net.score();

        } while (net.score() < 0.85);
        INDArray testIn = list.get(list.size() - 1).getFeatures();
        INDArray outputT = net.output(testIn);
        if (outputT.isScalar()) {
            System.out.println(outputT.getScalar(0, 0) + " after whatever");
        }

        try {
            ModelSerializer.writeModel(net, model, true);

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("finished training #2");
    }

    public static void addPair(BoardWinPair... pairs) {
        EvaluatorNN.recordsWinPairs.addAll(Arrays.asList(pairs));
    }

    public static void firstNeuralTest(List<BoardWinPair> records, int totalSize, File model) {
        int numInput = totalSize;
        int nHidden = totalSize / 2;
        int numOutputs = 1;
        if (net == null) {
            net = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                    .seed(seed)
                    .iterations(iterations)
                    .optimizationAlgo(OptimizationAlgorithm.LBFGS)
                    .weightInit(WeightInit.XAVIER_LEGACY)
                    //.updater(Updater.ADAGRAD)
                    .list()
                    .layer(0, new DenseLayer.Builder().nIn(numInput).nOut(nHidden)
                            .activation(Activation.LEAKYRELU).learningRate(0.05)
                            .build())
                    .layer(1, new DenseLayer.Builder().nIn(nHidden).nOut(nHidden / 3)
                            .activation(Activation.RELU).learningRate(0.05)
                            .build())
                    .layer(2, new DenseLayer.Builder().nIn(nHidden / 3).nOut(nHidden / 6)
                            .activation(Activation.ELU).learningRate(0.05)
                            .build())
                    .layer(3, new DenseLayer.Builder().nIn(nHidden / 6).nOut(nHidden / 14)
                            .activation(Activation.RRELU).learningRate(0.05)
                            .build())
                    .layer(4, new OutputLayer.Builder(LossFunctions.LossFunction.SQUARED_HINGE)
                            .activation(Activation.IDENTITY)
                            .nIn(nHidden / 14).nOut(numOutputs).build())
                    .pretrain(false).backprop(true).build()
            );

            net.init();

            net.setListeners(new ScoreIterationListener(1), new StatsListener(storage));
        }
        if (records.size() != 0) {
            double[][] inputData = new double[records.size()][totalSize];
            double[][] outputData = new double[records.size()][1];

            for (int i = 0; i < inputData.length; i++) {
                inputData[i] = records.get(i).getBoard();
                outputData[i] = new double[]{records.get(i).getOutcome()};
            }


            INDArray input = Nd4j.create(inputData);
            INDArray output = Nd4j.create(outputData);
            DataSet dataSet = new DataSet(input, output);
            List<DataSet> list = dataSet.asList();
            Collections.shuffle(list);
            Collections.shuffle(list);
            Collections.shuffle(list);
            DataSetIterator iterator = new ListDataSetIterator(list, records.size());

            net.setListeners(new ScoreIterationListener(1), new StatsListener(storage));

            System.out.println("start training");
            int epoch = 1;
            do {

                iterator.reset();
                net.fit(iterator);
                //++epoch < 750
            } while (++epoch < 1000);
            System.out.println("finished training");

        }

        try {
            ModelSerializer.writeModel(net, model, true);

        } catch (IOException e) {
            e.printStackTrace();
        }
        EvaluatorNN.recordsWinPairs.addAll(records);

    }

    public static List<BoardWinPair> getRecords() {
        return recordsWinPairs;
    }

    public static double[] bestColumnFromHere(State game) {
        System.out.println("entered \"brute force\" best column selector");
        double[] bestColumn = new double[game.getWidth()];
        double max = -100;
        State nextMoveState;

        for (int column = 0; column < 7; column++) {
            nextMoveState = new State(game);
            nextMoveState.makeMove(-1, column);

            double[][] boardArray = new double[1][];

            boardArray[0] = nextMoveState.convertToArray();
            INDArray input = Nd4j.create(boardArray);
            INDArray output = net.output(input, false);
            if (output.isScalar()) {
                double evaluated = output.getDouble(0);
                if (evaluated > max) {
                    Arrays.fill(bestColumn, 0);
                    bestColumn[column] = 1;
                    max = evaluated;
                }
            }
        }

        return bestColumn;
    }

}
