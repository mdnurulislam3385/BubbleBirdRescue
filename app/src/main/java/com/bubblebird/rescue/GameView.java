package com.bubblebird.rescue;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();

    private static final int ROWS = 11;
    private static final int COLS = 8;

    private final int[][] board = new int[ROWS][COLS];

    private float shooterX;
    private float shooterY;
    private float aimX;
    private float aimY;

    private int currentBubble;
    private int nextBubble;

    private int score = 0;
    private int shots = 0;
    private int rescued = 0;

    private boolean moving = false;
    private float ballX;
    private float ballY;
    private float vx;
    private float vy;

    private final int[] bubbleColors = {
            Color.rgb(255, 82, 82),
            Color.rgb(255, 193, 7),
            Color.rgb(66, 165, 245),
            Color.rgb(102, 187, 106),
            Color.rgb(171, 71, 188)
    };

    public GameView(Context context) {
        super(context);

        paint.setTypeface(android.graphics.Typeface.create(
                "sans-serif", android.graphics.Typeface.BOLD));

        newGame();
    }

    private void newGame() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                board[r][c] = -1;
            }
        }

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < COLS; c++) {
                board[r][c] = random.nextInt(bubbleColors.length);
            }
        }

        score = 0;
        shots = 0;
        rescued = 0;

        currentBubble = random.nextInt(bubbleColors.length);
        nextBubble = random.nextInt(bubbleColors.length);

        moving = false;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(10, 18, 40));
        canvas.drawRect(0, 0, w, h, paint);

        drawHeader(canvas, w);
        drawBoard(canvas, w, h);
        drawAim(canvas);
        drawShooter(canvas, w, h);

        if (moving) {
            drawMovingBubble(canvas);
        }
    }

    private void drawHeader(Canvas canvas, float w) {
        paint.setColor(Color.rgb(25, 39, 75));
        canvas.drawRoundRect(
                new RectF(18, 18, w - 18, 88),
                22, 22, paint);

        paint.setTextSize(22);
        paint.setColor(Color.WHITE);
        canvas.drawText("Bubble Bird Rescue", 35, 48, paint);

        paint.setTextSize(16);
        paint.setColor(Color.rgb(190, 210, 255));

        canvas.drawText("Score: " + score, 35, 73, paint);
        canvas.drawText("Birds: " + rescued + "/10", w - 125, 73, paint);
    }

    private void drawBoard(Canvas canvas, float w, float h) {
        float top = 110;
        float bottom = h - 155;

        float cellW = w / COLS;
        float cellH = (bottom - top) / ROWS;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {

                if (board[r][c] == -1) {
                    continue;
                }

                float cx = c * cellW + cellW / 2f;
                float cy = top + r * cellH + cellH / 2f;

                drawBubble(
                        canvas,
                        cx,
                        cy,
                        Math.min(cellW, cellH) * 0.40f,
                        board[r][c]
                );
            }
        }

        // small rescued birds
        for (int i = 0; i < rescued; i++) {
            float bx = 35 + (i % 10) * 34;
            float by = bottom + 20 + (i / 10) * 30;

            paint.setTextSize(22);
            canvas.drawText("🐦", bx, by, paint);
        }
    }

    private void drawAim(Canvas canvas) {
        if (
