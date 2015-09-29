package com.livejournal.karino2.irotore;

import android.net.Uri;

/**
 * Created by karino on 9/29/15.
 */
public class RandomScenario {
    Uri imageUri;
    int width;
    int height;

    public RandomScenario(Uri uri) {
        imageUri = uri;
        width = -1;
        height = -1;
    }

    public Uri getTargetImage() {
        return imageUri;
    }
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }

    public boolean hasNext() {
        return true; // infinite.
    }

    ScenarioItem generateRandomItem() {
        if(width == -1)
            throw new RuntimeException("Not set width, height. Must not happen.");
        int targetX = (int)(width*Math.random());
        int targetY = (int)(height*Math.random());
        return new ScenarioItem(imageUri, width, height, targetX, targetY);
    }

    public void gotoNextScenarioItem() {
        item = generateRandomItem();
    }

    ScenarioItem item;

    public ScenarioItem getCurrentItem() {
        if(item == null) {
            gotoNextScenarioItem();
        }
        return item;
    }
}
