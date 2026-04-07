package com.adaplu.clickdungeon;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Lightweight scaffold test for leaderboard service mocks.
 */
public class LeaderboardServiceMockTest {

    static class MockLeaderboard {
        int lastScore = 0;
        void submitScore(String playerId, int score) { lastScore = score; }
        int getTopScore(String playerId) { return lastScore; }
    }

    @Test
    public void mockLeaderboard_submitAndQuery() {
        MockLeaderboard lb = new MockLeaderboard();
        lb.submitScore("player1", 1234);
        assertEquals(1234, lb.getTopScore("player1"));
    }
}
