

import com.diffplug.common.base.TreeNode;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by itamar on 23-Mar-17.
 */
public class WinAssessment {
    public static boolean[] fill;
    public static int diff = 0;
    static Board.myPanel panel;
    private static int count = 0;
    private static int countBlue = 0;
    private static int countRed = 0;
    private static int lastLine = -1;

    public static TreeNode<State> assessWin(State game, int player, Board.myPanel panel) {
        //fill = new boolean[game.getH()][game.getW()];

        State clone = new State(game);
        WinAssessment.panel = panel;
        TreeNode<State> futureStates = new TreeNode<State>(null, game, game.getW());
        formLayer(futureStates, player, futureStates);
        diff = (countBlue - countRed) * player;
        return futureStates;

    }

    private static void formLayer(TreeNode<State> node, int player, TreeNode<State> parentNode) {
        State current = node.getContent();
        //current.print();


        double r1 = (double) countBlue;
        double r2 = (double) countRed;
        if (Math.abs(r1 - r2) > count / 45 && count > 25000) {
            return;
        }

        if (count > 500000 /* || (Math.abs(countBlue-countRed)/(double)Math.max(countBlue,countRed))>0.75*/) {
            return;
        }
        if (!node.getContent().checkWin().equals(cellState.EMPTY)) {
            return;
        } else {
            for (int i = 0; i < current.getW(); i++) {
                State next = new State(current);

                if (!next.makeMove(player, i)) {
                    return;
                }
                count++;
                if (!next.checkWin().equals(cellState.EMPTY)) {
                    switch (next.checkWin()) {
                        case BLUE:
                            countBlue++;
                        case RED:
                            countRed++;
                    }

                }
                TreeNode<State> nextNode = new TreeNode<State>(node, next, next.getW());

                if (next.checkWin().equals(cellState.EMPTY)) {

                    formLayer(nextNode, player * (-1), parentNode);


                }


            }
        }
    }

    protected static enum cellState {
        BLUE, RED, EMPTY;
    }
}
