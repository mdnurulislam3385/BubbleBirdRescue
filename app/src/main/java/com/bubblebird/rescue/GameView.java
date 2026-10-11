package com.bubblebird.rescue;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private static final int ROWS = 11, COLS = 8;
    private final int[][] board = new int[ROWS][COLS];
    private final int[] colors = {
        Color.rgb(255,82,82), Color.rgb(255,193,7),
        Color.rgb(66,165,245), Color.rgb(102,187,106),
        Color.rgb(171,71,188)
    };
    private int current, next, score, rescued;
    private float sx, sy, ax, ay, bx, by, vx, vy;
    private boolean moving = false;

    public GameView(Context context) {
        super(context);
        p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        resetGame();
    }

    private void resetGame() {
        for (int r=0;r<ROWS;r++)
            for (int c=0;c<COLS;c++)
                board[r][c] = r<5 ? random.nextInt(colors.length) : -1;
        current=random.nextInt(colors.length);
        next=random.nextInt(colors.length);
        score=0;
        rescued=0;
        moving=false;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w=getWidth(), h=getHeight();
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(10,18,40));
        canvas.drawRect(0,0,w,h,p);

        p.setColor(Color.rgb(25,39,75));
        canvas.drawRoundRect(new RectF(15,15,w-15,90),22,22,p);
        p.setColor(Color.WHITE);
        p.setTextSize(21);
        canvas.drawText("Bubble Bird Rescue",25,45,p);
        p.setTextSize(16);
        canvas.drawText("Score: "+score,25,72,p);
        canvas.drawText("Birds: "+rescued+"/10",w-125,72,p);

        float top=110, bottom=h-155;
        float cw=w/COLS, ch=(bottom-top)/ROWS;
        for (int r=0;r<ROWS;r++) {
            for (int c=0;c<COLS;c++) {
                if (board[r][c]>=0)
                    bubble(canvas,c*cw+cw/2,top+r*ch+ch/2,
                           Math.min(cw,ch)*0.40f,board[r][c]);
            }
        }

        sx=w/2;
        sy=h-90;
        if (!moving) {
            float dx=ax-sx, dy=ay-sy;
            float len=(float)Math.sqrt(dx*dx+dy*dy);
            if (len>0 && dy<0) {
                p.setColor(Color.LTGRAY);
                p.setStrokeWidth(3);
                canvas.drawLine(sx,sy,sx+dx/len*220,sy+dy/len*220,p);
            }
        }

        p.setColor(Color.rgb(45,62,105));
        canvas.drawCircle(sx,sy,43,p);
        bubble(canvas,sx,sy,25,current);
        p.setColor(Color.WHITE);
        p.setTextSize(14);
        canvas.drawText("NEXT",sx+53,sy-12,p);
        bubble(canvas,sx+82,sy+5,17,next);

        if (moving) bubble(canvas,bx,by,19,current);

        p.setColor(Color.WHITE);
        p.setTextSize(15);
        canvas.drawText("Tap above to shoot!",20,h-20,p);
    }

    private void bubble(Canvas canvas,float x,float y,float radius,int color) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(colors[color]);
        canvas.drawCircle(x,y,radius,p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2);
        p.setColor(Color.argb(180,255,255,255));
        canvas.drawCircle(x,y,radius,p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb(100,255,255,255));
        canvas.drawCircle(x-radius*.3f,y-radius*.3f,radius*.2f,p);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x=e.getX(), y=e.getY();
        if (e.getAction()==MotionEvent.ACTION_DOWN ||
            e.getAction()==MotionEvent.ACTION_MOVE) {
            ax=x;
            ay=y;
            invalidate();
            return true;
        }
        if (e.getAction()==MotionEvent.ACTION_UP) {
            if (!moving) shoot(x,y);
            return true;
        }
        return true;
    }

    private void shoot(float x,float y) {
        sx=getWidth()/2f;
        sy=getHeight()-90;
        float dx=x-sx, dy=y-sy;
        float len=(float)Math.sqrt(dx*dx+dy*dy);
        if (len<1 || dy>=-20) return;
        vx=dx/len*20;
        vy=dy/len*20;
        bx=sx;
        by=sy;
        moving=true;
        moveStep();
    }

    private void moveStep() {
        if (!moving) return;
        bx+=vx;
        by+=vy;
        if (bx<20 || bx>getWidth()-20) vx=-vx;
        if (by<110 || hitBubble()) {
            placeBubble();
            return;
        }
        invalidate();
        postDelayed(this::moveStep,16);
    }

    private boolean hitBubble() {
        float top=110, bottom=getHeight()-155;
        float cw=getWidth()/(float)COLS;
        float ch=(bottom-top)/ROWS;
        for (int r=0;r<ROWS;r++)
            for (int c=0;c<COLS;c++) {
                if (board[r][c]<0) continue;
                float x=c*cw+cw/2;
                float y=top+r*ch+ch/2;
                float dx=bx-x, dy=by-y;
                if (dx*dx+dy*dy<38*38) return true;
            }
        return false;
    }

    private void placeBubble() {
        float top=110, bottom=getHeight()-155;
        float cw=getWidth()/(float)COLS;
        float ch=(bottom-top)/ROWS;
        int c=Math.max(0,Math.min(COLS-1,(int)(bx/cw)));
        int r=Math.max(0,Math.min(ROWS-1,(int)((by-top)/ch)));
        while (r<ROWS && board[r][c]>=0) r++;
        if (r>=ROWS) r=ROWS-1;
        board[r][c]=current;
        moving=false;

        List<int[]> group=new ArrayList<>();
        collect(r,c,current,group);
        if (group.size()>=3) {
            for (int[] cell:group) board[cell[0]][cell[1]]=-1;
            score+=group.size()*10;
            if (group.size()>=5) rescued=Math.min(10,rescued+1);
            removeFloating();
        }
        current=next;
        next=random.nextInt(colors.length);
        invalidate();
    }

    private void collect(int r,int c,int color,List<int[]> group) {
        if (r<0 || r>=ROWS || c<0 || c>=COLS) return;
        if (board[r][c]!=color) return;
        for (int[] cell:group)
            if (cell[0]==r && cell[1]==c) return;
        group.add(new int[]{r,c});
        collect(r+1,c,color,group);
        collect(r-1,c,color,group);
        collect(r,c+1,color,group);
        collect(r,c-1,color,group);
    }

    private void removeFloating() {
        boolean[][] connected=new boolean[ROWS][COLS];
        List<int[]> queue=new ArrayList<>();
        for (int c=0;c<COLS;c++)
            if (board[0][c]>=0) {
                connected[0][c]=true;
                queue.add(new int[]{0,c});
            }
        for (int i=0;i<queue.size();i++) {
            int r=queue.get(i)[0], c=queue.get(i)[1];
            int[][] dirs={{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d:dirs) {
                int nr=r+d[0], nc=c+d[1];
                if (nr<0 || nr>=ROWS || nc<0 || nc>=COLS) continue;
                if (!connected[nr][nc] && board[nr][nc]>=0) {
                    connected[nr][nc]=true;
                    queue.add(new int[]{nr,nc});
                }
            }
        }
        for (int r=0;r<ROWS;r++)
            for (int c=0;c<COLS;c++)
                if (board[r][c]>=0 && !connected[r][c]) {
                    board[r][c]=-1;
                    score+=5;
                }
    }
}
