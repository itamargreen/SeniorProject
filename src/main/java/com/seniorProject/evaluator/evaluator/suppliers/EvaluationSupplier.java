package com.seniorProject.evaluator.evaluator.suppliers;

import java.util.function.Supplier;

/**
 * An attempt to make this cooler. Using suppliers and java 8 fancy stuff everywhere possible.
 * Created by itamar.
 */
@Deprecated
public class EvaluationSupplier {

    /**
     * A supplier that gives something. but it's deprecated.
     */
    public static Supplier<Double> evaluation = new Supplier<Double>() {

        /**
         * Gets the evaluation.
         *
         * @return an evaluation
         */
        @Override
        public Double get() {
            return 0.05;
        }
    };
}
