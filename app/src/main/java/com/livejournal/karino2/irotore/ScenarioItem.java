package com.livejournal.karino2.irotore;

import android.net.Uri;

/**
 * Created by karino on 9/29/15.
 */
public class ScenarioItem {
    Uri imageUri;
    int width;
    int height;
    int targetX;
    int targetY;

    public ScenarioItem(Uri uri, int width, int height, int targetX, int targetY) {
        imageUri = uri;
        this.width = width;
        this.height = height;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    public Uri getTargetImage() {
        return imageUri;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    void generateRandomTarget() {
        if(width == -1)
            throw new RuntimeException("Not set width, height. Must not happen.");
        targetX = (int)(width*Math.random());
        targetY = (int)(height*Math.random());
    }

    public int getTargetX() {
        if(targetX == -1) {
            generateRandomTarget();
        }
        return targetX;
    }
    public int getTargetY() {
        if(targetY == -1) {
            generateRandomTarget();
        }
        return targetY;
    }
}
