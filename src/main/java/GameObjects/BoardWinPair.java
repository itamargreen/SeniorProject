package GameObjects;

/**
 * Created by User on 18-Apr-17.
 */
public class BoardWinPair {

    private double[] board;
    private double outcome;

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
