package com.livejournal.karino2.irotore;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

/**
 * Created by _ on 2015/11/16.
 */
public class SmoothestFinder {
    final int INNER_WIDTH = 7;
    final int OUTER_WIDTH = 9;
    int[] scores = new int[INNER_WIDTH*INNER_WIDTH];
    int[] pixels = new int[OUTER_WIDTH*OUTER_WIDTH];
    int[] getPixels() { return pixels; }

    int colorDiff(int color1, int color2) {
        return IrotoreActivity.calculateColorDiff(color1, color2);
    }

    int getColor(int i, int j) {
        return pixels[j*OUTER_WIDTH+i];
    }


    void calculateOneDiffSum(int x, int y) {
        int i_origin = x;
        int j_origin = y;
        int centerX = i_origin+1;
        int centerY = j_origin+1;
        int baseColor = getColor(centerX, centerY);
        int diffSum = 0;
        for(int j = j_origin; j < j_origin+3; j++) {
            for(int i = i_origin; i < i_origin+3; i++) {
                if(!(i==centerX && (j == centerY)))
                    diffSum += colorDiff(baseColor, getColor(i, j));
            }
        }
        setScore(x, y, diffSum);
    }

    private void setScore(int x, int y, int diffSum) {
        scores[y*INNER_WIDTH+x] = diffSum;
    }

    public Point findNearestNeighbor(Bitmap bitmap, Point pos) {
        /*
        pos.x = Math.min(Math.max(3, pos.x), bitmap.getWidth() - 3-1);
        pos.y = Math.min(Math.max(3, pos.y), bitmap.getHeight()-3-1);

        bitmap.getPixels(getPixels(), 0, OUTER_WIDTH , pos.x - 3, pos.y - 3, OUTER_WIDTH, OUTER_WIDTH);
        */
        pos.x = Math.min(Math.max(4, pos.x), bitmap.getWidth() - 4-1);
        pos.y = Math.min(Math.max(4, pos.y), bitmap.getHeight()-4-1);
        bitmap.getPixels(getPixels(), 0, OUTER_WIDTH , pos.x - 4, pos.y - 4, OUTER_WIDTH, OUTER_WIDTH);


        calculateAll();
        Point relativeMin = findSmallest();

        // caution! reuse!
        pos.x += relativeMin.x-(INNER_WIDTH-1)/2;
        pos.y += relativeMin.y-(INNER_WIDTH-1)/2;
        return pos;
    }

    public void calculateAll() {
        for(int y = 0; y < INNER_WIDTH; y++) {
            for(int x = 0; x < INNER_WIDTH; x++) {
                calculateOneDiffSum(x, y);
            }
        }
    }

    Point outPoint = new Point();

    int getScore(int x, int y) {
        return scores[y*INNER_WIDTH+x];
    }

    public Point findSmallest() {
        StringBuffer buf = new StringBuffer();

        int minCandidate = getScore(0, 0);
        outPoint.x = 0;
        outPoint.y = 0;
        for(int y = 0; y < INNER_WIDTH; y++) {
            for(int x = 0; x < INNER_WIDTH; x++) {
                int curVal =  getScore(x, y);
                buf.append(curVal);
                buf.append(",");
                if(minCandidate > curVal) {
                    minCandidate = curVal;
                    outPoint.x = x;
                    outPoint.y = y;
                }
            }
        }
        // Log.d("IroTore", "dump=" + buf.toString());
        return outPoint;
    }



}
