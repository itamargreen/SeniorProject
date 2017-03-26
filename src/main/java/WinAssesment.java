

import com.diffplug.common.base.TreeNode;

import java.util.TreeSet;

/**
 * Created by itamar on 23-Mar-17.
 */
public class WinAssesment {
    protected static enum cellState {
        BLUE, RED, EMPTY;
    }

    public static TreeNode<State> assessWin(State game, int player) {
        State clone = new State(game);

        TreeNode<State> futureStates = new TreeNode<State>(null, game, game.getW());
        formLayer(futureStates, player,futureStates);
        diff = (countBlue-countRed)*player;
        return futureStates;

    }
    public static int diff = 0;
private static int count = 0;
    private static int countBlue = 0;
    private static int countRed = 0;
    private static void formLayer(TreeNode<State> node, int player,TreeNode<State> parentNode) {
        State current = node.getContent();


        if(count>500000){
            return;
        }
        if (!node.getContent().checkWin().equals(cellState.EMPTY)) {
            return;
        } else {
            for (int i = 0; i < current.getW(); i++) {
                State next = new State(current);
                count++;
                if (!next.makeMove(player, i)) {
                    return;
                }
                if(!next.checkWin().equals(cellState.EMPTY)){
                    switch (next.checkWin()){
                        case BLUE:
                            countBlue++;
                        case RED:
                            countRed++;
                    }

                }
                TreeNode<State> nextNode = new TreeNode<State>(node, next, next.getW());
                if (next.checkWin().equals(cellState.EMPTY))
                    formLayer(nextNode, player * (-1), parentNode);
//                else{
//                    continue;
//                }

            }
        }
    }
}
