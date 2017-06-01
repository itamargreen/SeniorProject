package gameObjects;

import java.util.Arrays;

/**
 * This class is an object that pairs the board to a column. Used as training data for {@link moveMaker.ColumnChooser chooser} neural network
 * <p>
 * Created by Itamar
 *
 * @see moveMaker.ColumnChooser
 */
public class BoardColumnPair {
    /**
     * An array of 1.0, 0.0 and -1.0. This represents the game board.
     */
    private double[] board;
    private double[] column;
    private double columnSingle;

    /**
     * Constructor for BoardColumnPair
     *
     * @param board  The double array that comes form {@link State#convertToArray()} method
     * @param column The column of the pair
     */
    public BoardColumnPair(double[] board, double column) {
        this.board = board;
        Arrays.fill(this.column, 0);
        columnSingle = column;
        this.column[(int) (column)] = 1;
    }

    /**
     * Constructor for BoardColumnPair
     *
     * @param board  The double array that comes form {@link State#convertToArray()} method
     * @param column The column vector of the pair
     */
    public BoardColumnPair(double[] board, double[] column) {
        this.board = board;
        this.column = column;
        for (int i = 0; i < column.length; i++) {
            double v = column[i];
            if (v == 1.0) {
                columnSingle = i;
                break;
            }
        }
    }

    /**
     * Getter for {@link BoardColumnPair#board}.
     *
     * @return an array of doubles that are either 1,0 or -1.
     */
    public double[] getBoard() {
        return board;
    }

    /**
     * Setter for {@link BoardColumnPair#board} field.
     *
     * @param board an array containing 7x6 doubles that are 1.0,0.0,-1.0 exclusively.
     */
    public void setBoard(double[] board) {
        this.board = board;
    }


    public double[] getColumn() {
        return column;
    }

    public void setColumn(double column) {
        Arrays.fill(this.column, 0);

        this.column[(int) (column)] = 1;

    }

    public double getColumnSingle() {
        return columnSingle;
    }
}
