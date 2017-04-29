import com.diffplug.common.base.TreeNode;
import data.BoardWinPair;
import data.restore.RestoreRecordFile;
import data.write.WriteToRecordsFile;
import neuralNets.NetworkTest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by itamar on 21-Mar-17.
 */
public class Board extends JFrame implements MouseListener, WindowListener {
    public static Stack<Integer>[] boardStacks;
    //<temporary>
    public static File dataFileDir;
    public static File recordFile;
    public static File model;
    protected static int playerTurn = 1;
    private static List<BoardWinPair> record = new ArrayList<BoardWinPair>();
    private static String env;
    private myPanel panel;
    private int w, h;
    private State s;
    private List<move> played = new ArrayList<move>();
    private JFrame frame = new JFrame();
    private JLabel whenAddingRecord = new JLabel("waiting...");
    //</temporary>


    public Board(int w, int h) {
        //<temporary>
        env = System.getenv("AppData") + "\\SeniorProjectDir\\";
        dataFileDir = new File(System.getenv("AppData") + "\\SeniorProjectDir\\");
        if (!dataFileDir.exists()) {
            dataFileDir.mkdir();
        } else if (!dataFileDir.isDirectory()) {
            dataFileDir.delete();
            dataFileDir = new File(System.getenv("AppData") + "\\SeniorProjectDir\\");
            dataFileDir.mkdir();
        }
        recordFile = new File(env + "\\records.txt");
        if (!recordFile.exists()) {
            try {
                recordFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        model = new File(env + "\\model.zip");
        //NetworkTest.loadNet(model);

        List<BoardWinPair> readingList = RestoreRecordFile.readRecords(recordFile);
        Board.record = readingList;

        //</temporary>

        this.w = w;
        this.h = h;
        s = new State(w, h);
        boardStacks = new Stack[w];
        for (int i = 0; i < w; i++) {
            boardStacks[i] = new Stack<Integer>();
            boardStacks[i].setSize(h);
        }
        setSize(w * 50 + 100, h * 50 + 100);
        panel = new myPanel(w, h);


        getContentPane().add(panel, BorderLayout.CENTER);

        WinAssessment.fill = new boolean[h];

        panel.setState(s);

        setLocationRelativeTo(null);
        setVisible(true);
        addMouseListener(this);
        Thread gameThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    panel.setState(s);

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


        //setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(this);

        Container container = frame.getContentPane();
        SpringLayout layout = new SpringLayout();
        container.setLayout(layout);

        final JCheckBox eval = new JCheckBox("Evaluate");

        JButton useRecords = new JButton("Use Records");
        useRecords.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NetworkTest.firstNeuralTest(record, w * h, model);
            }
        });
        container.add(useRecords);

        JButton loadNet = new JButton("Load NN");
        loadNet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (model.exists())
                    NetworkTest.loadNN(model);
            }
        });
        container.add(loadNet);

        JButton calculate = new JButton("Foresee");
        calculate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (s.checkWin().equals(CellState.EMPTY)) {
                    double[] input = s.convertToArray();
                    long tick = System.currentTimeMillis();
                    int out = doThing();
                    long tock = System.currentTimeMillis();
                    long distance = tock - tick;
                    BoardWinPair pair = new BoardWinPair(input, out);
                    if (eval.isSelected()) {
                        NetworkTest.testNetwork(pair);
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

        frame.setPreferredSize(new Dimension(350, 200));
        double x = getContentPane().getLocationOnScreen().getX() + (w * 50 + 100);
        double y = getContentPane().getLocationOnScreen().getY() + 10;
        frame.setLocation((int) (x + 10), (int) y);
        frame.pack();

        if (record.size() != 0) {
            whenAddingRecord.setText("now has " + record.size() + " records");
        }


        frame.setVisible(true);

        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

    }

    public State getS() {
        return s;
    }

    public int doThing() {
        CellState tes = s.checkWin();
        System.out.println(tes);
        TreeNode<State> future = WinAssessment.assessWin(s, 1, panel);
        return WinAssessment.diff;
    }

    @Override
    public void paintComponents(Graphics g) {
        super.paintComponents(g);
        double x = getContentPane().getLocationOnScreen().getX() + (w * 50 + 100);
        double y = getContentPane().getLocationOnScreen().getY() + 10;
        frame.setLocation((int) (x + 10), (int) y);
        frame.pack();
        frame.repaint();
    }

    public void mouseClicked(MouseEvent e) {


    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {
        Point location = e.getPoint();
        int unitW = getWidth() / w;
        int col = (int) (location.getX() / unitW + 30 * 1.5);
        int row = (int) (location.getY() / h + 30 * 1.5);
        move m = new move(row, col, playerTurn);


        for (int i = 0; i < w; i++) {
            int x = ((getWidth() - 2 * 30) / w) * (i % w) + (int) (30 * 1.5 - 0.5);
            if (location.getX() > (x) && location.getX() < (x + 30)) {
                s.makeMove(playerTurn, i);
                playerTurn = -playerTurn;
                played.add(m);
                panel.setState(s);
                repaint();
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

    class move {
        int row, col, player;

        public move(int row, int col, int player) {
            this.row = row;
            this.col = col;
            this.player = player;
        }
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

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.setColor(Color.black);
            for (int i = 0; i < w * h; i++) {
                g.drawOval(((getWidth() - 2 * size) / w) * (i % w) + (int) (size * 1.5 - 0.5), (getWidth() - size * 2) / w * (i / w) + (int) (size * 1.5 - 0.5), size + 1, size + 1);

            }
            if (state.getW() != 0) {
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
