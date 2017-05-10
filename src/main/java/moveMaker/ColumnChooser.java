package moveMaker;

import GameObjects.BoardColumnPair;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class contains the Neural Network which is the core of this program. This makes the choices for the computer's moves in the game. I have no idea how it actually manages to give the correct choice, but that's true for all neural networks.
 * <p>
 * I just know how it's built and the theory of how it works
 * <p>
 * Created by Itamar.
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
    private INDArray labels;

    /**
     * Constructor for the chooser.
     *
     * @param boardColumnPairs Training data
     * @param height           Height of the board.
     * @param width            Width of the board.
     * @param saved            Save file (.bin) of the model.
     *                         //TODO: do something about the saved parameter
     */
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

    /**
     * Create configuration for neural network and create the labels, which increases the usefulness.
     */
    private void createColumnChooser() {
        int totalSize = height * width;
        int numInput = totalSize;
        int nHidden = totalSize * 2;
        int numOutputs = 7;

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
        double[][] labelArray = new double[width][width];
        for (int i = 0; i < labelArray.length; i++) {
            Arrays.fill(labelArray[i], 0);
            labelArray[i][i] = 1.0;

        }
        this.labels = Nd4j.create(labelArray);
        net = new MultiLayerNetwork(configuration);
        net.init();
        net.setLabels(labels);
        net.setListeners(new ScoreIterationListener(1));

    }

    /**
     * This method trains the Chooser on the records list it has as a member.
     */
    private void trainNN() {
        System.out.println("entered trainNN in chooser");


        double[][] inputArray = new double[boardColumnPairs.size()][height * width];
        double[][] outputArray = new double[boardColumnPairs.size()][width];
        for (int i = 0; i < inputArray.length; i++) {
            inputArray[i] = boardColumnPairs.get(i).getBoard();
            int index = (int) boardColumnPairs.get(i).getColumnSingle();
            outputArray[i][index] = 1.0;
        }

        INDArray input = Nd4j.create(inputArray);
        INDArray output = Nd4j.create(outputArray);

        DataSet dataSet = new DataSet(input, output);
        List<DataSet> list = dataSet.asList();
        List<DataSet> trainData = list.subList(0, (list.size() / 3) * 2);
        List<DataSet> testData = list.subList((list.size() / 3) * 2, list.size());
        DataSetIterator iterator = new ListDataSetIterator(list, 1);
//        DataSetIterator myTrainData = new ListDataSetIterator(trainData, trainData.size());
//        DataSetIterator myTestData = new ListDataSetIterator(testData, testData.size());
//
//
//        EarlyStoppingConfiguration esConf = new EarlyStoppingConfiguration.Builder()
//                .epochTerminationConditions(new MaxEpochsTerminationCondition(30))
//                .iterationTerminationConditions(new MaxTimeIterationTerminationCondition(5, TimeUnit.MINUTES))
//                .scoreCalculator(new DataSetLossCalculator(myTestData, true))
//                .evaluateEveryNEpochs(1)
//                .modelSaver(new LocalFileModelSaver(saveFile2.getPath()))
//                .build();
//
//
//        EarlyStoppingTrainer trainer = new EarlyStoppingTrainer(esConf, configuration, myTrainData);
//        System.out.println("started early training");
//        EarlyStoppingResult result = trainer.fit();

        int epoch = 1;
        do {
            iterator = new ListDataSetIterator(list, epoch);
            net.fit(iterator);
            epoch++;
        } while (epoch < list.size());
//        iterator = new ListDataSetIterator(list, list.size());
//        while(epoch<30){
//            iterator.reset();
//            net.fit(iterator);
//            epoch++;
//        }


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
        INDArray output = net.output(input, false);
        if(output.isRowVector()){
            double max = -10;
            int index = -10;
            for (int i = 0; i < width; i++) {
                if(output.getRow(0).getDouble(0,i)>max){
                    max = output.getRow(0).getDouble(0,i);
                    index = i;
                }

            }
            return index;

        }
        return -1.0;

    }

    public void doTraining(BoardColumnPair pair) {
        boardColumnPairs.add(pair);
        trainNN();
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
        this.boardColumnPairs.addAll(records);
        trainNN();
    }
}
