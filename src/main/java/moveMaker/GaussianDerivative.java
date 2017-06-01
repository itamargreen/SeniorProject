package moveMaker;

import org.apache.commons.math3.util.FastMath;
import org.nd4j.linalg.api.complex.IComplexDouble;
import org.nd4j.linalg.api.complex.IComplexNumber;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.BaseTransformOp;
import org.nd4j.linalg.api.ops.Op;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Created by User on 05-May-17.
 */
@Deprecated
class GaussianDerivative extends BaseTransformOp {

    public GaussianDerivative() {
    }

    public GaussianDerivative(INDArray x, INDArray z) {
        super(x, z);
    }

    private GaussianDerivative(INDArray x, INDArray z, long n) {
        super(x, z, n);
    }

    public GaussianDerivative(INDArray x, INDArray y, INDArray z, long n) {
        super(x, y, z, n);
    }

    public GaussianDerivative(INDArray x) {
        super(x);
    }

    private static double gaussianDeriv(double input) {
        double gauss = FastMath.exp(-FastMath.pow(input, 2));
        double out = -2 * input * gauss;
        if (Nd4j.ENFORCE_NUMERICAL_STABILITY && (Double.isNaN(out) || Double.isInfinite(out))) {
            out = Nd4j.EPS_THRESHOLD;
        }
        return out;
    }

    private static IComplexNumber gaussianDeriv(IComplexNumber number) {
        double arg = number.complexArgument().doubleValue();
        double sigArg = 1 + (FastMath.exp(-arg)) - 1 + .5f;
        double ret = Math.exp(sigArg);
        IComplexDouble sigmoid = Nd4j.createDouble(ret, 0);
        IComplexNumber oneMinus = Nd4j.createComplexNumber(1, 1).subi(sigmoid);
        return sigmoid.mul(oneMinus);
    }

    @Override
    public int opNum() {
        return 29;
    }

    @Override
    public String name() {
        return "gaussianDerivative";
    }

    @Override
    public IComplexNumber op(IComplexNumber origin, double other) {
        return gaussianDeriv(origin);
    }

    @Override
    public IComplexNumber op(IComplexNumber origin, float other) {
        return gaussianDeriv(origin);
    }

    @Override
    public IComplexNumber op(IComplexNumber origin, IComplexNumber other) {
        return gaussianDeriv(origin);
    }

    @Override
    public float op(float origin, float other) {
        return (float) gaussianDeriv(origin);
    }

    @Override
    public double op(double origin, double other) {
        return gaussianDeriv(origin);
    }

    @Override
    public double op(double origin) {
        return gaussianDeriv(origin);
    }

    @Override
    public float op(float origin) {
        return (float) gaussianDeriv(origin);
    }

    @Override
    public IComplexNumber op(IComplexNumber origin) {
        return gaussianDeriv(origin);
    }

    @Override
    public Op opForDimension(int index, int dimension) {
        INDArray xAlongDimension = x.vectorAlongDimension(index, dimension);

        if (y() != null)
            return new GaussianDerivative(x.vectorAlongDimension(index, dimension), y.vectorAlongDimension(index, dimension), z.vectorAlongDimension(index, dimension), xAlongDimension.length());
        else
            return new GaussianDerivative(x.vectorAlongDimension(index, dimension), z.vectorAlongDimension(index, dimension), xAlongDimension.length());

    }

    @Override
    public Op opForDimension(int index, int... dimension) {
        INDArray xAlongDimension = x.tensorAlongDimension(index, dimension);

        if (y() != null)
            return new GaussianDerivative(x.tensorAlongDimension(index, dimension), y.tensorAlongDimension(index, dimension), z.tensorAlongDimension(index, dimension), xAlongDimension.length());
        else
            return new GaussianDerivative(x.tensorAlongDimension(index, dimension), z.tensorAlongDimension(index, dimension), xAlongDimension.length());

    }
}
