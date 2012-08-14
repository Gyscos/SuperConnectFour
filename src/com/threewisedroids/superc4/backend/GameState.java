package com.threewisedroids.superc4.backend;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Point;

public class GameState {

    int[][]                 board;

    int                     nextPlayer;
    boolean                 fillCorners;

    boolean                 victorious;
    List<Point>             winningCells         = new ArrayList<Point>();

    LinkedList<Point>       history              = new LinkedList<Point>();

    Runnable                onInfoUpdate;

    static int              directions[][]       = new int[8][2];

    public final static int DIR_LEFT             = 0;
    public final static int DIR_RIGHT            = 1;
    public final static int DIR_UP               = 2;
    public final static int DIR_DOWN             = 3;
    public final static int DIR_UL               = 4;
    public final static int DIR_DR               = 5;
    public final static int DIR_DL               = 6;
    public final static int DIR_UR               = 7;

    static int              free_heuristics[]    = new int[4];
    static int              hostile_heuristics[] = new int[3];

    static {
        directions[DIR_LEFT][0] = -1;
        directions[DIR_LEFT][1] = 0;

        directions[DIR_RIGHT][0] = 1;
        directions[DIR_RIGHT][1] = 0;

        directions[DIR_UP][0] = 0;
        directions[DIR_UP][1] = -1;

        directions[DIR_DOWN][0] = 0;
        directions[DIR_DOWN][1] = 1;

        directions[DIR_UL][0] = -1;
        directions[DIR_UL][1] = -1;

        directions[DIR_DR][0] = 1;
        directions[DIR_DR][1] = 1;

        directions[DIR_DL][0] = -1;
        directions[DIR_DL][1] = 1;

        directions[DIR_UR][0] = 1;
        directions[DIR_UR][1] = -1;

        free_heuristics[0] = 1;
        free_heuristics[1] = 3;
        free_heuristics[2] = 6;
        free_heuristics[3] = 1000;

        hostile_heuristics[0] = 2;
        hostile_heuristics[1] = 6;
        hostile_heuristics[2] = 100;
    }

    public GameState(int size, boolean fillCorners) {
        board = new int[size][size];
        this.fillCorners = fillCorners;

        init();
    }

    public boolean canPlay(int x, int y) {
        if (!isInRange(x, y))
            return false;

        if (getCell(x, y) != 0)
            return false;

        // Check both vertical and horizontal
        for (int i = 0; i < 4; i++) {
            int nX = x, nY = y;
            while (true) {
                nX += directions[i][0];
                nY += directions[i][1];

                if (!isInRange(nX, nY))
                    return true;

                if (getCell(nX, nY) == 0)
                    break;
            }
        }

        return false;
    }

    public void checkVictory() {
        int x = history.getLast().x;
        int y = history.getLast().y;

        if (!isInRange(x, y))
            return;

        int cell = getCell(x, y);

        if (cell == 0 || cell == 3)
            return;

        for (int direction = 0; direction < 8; direction += 2) {
            // In all directions...
            int countA = countLine(x, y, direction);
            int countB = countLine(x, y, direction + 1);

            System.out.println("CountA : " + countA + " ; CountB : " + countB);
            if (countA + countB > 4) {
                victorious = true;

                for (int i = 1 - countB; i < countA; i++) {
                    int nX = x + i * directions[direction][0];
                    int nY = y + i * directions[direction][1];
                    winningCells.add(new Point(nX, nY));
                }
                return;
            }
        }

        return;
    }

    public void clear() {
        history.clear();
        init();

        victorious = false;
        winningCells.clear();

        if (onInfoUpdate != null)
            onInfoUpdate.run();
    }

    @Override
    public GameState clone() {
        int gridsize = getGridSize();

        GameState result = new GameState(gridsize, fillCorners);
        for (Point p : history)
            result.play(p.x, p.y);

        return result;
    }

    int countLine(int x, int y, int direction) {
        int color = getCell(x, y);
        for (int i = 0; i < 4; i++) {
            x += directions[direction][0];
            y += directions[direction][1];
            if (!isInRange(x, y) || getCell(x, y) != color)
                return i + 1;
        }
        return 4;
    }

    public int evaluate(int x, int y) {
        // Check all monochrome 4-length segments

        int score = 0;

        // Check all directions
        for (int direction = 0; direction < 8; direction += 2) {
            // Shift can range from 0 to 4
            for (int shift = 0; shift < 4; shift++) {
                // Now, check the line...
                score += evaluateSegment(x, y, direction, shift);
            }
        }

        return score;
    }

    int evaluateSegment(int x, int y, int direction, int shift) {
        int length = 0;
        int type = 1;

        int color = 0;

        for (int i = 0; i < 4; i++) {
            int offset = i - shift;
            int nX = x + offset * directions[direction][0];
            int nY = y + offset * directions[direction][1];

            if (!isInRange(nX, nY))
                return 0;

            int cell = getCell(nX, nY);

            if (cell == 3)
                return 0;

            if (cell == 0)
                continue;

            if (color == 0)
                color = cell;
            else if (color != cell)
                return 0;

            length++;

            if (cell != nextPlayer)
                type = 2;
        }

        // System.out.println("@ " + x + ":" + y + " -> " + shift + "@" +
        // direction + " : " + length + " / " + type);

        if (type == 1)
            return free_heuristics[length];
        else
            // (type == 2)
            return hostile_heuristics[length - 1];
    }

    public int getCell(int x, int y) {
        return board[x][y];
    }

    public int getFirstFreeCell(int x, int y, int direction) {
        int gridsize = getGridSize();
        for (int i = 0; i < gridsize; i++) {
            int nX = x + directions[direction][0] * i;
            int nY = y + directions[direction][1] * i;

            if (!isInRange(nX, nY))
                return -1;

            if (getCell(nX, nY) == 0)
                return i;
        }

        return -1;
    }

    public int getGridSize() {
        return board.length;
    }

    public int getNextPlayer() {
        return nextPlayer;
    }

    public List<Point> getWinningCells() {
        return winningCells;
    }

    public boolean hasVictory() {
        return victorious;
    }

    void init() {
        int gridsize = getGridSize();

        for (int i = 0; i < gridsize; i++)
            for (int j = 0; j < gridsize; j++)
                board[i][j] = 0;

        if (fillCorners) {
            board[2][1] = 3;
            board[gridsize - 2][2] = 3;
            board[gridsize - 3][gridsize - 2] = 3;
            board[1][gridsize - 3] = 3;
        }

        nextPlayer = 1;
    }

    public boolean isInRange(int x, int y) {
        if (x < 0 || y < 0)
            return false;
        int gridSize = getGridSize();
        if (x >= gridSize || y >= gridSize)
            return false;

        return true;
    }

    public void load(String data) {
        String[] moves = data.split(";");
        for (String move : moves) {
            if (move == "")
                continue;

            String[] coords = move.split(":");
            play(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
        }
    }

    void next() {
        nextPlayer = 3 - nextPlayer;
    }

    public void play(int x, int y) {
        board[x][y] = nextPlayer;
        history.addLast(new Point(x, y));

        checkVictory();

        next();

        if (onInfoUpdate != null)
            onInfoUpdate.run();
    }

    public String save() {
        String result = "";
        for (Point p : history)
            result += p.x + ":" + p.y + ";";
        return result;
    }

    public void setInfoUpdate(Runnable onInfoUpdate) {
        this.onInfoUpdate = onInfoUpdate;
    }

    public void undo() {
        if (history.isEmpty())
            return;

        Point p = history.removeLast();
        board[p.x][p.y] = 0;

        victorious = false;
        winningCells.clear();

        next();

        if (onInfoUpdate != null)
            onInfoUpdate.run();
    }
}
