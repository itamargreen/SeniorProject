import com.diffplug.common.base.TreeNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * Created by itamar on 21-Mar-17.
 */
public class Board extends JFrame implements MouseListener {
    public static Stack<Integer>[] boardStacks;
    protected static int playerTurn = 1;
    private myPanel panel;
    private int w, h;
    private State s;
    private List<move> played = new ArrayList<move>();

    public Board(int w, int h) {
        this.w = w;
        this.h = h;
        s = new State(w, h);
        boardStacks = new Stack[w];
        for (int i = 0; i < w; i++) {
            boardStacks[i] = new Stack<Integer>();
            boardStacks[i].setSize(h);
        }
        setSize(w*50+100, h*50+100);
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

        WinAssessment.fill = new boolean[h];

        panel.setState(s);
        calculate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                double[] input = s.convertToArray();
                int out = doThing();
                RegressionSum.nnThings(input,new double[]{out});
            }
        });

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


        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

    public State getS() {
        return s;
    }

    public int doThing() {
        WinAssessment.cellState tes = s.checkWin();
        System.out.println(tes);
        TreeNode<State> future = WinAssessment.assessWin(s, 1, panel);


        return WinAssessment.diff;
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
        playerTurn = -playerTurn;
        played.add(m);

        for (int i = 0; i < w; i++) {
            int x = ((getWidth() - 2 * 30) / w) * (i % w) + (int) (30 * 1.5 - 0.5);
            if (location.getX() > (x) && location.getX() < (x + 30)) {
                s.makeMove(playerTurn, i);
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
