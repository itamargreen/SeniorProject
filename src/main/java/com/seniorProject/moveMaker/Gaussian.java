package com.seniorProject.moveMaker;

import org.apache.commons.math3.util.FastMath;
import org.nd4j.linalg.api.complex.IComplexNumber;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.BaseTransformOp;
import org.nd4j.linalg.api.ops.Op;
import org.nd4j.linalg.api.ops.TransformOp;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Created by User on 05-May-17.
 */
@Deprecated
class Gaussian extends BaseTransformOp {
    public Gaussian() {
    }

    public Gaussian(INDArray x, INDArray z) {
        super(x, z);
    }

    private Gaussian(INDArray x, INDArray z, long n) {
        super(x, z, n);
    }

    private Gaussian(INDArray x, INDArray y, INDArray z, long n) {
        super(x, y, z, n);
    }

    public Gaussian(INDArray x, INDArray y, INDArray z) {
        super(x, y, z, x.lengthLong());
    }

    public Gaussian(INDArray ndArray) {
        super(ndArray);
    }

    @Override
    public int opNum() {
        return 42;
    }

    @Override
    public String name() {
        return "gaussian";
    }

    @Override
    public IComplexNumber op(IComplexNumber origin, double other) {
        return gaussian(origin);
    }

    @Override
    public IComplexNumber op(IComplexNumber origin, float other) {
        return gaussian(origin);
    }

    @Override
    public IComplexNumber op(IComplexNumber origin, IComplexNumber other) {
        return gaussian(origin);
    }

    @Override
    public float op(float origin, float other) {
        return (float) gaussian(origin);
    }

    @Override
    public double op(double origin, double other) {
        return gaussian(origin);
    }

    @Override
    public double op(double origin) {
        return gaussian(origin);
    }

    @Override
    public float op(float origin) {
        return (float) gaussian(origin);
    }

    @Override
    public IComplexNumber op(IComplexNumber origin) {
        return gaussian(origin);
    }


    private double gaussian(double input) {
        double inputf = input;
        double val = FastMath.exp(-FastMath.pow(inputf, 2));
        if (Nd4j.ENFORCE_NUMERICAL_STABILITY && (Double.isNaN(val) || Double.isInfinite(val))) {
            val = Nd4j.EPS_THRESHOLD;
        }
        return val;
    }

    @Override
    public TransformOp derivative() {
        return new GaussianDerivative(x, y, z, n);
    }

    private IComplexNumber gaussian(IComplexNumber number) {
        double arg = number.complexArgument().doubleValue();
        double gaussArg = FastMath.exp(-FastMath.pow(arg, 2));
        double ret = Math.exp(gaussArg);
        return Nd4j.createDouble(ret, 0);
    }

    @Override
    public Op opForDimension(int index, int dimension) {
        INDArray xAlongDimension = x.vectorAlongDimension(index, dimension);

        if (y() != null)
            return new Gaussian(x.vectorAlongDimension(index, dimension), y.vectorAlongDimension(index, dimension), z.vectorAlongDimension(index, dimension), xAlongDimension.length());
        else
            return new Gaussian(x.vectorAlongDimension(index, dimension), z.vectorAlongDimension(index, dimension), xAlongDimension.length());

    }

    @Override
    public Op opForDimension(int index, int... dimension) {
        INDArray xAlongDimension = x.tensorAlongDimension(index, dimension);

        if (y() != null)
            return new Gaussian(x.tensorAlongDimension(index, dimension), y.tensorAlongDimension(index, dimension), z.tensorAlongDimension(index, dimension), xAlongDimension.length());
        else
            return new Gaussian(x.tensorAlongDimension(index, dimension), z.tensorAlongDimension(index, dimension), xAlongDimension.length());

    }

}