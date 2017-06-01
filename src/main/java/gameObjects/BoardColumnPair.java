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

    public double[] getBoard() {
        return board;
    }

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
