package com.seniorProject;

import com.seniorProject.data.restore.RestoreRecordFile;
import com.seniorProject.evaluator.EvaluatorNN;
import com.seniorProject.gameObjects.BoardWinPair;
import com.seniorProject.manualGame.Board;
import com.seniorProject.moveMaker.BoardNetworkCoordinator;
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

public class SeniorProject {

    /**
     * Logger for this class
     */
    private static Logger logger = LoggerFactory.getLogger(SeniorProject.class);
    /**
     * Metadata directory
     */
    private static File dataFileDir;
    /**
     * Training set for {@link com.seniorProject.evaluator.EvaluatorNN} neural network. This is a .txt file
     */
    private static File recordsWinPairs;
    /**
     * Training set for {@link com.seniorProject.moveMaker.ColumnChooser} neural network. This is a .txt file
     */
    private static File recordColumnFile;

    /**
     * This creates the folders and files needed to run the program.
     *
     * @param args - Passed from cmd. Not needed.
     */
    public static void main(String[] args) {

        String resourcePath = SeniorProject.class.getResource("/").getPath();
        String env = System.getenv("AppData") + "\\SeniorProjectDir\\";
        dataFileDir = new File(System.getenv("AppData") + "\\SeniorProjectDir\\");

        if (!dataFileDir.exists()) {
            dataFileDir.mkdir();
        } else if (!dataFileDir.isDirectory()) {//just to remove any FILES named like that
            dataFileDir.delete();
            dataFileDir = new File(System.getenv("AppData") + "\\SeniorProjectDir\\");
            dataFileDir.mkdir();
        }
        File res = new File(resourcePath);
//        Arrays.asList(res.listFiles()).forEach(file -> {
//            if (file.isFile()) {
//                String name = file.getName();
//                if (name.endsWith(".txt") || name.endsWith(".zip") || name.endsWith(".bin")) {
//                    Path destination = Paths.get(env, name);
//                    try {
//                        Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//        });
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
        EvaluatorNN.loadNN(model, record);

        BoardNetworkCoordinator networkCoordinator = new BoardNetworkCoordinator(chooser, recordColumnFile);
        networkCoordinator.setStorage(statsStorage);
        networkCoordinator.createChooser(6, 7);
        Board b = new Board(7, 6, env, dataFileDir, recordsWinPairs, model, record, networkCoordinator);


    }
}
