package com.threewisedroids.superc4.backend.ai;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;

import com.threewisedroids.superc4.backend.GameState;

public class AI
{
    boolean running;

    List<Integer> evaluateMoves(GameState state, List<Point> moves) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (Point p : moves)
            result.add(state.evaluate(p.x, p.y));
        return result;
    }

    int findMin(int[] list) {
        int min = 0;
        int minId = 0;
        for (int i = 0; i < list.length; i++)
            if (list[i] < min) {
                min = list[i];
                minId = i;
            }
        return minId;
    }

    int[] getBest(List<Integer> list, int n) {
        int[] bestIds = new int[n];
        int[] bestValues = new int[n];

        for (int i = 0; i < list.size(); i++) {
            int minId = findMin(bestValues);
            if (list.get(i) > bestValues[minId]) {
                bestValues[minId] = list.get(i);
                bestIds[minId] = i;
            }
        }

        return bestIds;
    }

    List<Point> getLegalMoves(GameState state) {
        ArrayList<Point> result = new ArrayList<Point>();
        int gridsize = state.getGridSize();

        // Check all 4 borders
        for (int i = 0; i < gridsize; i++) {
            int max = gridsize - 1;
            int bot = max - state.getFirstFreeCell(i, max, GameState.DIR_UP);
            int top = state.getFirstFreeCell(i, 0, GameState.DIR_DOWN);
            int left = state.getFirstFreeCell(0, i, GameState.DIR_RIGHT);
            int right = max
                    - state.getFirstFreeCell(max, i, GameState.DIR_LEFT);

            if (bot != -1 && bot < gridsize) {
                Point pBot = new Point(i, bot);
                if (!result.contains(pBot))
                    result.add(pBot);
            }
            if (top != -1 && top < gridsize) {
                Point pTop = new Point(i, top);
                if (!result.contains(pTop))
                    result.add(pTop);
            }
            if (left != -1 && left < gridsize) {
                Point pLeft = new Point(left, i);
                if (!result.contains(pLeft))
                    result.add(pLeft);
            }
            if (right != -1 && right < gridsize) {
                Point pRight = new Point(right, i);
                if (!result.contains(pRight))
                    result.add(pRight);
            }
        }

        return result;
    }

    public int play(GameState state) {
        running = true;
        return play(state, 0);
    }

    public int play(GameState state, int depth) {
        if (!running)
            return 0;

        // Check for termination
        if (depth > 2)
            return 0;

        if (state.hasVictory())
            return -1000;

        // Compute possible moves
        List<Point> moves = getLegalMoves(state);

        // Evaluate them
        List<Integer> values = evaluateMoves(state, moves);

        // Sample the K best
        int[] bests = getBest(values, 6);

        // Recurse
        int bestScore = 0;
        int bestId = 0;
        for (int id : bests) {
            Point move = moves.get(id);

            GameState next = state.clone();
            next.play(move.x, move.y);

            int score = values.get(id) - play(next, depth + 1);

            if (!running)
                return 0;

            if (score > bestScore) {
                bestScore = score;
                bestId = id;
            }
        }

        if (depth == 0) {
            Point bestMove = moves.get(bestId);
            state.play(bestMove.x, bestMove.y);
            // System.out.println("Decision : " + bestMove.x + ":" +
            // bestMove.y);
        }

        return bestScore;
    }

    public void stop() {
        running = false;
    }
}
