package com.seniorProject.manualGame;

import com.seniorProject.bruteForceCalculation.WinAssessment;
import com.seniorProject.data.write.WriteToRecordsFile;
import com.seniorProject.evaluator.EvaluatorNN;
import com.seniorProject.gameObjects.BoardColumnPair;
import com.seniorProject.gameObjects.BoardWinPair;
import com.seniorProject.gameObjects.CellState;
import com.seniorProject.gameObjects.State;
import com.seniorProject.moveMaker.BoardNetworkCoordinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is the main GUI creator and handles. The {@link myPanel myPanel} class is a JPanel that handles actually drawing the board. This class handles coordinating pieces of the program, except for the {@link com.seniorProject.moveMaker.ColumnChooser chooser}, which has its own {@link BoardNetworkCoordinator coordinator}.
 * <p>
 * I did it like this because this is how I do GUI when I have to make it from scratch (without any gui creator like WindowBuilder or netbeans ide).
 * Created by Itamar.
 */
public class Board extends JFrame implements MouseListener, WindowListener {

    private static final Logger log = LoggerFactory.getLogger(Board.class);
    /**
     * An integer that represents who's turn it is. 1 is blue, -1 is red.
     */
    private static int playerTurn = 1;
    /**
     * The path to the metadata directory
     */
    private static String env;
    /**
     * Counts number of moves made.
     */
    private static int moveCounter = 0;
    final JCheckBox vsC = new JCheckBox("Vs computer");
    /**
     * Checkbox in debugging and control panel for creating training com.seniorProject.data while playing.
     */
    private final JCheckBox autoCreateDataSet = new JCheckBox("Create Dataset");
    /**
     * A coordinator between the Board and the {@link com.seniorProject.moveMaker.ColumnChooser} that beats the human player.
     */
    private final BoardNetworkCoordinator networkCoordinator;
    /**
     * The panel on which the board is drawn.<br/>
     * See {@link Board.myPanel} for more details.
     */
    private final myPanel panel;
    /**
     * The {@link State} of the board of the game.
     */
    private final State gameState;
    /**
     * The control frame.
     */
    private final JFrame frame = new JFrame();
    /**
     * {@link com.seniorProject.evaluator.EvaluatorNN} training set counter.
     */
    private final JLabel whenAddingRecord = new JLabel("waiting...");
    /**
     * Directory of metadata
     */
    private File dataFileDir;
    /**
     * File containing training set for {@link com.seniorProject.evaluator.EvaluatorNN} neural network. The file is written by {@link WriteToRecordsFile}.
     */
    private File recordFile;
    /**
     * File containing the saved neural network model for {@link com.seniorProject.evaluator.EvaluatorNN}.
     */
    private File evaluatorModel;
    /**
     * A {@link List} of the training set for {@link com.seniorProject.evaluator.EvaluatorNN#net}.
     */
    private List<BoardWinPair> record = new ArrayList<>();
    /**
     * The number of columns on the board.
     */
    private int boardWidth;
    /**
     * The number of rows on the board.
     */
    private int boardHeight;

    /**
     * Game gui class constructor. Handles all the control buttons and gui liveliness.
     * <p>
     * Not used as logic block, i.e. doesn't do anything other than gui.
     *
     * @param boardWidth         Board width
     * @param boardHeight        Board height
     * @param env                Game com.seniorProject.data folder path
     * @param dataFileDir        Game com.seniorProject.data file
     * @param recordFile         Training com.seniorProject.data for com.seniorProject.evaluator nn
     * @param evaluatorModel     Evaluator nn save evaluatorModel
     * @param record             Evaluator nn training com.seniorProject.data in List com.seniorProject.data structure
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
        setSize(boardWidth * 50 + 100, boardHeight * 50 + 100);
        panel = new myPanel(boardWidth, boardHeight);


        getContentPane().add(panel, BorderLayout.CENTER);


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

        JButton discardList = new JButton("new game");
        discardList.addActionListener(e -> {
            gameState.newGame(boardWidth, boardHeight);
//            setRecord(new ArrayList<>());
//
//            whenAddingRecord.setText("now has " + record.size() + " records");
        });
        container.add(discardList);


        JButton calculate = new JButton("Foresee");
        calculate.addActionListener(e -> {
            if (gameState.checkWin().equals(CellState.EMPTY)) {
                double[] input = gameState.convertToArray();
                double out = assessRedWin();
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

        JButton trainChooser = new JButton("Train Chooser");
        writeToFile.addActionListener(e -> networkCoordinator.trainChooser());


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
        container.add(vsC);
        container.add(whenAddingRecord);
        JLabel player = new JLabel("currently: ");
        container.add(player);
        container.add(writeToFile);
        container.add(loadLatest);
        container.add(trainChooser);


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
        layout.putConstraint(SpringLayout.WEST, trainChooser, 0, SpringLayout.WEST, loadLatest);
        layout.putConstraint(SpringLayout.NORTH, trainChooser, 15, SpringLayout.SOUTH, loadLatest);
        layout.putConstraint(SpringLayout.EAST, vsC, -20, SpringLayout.WEST, trainChooser);
        layout.putConstraint(SpringLayout.NORTH, vsC, 15, SpringLayout.NORTH, trainChooser);

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


//

    public static String getEnv() {
        return env;
    }

    public static void setEnv(String env) {
        Board.env = env;
    }

    /**
     * Calculates with brute force the closeness to victory (by evaluating possibility tree)
     *
     * @return Returns the com.seniorProject.evaluator output as a double.
     * @see WinAssessment#assessWin(State, int)
     */
    private double assessRedWin() {
        CellState tes = gameState.checkWin();
        System.out.println(tes);
        WinAssessment.assessWin(gameState, playerTurn);
        return WinAssessment.getDiff();
    }

    /**
     * Trains the {@link EvaluatorNN#net} neural network on the existing training set, as well as the passed state.
     *
     * @param gameState the state of the game.
     */
    private void trainEvaluator(State gameState) {
        double[] input = gameState.convertToArray();
        double out = assessRedWin();
        BoardWinPair pair = new BoardWinPair(input, out);
        record.add(pair);
        whenAddingRecord.setText("now has " + record.size() + " records");
        WriteToRecordsFile.writeRecords(record, recordFile);
        EvaluatorNN.addPair(pair);
        //EvaluatorNN.train(evaluatorModel, gameState.getWidth() * gameState.getHeight());

    }

    /**
     * This calls the coordinator for the chooser neural network, and applies the chooser's output as the computer made move.
     */
    private void moveMade() {

        System.out.println("entered move maker method in board");
        int result = networkCoordinator.getNNAction(gameState);
        log.debug("the result was {}", result);

        if (result < 7 && result > -1) {

            if (gameState.makeMove(playerTurn, result)) {
                panel.setState(gameState);
                repaint();
                playerTurn = -playerTurn;
            } else if (result > 1 && gameState.makeMove(playerTurn, result - 1)) {
                panel.setState(gameState);
                repaint();
                playerTurn = -playerTurn;
            } else if (result < 5 && gameState.makeMove(playerTurn, result + 1)) {
                panel.setState(gameState);
                repaint();
                playerTurn = -playerTurn;
            }


        } else {
            log.error("Problem. Invalid result, {}", result);
            moveMade();
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

        double column = EvaluatorNN.bestColumnFromHere(game);
        BoardColumnPair pair = new BoardColumnPair(game.convertToArray(), column);

        if (networkCoordinator.isChooserNull()) {
            System.err.println("chooser was null!");
            networkCoordinator.createChooser(boardHeight, boardWidth);
        }
        networkCoordinator.trainChooser(pair);


    }

    /**
     * Copies the metadata from {@link Board#env} to a packable place.
     */
    private void copyToResources() {
        String resourcesPath = (Board.class.getResource("/").getPath());
        System.out.println(resourcesPath.substring(1));

        Arrays.asList(dataFileDir.listFiles()).forEach(file -> {
            if (file.isFile()) {
                String name = file.getName();
                if (name.endsWith(".txt") || name.endsWith(".zip") || name.endsWith(".bin")) {
                    Path destination = Paths.get(resourcesPath.substring(1) + name);

                    try {
                        Path flag = Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println(flag);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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

    /**
     * {@inheritDoc}
     */
    public void mouseClicked(MouseEvent e) {


    }

    /**
     * {@inheritDoc}
     */
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
                if (!gameState.checkWin().equals(CellState.EMPTY)) {
                    String message = "Game over. " + gameState.checkWin() + " has won\nNew game?";
                    int retval = JOptionPane.showConfirmDialog(null, message, "Game Over!", JOptionPane.YES_NO_OPTION);
                    if (retval == JOptionPane.YES_OPTION) {
                        gameState.newGame(boardWidth, boardHeight);
                    } else if (retval == JOptionPane.NO_OPTION) {
                        JOptionPane.showMessageDialog(null, "Ok, bye! Hope you enjoyed");
                        System.exit(0);
                    }

                }
                if (vsC.isSelected()) {
                    if (playerTurn == -1) {
                        log.info("thinking... {}", playerTurn);

                        moveMade();
                    }
                    log.info("now it's: {}", playerTurn);
                    if (playerTurn == 1 && autoCreateDataSet.isSelected()) {
                        BoardColumnPair playerPair = new BoardColumnPair(gameState.convertToArray(), i);
                        networkCoordinator.addPair(playerPair);
                        log.info("added record :)");
                    }
                } else {
                    if (playerTurn == 1 && autoCreateDataSet.isSelected()) {
                        BoardColumnPair playerPair = new BoardColumnPair(gameState.convertToArray(), i);
                        networkCoordinator.addPair(playerPair);
                        log.info("added record :)");
                    }
                }


                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void mouseEntered(MouseEvent e) {

    }

    /**
     * {@inheritDoc}
     */
    public void mouseExited(MouseEvent e) {

    }

    /**
     * {@inheritDoc}
     */
    public void windowClosing(WindowEvent e) {
        networkCoordinator.trainChooser();
        //EvaluatorNN.train(evaluatorModel, boardHeight * boardWidth);
        copyToResources();
        System.exit(0);
    }

    /**
     * {@inheritDoc}
     */
    public void windowOpened(WindowEvent e) {

    }

    /**
     * {@inheritDoc}
     */
    public void windowClosed(WindowEvent e) {

    }

    /**
     * {@inheritDoc}
     */
    public void windowIconified(WindowEvent e) {

    }

    /**
     * {@inheritDoc}
     */
    public void windowDeiconified(WindowEvent e) {

    }

    /**
     * {@inheritDoc}
     */
    public void windowActivated(WindowEvent e) {

    }

    /**
     * {@inheritDoc}
     */
    public void windowDeactivated(WindowEvent e) {

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

    class myPanel extends JPanel {
        final int size = 30;
        private final int w;
        private final int h;
        private State state;

        public myPanel(int w, int h) {
            this.w = w;
            this.h = h;
        }


        /**
         * Setter for the state.
         *
         * @param state the state to set as {@link myPanel#gameState}.
         */
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
