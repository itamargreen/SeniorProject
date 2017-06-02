package com.seniorProject.moveMaker;

import com.seniorProject.data.restore.RestoreRecordFile;
import com.seniorProject.data.write.WriteToRecordsFile;
import com.seniorProject.gameObjects.BoardColumnPair;
import com.seniorProject.gameObjects.State;
import com.seniorProject.manualGame.Board;
import org.deeplearning4j.api.storage.StatsStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Coordinator between {@link com.seniorProject.manualGame.Board} and {@link ColumnChooser}.
 * <p>
 * This manages getting results from the column chooser and training the chooser
 * <p>
 * Created by Itamar.
 */
public class BoardNetworkCoordinator {
    /**
     * File containing the model of the {@link ColumnChooser#net} neural network.
     * <p> This is where the model is saved to after being trained each time.</p>
     */
    private File chooserModel;
    /**
     * File containing the training set for {@link ColumnChooser#net}'s neural network.
     *
     * <p> This is where new training examples are saved to.</p>
     */
    private File recordsColumn;
    /**
     * The {@link ColumnChooser} that has a neural network which plays against the player
     */
    private ColumnChooser chooser;
    /**
     * A {@link List} of {@link BoardColumnPair} that represents the training set currently being used.
     */
    private List<BoardColumnPair> pairs;
    /**
     * Not sure.
     */
    private StatsStorage storage;

    /**
     * Constructor for the coordinator.
     *
     * @param chooserModel  the file in which the chooser model is saved.
     * @param recordsColumn the file in which the training set is saved.
     */
    public BoardNetworkCoordinator(File chooserModel, File recordsColumn) {
        pairs = new ArrayList<>();
        this.chooserModel = chooserModel;
        setRecordsColumn(recordsColumn);
    }

    public List<BoardColumnPair> getPairs() {
        return pairs;
    }

    /**
     * Adds the specified array of {@link BoardColumnPair} to {@link BoardNetworkCoordinator#pairs}.
     * <p>
     * <p> Also writes the new training set to {@link BoardNetworkCoordinator#recordsColumn}.
     *
     * @param pair A single training example for {@link ColumnChooser}.
     */
    public void addPair(BoardColumnPair... pair) {
        List<BoardColumnPair> pairList = Arrays.asList(pair);
        if (!pairs.containsAll(Arrays.asList(pair))) {
            pairs.addAll(pairList);
        }
        WriteToRecordsFile.writeColumnRecords(pairs, recordsColumn);
    }

    /**
     * This is actually the main point of the project. <p>Here the {@link ColumnChooser} is asked to make a choice. This method is called by {@link Board#moveMade()}.
     *
     * @param game The state of the game.
     * @return the computer's chosen action.
     */
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

    /**
     * Setter for the training set save file.
     *
     * @param recordsColumn the file to set.
     */
    private void setRecordsColumn(File recordsColumn) {
        this.recordsColumn = recordsColumn;
    }

    /**
     * Creates a {@link ColumnChooser} and passes the training set to it, for training.
     *
     * @param height - board height
     * @param width  - board width
     */
    public void createChooser(int height, int width) {
        System.out.println("entered create chooser in coordinator");
        pairs = RestoreRecordFile.readColumnRecords(recordsColumn);
        chooser = new ColumnChooser(pairs, height, width, chooserModel, storage);

    }

    /**
     * Setter for the UI storage
     *
     * @param storage UI storage thing.
     */
    public void setStorage(StatsStorage storage) {
        this.storage = storage;

    }

    /**
     * Checks if {@link BoardNetworkCoordinator#chooser} is null. <p> This once fixed a bug, but that bug never happens anymore, even without this method
     *
     * @return true if {@link BoardNetworkCoordinator#chooser} is null.
     */
    public boolean isChooserNull() {
        return (chooser == null);
    }

    /**
     * Trains the {@link BoardNetworkCoordinator#chooser} on {@link BoardNetworkCoordinator#pairs}.
     */
    public void trainChooser() {
        System.out.println("entered general trainer in coordinator");

        chooser.doTraining(pairs);
    }

    /**
     * Simply adds the given {@link BoardColumnPair} to {@link BoardNetworkCoordinator#pairs} and then calls {@link BoardNetworkCoordinator#trainChooser()}
     *
     * @param pair a single training example for {@link ColumnChooser}.
     */
    public void trainChooser(BoardColumnPair pair) {
        System.out.println("entered single pair trainer in coordinator");
        pairs.add(pair);
        trainChooser();

    }
}
