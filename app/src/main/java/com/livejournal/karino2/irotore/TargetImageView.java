package com.livejournal.karino2.irotore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class TargetImageView extends View {

    // Paint origPaint;
    Paint targetPositionPaint;
    final int RADIUS = 20;
    public TargetImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        targetPositionPaint = new Paint();
        targetPositionPaint.setColor(Color.RED);
        targetPositionPaint.setStrokeWidth(2);
        targetPositionPaint.setStyle(Paint.Style.STROKE);

        /*
        origPaint = new Paint();
        origPaint.setColor(Color.BLUE);
        origPaint.setStrokeWidth(2);
        origPaint.setStyle(Paint.Style.STROKE);
        */

    }


    final int MINIMUM_BITMAP_SIZE = 100;
    Matrix matrix = new Matrix();

    final int THUMBNAIL_RATIO = 4;
    Bitmap image;
    Bitmap thumbnail;
    public void setImage(Bitmap bitmap) {
        if(bitmap.getWidth() < MINIMUM_BITMAP_SIZE || bitmap.getHeight() < MINIMUM_BITMAP_SIZE)
            throw new IllegalArgumentException("Too small size");


        image = bitmap;
        thumbnail = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/THUMBNAIL_RATIO, bitmap.getHeight()/THUMBNAIL_RATIO, true);


        matrix = new Matrix();
        initialViewRegion = null;
        imageRegion = null;

        invalidate();
    }

    PointF initialPos;
    PointF initialPos2;

    private double distance(float x1, float y1, float x2, float y2) {
        return
                Math.sqrt(Math.pow(x1-x2, 2)+Math.pow(y1-y2, 2));

    }

    Matrix committed = new Matrix();
    Matrix tempInvert = new Matrix();

    long downUpTimeMill = -1;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(event.getPointerCount() == 1) {
                    initialPos = new PointF(event.getX(), event.getY());
                    initialPos2 = null;
                } else if (event.getPointerCount() >= 2) {
                    initialPos = new PointF(event.getX(), event.getY());
                    initialPos2 = new PointF(event.getX(1), event.getY(1));
                }
                downUpTimeMill = SystemClock.uptimeMillis();
                return true;
            case MotionEvent.ACTION_MOVE:
                if(event.getPointerCount() == 1) {
                    if(initialPos2 != null) {
                        // mouse move ended here.
                        committed.set(matrix);
                        initialPos = null;
                        initialPos2 = null;
                        return true;
                    }
                    if(initialPos == null) {
                        // already committed.
                        return true;
                    }
                    double distanceX = event.getX() - initialPos.x;
                    double distanceY = event.getY() - initialPos.y;
                    matrix.set(committed);
                    matrix.postTranslate((float) distanceX, (float) distanceY);
                    invalidate();
                    return true;
                } else {
                    if(initialPos == null)
                        return true;
                    if(initialPos2 == null) {
                        initialPos2 = new PointF(event.getX(1), event.getY(1));
                    }
                    double init_distance = distance(initialPos.x, initialPos.y, initialPos2.x, initialPos2.y);
                    double cur_distance = distance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    if(init_distance < 0.01)
                        return true;

                    float cx = getMeasuredWidth()/2.0f;
                    float cy = getMeasuredHeight()/2.0f;

                    float magnify = (float)(cur_distance/init_distance);
                    matrix.set(committed);
                    matrix.postScale(magnify, magnify, cx, cy);
                    invalidate();
                    return true;
                }
            case MotionEvent.ACTION_UP:
                if(initialPos == null)
                    return true; // do nothing
                if(event.getPointerCount() == 1) {
                    if(distanceSquare(initialPos, event.getX(), event.getY()) < 3 &&
                            isShortEnough(downUpTimeMill, SystemClock.uptimeMillis())) {
                        matrix.invert(tempInvert);
                        RectF rect = getImageViewRegion();
                        float[] posArr = new float[]{event.getX(), event.getY()};
                        tempInvert.mapPoints(posArr);
                        if(rect.contains(posArr[0], posArr[1])) {
                            onTapListener.onTap((float) ((posArr[0] - rect.left) / scale), (float) ((posArr[1] - rect.top) / scale));
                        }

                    }
                }
                committed.set(matrix);
                initialPos = null;
                initialPos2 = null;
                downUpTimeMill = -1;
                return true;
        }
        return super.onTouchEvent(event);
    }

    public interface OnTapListener {
        void onTap(float x, float y);
    }

    OnTapListener onTapListener = (x, y)-> {};

    public void setOnTapkListener(OnTapListener listener) {
        onTapListener = listener;
    }

    private boolean isShortEnough(long downUpTimeMill, long current) {
        if(downUpTimeMill == -1)
            return false;
        return current - downUpTimeMill < 500;
    }

    private float distanceSquare(PointF from, float x, float y) {
        if(from == null)
            return 0.0f;
        return (float)(Math.pow(from.x - x, 2)*Math.pow(from.y-y, 2));
    }

    Rect imageRegion = null;
    Rect getImageRegion() {
        if(imageRegion == null) {
            imageRegion = new Rect(0, 0, image.getWidth(), image.getHeight());
        }
        return imageRegion;
    }

    double scale = 1.0;
    RectF initialViewRegion = null;
    RectF getImageViewRegion() {
        if(initialViewRegion == null) {
            double xScale = ((double)getMeasuredWidth())/ ((double)image.getWidth());
            double yScale = ((double)getMeasuredHeight())/((double)image.getHeight());
            if(xScale > yScale) {
                // fill y.
                scale = yScale;
                double scaledWidth = image.getWidth()*yScale;
                double xSpace = getMeasuredWidth() - scaledWidth;
                initialViewRegion = new RectF((float)(xSpace/2.0), 0, (float)(xSpace/2.0+scaledWidth), getMeasuredHeight());
            } else {
                // fill x.
                scale = xScale;
                double scaledHeight = image.getHeight()*xScale;
                double ySpace = getMeasuredHeight() - scaledHeight;
                initialViewRegion = new RectF(0, (float)(ySpace/2.0), getMeasuredWidth(), (float)(ySpace/2.0+scaledHeight));
            }
        }
        return initialViewRegion;
    }




    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(image == null)
            return;

        int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.concat(matrix);
        canvas.drawBitmap(image, getImageRegion(), getImageViewRegion(), null);
        if(targetX != -1) {
            RectF rect = getImageViewRegion();
            canvas.drawCircle((int)(rect.left+targetX*scale), (int)(rect.top+targetY*scale), RADIUS, targetPositionPaint);
            // canvas.drawCircle((int)(rect.left+origTargetX*scale), (int)(rect.top+origTargetY*scale), RADIUS, origPaint);
        }
        canvas.restoreToCount(saveCount);
    }

    SmoothestFinder finder = new SmoothestFinder();
    /*
    int origTargetX;
    int origTargetY;
    */

    void adjustBestNeighbor(int originalX, int originalY) {
        /*
        origTargetX = originalX;
        origTargetY = originalY;
        */
        Point thumbnailPos = new Point(originalX / THUMBNAIL_RATIO,  originalY /THUMBNAIL_RATIO);
        // Log.d("IroTore", "orgthumX, y=" + thumbnailPos.x + "," + thumbnailPos.y);
        thumbnailPos = findBestNeighbor(thumbnail, thumbnailPos);
        // Log.d("IroTore", "res_thumX, y=" + thumbnailPos.x + "," + thumbnailPos.y);
        Point resultPos = new Point(thumbnailPos.x*THUMBNAIL_RATIO, thumbnailPos.y*THUMBNAIL_RATIO);
        /*
        Point resultPos = new Point(originalX, originalY);
        */
        resultPos = findBestNeighbor(image, resultPos);
        targetX = resultPos.x;
        targetY = resultPos.y;
        // Log.d("IroTore", "originX, y, resultX, y=" + originalX + "," + originalY + ", " + targetX + "," + targetY);
    }

    private Point findBestNeighbor(Bitmap bitmap, Point pos) {
        return finder.findNearestNeighbor(bitmap, pos);
    }


    int targetX = -1;
    int targetY = -1;
    public void setTargetXY(int targetX, int targetY) {
        adjustBestNeighbor(targetX, targetY);

        invalidate();
    }

    public int getAnswerColor() {
        return image.getPixel(targetX, targetY);
    }

    public void outputDebug(StringBuilder bldr) {
        bldr.append("targetX, Y=");
        bldr.append(targetX);
        bldr.append(",");
        bldr.append(targetY);
    }
}
