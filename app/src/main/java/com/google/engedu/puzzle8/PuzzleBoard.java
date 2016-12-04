package com.google.engedu.puzzle8;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.ArrayList;


public class PuzzleBoard {

    private static final int NUM_TILES = 4;
    private static final int[][] NEIGHBOUR_COORDS = {
            { -1, 0 },
            { 1, 0 },
            { 0, -1 },
            { 0, 1 }
    };
    private ArrayList<PuzzleTile> tiles;
    private int steps;
    private PuzzleBoard previousBoard;

    PuzzleBoard(Bitmap bitmap, int parentWidth) {
        tiles = new ArrayList<>();
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        for(int i=0;i<NUM_TILES*NUM_TILES - 1; i++) {
            Bitmap chunk = bitmap.createBitmap(bitmap, i%NUM_TILES * bitmapWidth/NUM_TILES,
                    i/NUM_TILES * bitmapHeight/NUM_TILES, bitmapWidth/NUM_TILES,
                    bitmapHeight/NUM_TILES);
            chunk = chunk.createScaledBitmap(chunk,parentWidth/NUM_TILES,parentWidth/NUM_TILES, false);
            tiles.add(new PuzzleTile(chunk, i));
        }
        tiles.add(null);
    }

    PuzzleBoard(PuzzleBoard otherBoard) {
        tiles = (ArrayList<PuzzleTile>) otherBoard.tiles.clone();
        steps = otherBoard.steps + 1;
        previousBoard = otherBoard;
    }

    public void reset() {
        // Nothing for now but you may have things to reset once you implement the solver.
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        return tiles.equals(((PuzzleBoard) o).tiles);
    }

    public void draw(Canvas canvas) {
        if (tiles == null) {
            return;
        }
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                tile.draw(canvas, i % NUM_TILES, i / NUM_TILES);
            }
        }
    }

    public boolean click(float x, float y) {
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                if (tile.isClicked(x, y, i % NUM_TILES, i / NUM_TILES)) {
                    return tryMoving(i % NUM_TILES, i / NUM_TILES);
                }
            }
        }
        return false;
    }

    private boolean tryMoving(int tileX, int tileY) {
        for (int[] delta : NEIGHBOUR_COORDS) {
            int nullX = tileX + delta[0];
            int nullY = tileY + delta[1];
            if (nullX >= 0 && nullX < NUM_TILES && nullY >= 0 && nullY < NUM_TILES &&
                    tiles.get(XYtoIndex(nullX, nullY)) == null) {
                swapTiles(XYtoIndex(nullX, nullY), XYtoIndex(tileX, tileY));
                return true;
            }

        }
        return false;
    }

    public boolean resolved() {
        for (int i = 0; i < NUM_TILES * NUM_TILES - 1; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile == null || tile.getNumber() != i)
                return false;
        }
        return true;
    }

    private int XYtoIndex(int x, int y) {
        return x + y * NUM_TILES;
    }

    protected void swapTiles(int i, int j) {
        PuzzleTile temp = tiles.get(i);
        tiles.set(i, tiles.get(j));
        tiles.set(j, temp);
    }

    public ArrayList<PuzzleBoard> neighbours() {
        ArrayList<PuzzleBoard> list = new ArrayList<>();
        int nullIndex = 0;
        while(tiles.get(nullIndex) != null && nullIndex < tiles.size()) {
            nullIndex++;
        }
        int nullX = nullIndex % NUM_TILES;
        int nullY = nullIndex / NUM_TILES;

        for(int i=0;i<NEIGHBOUR_COORDS.length;i++) {
            int x = nullX + NEIGHBOUR_COORDS[i][0];
            int y = nullY + NEIGHBOUR_COORDS[i][1];

            if (x >=0 && x < NUM_TILES && y >= 0 && y < NUM_TILES) {
                PuzzleBoard copy = new PuzzleBoard(this);
                copy.swapTiles(XYtoIndex(x,y),XYtoIndex(nullX,nullY));
                list.add(copy);
            }
        }

        return list;
    }

    public int priority() {
        int priority = steps;
        for(int i=0;i<NUM_TILES*NUM_TILES;i++) {
            if (tiles.get(i)!=null) {
                int tileNumber = tiles.get(i).getNumber();
                int targetX = tileNumber % NUM_TILES;
                int targetY = tileNumber / NUM_TILES;
                priority += Math.abs(i % NUM_TILES - targetX) + Math.abs(i / NUM_TILES - targetY);
            }
        }
        return priority;
    }

    PuzzleBoard getPreviousBoard() {
        return previousBoard;
    }

}
