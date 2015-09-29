package com.livejournal.karino2.irotore;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by karino on 9/29/15.
 */
public class ColorPickerView extends View {

    ColorPicker colorPicker;

    Paint basePaint;
    Rect wholeRect = new Rect();

    boolean isColorPickMode = true;
    int selectedColor = Color.BLACK;

    public interface OnColorChangedListener {
        void onColorChanged(int color);
    }

    OnColorChangedListener onColorChangedListener = new OnColorChangedListener() {
        @Override
        public void onColorChanged(int color) {

        }
    };

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.onColorChangedListener = listener;
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        colorPicker = new ColorPicker(false);
        basePaint = new Paint();
        basePaint.setColor(0xFFE0E0E0);
        basePaint.setStyle(Paint.Style.FILL);
    }

    final int MARGIN = 4;
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        wholeRect.set(0, 0, w, h);

        colorPicker.resize(h);

        Paint p = new Paint();
        colorPicker.updateHue(p);
        colorPicker.updateSV();


        int colorPanelWidth = Math.max(0, w - h);
        colorPanelWidth = Math.min(colorPanelWidth, h/2-MARGIN);
    }

    boolean  onTouchDown(int ix, int iy)
    {
        return colorPicker.onTouchDown(ix, iy);
        // handle select, answer here.
    }

    boolean onTouchMove(int ix, int iy)
    {
        if(colorPicker.onTouchMove(ix, iy))
        {
            applyColor(colorPicker.getChosenColor());
            invalidate();
            return true;
        }
        return false;
    }

    private void applyColor(int color) {
        selectedColor = color;
        onColorChangedListener.onColorChanged(color);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int ix = (int)event.getX();
        int iy = (int)event.getY();
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if(onTouchDown(ix, iy))
                    return true;
                break;
            case MotionEvent.ACTION_UP:
                colorPicker.onTouchUp(ix, iy);
                break;
            case MotionEvent.ACTION_MOVE:
                if(onTouchMove(ix, iy))
                    return true;
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(wholeRect, basePaint);
        colorPicker.updatePanel(canvas, 0, 0, basePaint);
    }
}
