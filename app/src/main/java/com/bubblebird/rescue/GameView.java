package com.bubblebird.rescue;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends View {

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();

    private static final int ROWS = 11;
    private static final int COLS = 8;

    private final int[][] board = new int[ROWS][COLS];

    private final int[] colors = {
        Color.rgb(255,82,100),
        Color.rgb(255,193,7),
        Color.rgb(66,165,245),
        Color.rgb(102,210,130),
        Color.rgb(185,95,245)
    };

    private static final int HOME = 0;
    private static final int PLAYING = 1;
    private static final int PAUSED = 2;
    private static final int COMPLETE = 3;
    private static final int GAME_OVER = 4;

    private int screen = HOME;
    private int level = 1;
    private int score = 0;
    private int rescued = 0;
    private int hints = 3;

    private int current;
    private int next;

    private float sx, sy, ax, ay;
    private float bx, by, vx, vy;

    private boolean moving = false;

    public GameView(Context context) {
        super(context);
        p.setTypeface(Typeface.DEFAULT_BOLD);
        resetBoard();
    }

    private void resetBoard() {
        int rowsToFill = Math.min(4 + level / 2, 7);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                board[r][c] = r < rowsToFill
                    ? random.nextInt(colors.length)
                    : -1;
            }
        }

        current = random.nextInt(colors.length);
        next = random.nextInt(colors.length);
        moving = false;
        invalidate();
   }

    private void drawHome(Canvas c, float w, float h) {
        p.setTextAlign(Paint.Align.CENTER);

        p.setColor(Color.rgb(255,210,60));
        p.setTextSize(Math.min(34, w * .09f));
        c.drawText("BUBBLE BIRD", w/2, h*.23f, p);
        c.drawText("RESCUE", w/2, h*.30f, p);

        drawBubble(c, w*.35f, h*.40f, 25, 0);
        drawBubble(c, w*.50f, h*.36f, 25, 2);
        drawBubble(c, w*.65f, h*.40f, 25, 3);
        drawBubble(c, w*.42f, h*.47f, 25, 4);
        drawBubble(c, w*.58f, h*.47f, 25, 1);

        p.setColor(Color.WHITE);
        p.setTextSize(16);
        c.drawText(
            "Match bubbles and rescue birds!",
            w/2, h*.56f, p
        );

        button(
            c, w*.18f, h*.66f, w*.82f, h*.75f,
            "PLAY NOW", Color.rgb(30,190,130)
        );

        button(
            c, w*.18f, h*.78f, w*.82f, h*.87f,
            "HOW TO PLAY", Color.rgb(55,100,185)
        );

        p.setTextSize(11);
        p.setColor(Color.LTGRAY);
        c.drawText(
            "Bubble Bird Rescue",
            w/2, h*.93f, p
        );

        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawGame(Canvas c, float w, float h) {

        p.setColor(Color.rgb(25,39,75));

        c.drawRoundRect(
            new RectF(12,12,w-12,92),
            20,20,p
        );

        p.setColor(Color.WHITE);
        p.setTextAlign(Paint.Align.LEFT);

        p.setTextSize(19);
        c.drawText(
            "BUBBLE BIRD",
            24,40,p
        );

        p.setTextSize(14);
        c.drawText(
            "Level " + level + "   Score " + score,
            24,68,p
        );

        button(
            c,w-90,23,w-20,72,
            "II",Color.rgb(65,90,145)
        );

        float top = 108;
        float bottom = h - 165;

        float cw = w / COLS;
        float ch = (bottom-top) / ROWS;

        for(int r=0;r<ROWS;r++) {
            for(int col=0;col<COLS;col++) {

                if(board[r][col] >= 0) {

                    drawBubble(
                        c,
                        col*cw+cw/2,
                        top+r*ch+ch/2,
                        Math.min(cw,ch)*.40f,
                        board[r][col]
                    );
                }
            }
        }

        p.setColor(Color.WHITE);
        p.setTextSize(13);

        c.drawText(
            "Birds rescued: " + rescued + "/10",
            18,h-136,p
        );

        p.setColor(Color.rgb(45,55,85));

        c.drawRoundRect(
            new RectF(18,h-124,w-18,h-112),
            8,8,p
        );

        float progress = Math.min(1f,rescued/10f);

        p.setColor(Color.rgb(40,220,150));

        c.drawRoundRect(
            new RectF(
                18,h-124,
                18+(w-36)*progress,
                h-112
            ),
            8,8,p
        );

        sx = w/2f;
        sy = h-70;

        if(!moving) {

            float dx = ax-sx;
            float dy = ay-sy;

            float len =
                (float)Math.sqrt(dx*dx+dy*dy);

            if(len > 0 && dy < -10) {

                p.setColor(Color.LTGRAY);
                p.setStrokeWidth(3);

                c.drawLine(
                    sx,sy,
                    sx+dx/len*180,
                    sy+dy/len*180,
                    p
                );
            }
        }

        p.setColor(Color.rgb(42,60,105));
        c.drawCircle(sx,sy,36,p);

        drawBubble(
            c,sx,sy,23,current
        );

        drawBubble(
            c,sx+65,sy+5,15,next
        );

        p.setColor(Color.WHITE);
        p.setTextSize(11);

        c.drawText(
            "NEXT",
            sx+50,sy-20,p
        );

        if(moving) {
            drawBubble(
                c,bx,by,19,current
            );
        }

        button(
            c,16,h-46,w*.47f,h-10,
            "HINT ("+hints+")",
            Color.rgb(110,70,180)
        );

        button(
            c,w*.53f,h-46,w-16,h-10,
            "BUY HINTS",
            Color.rgb(190,125,30)
);
}

private void drawPause(Canvas c, float w, float h) {
    p.setColor(Color.argb(220,0,0,0));
    c.drawRect(0,0,w,h,p);

    p.setTextAlign(Paint.Align.CENTER);
    p.setColor(Color.WHITE);
    p.setTextSize(32);
    c.drawText("PAUSED",w/2,h*.40f,p);

    button(c,w*.18f,h*.52f,w*.82f,h*.61f,
        "RESUME",Color.rgb(30,190,130));

        button(c,w*.18f,h*.64f,w*.82f,h*.73f,
            "HOME",Color.rgb(55,100,185));
    p.setTextAlign(Paint.Align.LEFT);
}

    private void drawComplete(Canvas c, float w, float h) {
        p.setTextAlign(Paint.Align.CENTER);

        p.setColor(Color.rgb(255,210,60));
        p.setTextSize(30);
        c.drawText("LEVEL COMPLETE!", w/2, h*.36f, p);

        p.setColor(Color.WHITE);
        p.setTextSize(17);
        c.drawText("Birds rescued: " + rescued, w/2, h*.44f, p);
        c.drawText("Score: " + score, w/2, h*.49f, p);

        button(c,w*.18f,h*.55f,w*.82f,h*.64f,
            "NEXT LEVEL",Color.rgb(30,190,130));

        button(c,w*.18f,h*.67f,w*.82f,h*.76f,
            "HOME",Color.rgb(55,100,185));

        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawGameOver(Canvas c, float w, float h) {
        p.setTextAlign(Paint.Align.CENTER);

        p.setColor(Color.rgb(255,80,90));
        p.setTextSize(32);
        c.drawText("GAME OVER",w/2,h*.38f,p);

        p.setColor(Color.WHITE);
        p.setTextSize(17);
        c.drawText("Score: " + score,w/2,h*.46f,p);

        button(c,w*.18f,h*.54f,w*.82f,h*.63f,
            "PLAY AGAIN",Color.rgb(30,190,130));

        button(c,w*.18f,h*.66f,w*.82f,h*.75f,
            "HOME",Color.rgb(55,100,185));

        p.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        float x = e.getX();
        float y = e.getY();
        float w = getWidth();
        float h = getHeight();

        if (e.getAction() == MotionEvent.ACTION_DOWN ||
            e.getAction() == MotionEvent.ACTION_MOVE) {

            ax = x;
            ay = y;
            invalidate();
            return true;
        }

        if (e.getAction() != MotionEvent.ACTION_UP) {
            return true;
        }

        if (screen == HOME) {

            if (y > h*.64f && y < h*.77f) {
                startGame();

            } else if (y > h*.77f && y < h*.90f) {

                Toast.makeText(
                    getContext(),
                    "Aim upward and match 3 or more bubbles!",
                    Toast.LENGTH_LONG
                ).show();
            }

        } else if (screen == PLAYING) {

            if (y < 95 && x > w-105) {

                screen = PAUSED;
                moving = false;
                invalidate();
                return true;
            }

            if (y > h-55 && x < w*.50f) {

                useHint();
                return true;
            }

            if (y > h-55 && x >= w*.50f) {

                Toast.makeText(
                    getContext(),
                    "Hint purchase will be connected to Google Play.",
                    Toast.LENGTH_LONG
                ).show();

                return true;
            }

            if (!moving) {
                shoot(x,y);
            }

        } else if (screen == PAUSED) {

            if (y > h*.50f && y < h*.63f) {
                screen = PLAYING;

            } else if (y > h*.63f && y < h*.76f) {
                screen = HOME;
            }

            invalidate();

        } else if (screen == COMPLETE) {

            if (y > h*.50f && y < h*.66f) {

                level++;
                rescued = 0;
                resetBoard();
                screen = PLAYING;

            } else if (y > h*.66f && y < h*.80f) {

                screen = HOME;
            }

            invalidate();

        } else if (screen == GAME_OVER) {

            if (y > h*.50f && y < h*.65f) {

                startGame();

            } else if (y > h*.65f && y < h*.80f) {

                screen = HOME;
            }

            invalidate();
        }

        return true;
    }

    private void useHint() {

        if (hints <= 0) {

            Toast.makeText(
                getContext(),
                "No free hints left!",
                Toast.LENGTH_SHORT
            ).show();

            return;
        }

        hints--;

        ax = getWidth()/2f;
        ay = 120;

        Toast.makeText(
            getContext(),
            "Hint used! Match your current bubble colour.",
            Toast.LENGTH_SHORT
        ).show();

        invalidate();
    }

    private void shoot(float x, float y) {

        sx = getWidth() / 2f;
        sy = getHeight() - 70;

        float dx = x - sx;
        float dy = y - sy;

        float len =
            (float)Math.sqrt(dx * dx + dy * dy);

        if (len < 1 || dy >= -20) {
            return;
        }

        vx = dx / len * 18;
        vy = dy / len * 18;

        bx = sx;
        by = sy;

        moving = true;

        moveStep();
    }

    private void moveStep() {

        if (!moving || screen != PLAYING) {
            return;
        }

        bx += vx;
        by += vy;

        if (bx < 20 || bx > getWidth() - 20) {
            vx = -vx;
        }

        if (by < 108 || hitBubble()) {
            placeBubble();
            return;
        }

        invalidate();

        postDelayed(
            new Runnable() {
                @Override
                public void run() {
                    moveStep();
                }
            },
            16
        );
    }

    private boolean hitBubble() {

        float top = 108;
        float bottom = getHeight() - 165;

        float cw = getWidth() / (float)COLS;
        float ch = (bottom - top) / ROWS;

        for (int r = 0; r < ROWS; r++) {

            for (int c = 0; c < COLS; c++) {

                if (board[r][c] < 0) {
                    continue;
                }

                float cx = c * cw + cw / 2;
                float cy = top + r * ch + ch / 2;

                float dx = bx - cx;
                float dy = by - cy;

                if (dx * dx + dy * dy < 38 * 38) {
                    return true;
                }
            }
        }

        return false;
    }

    private void placeBubble() {

        float top = 108;
        float bottom = getHeight() - 165;

        float cw = getWidth() / (float)COLS;
        float ch = (bottom - top) / ROWS;

        int col = Math.max(
            0,
            Math.min(
                COLS - 1,
                (int)(bx / cw)
            )
        );

        int row = Math.max(
            0,
            Math.min(
                ROWS - 1,
                (int)((by - top) / ch)
            )
        );

        while (row < ROWS && board[row][col] >= 0) {
            row++;
        }

        if (row >= ROWS) {
            moving = false;
            screen = GAME_OVER;
            invalidate();
            return;
        }

        board[row][col] = current;
        moving = false;

        List<int[]> group = new ArrayList<>();

        collect(
            row,
            col,
            current,
            group
        );

        if (group.size() >= 3) {

            for (int[] cell : group) {

                board[cell[0]][cell[1]] = -1;
            }

            score += group.size() * 10;

            rescued += group.size();

            if (rescued > 10) {
                rescued = 10;
            }

            removeFloating();

            if (rescued >= 10) {
                screen = COMPLETE;
            }
        }

        current = next;
        next = random.nextInt(colors.length);

        if (screen == PLAYING && boardIsFull()) {
            screen = GAME_OVER;
        }

        invalidate();
    }

    private void collect(
        int r,
        int c,
        int color,
        List<int[]> group
    ) {

        if (r < 0 || r >= ROWS ||
            c < 0 || c >= COLS) {
            return;
        }

        if (board[r][c] != color) {
            return;
        }

        for (int[] cell : group) {

            if (cell[0] == r &&
                cell[1] == c) {
                return;
            }
        }

        group.add(new int[]{r,c});

        collect(r + 1,c,color,group);
        collect(r - 1,c,color,group);
        collect(r,c + 1,color,group);
        collect(r,c - 1,color,group);
    }

    private void removeFloating() {

        boolean[][] connected =
            new boolean[ROWS][COLS];

        List<int[]> queue =
            new ArrayList<>();

        for (int c = 0; c < COLS; c++) {

            if (board[0][c] >= 0) {

                connected[0][c] = true;

                queue.add(
                    new int[]{0,c}
                );
            }
        }

        int[][] dirs = {
            {1,0},
            {-1,0},
            {0,1},
            {0,-1}
        };

        for (int i = 0;
             i < queue.size();
             i++) {

            int r = queue.get(i)[0];
            int c = queue.get(i)[1];

            for (int[] d : dirs) {

                int nr = r + d[0];
                int nc = c + d[1];

                if (nr < 0 ||
                    nr >= ROWS ||
                    nc < 0 ||
                    nc >= COLS) {
                    continue;
                }

                if (!connected[nr][nc] &&
                    board[nr][nc] >= 0) {

                    connected[nr][nc] = true;

                    queue.add(
                        new int[]{nr,nc}
                    );
                }
            }
        }

        for (int r = 0; r < ROWS; r++) {

            for (int c = 0; c < COLS; c++) {

                if (board[r][c] >= 0 &&
                    !connected[r][c]) {

                    board[r][c] = -1;

                    score += 5;
                }
            }
        }
    }

    private boolean boardIsFull() {

        for (int c = 0; c < COLS; c++) {

            if (board[ROWS - 1][c] >= 0) {
                return true;
            }
        }

        return false;
    }

    private void drawBubble(Canvas c, float x, float y, float radius, int colorIndex) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(colors[Math.max(0, Math.min(colorIndex, colors.length - 1))]);
        c.drawCircle(x, y, radius, p);

        p.setColor(Color.argb(90, 255, 255, 255));
        c.drawCircle(x - radius * 0.30f, y - radius * 0.30f, radius * 0.25f, p);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2);
        p.setColor(Color.argb(100, 255, 255, 255));
        c.drawCircle(x, y, radius, p);

        p.setStyle(Paint.Style.FILL);
    }

    private void button(Canvas c, float left, float top, float right, float bottom,
                        String text, int color) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        c.drawRoundRect(
            new RectF(left, top, right, bottom),
            18, 18, p
        );

        p.setColor(Color.WHITE);
        p.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        p.setTextSize(18);
        p.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics fm = p.getFontMetrics();
        float textY = (top + bottom) / 2f - (fm.ascent + fm.descent) / 2f;

        c.drawText(text, (left + right) / 2f, textY, p);

        p.setTypeface(Typeface.DEFAULT);
    }
}
