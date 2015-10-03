package com.livejournal.karino2.irotore;

import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Created by karino on 10/3/15.
 */
public class GameStateTest {
    class ScenarioTestDouble implements Scenario {

        public boolean returnHasNext = false;
        public boolean gotoNextScenarioItemCalled = false;
        public boolean restartCalled = false;

        @Override
        public boolean hasNext() {
            return returnHasNext;
        }

        @Override
        public void gotoNextScenarioItem() {
            gotoNextScenarioItemCalled = true;
        }

        @Override
        public ScenarioItem getCurrentItem() {
            return null;
        }

        @Override
        public void restart() {
            restartCalled = true;
        }
    }

    @Test
    public void testInitialState_Select()
    {
        ScenarioTestDouble scenario = new ScenarioTestDouble();
        GameState target = new GameState(scenario);

        assertEquals(GameState.STATE_SELECT, target.getCurrentState());
    }


    @Test
    public void testGotoNextState_SelectToAnswer()
    {
        ScenarioTestDouble scenario = new ScenarioTestDouble();
        scenario.returnHasNext = true;

        GameState target = new GameState(scenario);
        target.gotoNextState();

        assertEquals(GameState.STATE_ANSWER, target.getCurrentState());
    }

    @Test
    public void testGotoNextState_SelectToFinish()
    {
        ScenarioTestDouble scenario = new ScenarioTestDouble();
        scenario.returnHasNext = false;

        GameState target = new GameState(scenario);
        target.gotoNextState();

        assertEquals(GameState.STATE_FINISH, target.getCurrentState());
    }

    @Test
    public void testRestart()
    {
        ScenarioTestDouble scenario = new ScenarioTestDouble();
        scenario.returnHasNext = false;

        GameState target = new GameState(scenario);
        target.gotoNextState();

        assertFalse(scenario.restartCalled);

        target.restart();

        assertTrue(scenario.restartCalled);
        assertEquals(GameState.STATE_SELECT, target.getCurrentState());
    }
}
