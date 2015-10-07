package com.livejournal.karino2.irotore;

/**
 * Created by karino on 10/3/15.
 */
public interface Scenario {
    boolean hasNext();

    void gotoNextScenarioItem();

    void restart();

    ScenarioItem getCurrentItem();

    void skipCurrent();
}
