import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
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

public class RegressionSum {
    //Random number generator seed, for reproducability
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
    // The range of the sample data, data in range (0-1 is sensitive for NN, you can try other ranges and see how it effects the results
    // also try changing the range along with changing the activation function
    public static int MIN_RANGE = 0;
    public static int MAX_RANGE = 3;

    public static void nnThings(double[] input, double[] out, boolean ticked) throws IOException {
        File locationToSave = new File(basicXOR.dataFolder + "\\network.zip");
        //Generate the training data
        DataSetIterator iterator = getTrainingData(input, out);

        MultiLayerNetwork net;
        if (!locationToSave.exists()) {
            //Create the network
            int numInput = 16;
            int numOutputs = 1;
            int nHidden = 100;
            net = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
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


            boolean saveUpdater = true;                                             //Updater: i.e., the state for Momentum, RMSProp, Adagrad etc. Save this if you want to train your network more in the future
            ModelSerializer.writeModel(net, locationToSave, saveUpdater);
        } else {
            net = ModelSerializer.restoreMultiLayerNetwork(locationToSave);

            System.out.println("loaded");
        }
        if (!ticked) {
            for (int i = 0; i < nEpochs; i++) {
                iterator.reset();
                net.fit(iterator);
            }
        }else{
            DataSetIterator test = getTestingData(input);
            INDArray output = net.output(test,false);
            System.out.println("tested");
        }


        boolean saveUpdater = true;
        ModelSerializer.writeModel(net, locationToSave, saveUpdater);

    }

    public static DataSetIterator getTrainingData(double[] in, double[] out) {


        INDArray inputNDArray = Nd4j.create(in);
        INDArray outPut = Nd4j.create(out);
        DataSet dataSet = new DataSet(inputNDArray, outPut);
        List<DataSet> listDs = dataSet.asList();
        Collections.shuffle(listDs, rng);
        return new ListDataSetIterator(listDs, batchSize);

    }
    public static DataSetIterator getTestingData(double[] in) {


        INDArray inputNDArray = Nd4j.create(in);

        DataSet dataSet = new DataSet();
        dataSet.addFeatureVector(inputNDArray);

        List<DataSet> listDs = dataSet.asList();
        Collections.shuffle(listDs, rng);
        return new ListDataSetIterator(listDs, batchSize);

    }
}