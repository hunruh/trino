//code taken from http://www.codebytes.in/2015/02/a-shortest-path-finding-algorithm.html

package tiktaalik.util;
import java.util.*;

public class AStar {
    private static final int COST = 1;

    private class Cell implements Comparable<Cell>{
        int heuristicCost = 0; //Heuristic cost
        int finalCost = 0; //G+H
        int i, j;
        Cell parent;

        Cell(int i, int j) {
            this.i = i;
            this.j = j;
        }

        @Override
        public String toString() {
            return "[" + this.i + ", " + this.j + "]";
        }

        public int compareTo(Cell c){
            return this.finalCost<c.finalCost?-1:
                    this.finalCost>c.finalCost?1:0;
        }
    }

    //Blocked cells are just null Cell values in grid
    private Cell[][] grid;

    private PriorityQueue<Cell> open;

    private boolean closed[][];
    private int startI, startJ;
    private int endI, endJ;

    public void setBlocked(int i, int j) {
        grid[i][j] = null;
    }

    public void checkAndUpdateCost(Cell current, Cell t, int cost) {
        if (t == null || closed[t.i][t.j]) return;
        int t_final_cost = t.heuristicCost + cost;

        boolean inOpen = open.contains(t);
        if (!inOpen || t_final_cost < t.finalCost) {
            t.finalCost = t_final_cost;
            t.parent = current;
            if (!inOpen) open.add(t);
        }
    }

    public AStar(int x, int y, int sx, int sy, int ex, int ey, int[][] blocked) {

        //add the start location to open list
        grid = new Cell[x][y];
        closed = new boolean[x][y];
        for(int i=0;i<x;++i){
            for(int j=0;j<y;++j){
                grid[i][j] = new Cell(i, j);
                grid[i][j].heuristicCost = Math.abs(i-endI)+Math.abs(j-endJ);
//                  System.out.print(grid[i][j].heuristicCost+" ");
            }
//              System.out.println();
        }
        grid[sx][sy].finalCost = 0;

        for(int i=0;i<blocked.length;++i){
            setBlocked(blocked[i][0], blocked[i][1]);
        }

        startI = sx;
        startJ = sy;
        endI = ex;
        endJ = ey;

        open = new PriorityQueue<Cell>();

    }

    public void computePath(){
        Cell current;
        System.out.println(grid[startI][startJ]);
        open.add(grid[startI][startJ]);

        while (true) {
            current = open.poll();
            if (current == null) break;
            closed[current.i][current.j] = true;

            if (current.equals(grid[endI][endJ])) {
                return;
            }

            Cell t;
            if (current.i - 1 >= 0) {
                t = grid[current.i - 1][current.j];
                checkAndUpdateCost(current, t, current.finalCost + COST);
            }

            if (current.j - 1 >= 0) {
                t = grid[current.i][current.j - 1];
                checkAndUpdateCost(current, t, current.finalCost + COST);
            }

            if (current.j + 1 < grid[0].length) {
                t = grid[current.i][current.j + 1];
                checkAndUpdateCost(current, t, current.finalCost + COST);
            }

            if (current.i + 1 < grid.length) {
                t = grid[current.i + 1][current.j];
                checkAndUpdateCost(current, t, current.finalCost + COST);

            }
        }
    }

    public void printResults(){
        System.out.println(closed);
        if(closed[endI][endJ]){
            //Trace back the path
            System.out.println("Path: ");
            Cell current = grid[endI][endJ];
            System.out.print(current);
            while(current.parent!=null){
                System.out.print(" -> "+current.parent);
                current = current.parent;
            }
            System.out.println();
        }else System.out.println("No possible path");
    }
}
