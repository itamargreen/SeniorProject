package com.seniorProject.gameObjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This object is used everywhere. It is an object used for describing a board (not to be confused with {@link com.seniorProject.manualGame.Board}, which does something else).
 * <p>
 * Has a handful of methods that make using the information it stores very simple
 *
 * @see com.seniorProject.manualGame.Board
 * Created by Itamar.
 */
public class State {

    private CellState[][] cellStates;
    private int width = 0, height = 0;

    /**
     * Constructor for the state of the board. Initializes the 2D array with {@link CellState#EMPTY} cells
     *
     * @param width  The width of the board (num of columns).
     * @param height the height of the board (num of rows).
     */
    public State(int width, int height) {
        this.width = width;
        this.height = height;
        cellStates = new CellState[height][width];
        for (int i = 0; i < this.height; i++) {
            Arrays.fill(cellStates[i], CellState.EMPTY);
        }


    }

    /**
     * Copy constructor.
     *
     * @param copy The state to copy the com.seniorProject.data from.
     */
    public State(State copy) {
        cellStates = new CellState[copy.getHeight()][copy.getWidth()];
        width = copy.getWidth();
        height = copy.getHeight();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Converts the {@link State#cellStates} 2D array to a 1D double array, because that's the only thing a Neural network can use.
     * <p>
     * Uses this conversion:
     * <p>
     * <center>
     * <table border="1" width="75%" summary="conversion table">
     * <tr>
     * <td>
     * <b>Cell State</b>
     * </td>
     * <td>
     * Empty
     * </td>
     * <td>
     * Red
     * </td>
     * <td>Blue</td>
     * </tr>
     * <tr>
     * <td><b>Value</b></td>
     * <td>0.0</td>
     * <td>-1.0</td>
     * <td>1.0</td>
     * </tr>
     * <p>
     * </table></center>
     *
     * @return an array of doubles converted with the above table.
     */
    public double[] convertToArray() {
        List<Double> temp = new ArrayList<>();
        double[] res = new double[width * height];

        for (int i = 0; i < height; i++) {

            for (int j = 0; j < width; j++) {

                switch (cellStates[i][j]) {
                    case BLUE:
                        res[i * width + j] = 1.0;
                        temp.add(1.0);
                        break;
                    case RED:
                        res[i * width + j] = -1.0;
                        temp.add(-1.0);
                        break;
                    case EMPTY:
                        res[i * width + j] = 0.0;
                        temp.add(0.0);
                        break;

                }


            }

        }
        for (int i = 0; i < res.length; i++) {
            res[i] = temp.get(i);
        }


        return res;
    }

    /**
     * Loads the state that was saved to the given array
     *
     * @param boardArray array size of {@link State#height}*{@link State#width} that has {@code 1.0,-1.0,0.0}
     */
    public void fromArray(double[] boardArray) {

        for (int i = 0; i < height * width; i++) {


            int temp = (int) boardArray[i];
            int j = i % width;
            switch (temp) {
                case 1:
                    cellStates[i][j] = CellState.BLUE;
                    break;
                case -1:
                    cellStates[i][j] = CellState.RED;
                    break;
                case 0:
                    cellStates[i][j] = CellState.EMPTY;
                    break;

            }


        }


    }


    /**
     * This method is called externally, and handles telling the caller if the move happened or not.
     *
     * @param player The id of the player that is making the move (1 for blue, -1 for red).
     * @param column The column in which a move is attempted.
     * @return true if and only if a move in the specified column was successfully made.
     */
    public boolean makeMove(int player, int column) {

        switch (player) {
            case 1:
                return applyMove(1, column);
            case -1:
                return applyMove(-1, column);
        }
        return false;
    }

    /**
     * This actually checks if the player is allowed to make a move in the specified column.
     * <br/> If he is, then the move is applied (a change is made in the cell array) and true is returned.
     * <br/> Otherwise, false is returned.
     *
     * @param player The id of the player making the move.
     * @param col    The column in which the move is made.
     * @return true if and only if the move is allowed and was made.
     */
    private boolean applyMove(int player, int col) {
        CellState[] column = new CellState[height];
        int lastRow = height;
        for (int j = height - 1; j >= 0; j--) {
            column[j] = cellStates[j][col];
            if (column[j] == CellState.EMPTY && lastRow == height) {
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

    /**
     * Checks if there are 4 in a row, column or diagonal. (Commented code allows checking for 3 in a row)
     *
     * @return The type of player that won. If no player currently has victory, then {@link CellState#EMPTY} is returned.
     */
    public CellState checkWin() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                CellState temp = cellStates[i][j];
                if (temp == CellState.EMPTY)
                    continue;
//                if (j < width - 2) {
//                    if (cellStates[i][j + 1] == temp && cellStates[i][j + 2] == temp) {
//                        return temp;
//                    }
//                    if (i < height - 2) {
//                        if (cellStates[i + 1][j + 1] == temp && cellStates[i + 2][j + 2] == temp) {
//                            return temp;
//                        }
//                    }
//                }
//                if (i < height - 2) {
//                    if (cellStates[i + 1][j] == temp && cellStates[i + 2][j] == temp) {
//                        return temp;
//                    }
//                }
//                if (i > 2 && j < width - 2) {
//                    if (cellStates[i - 1][j + 1] == temp && cellStates[i - 2][j + 2] == temp) {
//                        return temp;
//                    }
//                }


                if (j < width - 3) {
                    if (cellStates[i][j + 1] == temp && cellStates[i][j + 2] == temp && cellStates[i][j + 3] == temp) {
                        return temp;
                    }
                    if (i < height - 3) {
                        if (cellStates[i + 1][j + 1] == temp && cellStates[i + 2][j + 2] == temp && cellStates[i + 3][j + 3] == temp) {
                            return temp;
                        }
                    }
                }
                if (i < height - 3) {
                    if (cellStates[i + 1][j] == temp && cellStates[i + 2][j] == temp && cellStates[i + 3][j] == temp) {
                        return temp;
                    }
                }
                if (i > 3 && j < width - 3) {
                    if (cellStates[i - 1][j + 1] == temp && cellStates[i - 2][j + 2] == temp && cellStates[i - 3][j + 3] == temp) {
                        return temp;
                    }
                }

            }
        }
        return CellState.EMPTY;
    }

    /**
     * Getter for {@link State#cellStates}
     *
     * @return a 2D array of {@link CellState} that represents the state of the board.
     */
    public CellState[][] getCellStates() {
        return cellStates;
    }

    public void newGame(int width, int height) {
        this.width = width;
        this.height = height;
        cellStates = new CellState[height][width];
        for (int i = 0; i < this.height; i++) {
            Arrays.fill(cellStates[i], CellState.EMPTY);
        }
    }

}
