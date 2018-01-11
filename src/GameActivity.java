package antimonypidgey.untangle;

import android.content.Context;
import android.content.Intent;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class GameActivity extends AppCompatActivity
        implements View.OnTouchListener {

    GameSurfaceView gsv;
    boolean disableTouch;
    public boolean snipsHeld = false;
    public float snipsX = 0;
    public float snipsY = 0;
    final int SNIPS_SIZE = 128;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gsv = new GameSurfaceView(this, this);
        gsv.setOnTouchListener(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        LinearLayout ll = new LinearLayout(this);
        ll.addView(gsv);
        setContentView(ll);
    }

    protected void onPause(){
        super.onPause();
        gsv.pause();
    }

    protected void onResume(){
        super.onResume();
        gsv.resume();
    }

    Node selected=null;
    public boolean onTouch(View v, MotionEvent m){
        if (!disableTouch) {
            switch (m.getAction() & MotionEvent.ACTION_MASK) {
                case (MotionEvent.ACTION_DOWN):
                    selected = null;
                    for (int i = 0; i < gsv.game.nodes.size(); i++) {
                        if ((((m.getX() >= gsv.game.nodes.get(i).x() - (gsv.game.nodes.get(i).getAdjustedNodeSize())
                                && m.getX() <= gsv.game.nodes.get(i).x() + (gsv.game.nodes.get(i).getAdjustedNodeSize()))
                                && (m.getY() >= gsv.game.nodes.get(i).y() - (gsv.game.nodes.get(i).getAdjustedNodeSize()))
                                && m.getY() <= gsv.game.nodes.get(i).y() + (gsv.game.nodes.get(i).getAdjustedNodeSize())))) {
                            selected = gsv.game.nodes.get(i);
                        }
                    }
                    if (m.getX()<=SNIPS_SIZE && m.getY()<=SNIPS_SIZE){
                        snipsHeld=true;
                    }
                    return true;
                case (MotionEvent.ACTION_CANCEL):
                    // never called
                    return true;
                case (MotionEvent.ACTION_MOVE):
                    if (snipsHeld){
                        snipsX = m.getX()-SNIPS_SIZE/2;
                        snipsY = m.getY()-SNIPS_SIZE/2;
                    }
                    else if (selected != null) {
                        if (!gsv.game.timer.isRunning()) {
                            gsv.game.timer.start();
                        }
                        if (m.getX()<selected.getAdjustedNodeSize()){
                            selected.xPos = selected.getAdjustedNodeSize();
                        }
                        else if (m.getX()>gsv.game.xSize-selected.getAdjustedNodeSize()){
                            selected.xPos = gsv.game.xSize-selected.getAdjustedNodeSize();
                        }
                        else {
                            selected.xPos = Math.round(m.getX());
                        }
                        if (m.getY()<SNIPS_SIZE+selected.getAdjustedNodeSize()){
                            selected.yPos = SNIPS_SIZE+selected.getAdjustedNodeSize();
                        }
                        else if (m.getY()>gsv.game.ySize-selected.getAdjustedNodeSize()){
                            selected.yPos = gsv.game.ySize-selected.getAdjustedNodeSize();
                        }
                        else {
                            selected.yPos = Math.round(m.getY());
                        }
                    }
                    return true;
                case (MotionEvent.ACTION_UP):
                    // If the puzzle has no intersecting lines, stop the timer and disable touch control.
                    if (snipsHeld){
                        if (gsv.snipTargetIndex!=-1){
                            gsv.game.connections.get(gsv.snipTargetIndex)[0].connectionCount--;
                            gsv.game.connections.get(gsv.snipTargetIndex)[1].connectionCount--;
                            gsv.game.connections.remove(gsv.snipTargetIndex);
                        }
                        snipsHeld = false;
                        gsv.triggerVictory = true;
                    }
                    if (gsv.crossedLines == 0){
                        if (gsv.finalTime.equals("")){
                            gsv.finalTime = gsv.game.timer.getElapsed();
                        }
                        disableTouch = true;
                        gsv.victoryTimer = 30;
                    }
                    return true;
            }
            return true;
        }
        return false;
    }

    public class GameSurfaceView extends SurfaceView implements Runnable{

        GameActivity parent;
        Thread surfaceThread = null;
        SurfaceHolder holder;
        public Game game;
        String elapsedTime = "00:00:00:00";
        public int crossedLines;
        public String finalTime = "";
        public int victoryTimer;
        public int snipTargetIndex;
        public boolean triggerVictory = false;

        boolean paused = false;

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (game == null) {
                Intent intent = getIntent();

                game = new Game(gsv.getWidth(), gsv.getHeight(),
                        intent.getIntExtra("EXTRA_NODE_COUNT", 12),
                        intent.getIntExtra("EXTRA_CONNECTION_MAX", 6),
                        intent.getIntExtra("EXTRA_SEED", 42), 36);
            }
        }

        public GameSurfaceView(Context context, GameActivity _parent){
            super(context);
            parent = _parent;
            holder = getHolder();
        }

        public void run(){
            boolean crossed;
            victoryTimer=31;
            double closestSnipLine;
            // Paint for nodes.
            Paint nodePaint = new Paint();
            nodePaint.setARGB(255, 255, 255, 255);
            // Paint for uncrossed lines.
            Paint linePaint = new Paint();
            linePaint.setARGB(255, 200, 200, 255);
            linePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            linePaint.setStrokeWidth(6);
            // Paint for crossed lines.
            Paint linePaintCross = new Paint();
            linePaintCross.setARGB(255, 255, 0, 0);
            linePaintCross.setFlags(Paint.ANTI_ALIAS_FLAG);
            linePaintCross.setStrokeWidth(6);
            // Paint for text.
            Paint textPaint = new Paint();
            textPaint.setARGB(255, 50, 50, 255);
            textPaint.setTextSize(48);
            // Paint for victory test.
            Paint victoryPaint = new Paint();
            victoryPaint.setTextAlign(Paint.Align.CENTER);
            victoryPaint.setARGB(255, 255, 100, 255);
            victoryPaint.setTextSize(128);
            // Blur filter paint.
            Paint blurPaint = new Paint();
            //blurPaint.set(_paintSimple);
            blurPaint.setARGB(255, 255, 255, 255);
            blurPaint.setStrokeWidth(30f);
            blurPaint.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL));
            Paint dockPaint = new Paint();
            dockPaint.setARGB(255, 86, 130, 3);
            dockPaint.setStyle(Paint.Style.FILL);
            Paint snipsPaint = new Paint();
            snipsPaint.setARGB(255, 128, 0, 128);
            snipsPaint.setStyle(Paint.Style.FILL);
            Paint snipTargetPaint = new Paint();
            snipTargetPaint.setARGB(255, 255, 0, 255);
            snipTargetPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            snipTargetPaint.setStrokeWidth(6);

            while (!paused){
                // If surface is not valid, skip the rest of the loop.
                if (!holder.getSurface().isValid()){
                    continue;
                }
                Canvas surfaceCanvas = holder.lockCanvas();
                surfaceCanvas.drawARGB(255, 32, 32, 32);

                // Draw each node in the game's node list.
                for (int i = 0; i < game.nodes.size(); i++){
                    if (victoryTimer<30){
                        blurPaint.setARGB(255, 255, 255, 255);
                        blurPaint.setStrokeWidth(1.5f*(30-victoryTimer));
                        surfaceCanvas.drawCircle(game.nodes.get(i).x(), game.nodes.get(i).y(), game.nodes.get(i).getAdjustedNodeSize(), blurPaint);
                    }
                    surfaceCanvas.drawCircle(game.nodes.get(i).x(), game.nodes.get(i).y(), game.nodes.get(i).getAdjustedNodeSize(), nodePaint);
                }
                // If snips are held, find the nearest line to the snips and store it in an index.
                snipTargetIndex = -1;
                closestSnipLine=9999;
                if (parent.snipsHeld){
                    for (int i = 0; i < game.connections.size(); i++){
                        //double tempSnip = snipsToConnectionDistance(game.connections.get(i), Math.round(parent.snipsX), Math.round(parent.snipsY));
                        if (pointWithinBox(new Point(game.connections.get(i)[0].x(), game.connections.get(i)[0].y()), new Point(game.connections.get(i)[1].x(), game.connections.get(i)[1].y()), new Point(Math.round(parent.snipsX), Math.round(parent.snipsY)))) {
                            double tempSnip = pointToLineDistance(new Point(game.connections.get(i)[0].x(), game.connections.get(i)[0].y()), new Point(game.connections.get(i)[1].x(), game.connections.get(i)[1].y()), new Point(Math.round(parent.snipsX), Math.round(parent.snipsY)));
                            if (tempSnip < closestSnipLine) {
                                snipTargetIndex = i;
                                closestSnipLine = tempSnip;
                            }
                        }
                    }
                }
                // Draw each of the connections in the game's connections list.
                crossedLines = 0;
                for (int i = 0; i < game.connections.size(); i++){
                    crossed=false;
                    for (int j = 0; j < game.connections.size(); j++){
                        // Minimizes expensive intersect tests; Does not test further if one intersection is found.
                        // Skips any connection that contains a node found in line 1.
                        if (!crossed && i!=j && !samePoint(
                                game.connections.get(i)[0].x(),
                                game.connections.get(i)[0].y(),
                                game.connections.get(i)[1].x(),
                                game.connections.get(i)[1].y(),
                                game.connections.get(j)[0].x(),
                                game.connections.get(j)[0].y(),
                                game.connections.get(j)[1].x(),
                                game.connections.get(j)[1].y()
                        ) && intersectTest(
                                game.connections.get(i)[0].x(),
                                game.connections.get(i)[0].y(),
                                game.connections.get(i)[1].x(),
                                game.connections.get(i)[1].y(),
                                game.connections.get(j)[0].x(),
                                game.connections.get(j)[0].y(),
                                game.connections.get(j)[1].x(),
                                game.connections.get(j)[1].y())){
                            crossed=true;
                            crossedLines++;
                        }
                    }
                    if (snipTargetIndex == i && closestSnipLine < 16){
                        surfaceCanvas.drawLine(game.connections.get(i)[0].x(), game.connections.get(i)[0].y(),
                                game.connections.get(i)[1].x(), game.connections.get(i)[1].y(), snipTargetPaint);
                    }
                    else if(crossed){
//                        blurPaint.setARGB(255, 255, 0, 0);
//                        blurPaint.setStrokeWidth(12);
//                        surfaceCanvas.drawLine(game.connections.get(i)[0].x(), game.connections.get(i)[0].y(),
//                                game.connections.get(i)[1].x(), game.connections.get(i)[1].y(), blurPaint);
                        surfaceCanvas.drawLine(game.connections.get(i)[0].x(), game.connections.get(i)[0].y(),
                                game.connections.get(i)[1].x(), game.connections.get(i)[1].y(), linePaintCross);
                    }
                    else{
//                        blurPaint.setARGB(255, 50, 50, 255);
//                        blurPaint.setStrokeWidth(12);
//                        surfaceCanvas.drawLine(game.connections.get(i)[0].x(), game.connections.get(i)[0].y(),
//                                game.connections.get(i)[1].x(), game.connections.get(i)[1].y(), blurPaint);
                        surfaceCanvas.drawLine(game.connections.get(i)[0].x(), game.connections.get(i)[0].y(),
                                game.connections.get(i)[1].x(), game.connections.get(i)[1].y(), linePaint);
                        if (victoryTimer<30){
                            blurPaint.setARGB(255, 255, 255, 255);
                            blurPaint.setStrokeWidth(30-victoryTimer);
                            surfaceCanvas.drawLine(game.connections.get(i)[0].x(), game.connections.get(i)[0].y(),
                            game.connections.get(i)[1].x(), game.connections.get(i)[1].y(), blurPaint);
                        }
                    }
                }

                // If there are no lines crossed, start the victory timer and wait 30 frames.
                if (crossedLines ==0){
                    if (triggerVictory){
                        victoryTimer = 30;
                    }
                    if (victoryTimer<=0) {
                        surfaceCanvas.drawText("Victory!", game.xSize / 2, game.ySize / 2, victoryPaint);
                    }
                    else{
                        if (victoryTimer <=30) {
                            victoryTimer--;
                        }
                    }
                }
                triggerVictory = false;

                // Draw the dock at the top of the screen and the Snips item at snipsX,snipsY
                surfaceCanvas.drawRect(0,0,game.xSize,128, dockPaint);
                surfaceCanvas.drawRect(Math.round(parent.snipsX), Math.round(parent.snipsY), SNIPS_SIZE + Math.round(parent.snipsX), SNIPS_SIZE + Math.round(parent.snipsY),snipsPaint);

                // Draw the timer, stopping it when signalled by the finalTime variable changing.
                if (finalTime.equals("")) {
                    elapsedTime = game.timer.getElapsed();
                }
                else{
                    elapsedTime = finalTime;
                }
                surfaceCanvas.drawText(elapsedTime, 160, 120, textPaint);

                // If the Snips item is not being held but is away from its dock at 0,0, step it back there slowly.
                if (!parent.snipsHeld && (parent.snipsX > 0||parent.snipsY > 0)){
                    if (parent.snipsX > 0){
                        parent.snipsX -= 0.3f*parent.snipsX;
                    }
                    if (parent.snipsY > 0){
                        parent.snipsY -= 0.3f*parent.snipsY;
                    }
                }
                holder.unlockCanvasAndPost(surfaceCanvas);
            }
        }

        public void pause() {
            paused = true;
                try {
                    surfaceThread.join();
                }
                catch(InterruptedException e){
                    e.printStackTrace();
            }
            surfaceThread = null;
        }

        public void resume() {
            paused = false;
            surfaceThread = new Thread(this);
            surfaceThread.start();
        }

        // Tests whether any two connections share a node.
        private boolean samePoint(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4){
            return (((x1 == x3 || x1 == x4)&&(y1 == y3 || y1 == y4))||((x2 == x3 || x2 == x4)&&(y2 == y3 || y2 == y4)));
        }

        // Faster Intersection test provided by CommanderKeith (http://www.java-gaming.org/index.php?topic=22590.0)
        private boolean intersectTest(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
            // Return false if either of the lines have zero length
            if (x1 == x2 && y1 == y2 ||
                    x3 == x4 && y3 == y4) {
                return false;
            }
            // Fastest method, based on Franklin Antonio's "Faster Line Segment Intersection" topic "in Graphics Gems III" book (http://www.graphicsgems.org/)
            int ax = x2 - x1;
            int ay = y2 - y1;
            int bx = x3 - x4;
            int by = y3 - y4;
            int cx = x1 - x3;
            int cy = y1 - y3;

            int alphaNumerator = by * cx - bx * cy;
            int commonDenominator = ay * bx - ax * by;
            if (commonDenominator > 0) {
                if (alphaNumerator < 0 || alphaNumerator > commonDenominator) {
                    return false;
                }
            } else if (commonDenominator < 0) {
                if (alphaNumerator > 0 || alphaNumerator < commonDenominator) {
                    return false;
                }
            }
            double betaNumerator = ax * cy - ay * cx;
            if (commonDenominator > 0) {
                if (betaNumerator < 0 || betaNumerator > commonDenominator) {
                    return false;
                }
            } else if (commonDenominator < 0) {
                if (betaNumerator > 0 || betaNumerator < commonDenominator) {
                    return false;
                }
            }
            if (commonDenominator == 0) {
                // This code wasn't in Franklin Antonio's method. It was added by Keith Woodward.
                // The lines are parallel.
                // Check if they're collinear.
                int y3LessY1 = y3 - y1;
                int colinearityTestForP3 = x1 * (y2 - y3) + x2 * (y3LessY1) + x3 * (y1 - y2);   // see http://mathworld.wolfram.com/Collinear.html
                // If p3 is collinear with p1 and p2 then p4 will also be collinear, since p1-p2 is parallel with p3-p4
                if (colinearityTestForP3 == 0) {
                    // The lines are collinear. Now check if they overlap.
                    if (x1 >= x3 && x1 <= x4 || x1 <= x3 && x1 >= x4 ||
                            x2 >= x3 && x2 <= x4 || x2 <= x3 && x2 >= x4 ||
                            x3 >= x1 && x3 <= x2 || x3 <= x1 && x3 >= x2) {
                        if (y1 >= y3 && y1 <= y4 || y1 <= y3 && y1 >= y4 ||
                                y2 >= y3 && y2 <= y4 || y2 <= y3 && y2 >= y4 ||
                                y3 >= y1 && y3 <= y2 || y3 <= y1 && y3 >= y2) {
                            return true;
                        }
                    }
                }
                return false;
            }
            return true;
        }

        private double pointToLineDistance(Point A, Point B, Point P) {
            double normalLength = Math.sqrt((B.x-A.x)*(B.x-A.x)+(B.y-A.y)*(B.y-A.y));
            return Math.abs((P.x-A.x)*(B.y-A.y)-(P.y-A.y)*(B.x-A.x))/normalLength;
        }

        // Returns true if a point lies within a bounding box.
        private boolean pointWithinBox(Point boxCorner1, Point boxCorner2, Point point){
            boolean betweenX = false;
            boolean betweenY = false;
            // X check
            if (boxCorner1.x>boxCorner2.x){
                if (boxCorner2.x<point.x && point.x < boxCorner1.x){
                    betweenX=true;
                }
            }
            else {
                if (boxCorner1.x<point.x && point.x < boxCorner2.x){
                    betweenX=true;
                }
            }
            // Y check
            if (boxCorner1.y>boxCorner2.y){
                if (boxCorner2.y<point.y && point.y < boxCorner1.y){
                    betweenY=true;
                }
            }
            else {
                if (boxCorner1.y<point.y && point.y < boxCorner2.y){
                    betweenY=true;
                }
            }

            return (betweenX && betweenY);
        }

    }
}
