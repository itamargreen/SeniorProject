package manualGame;

import bruteForceCalculation.WinAssessment;
import data.write.WriteToRecordsFile;
import evaluator.EvaluatorNN;
import gameObjects.BoardColumnPair;
import gameObjects.BoardWinPair;
import gameObjects.CellState;
import gameObjects.State;
import moveMaker.BoardNetworkCoordinator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * This class is the main GUI creator and handles. The {@link myPanel myPanel} class is a JPanel that handles actually drawing the board. This class handles coordinating pieces of the program, except for the {@link moveMaker.ColumnChooser chooser}, which has its own {@link BoardNetworkCoordinator coordinator}.
 * <p>
 * I did it like this because this is how I do GUI when I have to make it from scratch (without any gui creator like WindowBuilder or netbeans ide).
 * Created by Itamar.
 */
public class Board extends JFrame implements MouseListener, WindowListener {
    private static Stack<Integer>[] boardStacks;
    private static int playerTurn = 1;
    private static String env;
    private static int moveCounter = 0;
    private final JCheckBox autoCreateDataSet = new JCheckBox("Create Dataset");
    private final BoardNetworkCoordinator networkCoordinator;
    private final myPanel panel;
    private final State gameState;
    private final JFrame frame = new JFrame();
    private final JLabel whenAddingRecord = new JLabel("waiting...");
    private File dataFileDir;
    private File recordFile;
    private File evaluatorModel;
    private List<BoardWinPair> record = new ArrayList<>();
    private int boardWidth, boardHeight;

    /**
     * Game gui class constructor. Handles all the control buttons and gui liveliness.
     * <p>
     * Not used as logic block, i.e. doesn't do anything other than gui.
     *
     * @param boardWidth         Board width
     * @param boardHeight        Board height
     * @param env                Game data folder path
     * @param dataFileDir        Game data file
     * @param recordFile         Training data for evaluator nn
     * @param evaluatorModel     Evaluator nn save evaluatorModel
     * @param record             Evaluator nn training data in List data structure
     * @param networkCoordinator Coordinator between the game and the column chooser NN
     */
    public Board(int boardWidth, int boardHeight, String env, File dataFileDir, File recordFile, File evaluatorModel, List<BoardWinPair> record, BoardNetworkCoordinator networkCoordinator) {
        this.evaluatorModel = evaluatorModel;

        this.dataFileDir = dataFileDir;
        this.recordFile = recordFile;
        this.record = record;
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;

        gameState = new State(boardWidth, boardHeight);
        boardStacks = new Stack[boardWidth];
        for (int i = 0; i < boardWidth; i++) {
            boardStacks[i] = new Stack<>();
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
        Thread gameThread = new Thread(() -> {
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
        });
        gameThread.start();


        addWindowListener(this);
        Container container = frame.getContentPane();
        SpringLayout layout = new SpringLayout();
        container.setLayout(layout);

        final JCheckBox eval = new JCheckBox("Evaluate");


        JButton useRecords = new JButton("Use Records");
        useRecords.addActionListener(e -> EvaluatorNN.firstNeuralTest(getRecord(), boardWidth * boardHeight, evaluatorModel));
        container.add(useRecords);

        JButton createTrainingSetForChooser = new JButton("Create chooser training set");
        createTrainingSetForChooser.addActionListener(e -> createChooserTrainSet(gameState));
        container.add(createTrainingSetForChooser);

        JButton loadNet = new JButton("Load NN");
        loadNet.addActionListener(e -> {
            if (evaluatorModel.exists())
                EvaluatorNN.loadNN(evaluatorModel);
        });
        container.add(loadNet);

        JButton discardList = new JButton("new data");
        discardList.addActionListener(e -> {
            setRecord(new ArrayList<BoardWinPair>());

            whenAddingRecord.setText("now has " + record.size() + " records");
        });
        container.add(discardList);


        JButton calculate = new JButton("Foresee");
        calculate.addActionListener(e -> {
            if (gameState.checkWin().equals(CellState.EMPTY)) {
                double[] input = gameState.convertToArray();
                double out = doThing();
                BoardWinPair pair = new BoardWinPair(input, out);


                record.add(pair);
                whenAddingRecord.setText("now has " + record.size() + " records");
            }


        });

        JButton writeToFile = new JButton("Write to file");
        writeToFile.addActionListener(e -> {
            WriteToRecordsFile.writeRecords(record, recordFile);
            JOptionPane.showMessageDialog(null, "Done");
        });

        JButton loadLatest = new JButton("load latest");
        loadLatest.addActionListener(e -> {
            List<BoardWinPair> forLatest = record;
            gameState.fromArray(forLatest.get(forLatest.size() - 1).getBoard());
            panel.setState(gameState);
            repaint();
            JOptionPane.showMessageDialog(null, "loaded latest game");

        });


        container.add(calculate);
        container.add(eval);
        container.add(autoCreateDataSet);
        container.add(whenAddingRecord);
        JLabel player = new JLabel("currently: ");
        container.add(player);
        container.add(writeToFile);
        container.add(loadLatest);

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
        layout.putConstraint(SpringLayout.WEST, autoCreateDataSet, 0, SpringLayout.WEST, eval);
        layout.putConstraint(SpringLayout.NORTH, autoCreateDataSet, 15, SpringLayout.SOUTH, eval);
        layout.putConstraint(SpringLayout.WEST, loadLatest, 0, SpringLayout.WEST, createTrainingSetForChooser);
        layout.putConstraint(SpringLayout.NORTH, loadLatest, 15, SpringLayout.SOUTH, createTrainingSetForChooser);

        frame.setPreferredSize(new Dimension(350, 300));
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

    public static String getEnv() {
        return env;
    }

    public static void setEnv(String env) {
        Board.env = env;
    }

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

    public File getEvaluatorModel() {
        return evaluatorModel;
    }

    public void setEvaluatorModel(File evaluatorModel) {
        this.evaluatorModel = evaluatorModel;
    }

    private List<BoardWinPair> getRecord() {
        return record;
    }

    private void setRecord(List<BoardWinPair> record) {
        this.record = record;
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
     * Calculates with brute force the closeness to victory (by evaluating possibility tree)
     *
     * @return Returns the evaluator output as a double.
     */
    private double doThing() {
        CellState tes = gameState.checkWin();
        System.out.println(tes);
        WinAssessment.assessWin(gameState, -1);
        return WinAssessment.diff;
    }

    private void trainEvaluator(State gameState) {
        double[] input = gameState.convertToArray();
        double out = doThing();
        BoardWinPair pair = new BoardWinPair(input, out);
        this.record.add(pair);
        whenAddingRecord.setText("now has " + record.size() + " records");
        WriteToRecordsFile.writeRecords(this.record, this.recordFile);
        EvaluatorNN.addPair(pair);
        EvaluatorNN.train(evaluatorModel, gameState.getWidth() * gameState.getHeight());
    }

    /**
     * This calls the coordinator for the chooser neural network, and applies the chooser's output as the computer made move.
     */
    private void moveMade() {
        if (moveCounter > 30) {
            int choice = (int) (Math.random() * 7);
            while (!(gameState.makeMove(playerTurn, choice))) {
                choice = (int) (Math.random() * 7);
            }
            gameState.makeMove(playerTurn, choice);
        } else {
            System.out.println("entered move maker method in board");
            int result = networkCoordinator.getNNAction(gameState);
            if (result < 7 && result > -1) {

                if (gameState.makeMove(playerTurn, result)) {
                    moveCounter = 0;
                    panel.setState(gameState);
                    repaint();
                    playerTurn = -playerTurn;
                } else {
                    moveCounter++;
                    moveMade();
                }


            } else {
                System.err.println("Problem!!");
            }
        }
    }

    /**
     * Creates a training set of {@link BoardColumnPair} array that is passed to Coordinator for future use in training the network. This allows bigger training set while training, which makes the network's choices cleverer
     *
     * @param game The state of the game from which to create the training set.
     */
    private void createChooserTrainSet(State game) {

        BoardColumnPair boardColumnPairs = new BoardColumnPair(game.convertToArray(), EvaluatorNN.bestColumnFromHere(game));

        networkCoordinator.addPair(boardColumnPairs);
    }

    /**
     * Creates a single training {@link BoardColumnPair} that is passed to Coordinator and immediately trained with.
     *
     * @param game The state of the game from which to create the training set.
     */
    private void trainChooser(State game) {
        System.out.println("entered training chooser method in board");

        double[] column = EvaluatorNN.bestColumnFromHere(game);
        BoardColumnPair pair = new BoardColumnPair(game.convertToArray(), column);

        if (networkCoordinator.isChooserNull()) {
            System.err.println("chooser was null!");
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


        for (int i = 0; i < boardWidth; i++) {
            int x = ((getWidth() - 2 * 30) / boardWidth) * (i % boardWidth) + (int) (30 * 1.5 - 0.5);
            if (location.getX() > (x) && location.getX() < (x + 30)) {
                gameState.makeMove(playerTurn, i);
                playerTurn = -playerTurn;

                panel.setState(gameState);

                repaint();
                if (autoCreateDataSet.isSelected()) {
                    trainEvaluator(gameState);
                    createChooserTrainSet(gameState);
                    networkCoordinator.trainChooser();

                }
                if (playerTurn == -1) {
                    System.out.println("Computer making move");
                    moveMade();
                }

                break;
            }
        }
    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void windowClosing(WindowEvent e) {

        if (autoCreateDataSet.isSelected())
            networkCoordinator.trainChooser();
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
        final int size = 30;
        private final int w;
        private final int h;
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
