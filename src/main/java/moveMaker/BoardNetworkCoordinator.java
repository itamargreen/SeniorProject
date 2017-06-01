package moveMaker;

import data.restore.RestoreRecordFile;
import data.write.WriteToRecordsFile;
import gameObjects.BoardColumnPair;
import gameObjects.State;
import org.deeplearning4j.api.storage.StatsStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by User on 01-May-17.
 */
public class BoardNetworkCoordinator {
    private File chooserModel;
    private File recordsColumn;
    private ColumnChooser chooser;
    private List<BoardColumnPair> pairs;
    private StatsStorage storage;

    public BoardNetworkCoordinator(File chooserModel, File recordsColumn) {
        pairs = new ArrayList<>();
        this.chooserModel = chooserModel;
        setRecordsColumn(recordsColumn);
    }

    public List<BoardColumnPair> getPairs() {
        return pairs;
    }

    public void addPair(BoardColumnPair... pair) {
        List<BoardColumnPair> pairList = Arrays.asList(pair);
        if (!pairs.containsAll(Arrays.asList(pair))) {
            pairs.addAll(pairList);
        }
        WriteToRecordsFile.writeColumnRecords(pairs, recordsColumn);
    }

    public int getNNAction(State game) {
        double[] res = chooser.chooseColumn(game);
        double max = Double.MIN_VALUE;
        int result = -1;
        for (int i = 0; i < res.length; i++) {
            if (res[i] > max) {
                max = res[i];
                result = i;
            }

        }
        return result;
    }

    private void setRecordsColumn(File recordsColumn) {
        this.recordsColumn = recordsColumn;
    }

    /**
     * @param height - board height
     * @param width  - board width
     */
    public void createChooser(int height, int width) {
        System.out.println("entered create chooser in coordinator");
        pairs = RestoreRecordFile.readColumnRecords(recordsColumn);
        chooser = new ColumnChooser(pairs, height, width, chooserModel, storage);

    }

    public void setStorage(StatsStorage storage) {
        this.storage = storage;

    }

    public boolean isChooserNull() {
        return (chooser == null);
    }

    public void trainChooser() {
        System.out.println("entered general trainer in coordinator");

        chooser.doTraining(pairs);
    }

    public void trainChooser(BoardColumnPair pair) {
        System.out.println("entered single pair trainer in coordinator");
        pairs.add(pair);
        trainChooser();

    }
}
