import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by itamar on 23-Mar-17.
 */
public class State {

    private WinAssessment.cellState[][] cellStates;
    private int w = 0, h = 0;

    public State(int w, int h) {
        this.w = w;
        this.h = h;
        cellStates = new WinAssessment.cellState[h][w];
        for (int i = 0; i < h; i++) {
            Arrays.fill(cellStates[i], WinAssessment.cellState.EMPTY);
        }
        print();

    }

    public State(State copy) {
        cellStates = new WinAssessment.cellState[copy.getH()][copy.getW()];
        w = copy.getW();
        h = copy.getH();
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                switch (copy.getCellStates()[i][j]) {
                    case BLUE:
                        cellStates[i][j] = WinAssessment.cellState.BLUE;
                        break;
                    case RED:
                        cellStates[i][j] = WinAssessment.cellState.RED;
                        break;
                    case EMPTY:
                        cellStates[i][j] = WinAssessment.cellState.EMPTY;
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

    public void print() {
        System.out.println("new: ");


        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w * 10; j++) {
                System.out.print("-");
            }
            System.out.println();
            System.out.print("|");
            for (int j = 0; j < w; j++) {
                int spaces = 10 - cellStates[i][j].name().length();

                System.out.print(cellStates[i][j].name());
                for (int k = 0; k < spaces; k++) {
                    if (spaces - k == 4) {
                        System.out.print("|");
                    }else{
                        System.out.print(" ");
                    }

                }


            }

            System.out.println();
        }
    }

    public WinAssessment.cellState[][] transpose() throws IllegalArgumentException {
        WinAssessment.cellState[][] arr = cellStates;
        if (arr.length > 0) {
            WinAssessment.cellState[][] res = new WinAssessment.cellState[arr[0].length][arr.length];

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

//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < h; i++) {
//            for (int j = 0; j < w; j++) {
//                sb.append(cellStates[i][j].name() + "\t");
//            }
//            sb.append("\n");
//        }
//        try {
//            FileWriter fw = new FileWriter(new File("E:\\Users\\itamar\\Desktop\\test.txt"));
//            fw.write(sb.toString());
//            fw.flush();
//            fw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        return sb.toString();
//    }

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
        for (int i = 0; i < res.length; i++) {
            res[i] = temp.get(i);
        }


        return res;
    }
public int lastRow(){
        int row = 0;
    for (int i = h-1; i >=0; i--) {

        WinAssessment.cellState[] r = cellStates[i];
        if(!Arrays.asList(r).contains(WinAssessment.cellState.EMPTY)){
            row = i;
            break;
        }

    }
    return row;
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
     *
     * @param player
     * @param col
     * @return
     */

    private boolean applyMove(int player, int col) {
        WinAssessment.cellState[] column = new WinAssessment.cellState[h];
        int lastRow = h;
        for (int j = h - 1; j >= 0; j--) {
            column[j] = cellStates[j][col];
            if (column[j] == WinAssessment.cellState.EMPTY && lastRow == h) {
                lastRow = j;
            }
        }
        if (column[0] != WinAssessment.cellState.EMPTY) {
            return false;
        } else {
            switch (player) {
                case 1:
                    cellStates[lastRow][col] = WinAssessment.cellState.BLUE;

                    break;
                case -1:
                    cellStates[lastRow][col] = WinAssessment.cellState.RED;

                    break;

            }
            return true;

        }
    }

    public WinAssessment.cellState checkWin() {
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                WinAssessment.cellState temp = cellStates[i][j];
                if (temp == WinAssessment.cellState.EMPTY)
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
        return WinAssessment.cellState.EMPTY;
    }

    public WinAssessment.cellState[][] getCellStates() {
        return cellStates;
    }

}
