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

    boolean isPanelSelected = false;
    Paint basePaint = new Paint();
    Paint framePaint = new Paint();
    Paint selectedFramePaint = new Paint();

    final int FRAME_WIDTH = 4;

    public ColorPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(FRAME_WIDTH);
        framePaint.setColor(0xFF404040);

        selectedFramePaint.setStyle(Paint.Style.STROKE);
        selectedFramePaint.setStrokeWidth(FRAME_WIDTH);
        selectedFramePaint.setColor(0xFFEE1010);
    }

    public void setPanelSelected(boolean isSelected) {
        this.isPanelSelected = isSelected;
        invalidate();
    }
    public boolean isPanelSelected() {
        return isPanelSelected;
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

        Paint fpaint = isPanelSelected ? selectedFramePaint : framePaint;
        canvas.drawRect(FRAME_WIDTH, FRAME_WIDTH, getWidth()-FRAME_WIDTH, getHeight()-FRAME_WIDTH, fpaint);

    }
}
