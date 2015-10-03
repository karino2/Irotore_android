package com.livejournal.karino2.irotore;

/**
 * Created by karino on 10/3/15.
 */
public class GameState {
    public static final int STATE_SELECT = 1;
    public static final int STATE_ANSWER = 2;
    public static final int STATE_FINISH = 3;


    int currentState;
    public int getCurrentState() {
        return currentState;
    }

    public GameState(Scenario scenario) {
        this.scenario = scenario;
        currentState = STATE_SELECT;
    }


    public void gotoNextState() {
        if(currentState == STATE_SELECT) {
            if(scenario.hasNext())
                currentState = STATE_ANSWER;
            else
                currentState = STATE_FINISH;
        } else if (currentState == STATE_ANSWER) {
            currentState = STATE_SELECT;
            scenario.gotoNextScenarioItem();
        } else {
            throw new RuntimeException("Call next when state finish, never comming here.");
        }
    }

    public ScenarioItem getCurrentScenarioItem() { return scenario.getCurrentItem(); }

    public void restart() {
        currentState = STATE_SELECT;
        scenario.restart();
    }

    Scenario scenario;


}
