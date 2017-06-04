package com.seniorProject.bruteForceCalculation;

import com.diffplug.common.base.TreeNode;
import com.seniorProject.gameObjects.CellState;
import com.seniorProject.gameObjects.State;

/**
 * This class would be static, if Java was like C#. But this is Java so it has static methods and members. It creates a single training set for the Evaluator Neural network, using brute force
 *
 * @see WinAssessment#formLayer(TreeNode, int, TreeNode, int)
 * <p>
 * <p>
 * Created by itamar on 23-Mar-17.
 */
public class WinAssessment {

    private static double diff = 0;
    /**
     * Counter for number of moves tested.
     */
    private static int count = 0;
    /**
     * Estimate of how close blue player is to winning.
     */
    private static double countBlue = 0;
    /**
     * Estimate of how close red player is to winning.
     */
    private static double countRed = 0;

    private static int maxDepth = 0;

    /**
     * Calls recursive method that calculates by brute force the "probability" for red player to win
     *
     * @param game   The current game state from which to assess the win.
     * @param player Not used. Previously used for assessing the victory of specified player (1 or -1).
     * @return A parent tree that is the current game, and whose children are all the possible game states.
     * @see WinAssessment#formLayer(TreeNode, int, TreeNode, int)
     */
    public static TreeNode<State> assessWin(State game, int player) {

        count = 0;
        countBlue = 0;
        countRed = 0;


        TreeNode<State> futureStates = new TreeNode<>(null, game, game.getWidth());
        formLayer(futureStates, -player, futureStates, 1);
        alphabeta(futureStates, maxDepth - 1, Double.MIN_VALUE, Double.MAX_VALUE, true);
        diff = (countRed + countBlue) / Math.max(Math.abs(countBlue), Math.abs(countRed));

        System.out.println(diff + " after " + count);

        return futureStates;

    }

    /**
     * This calculates with brute force how close a player is to winning. It increases the {@link WinAssessment#countBlue countBlue}, {@link WinAssessment#countRed countRed} and {@link WinAssessment#count count} variables like this:
     * <p>
     * <center>red or blue count increase = 1/depth</center>
     * This means that a player that has 2 wins in 2 turns will get a higher score than a player with 5 wins in 10 moves, which makes sense.
     *
     * @param node       The tree node from which to calculate the possible game states.
     * @param player     the player that is making the move in the current step (1 or -1).
     * @param parentNode futureStates variable in {@link WinAssessment#assessWin(State, int)} method.
     * @param depth      Tracks number of moves that the recursion has stepped into.
     */
    private static void formLayer(TreeNode<State> node, int player, TreeNode<State> parentNode, int depth) {
        State current = node.getContent();
        if (depth > 25)
            return;
        if (/*(Math.abs(r1-r2)/Math.max(countBlue,countRed)> 0.35 && count>1000000 )|| */count > 10000000) {

            return;
        }

        if (!node.getContent().checkWin().equals(CellState.EMPTY)) {
            System.out.println("found victory for " + (node.getContent().checkWin()) + " after " + depth + " turns");
            if (depth == 1) {
                switch (current.checkWin()) {
                    case BLUE:
                        countBlue -= 6;
                        break;
                    case RED:
                        countRed += 10;
                        break;
                }
            }
            return;
        } else {
            //iterate over columns and create possibility tree for each column
            for (int i = 0; i < current.getWidth(); i++) {
                State next = new State(current);
                if (!next.makeMove(player, i)) {
                    //System.out.println("cannot make move at column "+i+", and at depth: "+depth);
                    return;
                }
                count++;
                if (!next.checkWin().equals(CellState.EMPTY)) {
                    if (depth == 1) {
                        switch (next.checkWin()) {
                            case BLUE:
                                countBlue -= 6;
                                break;
                            case RED:
                                countRed += 10;
                                break;
                        }
                    } else {
                        switch (next.checkWin()) {
                            case BLUE:
                                double loseWeight = 0.05;
                                countBlue -= ((1 + loseWeight) / Math.pow(depth, 1));
                                break;
                            case RED:
                                countRed += (1 / Math.pow(depth, 1));
                                break;
                        }
                    }
                }
                TreeNode<State> nextNode = new TreeNode<>(node, next, next.getWidth());
                if (next.checkWin().equals(CellState.EMPTY)) {
                    if (depth > maxDepth)
                        maxDepth = depth;
                    formLayer(nextNode, player * (-1), parentNode, depth + 1);
                }


            }
        }
    }

    private static double heuristic(TreeNode<State> node) {
        return 1.0;
    }

    public static double alphabeta(TreeNode<State> node, int depth, double alpha, double beta, boolean maximizingPlayer) {
        if (depth == 0 || !node.getContent().checkWin().equals(CellState.EMPTY))
            return heuristic(node);
        else {
            if (maximizingPlayer) {
                double value = Double.MIN_VALUE;
                for (TreeNode<State> child : node.getChildren()) {
                    value = Math.max(value, alphabeta(child, depth - 1, alpha, beta, false));
                    alpha = Math.max(alpha, value);
                    if (beta <= alpha)
                        break;

                }
                return value;
            } else {
                double value = Double.MAX_VALUE;
                for (TreeNode<State> child : node.getChildren()) {
                    value = Math.min(value, alphabeta(child, depth - 1, alpha, beta, true));
                    alpha = Math.min(alpha, value);
                    if (beta <= alpha)
                        break;

                }
                return value;
            }
        }
    }

    public static double getDiff() {
        return diff;
    }
}
