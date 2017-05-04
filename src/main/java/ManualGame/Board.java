package ManualGame;

import GameObjects.BoardColumnPair;
import GameObjects.CellState;
import GameObjects.State;
import MoveMaker.BoardNetworkCoordinator;
import com.diffplug.common.base.TreeNode;
import GameObjects.BoardWinPair;
import data.write.WriteToRecordsFile;
import evaluator.EvaluatorNN;
import bruteForceCalculation.*;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * This class is the main GUI creator and handles. The {@link myPanel myPanel} class is a JPanel that handles actually drawing the board. This class handles coordinating pieces of the program, except for the {@link MoveMaker.ColumnChooser chooser}, which has its own {@link BoardNetworkCoordinator coordinator}.
 *
 * I did it like this because this is how I do GUI when I have to make it from scratch (without any gui creator like WindowBuilder or netbeans ide).
 * Created by Itamar.
 */
public class Board extends JFrame implements MouseListener, WindowListener {
    public static Stack<Integer>[] boardStacks;
    private BoardNetworkCoordinator networkCoordinator;
    private File dataFileDir;
    private File recordFile;
    private File model;
    protected static int playerTurn = 1;
    private List<BoardWinPair> record = new ArrayList<BoardWinPair>();
    private static String env;
    private myPanel panel;
    private int boardWidth, boardHeight;
    private State gameState;

    private JFrame frame = new JFrame();
    private JLabel whenAddingRecord = new JLabel("waiting...");
    private JLabel player = new JLabel("currently: ");


    public File getDataFileDir() {
        return dataFileDir;
    }

    public void setDataFileDir(File dataFileDir) {
        this.dataFileDir = dataFileDir;
    }

    public File getRecordFile() {
        return recordFile;
    }

    public void setRecordFile(File recordFile) {
        this.recordFile = recordFile;
    }

    public File getModel() {
        return model;
    }

    public void setModel(File model) {
        this.model = model;
    }

    public List<BoardWinPair> getRecord() {
        return record;
    }

    public void setRecord(List<BoardWinPair> record) {
        this.record = record;
    }

    public static String getEnv() {
        return env;
    }

    public static void setEnv(String env) {
        Board.env = env;
    }

    public int getBoardWidth() {
        return boardWidth;
    }

    public void setBoardWidth(int boardWidth) {
        this.boardWidth = boardWidth;
    }

    public int getBoardHeight() {
        return boardHeight;
    }

    public void setBoardHeight(int boardHeight) {
        this.boardHeight = boardHeight;
    }

    /**
     * Game gui class constructor. Handles all the control buttons and gui liveliness.
     *
     * Not used as logic block, i.e. doesn't do anything other than gui.
     *
     * @param boardWidth                  Board width
     * @param boardHeight                  Board height
     * @param env               Game data folder path
     * @param dataFileDir       Game data file
     * @param recordFile        Training data for evaluator nn
     * @param model             Evaluator nn save model
     * @param record            Evaluator nn training data in List data structure
     * @param networkCoordinator Coordinator between the game and the column chooser NN
     */
    public Board(int boardWidth, int boardHeight, String env, File dataFileDir, File recordFile, File model, List<BoardWinPair> record, BoardNetworkCoordinator networkCoordinator) {
        this.model = model;

        this.dataFileDir = dataFileDir;
        this.recordFile = recordFile;
        this.record = record;
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;

        gameState = new State(boardWidth, boardHeight);
        boardStacks = new Stack[boardWidth];
        for (int i = 0; i < boardWidth; i++) {
            boardStacks[i] = new Stack<Integer>();
            boardStacks[i].setSize(boardHeight);
        }
        setSize(boardWidth * 50 + 100, boardHeight * 50 + 100);
        panel = new myPanel(boardWidth, boardHeight);


        getContentPane().add(panel, BorderLayout.CENTER);

        WinAssessment.fill = new boolean[boardHeight];

        panel.setState(gameState);

        setLocationRelativeTo(null);
        setVisible(true);
        addMouseListener(this);
        Thread gameThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    panel.setState(gameState);

                    repaint();
                    panel.repaint();
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        gameThread.start();


        addWindowListener(this);
        Container container = frame.getContentPane();
        SpringLayout layout = new SpringLayout();
        container.setLayout(layout);

        final JCheckBox eval = new JCheckBox("Evaluate");

        JButton useRecords = new JButton("Use Records");
        useRecords.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EvaluatorNN.firstNeuralTest(getRecord(), boardWidth * boardHeight, model);
            }
        });
        container.add(useRecords);

        JButton createTrainingSetForChooser = new JButton("Create chooser trainig set");
        createTrainingSetForChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createChooserTrainSet(gameState);
            }
        });
        container.add(createTrainingSetForChooser);

        JButton loadNet = new JButton("Load NN");
        loadNet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (model.exists())
                    EvaluatorNN.loadNN(model);
            }
        });
        container.add(loadNet);

        JButton discardList = new JButton("new data");
        discardList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setRecord(new ArrayList<BoardWinPair>());

                whenAddingRecord.setText("now has " + record.size() + " records");
            }
        });
        container.add(discardList);


        JButton calculate = new JButton("Foresee");
        calculate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (gameState.checkWin().equals(CellState.EMPTY)) {
                    double[] input = gameState.convertToArray();
                    double out = doThing();
                    BoardWinPair pair = new BoardWinPair(input, out);
                    if (eval.isSelected()) {
                        EvaluatorNN.testNetwork(pair);
                    }


                    record.add(pair);
                    whenAddingRecord.setText("now has " + record.size() + " records");
                }


            }
        });

        JButton writeToFile = new JButton("Write to file");
        writeToFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WriteToRecordsFile.writeRecords(record, recordFile);
                JOptionPane.showMessageDialog(null, "Done");
            }
        });


        container.add(calculate);
        container.add(eval);
        container.add(whenAddingRecord);
        container.add(player);
        container.add(writeToFile);

        layout.putConstraint(SpringLayout.WEST, calculate, 20, SpringLayout.WEST, container);
        layout.putConstraint(SpringLayout.NORTH, calculate, 10, SpringLayout.NORTH, container);
        layout.putConstraint(SpringLayout.WEST, useRecords, 10, SpringLayout.EAST, calculate);
        layout.putConstraint(SpringLayout.NORTH, useRecords, 0, SpringLayout.NORTH, calculate);
        layout.putConstraint(SpringLayout.WEST, eval, 10, SpringLayout.EAST, useRecords);
        layout.putConstraint(SpringLayout.NORTH, eval, 0, SpringLayout.NORTH, useRecords);
        layout.putConstraint(SpringLayout.WEST, whenAddingRecord, 0, SpringLayout.WEST, calculate);
        layout.putConstraint(SpringLayout.NORTH, whenAddingRecord, 10, SpringLayout.SOUTH, calculate);
        layout.putConstraint(SpringLayout.WEST, writeToFile, 0, SpringLayout.EAST, whenAddingRecord);
        layout.putConstraint(SpringLayout.NORTH, writeToFile, 7, SpringLayout.SOUTH, calculate);
        layout.putConstraint(SpringLayout.WEST, loadNet, 0, SpringLayout.WEST, whenAddingRecord);
        layout.putConstraint(SpringLayout.NORTH, loadNet, 15, SpringLayout.SOUTH, whenAddingRecord);
        layout.putConstraint(SpringLayout.WEST, discardList, 15, SpringLayout.EAST, loadNet);
        layout.putConstraint(SpringLayout.NORTH, discardList, 0, SpringLayout.NORTH, loadNet);
        layout.putConstraint(SpringLayout.WEST, player, 0, SpringLayout.WEST, loadNet);
        layout.putConstraint(SpringLayout.NORTH, player, 15, SpringLayout.SOUTH, loadNet);
        layout.putConstraint(SpringLayout.WEST, createTrainingSetForChooser, 0, SpringLayout.WEST, discardList);
        layout.putConstraint(SpringLayout.NORTH, createTrainingSetForChooser, 15, SpringLayout.SOUTH, discardList);

        frame.setPreferredSize(new Dimension(350, 200));
        double x = getContentPane().getLocationOnScreen().getX() + (boardWidth * 50 + 100);
        double y = getContentPane().getLocationOnScreen().getY() + 10;
        frame.setLocation((int) (x + 10), (int) y);
        frame.pack();

        if (record.size() != 0) {
            whenAddingRecord.setText("now has " + record.size() + " records");
        }


        frame.setVisible(true);

        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        this.networkCoordinator = networkCoordinator;

        player.setText("Currently: " + playerTurn);
    }


    /**
     * Calculates with brute force the closeness to victory (by evaluating possibility tree)
     *
     * @return Returns the evaluator output as a double.
     */
    public double doThing() {
        CellState tes = gameState.checkWin();
        System.out.println(tes);
        TreeNode<State> future = WinAssessment.assessWin(gameState, -1);
        return WinAssessment.diff;
    }

    /**
     * This calls the coordinator for the chooser neural network, and applies the chooser's output as the computer made move.
     *
     * @param game The state of the game after the human
     */
    private void moveMade(State game) {
        System.out.println("entered move maker method in board");
        //trainChooser(game);
        System.out.println("trained chooser from board");
        int result = networkCoordinator.getNNAction(game);
        if (result < 7 && result > -1) {

            gameState.makeMove(playerTurn, result);

            panel.setState(gameState);
            repaint();
            playerTurn = -playerTurn;
        } else {
            System.err.println("Problem!!");
        }
    }

    /**
     * Creates a training set of {@link BoardColumnPair} array that is passed to Coordinator for future use in training the network. This allows bigger training set while training, which makes the network's choices cleverer
     *
     * @param game The state of the game from which to create the training set.
     */
    private void createChooserTrainSet(State game) {
        State copy = new State(game);
        BoardColumnPair[] boardColumnPairs = new BoardColumnPair[boardWidth + 1];
        boardColumnPairs[0] = new BoardColumnPair(copy.convertToArray(), EvaluatorNN.bestColumnFromHere(copy));
        for (int i = 1; i < boardColumnPairs.length; i++) {
            copy = new State(game);
            copy.makeMove(-1, i - 1);
            boardColumnPairs[i] = new BoardColumnPair(copy.convertToArray(), EvaluatorNN.bestColumnFromHere(copy));

        }
        networkCoordinator.addPair(boardColumnPairs);
    }

    /**
     * Creates a single training {@link BoardColumnPair} that is passed to Coordinator and immediately trained with.
     *
     * @param game The state of the game from which to create the training set.
     */
    private void trainChooser(State game) {
        System.out.println("entered training chooser method in board");

        int column = EvaluatorNN.bestColumnFromHere(game);
        BoardColumnPair pair = new BoardColumnPair(game.convertToArray(), column);

        if (networkCoordinator.isChooserNull()) {
            System.err.println("chooser was null!");
            //networkCoordinator.addPair(pair);
            networkCoordinator.createChooser(boardHeight, boardWidth);
        }
        this.networkCoordinator.trainChooser(pair);


    }

    /**
     * Overridden method for painting the components in the JFrame. Needed to ensure the control JFrame's existence
     *
     * @param g Graphics object for the JFrame.
     */
    @Override
    public void paintComponents(Graphics g) {
        super.paintComponents(g);
        double x = getContentPane().getLocationOnScreen().getX() + (boardWidth * 50 + 100);
        double y = getContentPane().getLocationOnScreen().getY() + 10;
        frame.setLocation((int) (x + 10), (int) y);
        frame.pack();
        frame.repaint();

    }

    public void mouseClicked(MouseEvent e) {


    }

    public void mousePressed(MouseEvent e) {

    }


    /**
     * Used to respond to player actions such as making a move, and then allowing the computer to respond.
     *
     * @param e {@link MouseEvent} of mouse click in JFrame
     */
    public void mouseReleased(MouseEvent e) {
        Point location = e.getPoint();
        int unitW = getWidth() / boardWidth;
        int col = (int) (location.getX() / unitW + 30 * 1.5);
        int row = (int) (location.getY() / boardHeight + 30 * 1.5);


        for (int i = 0; i < boardWidth; i++) {
            int x = ((getWidth() - 2 * 30) / boardWidth) * (i % boardWidth) + (int) (30 * 1.5 - 0.5);
            if (location.getX() > (x) && location.getX() < (x + 30)) {
                gameState.makeMove(playerTurn, i);
                playerTurn = -playerTurn;

                panel.setState(gameState);

                repaint();
//                if (playerTurn == -1) {
//                    System.out.println("Computer making move");
//                    moveMade(gameState);
//
//
//                    //trainChooser(gameState); //For training the chooser
//                }

                break;
            }
        }
    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void windowClosing(WindowEvent e) {

        //WriteToRecordsFile.writeRecords(record, recordFile);
        System.exit(0);
    }

    public void windowOpened(WindowEvent e) {

    }

    public void windowClosed(WindowEvent e) {

    }

    public void windowIconified(WindowEvent e) {

    }

    public void windowDeiconified(WindowEvent e) {

    }

    public void windowActivated(WindowEvent e) {

    }

    public void windowDeactivated(WindowEvent e) {

    }


    class myPanel extends JPanel {
        protected int size = 30;
        private int w, h;
        private State state;

        public myPanel(int w, int h) {
            this.w = w;
            this.h = h;
        }


        public void setState(State state) {
            this.state = state;
        }

        /**
         * Handles drawing the boards and the game.
         *
         * @param g Graphics object passed from JFrame whenever the JFrame's {@link Board#paintComponents}.
         */
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.setColor(Color.black);
            for (int i = 0; i < w * h; i++) {
                g.drawOval(((getWidth() - 2 * size) / w) * (i % w) + (int) (size * 1.5 - 0.5), (getWidth() - size * 2) / w * (i / w) + (int) (size * 1.5 - 0.5), size + 1, size + 1);

            }
            if (state.getWidth() != 0) {
                for (int i = 0; i < h; i++) {
                    for (int j = 0; j < w; j++) {
                        g.setColor(Color.black);
                        int x1 = ((getWidth() - 2 * 30) / w) * (j % w) + (int) (30 * 1.5 - 0.5);

                        g.drawLine((x1 - 6), (getWidth() - size * 2) / w * (i) + (int) (size * 1.5), (x1 - 6), (getWidth() - size * 2) / w * (i) + (int) (size * 2.5));
                        g.drawLine((x1 + 36), (getWidth() - size * 2) / w * (i) + (int) (size * 1.5), (x1 + 36), (getWidth() - size * 2) / w * (i) + (int) (size * 2.5));
                        switch (state.getCellStates()[i][j]) {
                            case BLUE:
                                g.setColor(Color.BLUE);
                                g.fillOval(((getWidth() - 2 * size) / w) * (j) + (int) (size * 1.5), (getWidth() - size * 2) / w * (i) + (int) (size * 1.5), size, size);
                                break;
                            case RED:
                                g.setColor(Color.RED);
                                g.fillOval(((getWidth() - 2 * size) / w) * (j) + (int) (size * 1.5), (getWidth() - size * 2) / w * (i) + (int) (size * 1.5), size, size);
                                break;
                            case EMPTY:
                                g.setColor(Color.WHITE);
                                g.fillOval(((getWidth() - 2 * size) / w) * (j) + (int) (size * 1.5), (getWidth() - size * 2) / w * (i) + (int) (size * 1.5), size, size);
                                break;
                        }

                    }
                }
            }
        }
    }
}
