package GameObjects;

/**
 * This class is an object that pairs the board to a column. Used as training data for {@link moveMaker.ColumnChooser chooser} neural network
 *
 * Created by Itamar
 * @see moveMaker.ColumnChooser
 */
public class BoardColumnPair {
    private double[] board;
    private double[] columns;
    private double column;

    /**
     * Constructor for BoardColumnPair
     *
     * @param board The double array that comes form {@link State#convertToArray()} method
     * @param columns The column of the pair
     */
//    public BoardColumnPair(double[] board, double column) {
//        this.board = board;
//        this.column = column;
//    }
    public BoardColumnPair(double[] board, double[] columns) {
        this.board = board;
        this.columns = columns;
        for (int i = 0; i < columns.length; i++) {
            if(columns[i]>0.5){
                this.column = i;
            }

        }
    }
    public double[] getBoard() {
        return board;
    }

    public void setBoard(double[] board) {
        this.board = board;
    }

    //public double getColumn() {
//        return column;
//    }

    public void setColumn(double column) {
        this.column = column;
    }

    public double[] getColumns() {
        return columns;
    }
}
