import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by itamar on 23-Mar-17.
 */
public class State {

    private WinAssesment.cellState[][] cellStates;
    private int w = 0, h = 0;

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public WinAssesment.cellState[][] transpose() throws IllegalArgumentException {
        WinAssesment.cellState[][] arr = cellStates;
        if (arr.length > 0) {
            WinAssesment.cellState[][] res = new WinAssesment.cellState[arr[0].length][arr.length];

            for (int i = 0; i < arr[0].length; i++) {
                for (int j = 0; j < arr.length; j++) {
                    res[i][j] = arr[j][i];
                }

            }
            return res;
        } else {
            throw new IllegalArgumentException("Invalid array!");
        }
    }

    public State(int w, int h) {
        this.w = w;
        this.h = h;
        cellStates = new WinAssesment.cellState[w][h];
        for (int i = 0; i < h; i++) {
            Arrays.fill(cellStates[i], WinAssesment.cellState.EMPTY);
        }

    }

    public State(State copy) {
        cellStates = new WinAssesment.cellState[copy.getW()][copy.getH()];
        w = copy.getW();
        h = copy.getH();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                switch (copy.getCellStates()[i][j]) {
                    case BLUE:
                        cellStates[i][j] = WinAssesment.cellState.BLUE;
                        break;
                    case RED:
                        cellStates[i][j] = WinAssesment.cellState.RED;
                        break;
                    case EMPTY:
                        cellStates[i][j] = WinAssesment.cellState.EMPTY;
                        break;
                }

            }
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                sb.append(cellStates[i][j].name() + "\t");
            }
            sb.append("\n");
        }
        try {
            FileWriter fw = new FileWriter(new File("C:\\Users\\itama\\Desktop\\test.txt"));
            fw.write(sb.toString());
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return sb.toString();
    }

    public double[] convertToArray() {
        List<Double> temp = new ArrayList<Double>();
        double[] res = new double[w * h];
        for (int i = 0; i < h; i++) {

            for (int j = 0; j < w; j++) {

                switch (cellStates[i][j]) {
                    case BLUE:
                        res[i * h + j] = 1.0;
                        temp.add(1.0);
                    case RED:
                        res[i * h + j] = -1.0;
                        temp.add(-1.0);
                    case EMPTY:
                        res[i * h + j] = 0.0;
                        temp.add(0.0);

                }


            }

        }
        for(int i = 0; i<res.length;i++){
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

    private boolean applyMove(int player, int col) {
        WinAssesment.cellState[] column = new WinAssesment.cellState[h];
        int lastRow = w;
        for (int j = w - 1; j >= 0; j--) {
            column[j] = cellStates[j][col];
            if (column[j] == WinAssesment.cellState.EMPTY && lastRow == w) {
                lastRow = j;
            }
        }
        if (column[0] != WinAssesment.cellState.EMPTY) {
            return false;
        } else {
            switch (player) {
                case 1:
                    cellStates[lastRow][col] = WinAssesment.cellState.BLUE;
                    break;
                case -1:
                    cellStates[lastRow][col] = WinAssesment.cellState.RED;
                    break;

            }
            return true;

        }
    }

    public WinAssesment.cellState checkWin() {
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                WinAssesment.cellState temp = cellStates[i][j];
                if (temp == WinAssesment.cellState.EMPTY)
                    continue;
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
        return WinAssesment.cellState.EMPTY;
    }

    public WinAssesment.cellState[][] getCellStates() {
        return cellStates;
    }

}
