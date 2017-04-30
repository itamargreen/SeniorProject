import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by itamar on 23-Mar-17.
 */
public class State {

    private CellState[][] cellStates;
    private int w = 0, h = 0;

    public State(int w, int h) {
        this.w = w;
        this.h = h;
        cellStates = new CellState[h][w];
        for (int i = 0; i < h; i++) {
            Arrays.fill(cellStates[i], CellState.EMPTY);
        }


    }

    public State(State copy) {
        cellStates = new CellState[copy.getH()][copy.getW()];
        w = copy.getW();
        h = copy.getH();
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                switch (copy.getCellStates()[i][j]) {
                    case BLUE:
                        cellStates[i][j] = CellState.BLUE;
                        break;
                    case RED:
                        cellStates[i][j] = CellState.RED;
                        break;
                    case EMPTY:
                        cellStates[i][j] = CellState.EMPTY;
                        break;
                }

            }
        }
        //print();

    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }


    public double[] convertToArray() {
        List<Double> temp = new ArrayList<Double>();
        double[] res = new double[w * h];
        for (int i = 0; i < h; i++) {

            for (int j = 0; j < w; j++) {

                switch (cellStates[i][j]) {
                    case BLUE:
                        res[i * w + j] = 1.0;
                        temp.add(1.0);
                    case RED:
                        res[i * w + j] = -1.0;
                        temp.add(-1.0);
                    case EMPTY:
                        res[i * w + j] = 0.0;
                        temp.add(0.0);

                }


            }

        }
        for (int i = 0; i < res.length; i++) {
            res[i] = temp.get(i);
        }


        return res;
    }


    public boolean makeMove(int player, int col) {

        switch (player) {
            case 1:
                if (applyMove(1, col)) {
                    return true;
                } else {
                    return false;
                }
            case -1:
                if (applyMove(-1, col)) {
                    return true;
                } else {
                    return false;
                }
        }
        return false;
    }

    /**
     * @param player
     * @param col
     * @return
     */

    private boolean applyMove(int player, int col) {
        CellState[] column = new CellState[h];
        int lastRow = h;
        for (int j = h - 1; j >= 0; j--) {
            column[j] = cellStates[j][col];
            if (column[j] == CellState.EMPTY && lastRow == h) {
                lastRow = j;
            }
        }
        if (column[0] != CellState.EMPTY) {
            return false;
        } else {
            switch (player) {
                case 1:
                    cellStates[lastRow][col] = CellState.BLUE;

                    break;
                case -1:
                    cellStates[lastRow][col] = CellState.RED;

                    break;

            }
            return true;

        }
    }

    public CellState checkWin() {
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                CellState temp = cellStates[i][j];
                if (temp == CellState.EMPTY)
                    continue;
//                if (j < w - 2) {
//                    if (cellStates[i][j + 1] == temp && cellStates[i][j + 2] == temp) {
//                        return temp;
//                    }
//                    if (i < h - 2) {
//                        if (cellStates[i + 1][j + 1] == temp && cellStates[i + 2][j + 2] == temp) {
//                            return temp;
//                        }
//                    }
//                }
//                if (i < h - 2) {
//                    if (cellStates[i + 1][j] == temp && cellStates[i + 2][j] == temp) {
//                        return temp;
//                    }
//                }
//                if (i > 2 && j < w - 2) {
//                    if (cellStates[i - 1][j + 1] == temp && cellStates[i - 2][j + 2] == temp) {
//                        return temp;
//                    }
//                }


                if (j < w - 3) {
                    if (cellStates[i][j + 1] == temp && cellStates[i][j + 2] == temp && cellStates[i][j + 3] == temp) {
                        return temp;
                    }
                    if (i < h - 3) {
                        if (cellStates[i + 1][j + 1] == temp && cellStates[i + 2][j + 2] == temp && cellStates[i + 3][j + 3] == temp) {
                            return temp;
                        }
                    }
                }
                if (i < h - 3) {
                    if (cellStates[i + 1][j] == temp && cellStates[i + 2][j] == temp && cellStates[i + 3][j] == temp) {
                        return temp;
                    }
                }
                if (i > 3 && j < w - 3) {
                    if (cellStates[i - 1][j + 1] == temp && cellStates[i - 2][j + 2] == temp && cellStates[i - 3][j + 3] == temp) {
                        return temp;
                    }
                }

            }
        }
        return CellState.EMPTY;
    }

    public CellState[][] getCellStates() {
        return cellStates;
    }

}
