/**
 * Created by User on 29-Apr-17.
 */
public interface AssessmentRunnable extends  Runnable {

    public void setState(State state);
    public void setPlayer(int player);

    public void setPanel(Board.myPanel panel);



}
