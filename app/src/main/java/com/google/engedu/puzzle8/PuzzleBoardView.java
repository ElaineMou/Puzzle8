package com.google.engedu.puzzle8;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

public class PuzzleBoardView extends View {
    public static final int NUM_SHUFFLE_STEPS = 40;
    private Activity activity;
    private PuzzleBoard puzzleBoard;
    private ArrayList<PuzzleBoard> animation;
    private Random random = new Random();

    public PuzzleBoardView(Context context) {
        super(context);
        activity = (Activity) context;
        animation = null;
    }

    public void initialize(Bitmap imageBitmap) {
        int width = getWidth();
        puzzleBoard = new PuzzleBoard(imageBitmap, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (puzzleBoard != null) {
            if (animation != null && animation.size() > 0) {
                puzzleBoard = animation.remove(0);
                puzzleBoard.draw(canvas);
                if (animation.size() == 0) {
                    animation = null;
                    puzzleBoard.reset();
                    Toast toast = Toast.makeText(activity, "Solved! ", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    this.postInvalidateDelayed(500);
                }
            } else {
                puzzleBoard.draw(canvas);
            }
        }
    }

    public void shuffle() {
        Random random = new Random();
        if (animation == null && puzzleBoard != null) {
            for(int i=0;i<NUM_SHUFFLE_STEPS;i++) {
                ArrayList<PuzzleBoard> neighbors = puzzleBoard.neighbours();
                puzzleBoard = neighbors.get(random.nextInt(neighbors.size()));
            }
            // Do something. Then:
            puzzleBoard.reset();
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (animation == null && puzzleBoard != null) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (puzzleBoard.click(event.getX(), event.getY())) {
                        invalidate();
                        if (puzzleBoard.resolved()) {
                            Toast toast = Toast.makeText(activity, "Congratulations!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        return true;
                    }
            }
        }
        return super.onTouchEvent(event);
    }

    public void solve() {
        PriorityQueue<PuzzleBoard> pQueue = new PriorityQueue(NUM_SHUFFLE_STEPS, new Comparator<PuzzleBoard>() {
            @Override
            public int compare(PuzzleBoard puzzleBoard, PuzzleBoard t1) {
                return Integer.compare(puzzleBoard.priority(), t1.priority());
            }
        });

        pQueue.add(puzzleBoard);

        while(!pQueue.isEmpty()) {
            PuzzleBoard lowestPriority = pQueue.poll();
            Log.d("PQueue size: ","" + pQueue.size());
            if (!lowestPriority.resolved()) {
                for(PuzzleBoard neighbor : lowestPriority.neighbours()) {
                    if (neighbor != lowestPriority.getPreviousBoard()) {
                        pQueue.add(neighbor);
                    }
                }
            } else {
                ArrayList<PuzzleBoard> series = new ArrayList<>();
                series.add(lowestPriority);
                PuzzleBoard previous = lowestPriority.getPreviousBoard();
                while(previous!= puzzleBoard) {
                    series.add(previous);
                    previous = previous.getPreviousBoard();
                }
                Collections.reverse(series);
                animation = series;
                invalidate();
                break;
            }
        }
    }
}
