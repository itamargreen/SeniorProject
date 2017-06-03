package com.seniorProject.evaluator;

import com.seniorProject.data.write.WriteToRecordsFile;
import com.seniorProject.gameObjects.BoardWinPair;
import com.seniorProject.gameObjects.State;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.eval.RegressionEvaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by itamar.
 */
public class EvaluatorNN {
    /**
     * The number of iterations preformed on the com.seniorProject.data
     */
    private static final int iterations = 1;
    private static final Logger log = LoggerFactory.getLogger(EvaluatorNN.class);
    /**
     * The neural network used by this class. Initialized in {@link EvaluatorNN#loadNN(File)} or in  {@link EvaluatorNN#firstNeuralTest(List, int, File)}
     */
    private static MultiLayerNetwork net = null;
    /**
     * List of {@link BoardWinPair} that are a training dataset for {@link EvaluatorNN#net}.
     */
    private static List<BoardWinPair> recordsWinPairs = new ArrayList<>();
    /**
     * The file in which {@link EvaluatorNN#recordsWinPairs} gets saved by {@link com.seniorProject.data.write.WriteToRecordsFile#writeRecords(List, File)}
     */
    private static File recordFile = new File(System.getenv("AppData") + "\\SeniorProjectDir\\recordsWinPairs.txt");
    /**
     * Storage variable from the UI manager
     */
    private static StatsStorage storage;

    /**
     * Setter for {@link EvaluatorNN#storage}.
     *
     * @param stats the {@link StatsStorage} to set as {@link EvaluatorNN#storage}
     */
    public static void setStats(StatsStorage stats) {
        storage = stats;
    }

    /**
     * Loads the neural network saved in the zip file specified by {@code model}, then sets the restored model to {@link EvaluatorNN#net}.
     * <p>
     * <p>Uses {@link org.deeplearning4j.util.ModelSerializer#restoreMultiLayerNetwork(java.io.File)} to load the model.
     *
     * @param model From where to load the neural network to use as the com.seniorProject.evaluator.
     */
    public static void loadNN(File model) {
        try {

            MultiLayerNetwork restored = ModelSerializer.restoreMultiLayerNetwork(model);
            net = restored;
            net.setListeners(new ScoreIterationListener(3), new StatsListener(storage));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Used instead of {@link EvaluatorNN#loadNN(File)} to create a neural network, using the configuration described in {@link EvaluatorNN#firstNeuralTest(List, int, File)};
     *
     * @param model   Where to save the neural network after creating and training it on the com.seniorProject.data in {@code records}
     * @param records The list of {@link BoardWinPair} that are a training dataset for the neural network.
     */
    public static void loadNN(File model, List<BoardWinPair> records) {
        firstNeuralTest(records, 42, model);
    }

    /**
     * Trains the neural network on the com.seniorProject.data in {@link EvaluatorNN#recordsWinPairs}.
     * <p>
     * <p>
     * <p>After training, saves the trained model to the file specified by {@code model}
     *
     * @param model     Location to save the trained model in.
     * @param totalSize Size of the board (width x height)
     */
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
        int epoch = 1;

        do {

            iterator.reset();
            net.fit(iterator);

        } while (++epoch < 200);

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


    /**
     * Adds the given pairs to the ones in {@link EvaluatorNN#recordsWinPairs}.
     * <p>
     * <p>This is used to increase the size of the training set, before training it.
     * <p>
     * <p>NOTE: doesn't call any training method.
     *
     * @param pairs An array of {@link BoardWinPair} to add to the training set.
     */
    public static void addPair(BoardWinPair... pairs) {

        List<BoardWinPair> pairList = Arrays.asList(pairs);
        if (!recordsWinPairs.containsAll(Arrays.asList(pairs))) {
            recordsWinPairs.addAll(pairList);
        }
        WriteToRecordsFile.writeRecords(recordsWinPairs, recordFile);
    }

    /**
     * Creates a neural network and sets {@link EvaluatorNN#net} to it. The configuration is as follows:
     * <p>
     * <table>
     * <tr>
     * <td>Index: 0</td>
     * <td>input count: {@code totalSize}</td>
     * <td>output count: </td>
     * <td>Index: 1</td>
     * </tr>
     * <p>
     * </table>
     *
     * @param records   list of {@link BoardWinPair} that is the training set
     * @param totalSize size of board
     * @param model     the file to save the net model to.
     */
    public static void firstNeuralTest(List<BoardWinPair> records, int totalSize, File model) {
        int nHidden = totalSize * 2;
        int numOutputs = 1;
        if (net == null) {
            net = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                    .seed(1234)
                    .iterations(1)
                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                    .learningRate(1e-5)
                    .regularization(true).l2(1e-4)
                    .miniBatch(false)
                    .gradientNormalization(GradientNormalization.ClipL2PerLayer)
                    .gradientNormalizationThreshold(1)
                    .weightInit(WeightInit.RELU)
                    .activation(Activation.RELU)
                    .list()
                    .layer(0, new DenseLayer.Builder().nIn(totalSize).nOut(totalSize / 3)
                            .build())
                    .layer(1, new DenseLayer.Builder().nIn(totalSize / 3).nOut(totalSize / 7)
                            .build())
                    .layer(2, new DenseLayer.Builder().nIn(totalSize / 7).nOut(3)
                            .build())
                    .layer(3, new DenseLayer.Builder().nIn(3).nOut(totalSize / 7)
                            .build())
                    .layer(4, new DenseLayer.Builder().nIn(totalSize / 7).nOut(totalSize / 6)
                            .build())
                    .layer(5, new DenseLayer.Builder().nIn(totalSize / 6).nOut(nHidden)
                            .build())
                    .layer(6, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                            .activation(Activation.IDENTITY)
                            .nIn(nHidden).nOut(numOutputs).build())
                    .pretrain(false).backprop(true).build());


            net.init();

            net.setListeners(new ScoreIterationListener(3), new StatsListener(storage));
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

            DataSetIterator iterator = new ListDataSetIterator(list, records.size());
            RegressionEvaluation evaluation = net.evaluateRegression(iterator);

            System.out.println("start training");
            int epoch = 1;
            double initScore = 0;
            do {

                iterator.reset();
                net.fit(iterator);

            } while (++epoch < 200);

            System.out.println("finished training on epoch " + epoch + " and score change of " + (net.score() - initScore));

        }

        try {
            ModelSerializer.writeModel(net, model, true);

        } catch (IOException e) {
            e.printStackTrace();
        }
        EvaluatorNN.recordsWinPairs.addAll(records);

    }

    /**
     * Getter method for the training dataset.
     *
     * @return The dataset as a {@link List}.
     */
    public static List<BoardWinPair> getRecords() {
        return recordsWinPairs;
    }

    /**
     * Uses the neural network to evaluate the best move for the red player, given the state of the board.
     *
     * @param game The state of the board.
     * @return an array that looks like {@code {1,0,0,0,0,0,0,0}}, where the {@code 1} is placed in the index of the column that is the best move.
     */
    public static double bestColumnFromHere(State game) {
        System.out.println("entered \"brute force\" best column selector");
        double bestColumn = -1;
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
                log.info("evaluation is: {}");
                if (evaluated > max) {
                    bestColumn = column;
                    max = evaluated;
                }
            }
        }

        return bestColumn;
    }

    /**
     * This method uses the neural network to evaluate the board and get the best column to choose.
     *
     * @param game the state of the board.
     * @return the best column to choose.
     */
    public static int getChoice(State game) {
        System.out.println("entered \"brute force\" best single column selector");
        int res = -1;
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
                    res = column;
                    max = evaluated;
                }
            }
        }

        return res;
    }

}
