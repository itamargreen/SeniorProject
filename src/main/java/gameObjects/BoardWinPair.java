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

    /**
     * An array of 1.0, 0.0 and -1.0. This represents the game board.
     */
    private double[] board;
    /**
     * The estimation of Red player's winning change
     */
    private double outcome;

    /**
     * Constructor for BoardWinPair.
     *
     * @param board   The double array that comes form {@link State#convertToArray()} method
     * @param outcome The outcome of the specified board, calculated by {@link bruteForceCalculation.WinAssessment} class.
     */
    public BoardWinPair(double[] board, double outcome) {
        this.board = board;
        this.outcome = outcome;
    }

    /**
     * Getter for {@link BoardWinPair#board}.
     *
     * @return an array of doubles that are either 1,0 or -1.
     */
    public double[] getBoard() {
        return board;
    }

    /**
     * Setter for {@link BoardWinPair#board} field.
     *
     * @param board an array containing 7x6 doubles that are 1.0,0.0,-1.0 exclusively.
     */
    public void setBoard(double[] board) {
        this.board = board;
    }

    /**
     * Getter for {@link BoardWinPair#outcome}
     *
     * @return the estimate stored in this pair object.
     */
    public double getOutcome() {
        return outcome;
    }

    /**
     * Setter for {@link BoardWinPair#outcome} field.
     *
     * @param outcome estimation of Red player's winning change
     */
    public void setOutcome(double outcome) {
        this.outcome = outcome;
    }
}
