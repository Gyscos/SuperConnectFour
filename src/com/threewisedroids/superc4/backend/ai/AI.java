package com.threewisedroids.superc4.backend.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.graphics.Point;

import com.threewisedroids.superc4.backend.GameState;

public class AI
{
    public static class ArrayHolder {
        public ArrayList<Integer> value = new ArrayList<Integer>();
    }

    public static class PointArrayHolder {
        public ArrayList<Point> value = new ArrayList<Point>();
    }

    boolean                      running;

    int                          maxDepth;
    int                          maxWidth;

    int                          cores;

    int[][]                      bestIds;
    int[][]                      bestValues;

    PointArrayHolder[]           results;
    ArrayHolder[]                evaluations;

    Point[][]                    pointPool;

    boolean                      random;

    int                          gridsize;

    private static final boolean debug = false;

    public AI(int maxDepth, int maxWidth, int cores, boolean random) {
        this.maxDepth = maxDepth;
        this.maxWidth = maxWidth;

        this.cores = cores;
        this.random = random;

    }

    List<Integer> evaluateMoves(GameState state, List<Point> moves, int depth) {
        ArrayList<Integer> result = evaluations[depth].value;
        result.clear();
        for (Point p : moves)
            result.add(state.evaluate(p.x, p.y));
        return result;
    }

    int findMin(int[] list) {
        int min = list[0];
        int minId = 0;
        for (int i = 0; i < list.length; i++)
            if (list[i] < min) {
                min = list[i];
                minId = i;
            }
        return minId;
    }

    int[] getBest(List<Integer> list, int depth) {
        for (int i = 0; i < list.size(); i++) {
            int minId = findMin(bestValues[depth]);
            if (list.get(i) > bestValues[depth][minId]) {
                bestValues[depth][minId] = list.get(i);
                bestIds[depth][minId] = i;
            }
        }

        return bestIds[depth];
    }

    List<Point> getLegalMoves(GameState state, int depth) {
        ArrayList<Point> result = results[depth].value;

        int index = 0;

        // Check all 4 borders
        for (int i = 0; i < gridsize; i++) {
            int max = gridsize - 1;
            int bot = max - state.getFirstFreeCell(i, max, GameState.DIR_UP);
            int top = state.getFirstFreeCell(i, 0, GameState.DIR_DOWN);
            int left = state.getFirstFreeCell(0, i, GameState.DIR_RIGHT);
            int right = max
                    - state.getFirstFreeCell(max, i, GameState.DIR_LEFT);

            if (bot != -1 && bot < gridsize) {
                Point pBot = pointPool[depth][index++];
                pBot.set(i, bot);
                if (!result.contains(pBot))
                    result.add(pBot);
            }
            if (top != -1 && top < gridsize) {
                Point pTop = pointPool[depth][index++];
                pTop.set(i, top);
                if (!result.contains(pTop))
                    result.add(pTop);
            }
            if (left != -1 && left < gridsize) {
                Point pLeft = pointPool[depth][index++];
                pLeft.set(left, i);
                if (!result.contains(pLeft))
                    result.add(pLeft);
            }
            if (right != -1 && right < gridsize) {
                Point pRight = pointPool[depth][index++];
                pRight.set(right, i);
                if (!result.contains(pRight))
                    result.add(pRight);
            }
        }

        return result;
    }

    public int play(GameState state) {
        running = true;
        System.out.println("===== START =====");

        // Init everything

        gridsize = state.getGridSize();

        pointPool = new Point[maxDepth][4 * gridsize];

        results = new PointArrayHolder[maxDepth];
        evaluations = new ArrayHolder[maxDepth];

        bestIds = new int[maxDepth][];
        bestValues = new int[maxDepth][];

        for (int i = 0; i < maxDepth; i++) {
            int width = maxWidth - 2 * i;
            bestIds[i] = new int[width];
            bestValues[i] = new int[width];

            results[i] = new PointArrayHolder();
            evaluations[i] = new ArrayHolder();

            for (int j = 0; j < 4 * gridsize; j++)
                pointPool[i][j] = new Point();
        }
        return play(state, 0);
    }

    public int play(GameState state, int depth) {
        if (!running)
            return 0;

        // Check for termination
        if (depth >= maxDepth)
            return 0;

        if (state.hasVictory())
            return -1000;

        // Compute possible moves
        List<Point> moves = getLegalMoves(state, depth);

        // Evaluate them
        List<Integer> values = evaluateMoves(state, moves, depth);

        if (debug)
            if (depth == 0) {
                for (int i = 0; i < values.size(); i++)
                    System.out.println("[" + moves.get(i).x + ":"
                            + moves.get(i).y
                            + "] : " + values.get(i));
            }

        // Sample the K best
        int[] bests = random ? sampleBest(values, depth) : getBest(values,
                depth);

        if (debug) {
            for (int id : bests)
                System.out.print("[" + moves.get(id).x + ":" + moves.get(id).y
                        + "] ; ");
            System.out.println();
        }

        // Recurse
        int bestScore = 0;
        int bestId = -1;
        for (int id : bests) {
            Point move = moves.get(id);

            GameState next = state.clone();

            if (debug)
                if (depth != maxDepth) {
                    System.out.print("(" + depth + ")");
                    for (int i = 0; i < depth; i++)
                        System.out.print("  ");
                    System.out.print("[" + move.x + ":" + move.y + "] --> ");
                }

            next.play(move.x, move.y);

            int nextScore = play(next, depth + 1);
            int score = values.get(id) - nextScore;

            if (debug) {
                System.out.print("(" + depth + ")");

                for (int i = 0; i < depth; i++)
                    System.out.print("  ");
                System.out
                        .println("[" + move.x + ":" + move.y + "] : "
                                + values.get(id) + " - " + nextScore + " = "
                                + score);
            }

            if (!running)
                return 0;

            if (score > bestScore || bestId == -1) {
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

    int[] sampleBest(List<Integer> list, int depth) {
        int[] result = bestIds[depth];

        // First, find the total weight
        int s = 0;
        for (int value : list)
            s += value;

        Random rnd = new Random();

        for (int i = 0; i < result.length; i++) {
            int r = rnd.nextInt(s);

            int index = 0;
            for (int j = 0; j < list.size(); j++) {
                boolean ok = true;

                for (int k = 0; k < i; k++)
                    if (result[k] == j)
                        ok = false;

                if (!ok)
                    continue;

                r -= list.get(j);
                if (r < 0) {
                    index = j;
                    break;
                }
            }

            s -= list.get(index);
            result[i] = index;
        }

        return result;
    }

    public void stop() {
        running = false;
    }
}
