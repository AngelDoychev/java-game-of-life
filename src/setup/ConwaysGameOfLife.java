package setup;

import global.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class ConwaysGameOfLife extends JFrame implements ActionListener {

    private final JMenuItem onFileExit;
    private final JMenuItem onGamePlay;
    private final JMenuItem onGameStop;
    private final JMenuItem onGameReset;
    private final GameBoard gameBoard;
    private Thread game;

    public static void main(String[] args) {
        // Setup the swing specifics
        JFrame game = new ConwaysGameOfLife();
        game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        game.setTitle("Conway's Game of Life");
        game.setSize(Constants.DEFAULT_WINDOW_SIZE);
        game.setMinimumSize(Constants.MINIMUM_WINDOW_SIZE);
        game.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - game.getWidth()) / 2,
                (Toolkit.getDefaultToolkit().getScreenSize().height - game.getHeight()) / 2);
        game.setVisible(true);
    }

    public ConwaysGameOfLife() {
        // Setup menu
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenu gameMenu = new JMenu("Game");
        menuBar.add(gameMenu);
        this.onFileExit = new JMenuItem("Exit");
        this.onFileExit.addActionListener(this);
        fileMenu.add(new JSeparator());
        fileMenu.add(this.onFileExit);
        this.onGamePlay = new JMenuItem("Play");
        this.onGamePlay.addActionListener(this);
        this.onGameStop = new JMenuItem("Stop");
        this.onGameStop.setEnabled(false);
        this.onGameStop.addActionListener(this);
        this.onGameReset = new JMenuItem("Reset");
        this.onGameReset.addActionListener(this);
        gameMenu.add(new JSeparator());
        gameMenu.add(this.onGamePlay);
        gameMenu.add(this.onGameStop);
        gameMenu.add(this.onGameReset);
        // Setup game board
        this.gameBoard = new GameBoard();
        add(this.gameBoard);
    }

    public void setGameBeingPlayed(boolean isBeingPlayed) {
        if (isBeingPlayed) {
            this.onGamePlay.setEnabled(false);
            this.onGameStop.setEnabled(true);
            this.game = new Thread(this.gameBoard);
            this.game.start();
        } else {
            this.onGamePlay.setEnabled(true);
            this.onGameStop.setEnabled(false);
            this.game.interrupt();
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource().equals(this.onFileExit)) {
            // Exit the game
            System.exit(0);
        } else if (actionEvent.getSource().equals(this.onGameReset)) {
            this.gameBoard.resetBoard();
            this.gameBoard.repaint();
        } else if (actionEvent.getSource().equals(this.onGamePlay)) {
            setGameBeingPlayed(true);
        } else if (actionEvent.getSource().equals(this.onGameStop)) {
            setGameBeingPlayed(false);
        }
    }

    private static class GameBoard extends JPanel implements ComponentListener, MouseListener, MouseMotionListener, Runnable {
        private Dimension gameBoardSize = null;
        private final ArrayList<Point> point = new ArrayList<Point>(0);

        public GameBoard() {
            // Add resizing listener
            addComponentListener(this);
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        private void updateArraySize() {
            ArrayList<Point> removeList = new ArrayList<Point>(0);
            for (Point current : this.point) {
                if ((current.x > this.gameBoardSize.width - 1) || (current.y > this.gameBoardSize.height - 1)) {
                    removeList.add(current);
                }
            }
            this.point.removeAll(removeList);
            repaint();
        }

        public void addPoint(int x, int y) {
            if (!this.point.contains(new Point(x, y))) {
                this.point.add(new Point(x, y));
            }
            repaint();
        }

        public void addPoint(MouseEvent mouseEvent) {
            int x = mouseEvent.getPoint().x / Constants.BLOCK_SIZE - 1;
            int y = mouseEvent.getPoint().y / Constants.BLOCK_SIZE - 1;
            if ((x >= 0) && (x < this.gameBoardSize.width) && (y >= 0) && (y < this.gameBoardSize.height)) {
                addPoint(x, y);
            }
        }

        public void resetBoard() {
            this.point.clear();
        }

        @Override
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            try {
                for (Point newPoint : this.point) {
                    // Draw new point
                    graphics.setColor(Color.black);
                    graphics.fillRect(Constants.BLOCK_SIZE + (Constants.BLOCK_SIZE * newPoint.x),
                            Constants.BLOCK_SIZE + (Constants.BLOCK_SIZE * newPoint.y),
                            Constants.BLOCK_SIZE, Constants.BLOCK_SIZE);
                }
            } catch (ConcurrentModificationException concurrentModificationException) {
            }
            // Setup grid
            graphics.setColor(Color.BLACK);
            for (int i = 0; i <= this.gameBoardSize.width; i++) {
                graphics.drawLine(((i * Constants.BLOCK_SIZE) + Constants.BLOCK_SIZE),
                        Constants.BLOCK_SIZE, (i * Constants.BLOCK_SIZE) + Constants.BLOCK_SIZE,
                        Constants.BLOCK_SIZE + (Constants.BLOCK_SIZE * this.gameBoardSize.height));
            }
            for (int i = 0; i <= this.gameBoardSize.height; i++) {
                graphics.drawLine(Constants.BLOCK_SIZE,
                        ((i * Constants.BLOCK_SIZE) + Constants.BLOCK_SIZE),
                        Constants.BLOCK_SIZE * (this.gameBoardSize.width + 1),
                        ((i * Constants.BLOCK_SIZE) + Constants.BLOCK_SIZE));
            }
        }

        @Override
        public void componentResized(ComponentEvent componentEvent) {
            // Set up the game board size with proper boundaries
            this.gameBoardSize = new Dimension(getWidth() / Constants.BLOCK_SIZE - 2,
                    getHeight() / Constants.BLOCK_SIZE - 2);
            updateArraySize();
        }

        @Override
        public void componentMoved(ComponentEvent componentEvent) {
        }

        @Override
        public void componentShown(ComponentEvent componentEvent) {
        }

        @Override
        public void componentHidden(ComponentEvent componentEvent) {
        }

        @Override
        public void mouseClicked(MouseEvent componentEvent) {
        }

        @Override
        public void mousePressed(MouseEvent componentEvent) {
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
            // Mouse was released (user clicked and added an alive cell)
            addPoint(mouseEvent);
        }

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {
        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {
        }

        @Override
        public void mouseDragged(MouseEvent mouseEvent) {
            // Mouse is being dragged, user wants multiple selections
            addPoint(mouseEvent);
        }

        @Override
        public void mouseMoved(MouseEvent mouseEvent) {
        }

        @Override
        public void run() {
            boolean[][] gameBoard = new boolean[this.gameBoardSize.width + 2][this.gameBoardSize.height + 2];
            for (Point current : this.point) {
                gameBoard[current.x + 1][current.y + 1] = true;
            }
            ArrayList<Point> survivingCells = new ArrayList<Point>(0);
            // Iterate through the array, follow game of life rules
            for (int i = 1; i < gameBoard.length - 1; i++) {
                for (int j = 1; j < gameBoard[0].length - 1; j++) {
                    int surroundingAliveCells = getSurroundingAliveCellsCount(gameBoard, i, j);
                    if (gameBoard[i][j]) {
                        // Cell is alive, Can the cell live? (2-3)
                        if ((surroundingAliveCells == 2) || (surroundingAliveCells == 3)) {
                            survivingCells.add(new Point(i - 1, j - 1));
                        }
                    } else {
                        // Cell is dead, will the cell be given birth? (3)
                        if (surroundingAliveCells == 3) {
                            survivingCells.add(new Point(i - 1, j - 1));
                        }
                    }
                }
            }
            resetBoard();
            this.point.addAll(survivingCells);
            repaint();
            try {
                int movesPerSecond = 3;
                Thread.sleep(1000 / movesPerSecond);
                run();
            } catch (InterruptedException ex) {
                // Stop sleep process
            }
        }

        private static int getSurroundingAliveCellsCount(boolean[][] gameBoard, int i, int j) {
            int surroundingAliveCells = 0;
            if (gameBoard[i - 1][j - 1]) {
                surroundingAliveCells++;
            }
            if (gameBoard[i - 1][j]) {
                surroundingAliveCells++;
            }
            if (gameBoard[i - 1][j + 1]) {
                surroundingAliveCells++;
            }
            if (gameBoard[i][j - 1]) {
                surroundingAliveCells++;
            }
            if (gameBoard[i][j + 1]) {
                surroundingAliveCells++;
            }
            if (gameBoard[i + 1][j - 1]) {
                surroundingAliveCells++;
            }
            if (gameBoard[i + 1][j]) {
                surroundingAliveCells++;
            }
            if (gameBoard[i + 1][j + 1]) {
                surroundingAliveCells++;
            }
            return surroundingAliveCells;
        }
    }
}
