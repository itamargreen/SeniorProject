package moveMaker;

import org.apache.commons.math3.util.Pair;
import org.nd4j.linalg.activations.BaseActivationFunction;
import org.nd4j.linalg.activations.IActivation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.transforms.Exp;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Created by User on 05-May-17.
 */
class ActivationGauss extends BaseActivationFunction implements IActivation {

    @Override
    public INDArray getActivation(INDArray in, boolean training) {
        Nd4j.getExecutioner().execAndReturn(new Exp((in.muli(in)).negi()));
        return in;
    }

    @Override
    public Pair<INDArray, INDArray> backprop(INDArray in, INDArray epsilon) {
        INDArray dLdz = Nd4j.getExecutioner().execAndReturn(new Exp((in.muli(in)).negi()).derivative());
        dLdz.muli(2);
        dLdz.negi();
        dLdz.muli(in);

        dLdz.muli(epsilon);
        return new Pair<>(dLdz, null);
    }

    @Override
    public String toString() {
        return "gaussian";
    }

}
