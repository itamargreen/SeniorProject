import GameObjects.BoardWinPair;
import ManualGame.Board;
import MoveMaker.BoardNetworkCoordinator;
import data.restore.RestoreRecordFile;
import neuralNets.NetworkTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by User on 29-Apr-17.
 */
public class ConnectingFourNNs {

    public static File dataFileDir;
    public static File recordFile;
    public static File recordColumnFile;
    private static String env;
    private static File model;
    private static File chooser;
    private static List<BoardWinPair> record = new ArrayList<BoardWinPair>();
    private static BoardNetworkCoordinator networkCoordinator;

    public static void main(String[] args) {

        env = System.getenv("AppData") + "\\SeniorProjectDir\\";

        dataFileDir = new File(System.getenv("AppData") + "\\SeniorProjectDir\\");
        if (!dataFileDir.exists()) {
            dataFileDir.mkdir();
        } else if (!dataFileDir.isDirectory()) {//just to remove any FILES named like that
            dataFileDir.delete();
            dataFileDir = new File(System.getenv("AppData") + "\\SeniorProjectDir\\");
            dataFileDir.mkdir();
        }
        recordFile = new File(env + "\\records.txt");
        if (!recordFile.exists()) {
            try {
                recordFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        recordColumnFile = new File(env + "\\recordColumns.txt");
        if (!recordColumnFile.exists()) {
            try {
                recordColumnFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        model = new File(env + "\\model.zip");
        chooser = new File(env + "\\chooser.zip");
        //NetworkTest.loadNet(model);
        NetworkTest.loadNN(model);
        record = RestoreRecordFile.readRecords(recordFile);
        networkCoordinator = new BoardNetworkCoordinator(chooser, recordColumnFile);
        networkCoordinator.createChooser(6, 7);
        Board b = new Board(7, 6, env, dataFileDir, recordFile, model, record, networkCoordinator);


    }
}
