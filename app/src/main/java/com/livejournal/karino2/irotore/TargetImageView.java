package com.livejournal.karino2.irotore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class TargetImageView extends View {

    Paint targetPositionPaint;
    final int RADIUS = 20;
    public TargetImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        targetPositionPaint = new Paint();
        targetPositionPaint.setColor(Color.RED);
        targetPositionPaint.setStrokeWidth(2);
        targetPositionPaint.setStyle(Paint.Style.STROKE);
    }


    Matrix matrix = new Matrix();

    Bitmap image;
    public void setImage(Bitmap bitmap) {
        image = bitmap;

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

    Matrix commited = new Matrix();


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
                return true;
            case MotionEvent.ACTION_MOVE:
                if(event.getPointerCount() == 1) {
                    if(initialPos2 != null) {
                        // mose move ended here.
                        commited.set(matrix);
                        initialPos = null;
                        initialPos2 = null;
                        return true;
                    }
                    if(initialPos == null) {
                        // already commited.
                        return true;
                    }
                    double distanceX = event.getX() - initialPos.x;
                    double distanceY = event.getY() - initialPos.y;
                    matrix.set(commited);
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
                    matrix.set(commited);
                    matrix.postScale(magnify, magnify, cx, cy);
                    invalidate();
                    return true;
                }
            case MotionEvent.ACTION_UP:
                if(initialPos == null)
                    return true; // do nothing
                if(event.getPointerCount() == 1) {
                    if(distanceSquare(initialPos, event.getX(), event.getY()) < 3) {
                        // onTap.onClick(this);
                    }
                }
                commited.set(matrix);
                initialPos = null;
                initialPos2 = null;
                return true;
        }
        return super.onTouchEvent(event);
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

    RectF initialViewRegion = null;
    RectF getImageViewRegion() {
        if(initialViewRegion == null) {
            double xScale = ((double)getMeasuredWidth())/ ((double)image.getWidth());
            double yScale = ((double)getMeasuredHeight())/((double)image.getHeight());
            if(xScale > yScale) {
                // fill y.
                double scaledWidth = image.getWidth()*yScale;
                double xSpace = getMeasuredWidth() - scaledWidth;
                initialViewRegion = new RectF((float)(xSpace/2.0), 0, (float)(xSpace/2.0+scaledWidth), getMeasuredHeight());
            } else {
                // fill x.
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
            canvas.drawCircle(rect.left+targetX, rect.top+targetY, RADIUS, targetPositionPaint);
        }
        canvas.restoreToCount(saveCount);
    }

    int targetX = -1;
    int targetY = -1;
    public void setTargetXY(int targetX, int targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
        invalidate();
    }
}
