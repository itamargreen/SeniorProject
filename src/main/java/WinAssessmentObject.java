

import com.diffplug.common.base.TreeNode;

/**
 * Created by itamar on 23-Mar-17.
 */
public class WinAssessmentObject implements AssessmentRunnable{
    static Board.myPanel panel;
    public boolean[] fill;
    public int diff = 0;
    private int count = 0;
    private int countBlue = 0;
    private int countRed = 0;
    private int lastLine = -1;




    public TreeNode<State> assessWin(State game, int player, Board.myPanel panel) {
        //fill = new boolean[game.getH()][game.getW()];
        count = 0;
        countBlue = 0;
        countRed = 0;
        State clone = new State(game);
        WinAssessmentObject.panel = panel;
        TreeNode<State> futureStates = new TreeNode<State>(null, game, game.getW());
        formLayer(futureStates, player, futureStates);
        diff = (countBlue - countRed) * player;
        return futureStates;

    }

    private void formLayer(TreeNode<State> node, int player, TreeNode<State> parentNode) {
        State current = node.getContent();
        //current.print();


        double r1 = (double) countBlue;
        double r2 = (double) countRed;
        if (Math.abs(r1 - r2) > count / 45 && count > 250000) {

            return;
        }

        if (count > 500000 /* || (Math.abs(countBlue-countRed)/(double)Math.max(countBlue,countRed))>0.75*/) {
            return;
        }
        if (!node.getContent().checkWin().equals(CellState.EMPTY)) {
            return;
        } else {
            for (int i = 0; i < current.getW(); i++) {
                State next = new State(current);

                if (!next.makeMove(player, i)) {
                    return;
                }
                count++;
                if (!next.checkWin().equals(CellState.EMPTY)) {
                    switch (next.checkWin()) {
                        case BLUE:
                            countBlue++;
                        case RED:
                            countRed++;
                    }

                }
                TreeNode<State> nextNode = new TreeNode<State>(node, next, next.getW());

                if (next.checkWin().equals(CellState.EMPTY)) {

                    formLayer(nextNode, player * (-1), parentNode);


                }


            }
        }
    }

    public void run() {

    }
    public void setState(State state) {

    }

    public void setPlayer(int player) {

    }

    public void setPanel(Board.myPanel panel) {

    }
}
