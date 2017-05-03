package bruteForceCalculation;

import GameObjects.CellState;
import GameObjects.State;
import ManualGame.Board;
import com.diffplug.common.base.TreeNode;

/**
 * Created by itamar on 23-Mar-17.
 */
public class WinAssessment {
    public static boolean[] fill;
    public static double diff = 0;

    private static int count = 0;
    private static double countBlue = 0;
    private static double countRed = 0;
    private static int lastLine = -1;

    public static TreeNode<State> assessWin(State game, int player) {
        //fill = new boolean[game.getH()][game.getW()];
        count = 0;
        countBlue = 0;
        countRed = 0;


        TreeNode<State> futureStates = new TreeNode<State>(null, game, game.getW());
        formLayer(futureStates, player, futureStates, 1);
        diff = (countBlue - countRed)/Math.max(countBlue, countRed);
        System.out.println((Math.abs(countBlue - countRed) / Math.max(countBlue, countRed)) + " after " + count);

        return futureStates;

    }

    private static void formLayer(TreeNode<State> node, int player, TreeNode<State> parentNode, int depth) {
        State current = node.getContent();
        //current.print();


//        double r1 = countBlue;
//        double r2 = countRed;
        //double temp = Math.abs(r1 - r2) / Math.max(countBlue, countRed);
        if (/*(Math.abs(r1-r2)/Math.max(countBlue,countRed)> 0.35 && count>1000000 )|| */count > 700000) {

            return;
        }

        if (!node.getContent().checkWin().equals(CellState.EMPTY)) {
            System.out.println("found victory for "+(node.getContent().checkWin())+" after "+depth +" turns");
            return;
        } else {
            for (int i = 0; i < current.getW(); i++) {
                State next = new State(current);

                if (!next.makeMove(player, i)) {
                    //System.out.println("cannot make move at column "+i+", and at depth: "+depth);
                    return;
                }
                count++;
                if (!next.checkWin().equals(CellState.EMPTY)) {
                    switch (next.checkWin()) {
                        case BLUE:
                            countBlue += (1 / Math.pow(depth, 1));
                        case RED:
                            countRed += (1 / Math.pow(depth, 1));
                    }

                }
                TreeNode<State> nextNode = new TreeNode<State>(node, next, next.getW());


                if (next.checkWin().equals(CellState.EMPTY)) {

                    formLayer(nextNode, player * (-1), parentNode, depth + 1);


                }


            }
        }
    }


}
