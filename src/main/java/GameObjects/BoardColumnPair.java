package GameObjects;

/**
 * Created by itamar on 03-May-17.
 */
public class BoardColumnPair {
    private double[] board;
    private double column;

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
