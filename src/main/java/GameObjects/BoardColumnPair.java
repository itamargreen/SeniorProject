package GameObjects;

/**
 * This class is an object that pairs the board to a column. Used as training data for {@link MoveMaker.ColumnChooser chooser} neural network
 *
 * Created by Itamar
 * @see MoveMaker.ColumnChooser
 */
public class BoardColumnPair {
    private double[] board;
    private double column;

    /**
     * Constructor for BoardColumnPair
     *
     * @param board The double array that comes form {@link State#convertToArray()} method
     * @param column The column of the pair
     */
    public BoardColumnPair(double[] board, double column) {
        this.board = board;
        this.column = column;
    }

    public double[] getBoard() {
        return board;
    }

    public void setBoard(double[] board) {
        this.board = board;
    }

    public double getColumn() {
        return column;
    }

    public void setColumn(double column) {
        this.column = column;
    }
}
