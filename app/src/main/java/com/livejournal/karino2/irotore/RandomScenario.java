package com.livejournal.karino2.irotore;

import android.net.Uri;

/**
 * Created by karino on 9/29/15.
 */
public class RandomScenario implements Scenario {
    Uri imageUri;
    int width;
    int height;

    int index;
    int maxScenarioNum = 5;

    public RandomScenario(Uri uri) {
        imageUri = uri;
        width = -1;
        height = -1;

        index = 0;
    }

    public Uri getTargetImage() {
        return imageUri;
    }
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getTotalItemNum() {
        return maxScenarioNum;
    }

    public int getCurrentIndex() {
        return index;
    }

    @Override
    public boolean hasNext()
    {
        return (index+1 < maxScenarioNum);
    }

    ScenarioItem generateRandomItem() {
        if(width == -1)
            throw new RuntimeException("Not set width, height. Must not happen.");
        int targetX = (int)(width*Math.random());
        int targetY = (int)(height*Math.random());
        return new ScenarioItem(imageUri, width, height, targetX, targetY);
    }

    @Override
    public void gotoNextScenarioItem() {
        item = generateRandomItem();
        index++;
    }

    @Override
    public void skipCurrent() {
        item = generateRandomItem();
    }

    @Override
    public void restart() {
        index = 0;
        item = generateRandomItem();
    }

    ScenarioItem item;

    @Override
    public ScenarioItem getCurrentItem() {
        if(item == null) {
            item = generateRandomItem();
        }
        return item;
    }
}
