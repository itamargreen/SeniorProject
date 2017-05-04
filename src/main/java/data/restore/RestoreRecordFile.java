package data.restore;

import GameObjects.BoardColumnPair;
import GameObjects.BoardWinPair;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class has static methods for reading lists of {@link BoardColumnPair} and {@link BoardWinPair} from their respective files.
 * <p>
 * Has a "brother" ({@link data.write.WriteToRecordsFile}) that writes to those same files.
 * Created by Itamar.

 */
public class RestoreRecordFile {

    /**
     * Reads a list of {@link BoardWinPair} from a file. Uses a bit of regex and a BufferedReader.
     *
     * @param recordFile The file from which to read the list.
     * @return A list of {@link BoardWinPair} read from the specified file
     * @see BufferedReader
     */
    public static List<BoardWinPair> readRecords(File recordFile) {
        List<BoardWinPair> records = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(recordFile));
            String lines = "";
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] data = line.split(":");
                if (data.length != 2) {
                    System.err.println("Wait what?");
                    System.exit(1);
                } else {
                    String boardData = data[0];
                    String output = data[1];
                    String[] boardString = boardData.split(",");
                    double out = Double.parseDouble(output);
                    double[] input = new double[boardString.length];
                    for (int i = 0; i < boardString.length; i++) {
                        double temp = Double.parseDouble(boardString[i]);
                        input[i] = temp;
                    }
                    BoardWinPair pair = new BoardWinPair(input, out);
                    records.add(pair);
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }
    /**
     * Reads a list of {@link BoardColumnPair} from a file. Uses a bit of regex and a BufferedReader.
     *
     * @param recordFile The file from which to read the list.
     * @return A list of {@link BoardColumnPair} read from the specified file
     * @see BufferedReader
     */
    public static List<BoardColumnPair> readColumnRecords(File recordFile) {
        List<BoardColumnPair> records = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(recordFile));
            String lines = "";
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] data = line.split(":");
                if (data.length != 2) {
                    System.err.println("Wait what?");
                    System.exit(1);
                } else {
                    String boardData = data[0];
                    String output = data[1];
                    String[] boardString = boardData.split(",");
                    double out = Double.parseDouble(output);
                    double[] input = new double[boardString.length];
                    for (int i = 0; i < boardString.length; i++) {
                        double temp = Double.parseDouble(boardString[i]);
                        input[i] = temp;
                    }
                    BoardColumnPair pair = new BoardColumnPair(input, out);
                    records.add(pair);
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }

}
