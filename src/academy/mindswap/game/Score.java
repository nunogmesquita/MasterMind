package academy.mindswap.game;

import java.util.HashMap;

public class Score {

    static HashMap<String, Integer> playerScore = new HashMap<>();

    public static void score(String name, int score) {
        playerScore.put(name, score);
    }
}
