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
        double[][] inputData = new double[500][3];
        double[][] outputData = new double[500][1];
        Random rngg = new Random(12345);

        INDArray labels = Nd4j.create(new double[]{-1.00, 1.00});
        net.setLabels(labels);


        for (int i = 0; i < inputData.length / 2; i++) {
            double num1 = rngg.nextDouble() * 20.0 - 10.0;
            double num2 = rngg.nextDouble() * 20.0 - 10.0;

            double third = num1 + num2;
            double[] arrTemp = {num1, num2, third};
            inputData[i] = arrTemp;
            outputData[i] = new double[]{1.00};
        }
        for (int i = inputData.length / 2; i < inputData.length; i++) {
            double num1 = rngg.nextDouble() * 20.0 - 10.0;
            double num2 = rngg.nextDouble() * 20.0 - 10.0;
            double num3 = rngg.nextDouble() * 40.0 - 20.0;

            double[] arrTemp = {num1, num2, num3};
            inputData[i] = arrTemp;
            outputData[i] = new double[]{-1.00};
        }


        INDArray input = Nd4j.create(inputData);
        INDArray output = Nd4j.create(outputData);
        DataSet dataSet = new DataSet(input, output);
        List<DataSet> list = dataSet.asList();
        Collections.shuffle(list);
        Collections.shuffle(list);
        Collections.shuffle(list);
        DataSetIterator iterator = new ListDataSetIterator(list, 3);
        int epoch = 0;
        do {
            iterator.reset();
            net.fit(iterator);
            epoch++;
        } while (epoch < iterations + 1);
        double[] test = new double[]{10.0, 3.5, 13.5};
        double[] test2 = inputData[260];
        INDArray testArray = Nd4j.create(test);
        INDArray testArray2 = Nd4j.create(test2);
        INDArray outputarray = net.output(testArray);
        INDArray outputarray2 = net.output(testArray2);
        System.out.println("hei");
    }
}
