package moveMaker;

import GameObjects.BoardColumnPair;
import GameObjects.State;
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
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.activations.IActivation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

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
public class ColumnChooser {
    private final int seed = 12345;
    private final int iterations = 30;
    private final double learningRate = 0.01;
    private MultiLayerNetwork net = null;
    private List<BoardColumnPair> boardColumnPairs;
    private MultiLayerConfiguration configuration;
    private LocalFileModelSaver saver;
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
        saver = new LocalFileModelSaver(saveFile2.getPath());
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
        int nHidden = totalSize / 2;
        int numOutputs = width;

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
                .seed(1562)
                .iterations(iterations)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(learningRate)
                .weightInit(WeightInit.XAVIER)

                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInput).nOut(nHidden)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(nHidden).nOut(nHidden/3)
                        .activation(Activation.ELU)
                        .build())



                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn((nHidden)/ 3).nOut(numOutputs).build())
                .pretrain(false).backprop(true).build();
//        }
        double[][] labelArray = new double[width][width];
        for (int i = 0; i < labelArray.length; i++) {
            labelArray[i][i] = (double)1;

        }
        this.labels = Nd4j.create(labelArray);
        net = new MultiLayerNetwork(configuration);
        net.init();
        //File bestModel = new File(System.getenv("AppData") + "\\SeniorProjectDir\\bestModel.bin");
        if (saveFile.exists()) {
            try {
                //System.out.println("loaded best model 1");
                /**net = **/ModelSerializer.restoreMultiLayerNetwork(saveFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        } else if (bestModel.exists()) {
//            try {
//                System.out.println("loaded best model 2");
//                net = saver.getBestModel();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
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
            outputArray[i] = boardColumnPairs.get(i).getColumns();
        }

        INDArray input = Nd4j.create(inputArray);
        INDArray output = Nd4j.create(outputArray);

        DataSet dataSet = new DataSet(input, output);
        List<DataSet> list = dataSet.asList();
        List<DataSet> trainData = list.subList(0, (list.size() / 3) * 2);
        List<DataSet> testData = list.subList((list.size() / 3) * 2, list.size());
        DataSetIterator iterator = new ListDataSetIterator(list, list.size());
//        DataSetIterator myTrainData = new ListDataSetIterator(trainData, trainData.size());
//        DataSetIterator myTestData = new ListDataSetIterator(testData, testData.size());
//
//
//        EarlyStoppingConfiguration esConf = new EarlyStoppingConfiguration.Builder()
//                .epochTerminationConditions(new MaxEpochsTerminationCondition(30))
//                .iterationTerminationConditions(new MaxTimeIterationTerminationCondition(20, TimeUnit.MINUTES))
//                .scoreCalculator(new DataSetLossCalculator(myTestData, true))
//                .evaluateEveryNEpochs(2)
//                .modelSaver(saver)
//                .build();
//
//
//        EarlyStoppingTrainer trainer = new EarlyStoppingTrainer(esConf, configuration, myTrainData);
//        System.out.println("started early training");
//        EarlyStoppingResult result = trainer.fit();
//        net = (MultiLayerNetwork) result.getBestModel();
//        System.out.println("finished early training");

        int epoch = 0;
        do {
            iterator.reset();
            net.fit(iterator);
            epoch++;
        } while (epoch < 15);
        System.out.println("finished training");
        net.setListeners(new ScoreIterationListener(1));
        net.setLabels(labels);
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
    public double[] chooseColumn(State gameState) {
        System.out.println("entered chooser in chooser");
        double[][] boardArray = new double[1][gameState.getHeight() * gameState.getWidth()];
        boardArray[0] = gameState.convertToArray();

        INDArray input = Nd4j.create(boardArray);
        try {
            INDArray output = net.output(input, false);
            if (output.isRowVector()) {
                double[] res = new double[width];
                for (int i = 0; i < res.length; i++) {
                    res[i] = output.getDouble(0,i);

                }
                return res;
            } else {
                return new double[]{-1.0};
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new double[]{-1.0};

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
