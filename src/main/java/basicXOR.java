import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.ActivationLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.api.buffer.*;


import java.io.*;
import java.util.Arrays;

/**
 * Created by itamar on 21-Feb-17.
 */
public class basicXOR {
    protected static Board b;
    private static File locationToSave = new File("C:\\Users\\itama\\Desktop\\MyMultiLayerNetwork.zip");      //Where to save the network. Note: the file is in .zip format - can be opened externally

    public static void main(String[] args) throws IOException {
        b = new Board(5, 5);
        MultiLayerNetwork net;
        if (!locationToSave.exists()) {


            MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder().activation(Activation.RELU)
                    .learningRate(0.05)
                    .optimizationAlgo(OptimizationAlgorithm.LINE_GRADIENT_DESCENT)
                    .weightInit(WeightInit.XAVIER)
                    .iterations(500).list()
                    .layer(0, new DenseLayer.Builder()
                            .nIn(25)
                            .nOut(50)
                            .activation(Activation.SIGMOID).build())
                    .layer(1, new DenseLayer.Builder()
                            .nIn(50)
                            .nOut(100)
                            .activation(Activation.TANH).build())
                    .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                            .activation(Activation.IDENTITY)
                            .nIn(100).nOut(1).build())
                    .pretrain(false).backprop(true)
                    .build();

            net = new MultiLayerNetwork(configuration);
        } else {
            net = ModelSerializer.restoreMultiLayerNetwork(locationToSave);
        }
        double[][] data = new double[][]{b.getS().convertToArray()};

        double[][] labels = new double[][]{{b.doThing()}};
        INDArray input = new NDArray(data);
        
        INDArray output = new NDArray(labels);
        net.setInput(input);

        net.setLabels(output);
        net.fit();
//        StringBuilder sb = new StringBuilder();
//        for (Layer l : net.getLayers()) {
//            DataBuffer arr = l.params().data();
//            for(double d : arr.asDouble()){
//                sb.append(d+"\t");
//            }
//            sb.append("\n**\n");
//
//        }
//        FileWriter fw = new FileWriter(new File("C:\\Users\\itama\\Desktop\\dump.txt"));
//        fw.write(sb.toString());
//        fw.flush();
//        fw.close();
        //= net.params().data();/*.write(new FileOutputStream(new File("C:\\Users\\itama\\Desktop\\dump.txt")));*/

        System.out.println("done2");


        boolean saveUpdater = true;                                             //Updater: i.e., the state for Momentum, RMSProp, Adagrad etc. Save this if you want to train your network more in the future
        try {
            ModelSerializer.writeModel(net, locationToSave, saveUpdater);
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("wat");
    }
}
