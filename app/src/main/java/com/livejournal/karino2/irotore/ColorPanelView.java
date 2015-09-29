package com.livejournal.karino2.irotore;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by karino on 9/29/15.
 */
public class ColorPanelView extends View {

    boolean isSelected = false;
    Paint basePaint = new Paint();
    Paint framePaint = new Paint();

    final int FRAME_WIDTH = 4;

    public ColorPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(FRAME_WIDTH);
        framePaint.setColor(0xFF404040);
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    int currentColor = Color.BLACK;
    public void setColor(int color) {
        currentColor = color;
        invalidate();
    }

    public int getColor() {
        return currentColor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        basePaint.setColor(currentColor);
        canvas.drawRect(0, 0, getWidth(), getHeight(), basePaint);

        canvas.drawRect(FRAME_WIDTH, FRAME_WIDTH, getWidth()-FRAME_WIDTH, getHeight()-FRAME_WIDTH, framePaint);

    }
}
