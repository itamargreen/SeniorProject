package gameObjects;

import evaluator.EvaluatorNN;

/**
 * This class is an object that pairs the board to an outcome. Used as training data for {@link EvaluatorNN} neural network
 * <p>
 * Created by Itamar
 *
 * @see EvaluatorNN
 */
public class BoardWinPair {

    private double[] board;
    private double outcome;

    /**
     * Constructor for BoardWinPair
     *
     * @param board   The double array that comes form {@link State#convertToArray()} method
     * @param outcome The outcome of the specified board, calculated by {@link bruteForceCalculation.WinAssessment} class.
     */
    public BoardWinPair(double[] board, double outcome) {
        this.board = board;
        this.outcome = outcome;
    }

    public double[] getBoard() {
        return board;
    }

    public void setBoard(double[] board) {
        this.board = board;
    }

    public double getOutcome() {
        return outcome;
    }

    public void setOutcome(double outcome) {
        this.outcome = outcome;
    }
}
