/**
 * Created by User on 18-Apr-17.
 */
public class BoardWinPair {

    private double[] board;
    private int outcome;

    public BoardWinPair(double[] board, int outcome) {
        this.board = board;
        this.outcome = outcome;
    }

    public double[] getBoard() {
        return board;
    }

    public void setBoard(double[] board) {
        this.board = board;
    }

    public int getOutcome() {
        return outcome;
    }

    public void setOutcome(int outcome) {
        this.outcome = outcome;
    }
}
