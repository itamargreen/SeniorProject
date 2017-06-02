package com.seniorProject.moveMaker;

import com.seniorProject.gameObjects.BoardColumnPair;
import com.seniorProject.gameObjects.State;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.earlystopping.saver.LocalFileModelSaver;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
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
import java.util.List;

/**
 * This class contains the Neural Network which is the core of this program. This makes the choices for the computer's moves in the game. I have no idea how it actually manages to give the correct choice, but that's true for all neural networks.
 * <p>
 * I just know how it's built and the theory of how it works
 * <p>
 * Created by Itamar.
 */
class ColumnChooser {
    private static final Logger log = LoggerFactory.getLogger(ColumnChooser.class);
    private final int seed = 12345;
    private MultiLayerNetwork net = null;
    private List<BoardColumnPair> boardColumnPairs;
    private File saveFile;
    private int height;
    private int width;
    private INDArray labels;
    private StatsStorage storage;

    /**
     * Constructor for the chooser.
     *
     * @param boardColumnPairs Training com.seniorProject.data
     * @param height           Height of the board.
     * @param width            Width of the board.
     * @param saved            Save file (.bin) of the model.
     */
    public ColumnChooser(List<BoardColumnPair> boardColumnPairs, int height, int width, File saved, StatsStorage storage) {
        File saveFile2 = new File(System.getenv("AppData") + "\\SeniorProjectDir\\");
        this.width = width;
        this.height = height;
        this.boardColumnPairs = boardColumnPairs;
        saveFile = saved;
        this.storage = storage;
        LocalFileModelSaver saver = new LocalFileModelSaver(saveFile2.getPath());
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

    /**
     * Create configuration for neural network and create the labels, which increases the usefulness.
     */
    private void createColumnChooser() {
        int totalSize = height * width;
        int numInput = totalSize;
        int nHidden = totalSize + 5;
        int numOutputs = 1;


        double learningRate = 1e-3;
        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
                .iterations(1)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.RELU)
                .activation(Activation.SIGMOID)
                .learningRate(learningRate)
                .regularization(true).l2(1e-4)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInput).nOut(nHidden)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(nHidden).nOut(nHidden)
                        .build())
                .layer(2, new DenseLayer.Builder().nIn(nHidden).nOut(width)
                        .activation(Activation.SOFTSIGN)
                        .build())
                .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(width).nOut(numOutputs).build())
                .pretrain(false).backprop(true).build();
//        }

        net = new MultiLayerNetwork(configuration);
        net.init();
        net.setListeners(new ScoreIterationListener(1), new StatsListener(storage));
        if (saveFile.exists()) {
            try {

                ModelSerializer.restoreMultiLayerNetwork(saveFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * This method trains the Chooser on the records list it has as a member.
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

        DataSetIterator iterator = new ListDataSetIterator(list, list.size());

        int epoch = 1;
        do {
            iterator.reset();

            net.fit(iterator);
            //net.score() < 0.65 &&
        } while (++epoch < 5);
        System.out.println("finished training");

        System.out.println("writing to file in chooser");
        try {
            ModelSerializer.writeModel(net, saveFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("finished trainNN in chooser");


    }

    /**
     * This gets a response from the chooser neural network which is how the computer chooses to play its next move.
     * <p>
     * This is both the most and least exciting method.
     *
     * @param gameState The state of the board to which the nn responds
     * @return The output from the network which is close to an integer between 0 and the {@link #width}.
     */
    public double chooseColumn(State gameState) {
        System.out.println("entered chooser in chooser");
        double[][] boardArray = new double[1][gameState.getHeight() * gameState.getWidth()];
        boardArray[0] = gameState.convertToArray();

        INDArray input = Nd4j.create(boardArray);
        try {
            INDArray output = net.output(input, false);
            log.info("network output is actually {}", output.getDouble(0));
            if (output.isScalar()) {
                double res = output.getDouble(0) * 9.0;
                return res;
            }
            return -1.0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1.0;

    }


    /**
     * Trains the neural network on all the records that it already has <b>AND</b> on the {@link List list} passed as a parameter.
     * <p>
     * Calls {@link ColumnChooser#trainNN()} method.
     *
     * @param records a list of {@link BoardColumnPair board column} pairs on which the neural network is trained.
     * @see ColumnChooser#trainNN()
     */
    public void doTraining(List<BoardColumnPair> records) {
        boardColumnPairs.addAll(records);
        trainNN();
    }
}
