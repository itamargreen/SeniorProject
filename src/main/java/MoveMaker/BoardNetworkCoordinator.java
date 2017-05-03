package MoveMaker;

import GameObjects.BoardColumnPair;
import GameObjects.BoardWinPair;
import GameObjects.State;
import data.restore.RestoreRecordFile;
import data.write.WriteToRecordsFile;
import org.deeplearning4j.util.ModelSerializer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 01-May-17.
 */
public class BoardNetworkCoordinator {
    private File chooserFile;
    private File recordsColumn;
    private ColumnChooser chooser;
    private List<BoardColumnPair> pairs;

    public List<BoardColumnPair> getPairs() {
        return pairs;
    }

    public void addPair(BoardColumnPair pair) {
        this.pairs.add(pair);
    }

    public void setPairs(List<BoardColumnPair> pairs) {
        this.pairs = pairs;
    }

    public int getNNAction(State game) {
        double res = chooser.chooseColumn(game);
        int result = (int) Math.round(res);
        return result;
    }

    public void setRecordsColumn(File recordsColumn) {
        this.recordsColumn = recordsColumn;
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

    public void trainChooser(BoardColumnPair pair) {
        System.out.println("entered single pair trainer in coordinator");
        pairs = RestoreRecordFile.readColumnRecords(recordsColumn);
        pairs.add(pair);
        WriteToRecordsFile.writeColumnRecords(this.pairs, recordsColumn);
        this.trainChooser(pairs);

    }

    public void trainChooser(List<BoardColumnPair> records) {
        System.out.println("entered list pairs trainer in coordinator");
        this.chooser.doTraining(records);
    }

    public BoardNetworkCoordinator(File chooserFile, File recordsColumn) {
        this.pairs = new ArrayList<BoardColumnPair>();
        this.chooserFile = chooserFile;
        setRecordsColumn(recordsColumn);
    }
}
