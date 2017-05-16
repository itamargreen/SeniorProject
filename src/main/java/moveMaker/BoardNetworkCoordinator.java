package moveMaker;

import GameObjects.BoardColumnPair;
import GameObjects.State;
import data.restore.RestoreRecordFile;
import data.write.WriteToRecordsFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by User on 01-May-17.
 */
public class BoardNetworkCoordinator {
    private File chooserFile;
    private File recordsColumn;
    private ColumnChooser chooser;
    private List<BoardColumnPair> pairs;

    public BoardNetworkCoordinator(File chooserFile, File recordsColumn) {
        this.pairs = new ArrayList<>();
        this.chooserFile = chooserFile;
        setRecordsColumn(recordsColumn);
    }

    public List<BoardColumnPair> getPairs() {
        return pairs;
    }



    public void addPair(BoardColumnPair... pair) {
        List<BoardColumnPair> pairList = Arrays.asList(pair);
        this.pairs.addAll(pairList);
        WriteToRecordsFile.writeColumnRecords(this.pairs,recordsColumn);
    }

    public int getNNAction(State game) {
        double res = chooser.chooseColumn(game);
        return (int) Math.round(res);

    }

    public void setRecordsColumn(File recordsColumn) {
        this.recordsColumn = recordsColumn;
        this.pairs = RestoreRecordFile.readColumnRecords(this.recordsColumn);
    }

    /**
     * @param records - List of data that relates board to how close a player is to winning. Can be received from evaluator nn or from brute force
     * @param height  - board height
     * @param width   - board width
     */
    public void createChooser(List<BoardColumnPair> records, int height, int width) {

        if (chooserFile.exists()) {
            this.chooser = new ColumnChooser(chooserFile);
        } else {
            this.chooser = new ColumnChooser(records, height, width, chooserFile);
        }


    }

    public void createChooser(int height, int width) {
        System.out.println("entered create chooser in coordinator");
        pairs = RestoreRecordFile.readColumnRecords(recordsColumn);
        this.chooser = new ColumnChooser(this.pairs, height, width, chooserFile);
    }

    public boolean isChooserNull() {
        return (chooser == null);
    }

    public void trainChooser() {
        System.out.println("entered general trainer in coordinator");

        List<BoardColumnPair> pairList = RestoreRecordFile.readColumnRecords(recordsColumn);
        this.pairs.addAll(pairList);
        this.chooser.doTraining(this.pairs);
    }

}
