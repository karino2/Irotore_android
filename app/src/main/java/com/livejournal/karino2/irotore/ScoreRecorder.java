package com.livejournal.karino2.irotore;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karino on 10/3/15.
 */
public class ScoreRecorder {
    List<Integer> scores = new ArrayList<Integer>();
    double pastAverage;
    final double AVERAGE_INTERVAL = 10.0;

    SharedPreferences prefs;

    public ScoreRecorder(SharedPreferences prefs) {
        this.prefs = prefs;
        pastAverage = prefs.getFloat("PAST_AVERAGE", -1);
    }

    public void addScore(int oneScore) {
        scores.add(oneScore);
    }

    public double calculateAverage() {
        int sum = 0;
        for(int score : scores) {
            sum+=score;
        }
        return ((double)sum)/scores.size();
    }

    public double setNewAverageAndResetScores(double mean) {
        if(pastAverage == -1) {
            pastAverage = mean;
        } else {
            pastAverage = (pastAverage * (AVERAGE_INTERVAL - 1) + mean) / AVERAGE_INTERVAL;
        }
        prefs.edit()
                .putFloat("PAST_AVERAGE", (float)pastAverage)
                .commit();
        scores.clear();
        return pastAverage;
    }



}
