package MoveMaker;

import GameObjects.BoardWinPair;
import GameObjects.State;
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
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by User on 01-May-17.
 */
public class ColumnChooser {
    private final int seed = 12345;
    private final int iterations = 30;
    private final double learningRate = 0.01;
    private MultiLayerNetwork net = null;

    public ColumnChooser(List<BoardWinPair> boardWinPairs, int height, int width) {
        this.net = createColumnChooser(boardWinPairs, height, width);
        trainNN(boardWinPairs,height,width);
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

    public void loadNN(File model) {
        try {
            MultiLayerNetwork restored = ModelSerializer.restoreMultiLayerNetwork(model);
            net = restored;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MultiLayerNetwork createColumnChooser(List<BoardWinPair> boardWinPairs, int height, int width) {
        int totalSize = height * width;
        int numInput = totalSize;
        int nHidden = totalSize * 2;
        int numOutputs = 1;
        MultiLayerNetwork net = null;
        if (net == null) {
            net = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
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
                            .activation(Activation.RELU)
                            .build())
                    .layer(2, new DenseLayer.Builder().nIn(nHidden / 3).nOut((nHidden / 3) * 2)
                            .activation(Activation.RELU)
                            .build())

                    .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                            .activation(Activation.IDENTITY)
                            .nIn((nHidden / 3) * 2).nOut(numOutputs).build())
                    .pretrain(false).backprop(true).build()
            );

            net.init();
            net.setListeners(new ScoreIterationListener(1));
            double[] labelArray = new double[width];
            for (int i = 0; i < labelArray.length; i++) {
                labelArray[i] = (double) i;

            }
            INDArray labels = Nd4j.create(labelArray);
            net.setLabels(labels);


            System.out.println("finished creating nn");

        }

        return net;


    }
    private void trainNN(List<BoardWinPair> boardWinPairs, int height, int width){
        double[][] inputArray = new double[boardWinPairs.size()][height*width];
        double[][] outputArray = new double[boardWinPairs.size()][1];
        for (int i = 0; i < inputArray.length; i++) {
            inputArray[i] = boardWinPairs.get(i).getBoard();
            outputArray[i] = new double[]{boardWinPairs.get(i).getOutcome()};
        }

        INDArray input = Nd4j.create(inputArray);
        INDArray output = Nd4j.create(outputArray);

        DataSet dataSet = new DataSet(input, output);
        List<DataSet> list = dataSet.asList();
        Collections.shuffle(list);
        Collections.shuffle(list);
        Collections.shuffle(list);
        DataSetIterator iterator = new ListDataSetIterator(list, boardWinPairs.size());
        System.out.println("start training");
        int epoch = 0;
        do {
            iterator.reset();
            net.fit(iterator);

            epoch++;
        } while (epoch < iterations + 1);
    }

    public double chooseColumn(State gameState){
        double[][] boardArray = new double[1][gameState.getH()*gameState.getW()];
        boardArray[0] = gameState.convertToArray();

        INDArray input = Nd4j.create(boardArray);
        INDArray output = net.output(input,false);
        if(output.isScalar()){
            return output.getDouble(0,0);
        }else{
            return -1.0;
        }

    }
}
