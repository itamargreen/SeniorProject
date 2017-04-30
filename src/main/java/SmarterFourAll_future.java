import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by User on 29-Apr-17.
 */
public class SmarterFourAll_future {

public static File dataFileDir;
public static File recordFile;
private static String env;
    public static void main(String[] args){
//        env = System.getenv("AppData")+"\\SeniorProjectDir\\";
//        dataFileDir = new File(System.getenv("AppData")+"\\SeniorProjectDir\\");
//        if(!dataFileDir.exists()){
//            dataFileDir.mkdir();
//        }else if(!dataFileDir.isDirectory()){
//            dataFileDir.delete();
//            dataFileDir = new File(System.getenv("AppData")+"\\SeniorProjectDir\\");
//            dataFileDir.mkdir();
//        }
//        recordFile = new File(env+"\\records.txt");
//        if(!recordFile.exists()){
//            try {
//                recordFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
double[] d = {1.0,0.0,5.0,1.3,3.0};
String line = Arrays.toString(d);
        line = line.replaceAll("\\[", "");
        line = line.replaceAll("\\]", "");
        line = line.replaceAll("\\s", "");
        line+=":out";
        String[] split = line.split(":");
        System.out.println(line);
    }
}
