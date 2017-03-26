import com.diffplug.common.base.TreeNode;
import javafx.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Created by itamar on 21-Mar-17.
 */
public class Board extends JFrame implements MouseListener {
    public static Stack<Integer>[] boardStacks;
    private myPanel panel;
    private int w, h;
    private State s;

    public State getS() {
        return s;
    }

    public Board(int w, int h) {
        this.w = w;
        this.h = h;
        s = new State(w, h);
        boardStacks = new Stack[w];
        for (int i = 0; i < w; i++) {
            boardStacks[i] = new Stack<Integer>();
            boardStacks[i].setSize(h);
        }
        setSize(500, 500);
        panel = new myPanel(w, h);

        JPanel buttonPanel = new JPanel();

        JButton calculate = new JButton("Foresee");
        buttonPanel.add(calculate);

        SpringLayout layout = new SpringLayout();
        buttonPanel.setLayout(layout);


        //layout.putConstraint(SpringLayout.EAST,calculate,buttonPanel.getWidth()/2-10,SpringLayout.EAST,buttonPanel);
        layout.putConstraint(SpringLayout.WEST, calculate, buttonPanel.getWidth() / 2, SpringLayout.WEST, buttonPanel);
        layout.putConstraint(SpringLayout.NORTH, calculate, 10, SpringLayout.NORTH, buttonPanel);
        //layout.putConstraint(SpringLayout.EAST,calculate,buttonPanel.getWidth()/2-10,SpringLayout.EAST,buttonPanel);
        //getContentPane().add(buttonPanel,BorderLayout.NORTH);
        JFrame frame = new JFrame();
        frame.getContentPane().add(buttonPanel);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        getContentPane().add(panel, BorderLayout.CENTER);


        s.makeMove(1, 1);
        s.makeMove(-1, 1);
        s.makeMove(1, 2);
        s.makeMove(-1, 1);
        s.makeMove(1, 3);
        s.makeMove(-1, 1);
//        s.makeMove(1, 4);
        panel.setState(s);
        calculate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doThing();
            }
        });

        setVisible(true);
        addMouseListener(this);
        Thread gameThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    panel.setState(s);
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


        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

    public int doThing() {
        WinAssesment.cellState tes = s.checkWin();
        System.out.println(tes);
        TreeNode<State> future = WinAssesment.assessWin(s, 1);
        System.out.println("done");

        return WinAssesment.diff;
    }

    private List<move> played = new ArrayList<move>();

    public void mouseClicked(MouseEvent e) {
        Point location = e.getPoint();
        int unitW = getWidth() / w;
        int col = (int) (location.getX() / unitW + 30 * 1.5);
        int row = (int) (location.getY() / h + 30 * 1.5);
        move m = new move(row, col, playerTurn);
        playerTurn = -playerTurn;
        played.add(m);
        s.makeMove(playerTurn, (int) (location.getX() / unitW));
    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    protected static int playerTurn = 1;

    class move {
        int row, col, player;

        public move(int row, int col, int player) {
            this.row = row;
            this.col = col;
            this.player = player;
        }
    }

    class myPanel extends JPanel {
        private int w, h;
        protected int size = 30;
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
                g.drawOval(((getWidth() - 2 * size) / w) * (i % w) + (int) (size * 1.5), (getWidth() - size * 2) / w * (i / w) + (int) (size * 1.5), size, size);

            }
            if (state.getW() != 0) {
                for (int i = 0; i < h; i++) {
                    for (int j = 0; j < w; j++) {
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
