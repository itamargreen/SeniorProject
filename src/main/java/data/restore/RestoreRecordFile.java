package data.restore;

import GameObjects.BoardColumnPair;
import GameObjects.BoardWinPair;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 29-Apr-17.
 */
public class RestoreRecordFile {


    public static List<BoardWinPair> readRecords(File recordFile) {
        List<BoardWinPair> records = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(recordFile));
            String lines="";
            String line="";
            while((line = br.readLine())!=null){
                String[] data = line.split(":");
                if(data.length!=2){
                    System.err.println("Wait what?");
                    System.exit(1);
                }else{
                    String boardData = data[0];
                    String output = data[1];
                    String[] boardString = boardData.split(",");
                    double out = Double.parseDouble(output);
                    double[] input = new double[boardString.length];
                    for(int i = 0;i<boardString.length;i++){
                        double temp = Double.parseDouble(boardString[i]);
                        input[i] = temp;
                    }
                    BoardWinPair pair = new BoardWinPair(input,out);
                    records.add(pair);
                }

            }



        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }
    public static List<BoardColumnPair> readColumnRecords(File recordFile) {
        List<BoardColumnPair> records = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(recordFile));
            String lines="";
            String line="";
            while((line = br.readLine())!=null){
                String[] data = line.split(":");
                if(data.length!=2){
                    System.err.println("Wait what?");
                    System.exit(1);
                }else{
                    String boardData = data[0];
                    String output = data[1];
                    String[] boardString = boardData.split(",");
                    double out = Double.parseDouble(output);
                    double[] input = new double[boardString.length];
                    for(int i = 0;i<boardString.length;i++){
                        double temp = Double.parseDouble(boardString[i]);
                        input[i] = temp;
                    }
                    BoardColumnPair pair = new BoardColumnPair(input,out);
                    records.add(pair);
                }

            }



        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }

}
