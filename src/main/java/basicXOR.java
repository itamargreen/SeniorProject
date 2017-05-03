import ManualGame.Board;


import java.io.*;

/**
 * Created by itamar on 21-Feb-17.
 */
public class basicXOR {
    protected static Board b;
    protected static String dataFolder = System.getenv("AppData")+"\\SeniorProjectDir";
    private static File locationToSave = new File("E:\\Users\\itamar\\Desktop\\MyMultiLayerNetwork.zip");      //Where to save the network. Note: the file is in .zip format - can be opened externally

    public static void main(String[] args) throws IOException {

        File f = new File(dataFolder);
        if(!f.exists()){
            f.mkdir();
        }

        b = new Board(7, 6);

    }
}
