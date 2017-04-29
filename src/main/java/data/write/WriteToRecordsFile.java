package data.write;

import data.BoardWinPair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by User on 29-Apr-17.
 */
public class WriteToRecordsFile {


    public static void writeRecords(List<BoardWinPair> records, File recordFile) {

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(recordFile));


            for (BoardWinPair pair : records) {
                double[] input = pair.getBoard();
                int out = pair.getOutcome();
                String line = Arrays.toString(input);
                line = line.replaceAll("\\[", "");
                line = line.replaceAll("\\]", "");
                line = line.replaceAll("\\s", "");
                line += ":" + out;
                bw.append(line);
                bw.newLine();
            }
            bw.flush();
            bw.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
