import data.restore.RestoreRecordFile;
import evaluator.EvaluatorNN;
import gameObjects.BoardWinPair;
import manualGame.Board;
import moveMaker.BoardNetworkCoordinator;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This is the main of the program. Here I create the files needed, and "install" the program (create a folder and put needed files in it).
 * <p>
 * Created by User on 29-Apr-17.
 */

public class ConnectingFourNNs {

    public static Logger logger = LoggerFactory.getLogger(ConnectingFourNNs.class);
    private static File dataFileDir;
    private static File recordsWinPairs;
    private static File recordColumnFile;

    /**
     * This creates the folders and files needed to run the program.
     * //TODO: Add zip saving of training and then make this a self extracting jar or something
     *
     * @param args - Passed from cmd. Not needed.
     */
    public static void main(String[] args) {

        logger.info("Test");

        String env = System.getenv("AppData") + "\\SeniorProjectDir\\";

        dataFileDir = new File(System.getenv("AppData") + "\\SeniorProjectDir\\");
        if (!dataFileDir.exists()) {
            dataFileDir.mkdir();
        } else if (!dataFileDir.isDirectory()) {//just to remove any FILES named like that
            dataFileDir.delete();
            dataFileDir = new File(System.getenv("AppData") + "\\SeniorProjectDir\\");
            dataFileDir.mkdir();
        }
        recordsWinPairs = new File(env + "\\recordsWinPairs.txt");
        if (!recordsWinPairs.exists()) {
            try {
                recordsWinPairs.createNewFile();
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
        UIServer uiServer = UIServer.getInstance();
        StatsStorage statsStorage = new InMemoryStatsStorage();
        uiServer.attach(statsStorage);
        File model = new File(env + "\\EvaluatorModel.zip");
        File chooser = new File(env + "\\ChooserModel.zip");
        List<BoardWinPair> record = RestoreRecordFile.readRecords(recordsWinPairs);
        EvaluatorNN.setStats(statsStorage);
        EvaluatorNN.loadNN(model);

        BoardNetworkCoordinator networkCoordinator = new BoardNetworkCoordinator(chooser, recordColumnFile);
        networkCoordinator.setStorage(statsStorage);
        networkCoordinator.createChooser(6, 7);
        Board b = new Board(7, 6, env, dataFileDir, recordsWinPairs, model, record, networkCoordinator);


    }
}
