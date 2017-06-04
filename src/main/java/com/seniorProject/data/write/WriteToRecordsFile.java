package com.seniorProject.data.write;

import com.seniorProject.gameObjects.BoardColumnPair;
import com.seniorProject.gameObjects.BoardWinPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * This class has static methods for writing lists of {@link BoardColumnPair} and {@link BoardWinPair} to their respective files.
 * <p>
 * Has a "brother" ({@link com.seniorProject.data.restore.RestoreRecordFile}) that reads from those same files and converts them back to lists.
 * Created by Itamar.
 */
public class WriteToRecordsFile {


    private static final Logger logger = LoggerFactory.getLogger(WriteToRecordsFile.class);

    /**
     * This method write a list of {@link BoardWinPair} to the specified file. Uses a bit of regex here and there and a BufferedWriter
     *
     * @param records    The list of records of {@link BoardWinPair} to write to file.
     * @param recordFile A file to save to. Currently .txt file, might change to protect from external changing.
     * @see BufferedWriter
     */
    public static void writeRecords(List<BoardWinPair> records, File recordFile) {
        List<BoardWinPair> list = new ArrayList<>();
        Set<BoardWinPair> uniqueValues = new HashSet<>();
        for (BoardWinPair record : records) {
            if (uniqueValues.add(record)) {
                list.add(record);
            }
        }
        records = list;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(recordFile));

            String line = "";
            for (BoardWinPair pair : records) {
                double[] input = pair.getBoard();
                double out = pair.getOutcome();
                line = Arrays.toString(input);
                line = line.replaceAll("\\[", "");
                line = line.replaceAll("\\]", "");
                line = line.replaceAll("\\s", "");
                line += ":" + out;
                bw.append(line);
                bw.newLine();
            }

            //presentSave(line);
            bw.flush();
            bw.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void presentSave(String text) {
        String line = text.split(":")[0];
        String[] values = line.split(",");
        for (int i = 0; i < values.length; i++) {
            System.out.print(values[i] + ",");
            if (i % 6 == 0 && i > 0) {
                System.out.println();
            }

        }
    }

    /**
     * This method write a list of {@link BoardColumnPair} to the specified file. Uses a bit of regex here and there and a BufferedWriter
     *
     * @param records    The list of records of {@link BoardColumnPair} to write to file.
     * @param recordFile A file to save to. Currently .txt file, might change to protect from external changing.
     * @see BufferedWriter
     */
    public static void writeColumnRecords(List<BoardColumnPair> records, File recordFile) {
        List<BoardColumnPair> list = new ArrayList<>();
        Set<BoardColumnPair> uniqueValues = new HashSet<>();
        for (BoardColumnPair record : records) {
            if (uniqueValues.add(record)) {
                list.add(record);
            }
        }
        records = list;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(recordFile));


            for (BoardColumnPair pair : records) {
                double[] input = pair.getBoard();
                double out = pair.getColumn();
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
