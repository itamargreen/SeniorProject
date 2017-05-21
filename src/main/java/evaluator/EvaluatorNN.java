package evaluator;

import GameObjects.BoardWinPair;
import GameObjects.State;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
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
import java.util.*;

/**
 * Created by itamar on 24-Apr-17.
 */
public class EvaluatorNN {
    public static final int seed = 12345;
    //Number of iterations per minibatch
    public static final int iterations = 30;
    //Number of epochs (full passes of the data)
    public static final int nEpochs = 200;
    //Number of data points
    public static final int nSamples = 1000;
    //Batch size: i.e., each epoch has nSamples/batchSize parameter updates
    public static final int batchSize = 1;
    //Network learning rate
    public static final double learningRate = 0.01;
    public static final Random rng = new Random(seed);
    private static MultiLayerNetwork net = null;
    private static boolean flag = false;
    private static List<BoardWinPair> records = new ArrayList<>();
    private static File recordFile = recordFile = new File(System.getenv("AppData") + "\\SeniorProjectDir\\records.txt");
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
        System.out.println("start training #2");
        int epoch = 1;
        do {

            iterator = new ListDataSetIterator(list, epoch);
            net.fit(iterator);
            epoch++;
        } while (epoch < list.size()+1);
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
        EvaluatorNN.records.addAll(Arrays.asList(pairs));
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
                    .learningRate(learningRate)
                    .weightInit(WeightInit.XAVIER)

                    .list()
                    .layer(0, new DenseLayer.Builder().nIn(numInput).nOut(nHidden)
                            .activation(Activation.CUBE)
                            .build())
                    .layer(1, new DenseLayer.Builder().nIn(nHidden).nOut(nHidden/2)
                            .activation(Activation.RELU)
                            .build())
                    .layer(2, new DenseLayer.Builder().nIn(nHidden / 2).nOut((nHidden / 3) * 2)
                            .activation(Activation.TANH)
                            .build())

                    .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                            .activation(Activation.IDENTITY)
                            .nIn((nHidden / 3) * 2).nOut(numOutputs).build())
                    .pretrain(false).backprop(true).build()
            );

            net.init();
            net.setListeners(new ScoreIterationListener(1));
        }

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
        System.out.println("start training");
        int epoch = 1;
        do {

            iterator = new ListDataSetIterator(list, epoch);
            net.fit(iterator);
            epoch++;
        } while (epoch < list.size()+1);


        try {
            ModelSerializer.writeModel(net, model, true);

        } catch (IOException e) {
            e.printStackTrace();
        }
        EvaluatorNN.records.addAll(records);

    }

    public static List<BoardWinPair> getRecords() {
        return records;
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
                double evaluated = output.meanNumber().doubleValue();
                if (evaluated > max) {
                    Arrays.fill(bestColumn, 0);
                    bestColumn[column] = 1;
                    max = evaluated;
                }
            }
        }
        return bestColumn;
    }

    public static double testNetwork(BoardWinPair pair) {
        if (net == null) {
            return 0.0;
        }
        double[][] inputData = new double[1][pair.getBoard().length];
        double[][] outputData = new double[1][1];
        inputData[0] = pair.getBoard();
        outputData[0] = new double[]{pair.getOutcome()};
        INDArray input = Nd4j.create(inputData);

        INDArray output = net.output(input, false);
        //TODO: fix this
        //FIXME: please
        return 0.0;

    }
}
