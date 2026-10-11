package com.bubblebird.rescue;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();

    private final ArrayList<Bubble> bubbles = new ArrayList<>();

    private float shooterX;
    private float shooterY;

    private float aimX;
    private float aimY;

    private int score = 0;
    private int birds = 0;

    private boolean shooting = false;
    private float shotX;
    private float shotY;
    private float shotDX;
    private float shotDY;
    private int shotColor;

    private final int[] bubbleColors = {
            Color.rgb(255, 82, 82),
            Color.rgb(66, 133, 244),
            Color.rgb(255, 193, 7),
            Color.rgb(156, 39, 176),
            Color.rgb(0, 200, 83)
    };

    public GameView(Context context) {
        super(context);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 8; col++) {
                float x = 55 + col * 82;
                if (row % 2 == 1) {
                    x += 41;
                }

                bubbles.add(
                        new Bubble(
                                x,
                                150 + row * 75,
                                bubbleColors[random.nextInt(bubbleColors.length)]
                        )
                );
            }
        }

        setFocusable(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.rgb(12, 25, 45));

        drawTopBar(canvas);
        drawBubbles(canvas);
        drawBirds(canvas);
        drawShooter(canvas);

        if (shooting) {
            drawShot(canvas);
            updateShot();
            postInvalidateDelayed(16);
        }
    }

    private void drawTopBar(Canvas canvas) {

        paint.setColor(Color.WHITE);
        paint.setTextSize(42);

        canvas.drawText("🐦 Bubble Bird Rescue", 30, 55, paint);

        paint.setTextSize(30);
        canvas.drawText("Score: " + score, 30, 100, paint);

        canvas.drawText("Birds: " + birds + "/10", 400, 100, paint);
    }

    private void drawBubbles(Canvas canvas) {

        for (Bubble b : bubbles) {
            paint.setColor(b.color);
            canvas.drawCircle(b.x, b.y, 31, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(b.x, b.y, 31, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    private void drawBirds(Canvas canvas) {

        paint.setTextSize(38);

        for (int i = 0; i < birds; i++) {
            canvas.drawText("🐥", 35 + i * 55, 550, paint);
        }
    }

    private void drawShooter(Canvas canvas) {

        shooterX = getWidth() / 2f;
        shooterY = getHeight() - 100;

        paint.setColor(Color.WHITE);
        canvas.drawCircle(shooterX, shooterY, 42, paint);

        paint.setColor(Color.rgb(0, 200, 83));
        canvas.drawCircle(shooterX, shooterY, 32, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        canvas.drawText("AIM & SHOOT", shooterX - 85, shooterY + 80, paint);

        if (!shooting && aimY < shooterY) {
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(5);
            canvas.drawLine(
                    shooterX,
                    shooterY,
                    aimX,
                    aimY,
                    paint
            );
        }
    }

    private void drawShot(Canvas canvas) {

        paint.setColor(shotColor);
        canvas.drawCircle(shotX, shotY, 24, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(shotX, shotY, 24, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void updateShot() {

        shotX += shotDX;
        shotY += shotDY;

        if (shotX < 20 || shotX > getWidth() - 20) {
            shotDX *= -1;
        }

        for (int i = bubbles.size() - 1; i >= 0; i--) {

            Bubble b = bubbles.get(i);

            float dx = shotX - b.x;
            float dy = shotY - b.y;

            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < 50) {

                if (b.color == shotColor) {

                    bubbles.remove(i);
                    score += 10;

                    if (score % 50 == 0 && birds < 10) {
                        birds++;
                    }

                } else {

                    b.y += 65;

                    if (b.y > 500 && birds < 10) {
                        birds++;
                    }
                }

                shooting = false;
                break;
            }
        }

        if (shotY < 120) {
            shooting = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            aimX = event.getX();
            aimY = event.getY();

            if (!shooting && aimY < shooterY) {
                shoot();
            }

            invalidate();
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {

            aimX = event.getX();
            aimY = event.getY();

            invalidate();
            return true;
        }

        return true;
    }

    private void shoot() {

        shooting = true;

        shotX = shooterX;
        shotY = shooterY;

        float dx = aimX - shooterX;
        float dy = aimY - shooterY;

        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length < 1) {
            length = 1;
        }

        shotDX = dx / length * 18;
        shotDY = dy / length * 18;

        shotColor =
                bubbleColors[random.nextInt(bubbleColors.length)];
    }

    private static class Bubble {

        float x;
        float y;
        int color;

        Bubble(float x, float y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }
}
