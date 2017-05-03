package MoveMaker;

import GameObjects.BoardWinPair;
import GameObjects.State;
import ManualGame.Board;

import java.util.List;

/**
 * Created by User on 01-May-17.
 */
public class BoardNetworkCoordinator {

    private ColumnChooser chooser;

    public int getNNAction(State game) {
        double res = chooser.chooseColumn(game);

        return (int)res;
    }

    /**
     * @param records - List of data that relates board to how close a player is to winning. Can be received from evaluator nn or from brute force
     * @param height  - board height
     * @param width   - board width
     */
    public void createChooser(List<BoardWinPair> records, int height, int width) {
        this.chooser = new ColumnChooser(records, height, width);

    }

    private void trainChooser(BoardWinPair pair){
        this.chooser.doTraining(pair);
    }
    private void trainChooser(List<BoardWinPair> records){
        this.chooser.doTraining(records);
    }
    public BoardNetworkCoordinator() {

    }
}
