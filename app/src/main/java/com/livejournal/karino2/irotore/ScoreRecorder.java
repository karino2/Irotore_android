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
    final int AVERAGE_NUM = 5;

    List<Double> pastScores = new ArrayList<Double>();

    SharedPreferences prefs;

    public ScoreRecorder(SharedPreferences prefs) {
        this.prefs = prefs;
        pastAverage = prefs.getFloat("PAST_AVERAGE", -1);
        for(int i = 0; i < AVERAGE_NUM; i++) {
            pastScores.add(pastAverage);
        }
    }

    public void addScore(int oneScore) {
        scores.add(oneScore);
    }
    public void resetCurrentScores() { scores.clear(); }

    public double calculateAverage() {
        int sum = 0;
        for(int score : scores) {
            sum+=score;
        }
        return ((double)sum)/scores.size();
    }

    public double calcAverageDouble(List<Double> arr) {
        double sum = 0;
        for(double score : arr) {
            sum+=score;
        }
        return sum/arr.size();
    }

    public double setNewAverageAndResetScores(double mean) {
        pastScores.remove(0);
        pastScores.add(mean);
        pastAverage = calcAverageDouble(pastScores);

        prefs.edit()
                .putFloat("PAST_AVERAGE", (float)pastAverage)
                .commit();
        scores.clear();
        return pastAverage;
    }



}
