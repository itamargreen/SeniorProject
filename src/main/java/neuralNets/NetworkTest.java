package neuralNets;

import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by itamar on 24-Apr-17.
 */
public class NetworkTest {
    public static final int seed = 12345;
    //Number of iterations per minibatch
    public static final int iterations = 1;
    //Number of epochs (full passes of the data)
    public static final int nEpochs = 200;
    //Number of data points
    public static final int nSamples = 1000;
    //Batch size: i.e., each epoch has nSamples/batchSize parameter updates
    public static final int batchSize = 1;
    //Network learning rate
    public static final double learningRate = 0.01;
    public static final Random rng = new Random(seed);

    public static void main(String[] args) {
        int numInput = 3;
        int nHidden = 50;
        int numOutputs = 1;
        MultiLayerNetwork net = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(learningRate)
                .weightInit(WeightInit.XAVIER)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInput).nOut(nHidden)
                        .activation(Activation.TANH)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(nHidden).nOut(nHidden)
                        .activation(Activation.TANH)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(nHidden).nOut(numOutputs).build())
                .pretrain(false).backprop(true).build()
        );
        net.init();
        net.setListeners(new ScoreIterationListener(1));
        double[][] inputData = new double[5][3];
        inputData[0] = new double[]{1.0, 3.0, 3.0};//true
        inputData[1] = new double[]{10.0, 3.0, 30.0};//true
        inputData[2] = new double[]{4.0, 3.5, 14.0};//true
        inputData[3] = new double[]{2.0, 3.0, 6.5};//false
        inputData[4] = new double[]{2.0, 8.0, 13.0};//false

        double[] outputData = new double[]{1.0, 1.0, 1.0, -1.0, -1.0};
        INDArray input = Nd4j.create(inputData);
        INDArray output = Nd4j.create(outputData);
        DataSet dataSet = new DataSet(input, output);
        List<DataSet> dataSetList = dataSet.asList();
        //Collections.shuffle(dataSetList);

        net.fit(input, new int[]{1, 1, 1, -1, -1});
        double[] test = new double[]{10.0, 3.5, 35.0};
        INDArray testArray = Nd4j.create(test);
        INDArray outputarray = net.output(testArray);
        System.out.println("hei");
    }
}
